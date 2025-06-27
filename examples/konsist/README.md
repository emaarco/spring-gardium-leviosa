# 🚀 Konsist - Enforcing Architecture Rules as Tests 🏗️

This module demonstrates how to use [Konsist](https://github.com/LemonAppDev/konsist) to enforce architecture rules through automated tests. Konsist is a pragmatic, lightweight library that allows you to check your Kotlin architecture by writing simple and readable tests.

## ✨ What is Konsist?

Konsist is a free, Kotlin-focused library for checking the architecture of your code. It lets you define architecture rules as Kotlin code, which are then verified during test execution. This ensures that your codebase adheres to your architectural decisions and prevents architectural drift over time.

Key benefits:
- Define architecture rules as readable Kotlin code
- Catch architectural violations early in the development process
- Integrate with your existing test suite (JUnit)
- Kotlin-first approach with idiomatic API
- No runtime dependencies in production code

## 🔧 Available Tests

This module provides two main abstract test classes that you can extend in your projects:

### 1. BasicCodingGuidelinesTest

Enforces basic coding guidelines:
- Ensures all classes have proper package declarations
- Prevents wildcard imports (except for java.util)

```kotlin
class YourBasicCodingGuidelinesTest : BasicCodingGuidelinesTest("com.yourcompany.yourproject")
```

### 2. HexagonalArchitectureTest

Enforces rules specific to hexagonal (ports and adapters) architecture:
- Verifies layer separation and dependencies
- Ensures ports are defined as interfaces
- Checks that application services implement exactly one use case
- Validates that adapters only fulfill one use case or query
- Ensures application services don't depend on other application services

```kotlin
class YourHexagonalArchitectureTest : HexagonalArchitectureTest("com.yourcompany.yourproject")
```

## 🚀 How to Use

1. Add this module as a dependency to your project
2. Create test classes that extend the provided abstract test classes
3. Pass your root package as a parameter to the constructor
4. Run the tests as part of your regular test suite

Example implementation:

```kotlin
// In your architecture test file
class CodingGuidelinesTest : BasicCodingGuidelinesTest("com.yourcompany.yourproject")

class ArchitectureTest : HexagonalArchitectureTest("com.yourcompany.yourproject")
```

## 📦 Module Structure

This example is implemented as a standalone module, making it easy to include in a monorepo environment. This approach allows:

- Multiple services in a monorepo to share the same architecture tests
- Consistent architecture enforcement across microservices
- Central maintenance of architecture rules

To use in a monorepo with multiple services, simply add this module as a dependency to each service's test configuration.

## 🔍 Architecture Validation

The module provides comprehensive validation for hexagonal architecture:

- **Layer Dependency Rules**: Ensures domain layer doesn't depend on anything, ports depend only on domain, adapters depend on domain and ports, etc.
- **Interface Implementation**: Verifies that application services implement exactly one use case interface
- **Naming Conventions**: Enforces consistent naming patterns for services and other components
- **Adapter Responsibility**: Ensures adapters only fulfill one use case to prevent performance issues

You can also create your own custom validations by extending Konsist's API.

---

"I solemnly swear that my code is up to good architecture! Mischief managed with Konsist! ⚡🧙‍♂️"
