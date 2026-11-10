# 🪄 Spring-Gardium-Leviosa

[![CI](https://github.com/emaarco/spring-gardium-leviosa/actions/workflows/pre-merge.yml/badge.svg)](https://github.com/emaarco/spring-gardium-leviosa/actions/workflows/pre-merge.yml)
[![License: MIT](https://img.shields.io/github/license/emaarco/spring-gardium-leviosa)](LICENSE)

**Runnable Kotlin and Spring Boot spells for architecture tests and Spring for GraphQL, one per blog post.**

Each example is a Gradle module that you run or test, then levitate into your own service, as light as a feather;
none of them is published as a dependency.

## 📚 The spellbook

Every spell is brewed with Kotlin 2.4 and Spring Boot 4.1.

| Example                                                                                                 | What it shows                                                                                       | Try it                                |
| ------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- | ------------------------------------- |
| [archunit](examples/archunit/README.md)                                                                 | Architecture rules as tests with ArchUnit, checked against the compiled bytecode                    | Tests in the GraphQL examples         |
| [konsist](examples/konsist/README.md)                                                                   | Architecture rules as tests with Konsist, checked against the Kotlin source                         | Tests in the GraphQL examples         |
| [architecture-combined](examples/architecture-combined/README.md)                                       | ArchUnit and Konsist rules for a service in a single base class                                     | Tests in the GraphQL examples         |
| [spring-for-graphql-http-headers](examples/spring-for-graphql-http-headers/README.md)                   | Reading HTTP headers in Spring for GraphQL with a custom interceptor                                | GraphiQL on port 8081                 |
| [spring-for-graphql-fragment-source](examples/spring-for-graphql-fragment-source/README.md)             | Loading GraphQL fragments from custom locations in tests with a custom `DocumentSource`             | GraphiQL on port 8082                 |
| [spring-for-graphql-multiple-data-sources](examples/spring-for-graphql-multiple-data-sources/README.md) | One GraphQL schema over several data sources with `@SchemaMapping` and `@BatchMapping`              | GraphiQL on port 8083                 |
| [spring-for-graphql-subscriptions](examples/spring-for-graphql-subscriptions/README.md)                 | GraphQL subscriptions that reach clients on every pod, with Kafka events packaged in a Boot starter | GraphiQL on 8084 and 8085, with Kafka |

## 🧙 Cast your first spell

Your wand is JDK 25. On Windows, use `gradlew.bat` instead of `./gradlew`.

1. Clone the repository and build every example:

    ```bash
    git clone https://github.com/emaarco/spring-gardium-leviosa.git
    cd spring-gardium-leviosa
    ./gradlew build
    ```

2. Start one example:

    ```bash
    ./gradlew :examples:spring-for-graphql-multiple-data-sources:bootRun
    ```

3. Open GraphiQL at http://localhost:8083/graphiql and send the queries from
   [the example's README](examples/spring-for-graphql-multiple-data-sources/README.md).

## 🦉 Owl post

-   **Blog posts:** find the posts behind the examples on [Medium](https://medium.com/@emaarco), the
    [Miragon blog](https://miragon.io/blog) and my [GitHub profile](https://github.com/emaarco/emaarco).
-   **Questions and bugs:** something does not run or is unclear?
    [Open an issue](https://github.com/emaarco/spring-gardium-leviosa/issues).

## 🌌 License

[MIT](LICENSE) © Marco Schäck

---

_"It's Levio-SAH, not Levio-SA."_ — Make sure to get those spells (and code) just right!
