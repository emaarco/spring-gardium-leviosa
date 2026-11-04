# 📡 GraphQL Subscriptions Across Several Pods: Kafka Fan-out as a Spring Boot Starter 🛰️

A GraphQL subscription is a long-lived WebSocket connection, and that connection ends on **exactly one pod**. As soon as
a service runs with more than one replica, this happens even with a single client: the load balancer sends the
client's HTTP requests to one pod and its WebSocket to another.

```
Frontend ──HTTP──────▶ pod-2   places the order, status changes happen here
Frontend ──WebSocket─▶ pod-1   holds the subscription for that order        ❓ how does pod-1 find out?
```

This example answers the question with Spring for GraphQL **inside the service itself**. Every pod publishes its
status changes to Kafka and reads **all** of them back, so each pod can notify the subscriptions connected to it.
There is no extra gateway and no extra process.

The Kafka part is not specific to orders, so it lives in a small Spring Boot starter that any service can add:

| Module                                     | Contains                                                                                   |
|--------------------------------------------|--------------------------------------------------------------------------------------------|
| [`pod-broadcast-starter`](pod-broadcast-starter) | `PodBroadcastPublisher` sends an event to all pods, `PodBroadcastReceiver` streams the events of all pods as a Kotlin `Flow`, plus a readiness check |
| [`order-service`](order-service)           | the GraphQL API, the order domain and one adapter that uses the starter                    |

## ✨ Use-Case

An online shop with **one client**, the shop frontend. The customer places an order and then watches it progress
live. The order is placed with `placeOrder`, and the server assigns its id. After that, fulfillment (payment provider,
warehouse) moves it on asynchronously, step by step:

```
PLACED ──▶ PAYMENT_CONFIRMED ──▶ READY_TO_SHIP ──▶ SHIPPED
(response of placeOrder)        (via subscription orderStatus)
```

```graphql
type Query        { pod: String! }
type Mutation     { placeOrder(article: String!, quantity: Int!): Order! }
type Subscription { orderStatus(orderId: ID!): OrderStatusChange! }

type Order             { id: ID!  article: String!  quantity: Int!  status: OrderStatus!  placedOnPod: String! }
type OrderStatusChange { orderId: ID!  status: OrderStatus!  changedAt: String!  changedOnPod: String! }
```

Fulfillment is simulated by `SimulatedOrderFulfillmentAdapter`: it reports one step every `order-fulfillment.step-delay`
(10 s by default) on the pod that took the order. In a real system, these updates would arrive as a webhook or as a
Kafka event of another service, again on whichever pod. `placedOnPod`, `changedOnPod` and `Query.pod` are only there
for the demo: they show which pod did what.

## 🔁 How it works

```mermaid
block-beta
    columns 3
    space kafka[("Kafka topic order-status-changes")] space
    space:3
    pod1["pod-1 · order-service\nholds the subscription"] space pod2["pod-2 · order-service\ntakes the order, runs the fulfillment"]
    space:3
    space frontend["Frontend · one client"] space
    frontend -- "① mutation placeOrder (HTTP)" --> pod2
    pod2 -- "② status change, key = orderId" --> kafka
    kafka -- "③ every pod reads every change" --> pod1
    pod1 -- "④ subscription orderStatus (WebSocket)" --> frontend
```

1. The frontend places the order. The request lands on **pod-2**, which returns the order id and starts the fulfillment.
2. Every status change is published to Kafka, keyed by the order id. pod-2 does **not** notify anyone directly.
3. Every pod consumes the topic with its **own** consumer group, so **pod-1** gets the change too (and so does pod-2).
4. pod-1 pushes the change into the subscription the frontend opened with the order id.

## 🧩 What the service has to do

The service adds the starter, names its pod and puts the starter's health check into readiness:

```kotlin
implementation(project(":examples:spring-for-graphql-subscriptions:pod-broadcast-starter"))
```

```yaml
pod-broadcast:
  pod-name: ${POD_NAME:pod-1}
  topic: order-status-changes

management.endpoint.health.group.readiness.include: readinessState,podBroadcast
```

Everything else is one outbound adapter, `OrderStatusBroadcastAdapter`. It publishes with `PodBroadcastPublisher` and
receives with `PodBroadcastReceiver`:

```kotlin
override fun broadcastToAllPods(change: OrderStatusChange) {
    publisher.publishToAllPods(change.orderId.value, OrderStatusChangeMessage.from(change))
}

override fun changesOf(orderId: OrderId) =
    receiver
        .receive<OrderStatusChangeMessage>()
        .filter { it.orderId == orderId.value }
        .map { it.toDomain() }
```

The GraphQL controller returns that `Flow`, which Spring for GraphQL adapts to a reactive stream
(`kotlinx-coroutines-reactor` has to be on the classpath):

```kotlin
@SubscriptionMapping
fun orderStatus(@Argument orderId: String): Flow<OrderStatusChangeDto> =
    query.trackOrderStatus(OrderId(orderId)).map { OrderStatusChangeDto.from(it) }
```

## ⚙️ What the starter does

`PodBroadcastAutoConfiguration` registers one bean per building block of the pattern:

| Bean                          | Job                                                                                    |
|-------------------------------|----------------------------------------------------------------------------------------|
| `PodBroadcastPublisher`       | sends an event to the topic, so that every pod gets it                                 |
| `PodBroadcastKafkaListener`   | reads the topic with a consumer group of its own and hands every event to the receiver |
| `PodBroadcastReceiver`        | keeps the events of this pod in memory and streams them per type to every subscriber   |
| `PodBroadcastHealthIndicator` | keeps the pod out of readiness until the listener can receive events                   |
| `podBroadcastTopic`           | creates the topic with three partitions                                                |

A service that only sends events uses the publisher, one that only reacts to them uses the receiver.

### 1. One consumer group per pod: broadcast instead of load balancing

Kafka delivers every record to **one consumer per consumer group**. If all replicas share one group, which is the
usual setup for work queues, only one random pod would see a change. `PodBroadcastKafkaListener` gives every pod its
**own** group, `<spring.application.name>-<pod-broadcast.pod-name>`, so every pod gets every event:

```kotlin
@KafkaListener(
    id = LISTENER_ID,
    topics = ["#{__listener.topic}"],
    groupId = "#{__listener.consumerGroup}",
    properties = ["auto.offset.reset=latest"],
)
fun onEvent(record: ConsumerRecord<String, String>)
```

The pod name comes from `POD_NAME`, which Kubernetes fills with the pod name via the Downward API
(see [`k8s/order-service.yaml`](k8s/order-service.yaml)). The log of the multi-pod test shows that each pod gets all
partitions:

```
order-service-pod-1: partitions assigned: [order-status-changes-0, order-status-changes-1, order-status-changes-2]
order-service-pod-2: partitions assigned: [order-status-changes-0, order-status-changes-1, order-status-changes-2]
```

`auto.offset.reset=latest` makes a new pod start at the end of the topic. Subscriptions are about *live* changes, so a
freshly started pod has nothing to replay.

### 2. Every event takes the same path, even on the pod where it happened

`PodBroadcastPublisher.publishToAllPods` only sends to Kafka. The publishing pod receives its own event back like every other
pod. This keeps a single delivery path: no duplicates, no special case for "local", and the same order on every pod,
because all events with the same key share a partition. The send waits for the broker's ack (`send(...).join()`), so
an event that Kafka did not accept fails loudly instead of disappearing.

The event is sent as JSON, with its class name in the header `pod-broadcast-event-type`. `PodBroadcastReceiver.receive<T>()` only emits
events of type `T`, so one topic can carry several event types.

### 3. A local hub per pod

`PodBroadcastReceiver` keeps an in-memory `MutableSharedFlow`. The listener pushes every event into it, and every open
subscription collects from it:

```kotlin
private val eventsFromAllPods = MutableSharedFlow<ReceivedEvent>(extraBufferCapacity = 256, onBufferOverflow = DROP_OLDEST)
```

- `tryEmit` never blocks the Kafka listener. A slow WebSocket client loses its oldest buffered events instead of
  slowing down all other subscriptions of the pod.
- A `SharedFlow` keeps no history, so a new subscription only sees events that arrive after it started.

### 4. Ready only when the pod can receive events

A pod that accepts subscriptions before its consumer owns partitions would silently miss events.
`PodBroadcastHealthIndicator` (health contributor `podBroadcast`) reports `DOWN` until partitions are assigned. Once
the service adds it to the readiness group, Kubernetes only routes WebSocket connections to a pod that receives live
events. The tests rely on this too.

## ⏱️ The gap between `placeOrder` and the subscription

The server assigns the order id, so the client can only subscribe **after** `placeOrder` has returned. A status change
that happens in between is not delivered, because the subscription does not exist yet and the hub keeps no history.

A real system closes the gap like this:

1. The order lives in a database that every pod can read.
2. After `placeOrder`, the client **first** opens the subscription and **then** loads the current status with a query.
3. Changes arriving via the subscription are only applied if they are newer than what the query returned.

The same steps apply to every reconnect, for example when a pod is replaced during a rolling update. This example has
no database on purpose. Instead, the simulated fulfillment waits one `step-delay` before its first step, which leaves
enough time to subscribe.

## 🚀 Run it

```bash
# 1. Kafka (podman or docker)
cd examples/spring-for-graphql-subscriptions && podman compose up -d

# 2. two pods of the same service, each in its own terminal
./gradlew :examples:spring-for-graphql-subscriptions:order-service:bootRun
./gradlew :examples:spring-for-graphql-subscriptions:order-service:bootRun --args='--server.port=8085 --pod.name=pod-2'

# 3. tests (incl. the multi-pod tests against an embedded Kafka, no container needed)
./gradlew :examples:spring-for-graphql-subscriptions:pod-broadcast-starter:test
./gradlew :examples:spring-for-graphql-subscriptions:order-service:test
```

Act as the frontend whose requests are spread across both pods. Place the order in GraphiQL of **pod-2**
(http://localhost:8085/graphiql):

```graphql
mutation {
  placeOrder(article: "Espresso machine", quantity: 1) { id status placedOnPod }
}
```

Within 10 seconds, subscribe with the returned id in GraphiQL of **pod-1** (http://localhost:8084/graphiql):

```graphql
subscription {
  orderStatus(orderId: "<id from placeOrder>") { status changedOnPod }
}
```

pod-1 streams every step, although all of them happened on pod-2:

```json
{ "orderStatus": { "status": "PAYMENT_CONFIRMED", "changedOnPod": "pod-2" } }
{ "orderStatus": { "status": "READY_TO_SHIP", "changedOnPod": "pod-2" } }
{ "orderStatus": { "status": "SHIPPED", "changedOnPod": "pod-2" } }
```

| Endpoint   | Path                                                               |
|------------|--------------------------------------------------------------------|
| HTTP       | `/api/graphql` (query, mutation)                                   |
| WebSocket  | `/api/graphql-ws` (subscription, `graphql-transport-ws` protocol) |
| GraphiQL   | `/graphiql`                                                        |
| Readiness  | `/actuator/health/readiness`                                       |

## 🧱 Hexagonal structure of the order-service

Kafka and the simulated fulfillment sit behind outbound ports. The application layer knows neither of them, nor the
starter.

| Inbound adapter                                | Inbound port               | Service                    | Outbound port              | Outbound adapter                   |
|------------------------------------------------|----------------------------|----------------------------|----------------------------|------------------------------------|
| `PlaceOrderController` (GraphQL mutation)      | `PlaceOrderUseCase`        | `PlaceOrderService`        | `OrderFulfillmentPort`     | `SimulatedOrderFulfillmentAdapter` |
| `FulfillmentUpdateListener` (fulfillment)      | `ChangeOrderStatusUseCase` | `ChangeOrderStatusService` | `OrderStatusBroadcastPort` | `OrderStatusBroadcastAdapter`      |
| `OrderStatusController` (GraphQL subscription) | `TrackOrderStatusQuery`    | `TrackOrderStatusService`  | `OrderStatusBroadcastPort` | `OrderStatusBroadcastAdapter`      |

The simulated fulfillment reports its updates as Spring application events (`FulfillmentUpdate`), and
`FulfillmentUpdateListener` picks them up like a webhook would.

## 🧪 Tests

| Test                                   | Proves                                                                                      |
|----------------------------------------|---------------------------------------------------------------------------------------------|
| `PodBroadcastTest` (starter)           | two pods (two Spring contexts) + embedded Kafka: an event published on one pod reaches both pods, and a receiver only gets the events of its type |
| `MultiPodSubscriptionTest`             | two order-service pods + embedded Kafka: the client places an order on pod-2, subscribes on pod-1 and gets every status change. The same path works when both land on the same pod |
| `OrderStatusControllerTest`            | the subscription streams every status change of the order (`@GraphQlTest`)                   |
| `PlaceOrderControllerTest`             | the mutation returns the order with the id assigned by the server (`@GraphQlTest`)           |
| `SimulatedOrderFulfillmentAdapterTest` | fulfillment reports its steps in order                                                       |
| `PlaceOrderServiceTest`                | a new order gets a new id and starts its fulfillment                                         |
| `ChangeOrderStatusServiceTest`         | a status change is only broadcast, never delivered locally directly                          |
| `ArchitectureTest`                     | the shared hexagonal rule set                                                                |

There is no Bruno collection for this example. The Bruno collections of the other examples only cover request/response
calls, while a subscription needs a WebSocket client, and the e2e job would need a Kafka broker on top.
`MultiPodSubscriptionTest` covers the e2e flow in `./gradlew build` instead.

## ⚖️ Trade-offs and production notes

- **At most once, live only.** A client that is not connected while a change happens does not get it later (see the
  gap between `placeOrder` and the subscription above). Use the subscription for changes, and a query for
  the current state.
- **Every pod reads every event.** Each pod consumes the full topic and filters in memory. That is cheap for
  notification-sized traffic. For very high volumes, use a dedicated, slim notification topic instead of the full
  domain event stream.
- **Consumer groups come and go with the pods.** Every new pod name creates a new group. Kafka removes empty groups
  after `offsets.retention.minutes` (7 days by default). Committed offsets are irrelevant here anyway, because a pod
  always starts at `latest`.
- **WebSockets and rolling updates.** When a pod stops, its subscriptions end. Clients must reconnect, and they land
  on another pod. Load balancer and ingress must allow WebSocket upgrades. Sticky sessions are **not** needed.
- **Other brokers work the same way.** What matters is broadcast semantics to every pod: Redis Pub/Sub, PostgreSQL
  `LISTEN/NOTIFY` and Hazelcast topics all provide it. Use the one you already run. Only the starter would change,
  the services keep calling `publishToAllPods` and `receive`.
- **Starter instead of sidecar.** The same building blocks could run in a sidecar container next to every pod. That
  pays off when the services are written in different languages or must not be changed. For Spring services, the
  starter is simpler: one process per pod, one log, and the multi-pod test runs in one JVM.
