# 🏨 Combining Multiple Data Sources in One Spring-for-GraphQL Query 🌐

This example shows how a single GraphQL query returns one nested result built from **two independent data sources**,
and how Spring for GraphQL makes sure the expensive source is only asked when needed and then only once per request
instead of once per item.

## ✨ Use-Case

A hotel search. Every hotel platform combines two systems that are separated in practice:

| Field                      | Data source        | Characteristics                                                    |
|----------------------------|--------------------|--------------------------------------------------------------------|
| `id`, `name`, `stars`      | **Content provider** (`HotelContentAdapter`) | master data, changes rarely, cheap to read |
| `availableRooms { … }`     | **Booking engine** (`BookingEngineAdapter`)  | availability & prices for a stay, volatile, expensive (rate-limited API) |

Both sources are linked by the hotel id. A result list or a map view only needs the master data, so the booking engine
should not be called at all. A detail list with prices needs both, so the booking engine should be called **once for
all hotels**, not once per hotel (the classic N+1 problem).

Both sources are deterministic in-memory fakes. This is a deliberate demo setup: no external accounts, no extra
infrastructure, and no GraphQL federation, because both sources are aggregated behind one schema in one service.

## 🚀 Run it

```bash
# start the application (GraphiQL: http://localhost:8083/graphiql)
./gradlew :examples:spring-for-graphql-multiple-data-sources:bootRun

# run the tests (GraphQL slice tests, integration test, architecture suite)
./gradlew :examples:spring-for-graphql-multiple-data-sources:test

# run the Bruno e2e collection against the running app
cd examples/spring-for-graphql-multiple-data-sources/bruno && npx --yes @usebruno/cli run . --env Local
```

## 🔎 Example

```graphql
query hotelSearch($city: String!, $checkIn: String!, $checkOut: String!) {
  hotelSearch(city: $city, checkIn: $checkIn, checkOut: $checkOut) {
    name
    stars
    availableRooms {
      roomType
      pricePerNightInEuro
      totalPriceInEuro
    }
  }
}
```

```json
{ "city": "München", "checkIn": "2026-12-11", "checkOut": "2026-12-13" }
```

```json
{
  "data": {
    "hotelSearch": [
      {
        "name": "Hotel Bayerischer Hof",
        "stars": 5,
        "availableRooms": [
          { "roomType": "Doppelzimmer", "pricePerNightInEuro": 320, "totalPriceInEuro": 640 },
          { "roomType": "Suite", "pricePerNightInEuro": 890, "totalPriceInEuro": 1780 }
        ]
      },
      {
        "name": "Hotel Isarblick",
        "stars": 3,
        "availableRooms": [
          { "roomType": "Einzelzimmer", "pricePerNightInEuro": 95, "totalPriceInEuro": 190 }
        ]
      },
      {
        "name": "Pension am Viktualienmarkt",
        "stars": 2,
        "availableRooms": []
      }
    ]
  }
}
```

The application log shows a single call to the booking engine for all three hotels:

```
BookingEngineAdapter : Booking engine requested for 3 hotel(s) and Stay(checkIn=2026-12-11, checkOut=2026-12-13)
```

Remove `availableRooms` from the query and that log line disappears.

## 🔀 Data flow

```
hotelSearch(city, checkIn, checkOut)            HotelSearchController      @QueryMapping
  └─ SearchHotelsQuery → HotelContentPort       content provider           1 call
       └─ List<HotelDto(id, name, stars, stay)>
            └─ availableRooms                   HotelAvailableRoomsDataLoader  @BatchMapping
                 └─ LoadAvailableRoomsQuery → RoomAvailabilityPort   booking engine  1 call per stay
```

1. `@QueryMapping hotelSearch` only loads the hotels. It does **not** build the full result upfront.
2. Spring for GraphQL resolves every selected field of `Hotel`. `id`, `name` and `stars` are plain properties of
   `HotelDto`.
3. `availableRooms` is not a property of `HotelDto`. It is resolved by the `@BatchMapping` method, which receives
   **all** hotels of the response at once, asks the booking engine with all hotel ids and returns one list of offers per
   hotel, in the same order as the hotels it received.

### `@QueryMapping`, `@SchemaMapping` and `@BatchMapping`

- `@QueryMapping` is a shortcut for `@SchemaMapping(typeName = "Query")` and resolves a root field.
- `@SchemaMapping(typeName = "Hotel", field = "availableRooms")` would resolve the field **once per hotel**:
  n hotels → n calls to the booking engine.
- `@BatchMapping(typeName = "Hotel", field = "availableRooms")` registers a `DataLoader` under the hood and resolves
  the field **once for all hotels** of a request. It is an *alternative* to `@SchemaMapping` for that field, so never
  put both on the same field.

`typeName` is set explicitly because the Kotlin class is called `HotelDto` while the GraphQL type is called `Hotel`.

### Why not assemble everything in the root resolver?

The root resolver could call both sources and return complete objects. It would then always call the booking engine,
even for a query that never selects `availableRooms`. With field-level resolution the data access follows the
**selection** of the client: no `availableRooms` in the query, no booking engine call.

### Why the stay is part of the root query and the batch key

A `@BatchMapping` method only receives the parent objects. It has **no access to field arguments**, because one batch
may contain keys from different places in the query. That is why the stay is an argument of `hotelSearch` (like on any
hotel website) and travels along inside the parent `HotelDto`.

`HotelDto` is the `DataLoader` key, so its `equals`/`hashCode` include the stay. If two aliased searches with different
dates are part of the same query, the same hotel appears as two different keys, and `HotelAvailableRoomsDataLoader`
groups the keys by stay: one booking engine call per stay. `HotelSearchControllerTest` covers this case.

The batch method returns a `List` in key order instead of a `Map`. A hotel without offers is mapped to `[]` with
`orEmpty()`, so no key can get lost because of a missing map entry.

## ⚠️ Nullability and partial responses

`availableRooms: [RoomOffer!]` is **nullable on purpose**, because the two cases mean different things:

- `[]`: the booking engine answered, and the hotel is **sold out** for the stay.
- `null` plus an entry in `errors`: the booking engine **could not be asked**. The hotel list is still returned as a
  partial response.

With `[RoomOffer!]!` a failing booking engine would null the hotel, and because of `[Hotel!]!` the whole `hotelSearch`.
One unreliable source would take down the whole result.

An invalid stay (`checkIn >= checkOut` or a date that is not `yyyy-MM-dd`) is mapped to a `BAD_REQUEST` error by a
`@GraphQlExceptionHandler` in `HotelSearchController`. Dates are ISO-8601 strings to avoid an extra scalar library.
