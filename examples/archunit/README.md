# 🚀 ArchUnit - Enforcing Architecture Rules as Tests 🏗️

This module demonstrates how to use [ArchUnit](https://www.archunit.org/) to enforce architecture rules through automated tests. ArchUnit is a library that allows you to check your Java or Kotlin architecture by writing simple and readable tests.

## ✨ What is ArchUnit?

ArchUnit is a free, simple, and extensible library for checking the architecture of your Java or Kotlin code. It lets you define architecture rules as code, which are then verified during test execution. This ensures that your codebase adheres to your architectural decisions and prevents architectural drift over time.

Key benefits:

- Define architecture rules as readable code
- Catch architectural violations early in the development process
- Integrate with your existing test suite (JUnit)
- No runtime dependencies in production code

## ⚙️ How It Works

ArchUnit analyzes your **compiled bytecode**. It reads the `.class` files via its
`ClassFileImporter` (see the test classes in this module) and builds a graph of classes,
methods, fields, annotations and — crucially — the **actual dependencies** between them.
Rules are expressed as fluent assertions (`ArchRuleDefinition`, `layeredArchitecture()`)
over that imported graph.

Because it works on the **bytecode**, not on the source:

- ✅ It resolves the _real_ dependency graph: method calls, field accesses, inheritance
  and annotation usage — even ones that are not obvious from a single source file.
- ✅ It is **language-agnostic on the JVM**: it checks Java and Kotlin (and any other JVM
  language) uniformly, which makes it a good fit for mixed-language codebases.
- ⚠️ The code must be **compiled first** — rules run as part of the test phase, after
  `.class` files exist.
- ⚠️ It cannot see source-only details that are erased or transformed by the Kotlin
  compiler: top-level functions become synthetic `…Kt` classes, type aliases disappear,
  KDoc and source declaration order are gone.

> 🔁 For a source-based alternative that understands Kotlin-specific constructs without
> compiling, see the sibling [`konsist`](../konsist/README.md) module.

## 🔍 ArchUnit vs. Konsist — at a glance

| Aspect                                                                                    | **ArchUnit** (this module)                               | **Konsist** ([sibling](../konsist/README.md)) |
| ----------------------------------------------------------------------------------------- | -------------------------------------------------------- | --------------------------------------------- |
| Analysis target                                                                           | Compiled **bytecode** (`.class` via `ClassFileImporter`) | Kotlin **source code** (AST/PSI)              |
| Needs compilation first                                                                   | ✅ Yes                                                   | ❌ No                                         |
| Languages                                                                                 | Any JVM language (Java **and** Kotlin)                   | Kotlin only                                   |
| Real call- & field-level dependency graph                                                 | ✅ Full (resolved from bytecode)                         | ⚠️ Limited (declared references)              |
| Kotlin source constructs (top-level funcs, type aliases, KDoc, declaration order, naming) | ⚠️ Limited / erased after compilation                    | ✅ First-class                                |
| Layer / dependency rules                                                                  | ✅ `layeredArchitecture()`                               | ✅ `assertArchitecture { … }`                 |
| Custom rules                                                                              | Custom `ArchCondition` classes                           | Lambdas over declarations                     |

**Rule of thumb:** reach for **ArchUnit** when your rules are about _what the compiled
code actually depends on_, or when you need to cover Java as well; reach for **Konsist**
when your rules are about _how the Kotlin source is written_ (style, naming, file
structure, Kotlin-specific declarations).

## 🔧 Available Tests

This module provides three abstract test classes that you can extend in your projects:

### 1. BasicCodingGuidelinesTest

Enforces basic coding guidelines:

- Ensures all classes have proper package declarations
- Verifies that classes are free of circular dependencies
- Forbids `println` / `System.out` in production code (use a logger instead)

```kotlin
class YourBasicCodingGuidelinesTest : BasicCodingGuidelinesTest("com.yourcompany.yourproject")
```

### 2. HexagonalArchitectureTest

Enforces rules specific to hexagonal (ports and adapters) architecture:

- Verifies layer separation and dependencies
- Ensures the **domain layer is technology-neutral** (no framework/infrastructure dependencies)
- Ensures the **application layer only orchestrates** (domain, ports and framework tooling only)
- Ensures ports are defined as interfaces and don't depend on services
- Keeps application & adapters in their expected sub-packages and keeps inbound/outbound adapters isolated
- Checks that application services implement exactly one use case
- Validates that adapters only fulfill one use case or query

```kotlin
class YourHexagonalArchitectureTest : HexagonalArchitectureTest("com.yourcompany.yourproject")
```

### 3. NamingConventionArchitectureTest

Enforces per-layer class-name conventions, kept separate from the structural rules above. Every rule
lists the suffixes allowed in a package together with a short rationale (`AllowedSuffix(suffix, reason)`),
so the test doubles as living documentation of the vocabulary:

- Inbound ports → `*UseCase` / `*Query`; outbound ports → `*Port` / `*Repository`
- Application services → `*Service` (or `*Configuration`)
- Inbound adapters → per sub-package (`graphql`, `rest`, `shared`, `spring`), e.g. `*Controller`,
  `*Configuration`, `*Interceptor`, `*Resolver`, `*Dto`, `*Input`
- Outbound adapters → `*PersistenceAdapter` / `*Adapter` / `*Mapper`

Packages a given service doesn't have simply match no classes, so the same rule set fits every service.

```kotlin
class YourNamingConventionTest : NamingConventionArchitectureTest("com.yourcompany.yourproject")
```

> 🧭 A service can depend on this module on its own (both `spring-for-graphql-*` examples show a
> standalone `ArchUnitArchitectureTest`) — or reach for the
> [`architecture-combined`](../architecture-combined/README.md) module, which combines these ArchUnit
> rules with Konsist's source-level rules into a single one-line suite.

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

## 🔍 Custom Conditions

The module includes custom conditions that can be used in your architecture tests:

- `InterfaceImplementationConditions`: Ensures classes implement exactly one interface from a specific package
- `UseCaseDependencyConditions`: Verifies that adapters only fulfill one use case to prevent performance issues

You can also create your own custom conditions by extending ArchUnit's `ArchCondition` class.

---

"Accio clean architecture! Your codebase will thank you later! ⚡🏰"
