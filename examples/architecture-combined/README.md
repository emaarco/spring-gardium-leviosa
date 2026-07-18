# 🧭 Combined Architecture Rules — ArchUnit **and** Konsist in One Module 🧱

The [`archunit`](../archunit/README.md) and [`konsist`](../konsist/README.md) modules each demonstrate
**one** tool in isolation. This module is the **common module you actually depend on from a service**:
it bundles both into a single ready-to-use suite, picking the right tool for each rule.

A service gets the full architecture check with one line:

```kotlin
import de.emaarco.architecture.ServiceArchitectureTest

class ArchitectureTest : ServiceArchitectureTest("com.yourcompany.yourservice")
```

That's it — no per-tool wiring, no duplicated rules.

## 🤔 Why combine them?

ArchUnit reads **compiled bytecode**, Konsist reads **Kotlin source**. That difference is not a
rivalry — it is a division of labor:

- Bytecode carries the *fully resolved dependency graph* (real method calls, field accesses,
  inheritance). That is what layering and technology-neutrality rules need. Konsist only sees
  *declared* references, so it is weaker here.
- Source carries *how the file is written* (how many top-level declarations it holds, wildcard
  imports, Kotlin-only constructs). The Kotlin compiler erases most of this into synthetic classes,
  so ArchUnit literally cannot see it.

So this module uses **each tool where it is strong** — and deliberately does *not* run the same rule
twice. Konsist's own hexagonal-layer checks are left out because ArchUnit already does layering
better on the resolved bytecode graph.

This module is **self-contained by design**: it carries both the ArchUnit and Konsist dependencies
and its **own copies** of the rules. It intentionally does *not* depend on the standalone
[`archunit`](../archunit) / [`konsist`](../konsist) example modules, so it can be dropped into a
service as a single, standalone dependency. The small amount of duplication is the price for keeping
the three modules independent.

## 🧩 Division of labor

| Rule                                                        | Enforced by  | Nested group      |
| ----------------------------------------------------------- | ------------ | ----------------- |
| Hexagonal layer dependencies                                | **ArchUnit** | `Dependencies`    |
| Domain is technology-neutral                                | **ArchUnit** | `Dependencies`    |
| Application only orchestrates (domain + ports + framework)  | **ArchUnit** | `Dependencies`    |
| Ports don't depend on services / adapter in↔out isolation   | **ArchUnit** | `Dependencies`    |
| Naming per layer (ports, services, in/out adapters)         | **ArchUnit** | `Naming`          |
| Packages free of cycles                                     | **ArchUnit** | `CodingGuidelines`|
| No `println` / `System.out` in production                   | **ArchUnit** | `CodingGuidelines`|
| **One top-level class/interface/object per `.kt` file**     | **Konsist**  | `KotlinSource`    |
| No wildcard imports                                         | **Konsist**  | `KotlinSource`    |

See [`ServiceArchitectureTest.kt`](src/main/kotlin/de/emaarco/architecture/ServiceArchitectureTest.kt)
for how the groups are wired — four `@Nested inner class`es delegating to the ArchUnit and Konsist
base classes.

## 📦 How it is packaged

This is a `src/main` rules module — like [`archunit`](../archunit) and [`konsist`](../konsist), it
ships **only abstract base classes**, no demo service of its own. Its `build.gradle.kts` pulls in both
tools directly (`api(libs.bundles.archunit)` + `api(libs.bundles.konsist)` + `api(libs.bundles.test)`)
and re-exports them, so a consumer only needs a single dependency:

```kotlin
// in your service's build.gradle.kts
testImplementation(project(":examples:architecture-combined"))
```

Both `spring-for-graphql-*` example services use exactly this setup — a one-line `ArchitectureTest`
(shown above) next to the standalone `ArchUnitArchitectureTest` and `KonsistArchitectureTest`, so you
can see all three consumption styles side by side.

## 🚀 Run it

```bash
./gradlew :examples:spring-for-graphql-http-headers:test
```

---

"Two spells, one wand — cast ArchUnit and Konsist together for airtight architecture! ⚡🪄"
