## 🚀 Access HTTP-Headers in spring-for-graphql 🌐

This repository contains the code example from the blog post: Seamlessly Accessing HTTP Headers in Spring for GraphQL.
The post explains how to handle HTTP headers in a Spring GraphQL application using a custom interceptor.

### 🏁 Getting Started

To test the application, simply clone the repository and start the application. No additional configuration is required.

### 🔍 Testing with GraphiQL

Once the application is running, you can perform test requests using GraphiQL, which is accessible
at: http://localhost:8081/graphiql.

Feel free to explore and test out how the headers are managed within the GraphQL context.

To use the `addTask` mutation make sure to set the headers as follows:

```json
{
  "x-user-id": "<add-an-user-id>"
}
```