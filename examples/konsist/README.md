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

## ⚙️ How It Works

Konsist analyzes your **Kotlin source code** directly. Under the hood it parses the
`.kt` files via the Kotlin compiler and exposes the resulting syntax tree (AST/PSI) as
a Kotlin-friendly declaration API (`KoClassDeclaration`, `KoFunctionDeclaration`,
`KoPropertyDeclaration`, …). You navigate these declarations and assert on them — see
`scopeFromProject()` / `scopeFromPackage(...)` in the test classes of this module.

Because it works on the **source**, not on compiled output:

- ✅ No compilation step is required — rules run against the raw `.kt` files.
- ✅ It understands Kotlin-only constructs: top-level functions, extension functions,
  type aliases, `data`/`sealed`/`value` modifiers, KDoc, declaration order, visibility,
  file & naming conventions.
- ⚠️ It is **Kotlin-only** — it cannot see Java source or third-party `.class` files.
- ⚠️ It reasons about _declared_ references in the source, not the fully resolved
  runtime call graph, so deep transitive call-level dependency analysis is limited.

> 🔁 For a bytecode-based alternative that also covers Java and resolves real call-level
> dependencies, see the sibling [`archunit`](../archunit/README.md) module.

## 🔍 Konsist vs. ArchUnit — at a glance

| Aspect                                                                                    | **Konsist** (this module)        | **ArchUnit** ([sibling](../archunit/README.md))          |
| ----------------------------------------------------------------------------------------- | -------------------------------- | -------------------------------------------------------- |
| Analysis target                                                                           | Kotlin **source code** (AST/PSI) | Compiled **bytecode** (`.class` via `ClassFileImporter`) |
| Needs compilation first                                                                   | ❌ No                            | ✅ Yes                                                   |
| Languages                                                                                 | Kotlin only                      | Any JVM language (Java **and** Kotlin)                   |
| Kotlin source constructs (top-level funcs, type aliases, KDoc, declaration order, naming) | ✅ First-class                   | ⚠️ Limited / erased after compilation                    |
| Real call- & field-level dependency graph                                                 | ⚠️ Limited (declared references) | ✅ Full (resolved from bytecode)                         |
| Layer / dependency rules                                                                  | ✅ `assertArchitecture { … }`    | ✅ `layeredArchitecture()`                               |
| Custom rules                                                                              | Lambdas over declarations        | Custom `ArchCondition` classes                           |

**Rule of thumb:** reach for **Konsist** when your rules are about _how the Kotlin source
is written_ (style, naming, file structure, Kotlin-specific declarations); reach for
**ArchUnit** when your rules are about _what the compiled code actually depends on_, or
when you need to cover Java as well.

## 🔧 Available Tests

This module provides three abstract test classes that you can extend in your projects:

### 1. BasicCodingGuidelinesTest

Enforces basic coding guidelines:

- Ensures all classes have proper package declarations
- Prevents wildcard imports (except for java.util)
- Enforces **one top-level class/interface/object per `.kt` file** (SRP)

> 💡 The one-declaration-per-file rule is the flagship example of *why Konsist complements ArchUnit*:
> the Kotlin compiler merges every top-level declaration of a file into synthetic class files, so
> ArchUnit (which reads bytecode) cannot tell how many declarations a source file holds — but Konsist
> reads the source and can.

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

### 3. NamingConventionArchitectureTest

The source-based counterpart to ArchUnit's naming test — it reads the `.kt` declarations directly and
enforces per-layer class-name suffixes, each with a short rationale (`AllowedSuffix(suffix, reason)`):

- Inbound ports → `*UseCase` / `*Query`; outbound ports → `*Port` / `*Repository`
- Application services → `*Service` (or `*Configuration`)
- Inbound adapters → per sub-package (`graphql`, `rest`, `shared`, `spring`)
- Outbound adapters → `*PersistenceAdapter` / `*Adapter` / `*Mapper`

Packages a given service doesn't have are skipped, so the same rule set fits every service.

```kotlin
class YourNamingConventionTest : NamingConventionArchitectureTest("com.yourcompany.yourproject")
```

> 🧭 A service can depend on this module on its own (both `spring-for-graphql-*` examples show a
> standalone `KonsistArchitectureTest`) — or reach for the
> [`architecture-combined`](../architecture-combined/README.md) module, which combines these Konsist
> rules with ArchUnit's bytecode-level rules into a single one-line suite.

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
