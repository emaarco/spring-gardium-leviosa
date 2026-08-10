# 🧬 Mutation Testing with pitest — a hands-on spike

> A time-boxed **learning & documentation** spike, not a production rollout.
> There is intentionally **no CI gate / threshold** here — the goal is to understand the tool,
> capture the pitfalls, and leave a reusable, blog-ready artifact.

This note answers three questions by example, using the `spring-for-graphql-fragment-source`
module — the one module in this repo with a real **behavioural** test
(`LoadTasksControllerTest`) rather than ArchUnit/Konsist architecture rules:

1. [**What** is mutation testing?](#1-what-is-mutation-testing)
2. [**What value** does it bring?](#2-what-value-does-it-bring)
3. [**How** do you set it up? (by example)](#3-how-to-set-it-up-by-example)

…and then walks through [one concrete survived mutant we deliberately killed](#4-walkthrough-kill-a-survived-mutant),
plus the [Kotlin & JVM-25 pitfalls](#5-pitfalls-we-actually-hit) we actually hit.

---

## 1. What is mutation testing?

Line/branch coverage only proves a line **ran**. It says nothing about whether a test would
**catch a bug** on that line. A test can execute every line and assert *nothing* meaningful and
still show 100 % coverage.

**Mutation testing** closes that gap. A tool ([pitest](https://pitest.org)) takes your compiled
production bytecode and introduces lots of tiny changes — **mutants** — one at a time:

| Original            | Mutant                       | Mutator                         |
|---------------------|------------------------------|---------------------------------|
| `a > b`             | `a >= b`                     | `NegateConditionals` / boundary |
| `return true`       | `return false`               | `BooleanFalseReturnVals`        |
| `return list`       | `return Collections.emptyList()` | `EmptyObjectReturnVals`     |
| `foo()` (void call) | *(call removed)*             | `VoidMethodCall`                |

For each mutant it re-runs the tests that cover the mutated line:

- **KILLED** — a test failed → the suite caught the fault. 🎉
- **SURVIVED** — all tests still passed → the tests *execute* that line but do not *assert* its
  behaviour. ⚠️ (or the mutant is *equivalent* — see the pitfalls section)
- **NO_COVERAGE** — no test exercises the line at all.

The **mutation score = killed / total mutants** measures *test quality*, not just execution.
pitest also reports **test strength = killed / (killed + survived)** — the score with
uncovered code excluded, i.e. "of the mutants my tests actually reached, how many did they
catch?".

---

## 2. What value does it bring?

- **Surfaces assertion-free / weak tests** that green line-coverage happily hides.
- **Points at concrete gaps** — each survived mutant is a specific, actionable "your test wouldn't
  notice if this line changed", instead of a blind coverage percentage to chase.
- Widely called the **"gold standard"** of coverage.

### Why this matters for AI-driven development 🪄

AI easily generates *many* green tests with high line coverage that assert nothing meaningful.
The **mutation score is an objective, automatable signal** of whether AI-generated tests actually
catch bugs — and **each survived mutant is a ready-made feedback loop** you can hand back to the
model:

> *"Mutant survived: `LoadTasksService::loadTasks` still passes when its return value is replaced
> with an empty list. Strengthen the test until this mutant is killed."*

That is exactly the loop we run in [section 4](#4-walkthrough-kill-a-survived-mutant) — by hand,
but it automates cleanly.

---

## 3. How to set it up (by example)

Everything below is applied **only** to this one module; the rest of the repo is untouched.

### 3.1 Version catalog — `gradle/libs.versions.toml`

```toml
[versions]
pitest_plugin_version = "1.19.0"   # Gradle-9-compatible; ships PIT 1.22.1
pitest_junit5_version = "1.2.2"    # teaches PIT how to run JUnit 5 (Jupiter) tests

[libraries]
pitest_junit5 = { module = "org.pitest:pitest-junit5-plugin", version.ref = "pitest_junit5_version" }

[plugins]
pitest = { id = "info.solidsoft.pitest", version.ref = "pitest_plugin_version" }
```

### 3.2 Module build — `examples/spring-for-graphql-fragment-source/build.gradle.kts`

```kotlin
plugins {
    // …existing plugins…
    alias(libs.plugins.pitest)
}

dependencies {
    // …existing dependencies…
    pitest(libs.pitest.junit5)
}

pitest {
    pitestVersion.set("1.22.1")
    // Mutate only the behavioural surface the tests actually assert on — keeps the score meaningful.
    targetClasses.set(
        setOf(
            "de.emaarco.example.adapter.inbound.graphql.*",
            "de.emaarco.example.adapter.inbound.shared.TaskDto*",
            "de.emaarco.example.application.service.LoadTasksService*",
        ),
    )
    targetTests.set(
        setOf(
            "de.emaarco.example.adapter.inbound.graphql.LoadTasksControllerTest",
            "de.emaarco.example.application.service.LoadTasksServiceTest",
        ),
    )
    threads.set(4)
    outputFormats.set(setOf("HTML", "XML"))
    timestampedReports.set(false) // stable path: build/reports/pitest/index.html
}
```

> **Why scope `targetClasses`?** This module also contains untested classes (`AddTaskService`,
> `TaskPersistenceAdapter`, the `domain` value classes, `TaskDataSink`). Mutating everything just
> produces a wall of `NO_COVERAGE` and hides the signal. Scoping to what the behavioural tests
> reach makes the mutation score a **clean, honest** number. Widen it once more behavioural tests
> exist.

### 3.3 Run it

```bash
./gradlew :examples:spring-for-graphql-fragment-source:pitest -PjavaToolchainVersion=21
```

(The `-PjavaToolchainVersion=21` part is the JVM-25 workaround — see
[section 5.2](#52-jvm-25--pitestasm-cant-read-the-newest-bytecode).)

Then open the HTML report:

```
examples/spring-for-graphql-fragment-source/build/reports/pitest/index.html
```

It shows each source file with every line colour-coded (green = mutants killed, red = survived)
and a per-mutator breakdown — click a line to see exactly which mutant survived.

### 3.4 What our run produces

```
>> Generated 12 mutations Killed 8 (67%)
>> Mutations with no coverage 2. Test strength 80%
```

| Status        | Count | Where                                                                     |
|---------------|-------|---------------------------------------------------------------------------|
| **KILLED**    | 8     | `TaskDto` getters + `from`, `LoadTasksController.tasks`, `LoadTasksService.loadTasks` |
| **SURVIVED**  | 2     | both *equivalent* — a log call and a Kotlin synthetic (see §5.1)          |
| **NO_COVERAGE** | 2   | the two `log.debug { … }` message lambdas (debug logging is off in tests) |

The takeaway: `LoadTasksControllerTest`'s `containsExactly(...)` is a genuinely **strong** test —
it kills every *killable* mutant on the controller and DTO. The only survivors there are
**equivalent mutants** you should *not* chase. The one meaningful gap we found and fixed was in the
service layer 👇.

---

## 4. Walkthrough: kill a survived mutant

The service under test is trivial — but that is the point:

```kotlin
// application/service/LoadTasksService.kt
@Service
class LoadTasksService(private val repository: TaskRepository) : LoadTasksQuery {
    override fun loadTasks() = repository.load()
}
```

### ❌ Before — a plausible "green but weak" test

This is exactly the kind of test an AI (or a rushed human) produces: it runs, it's green, coverage
is 100 %… and it asserts almost nothing.

```kotlin
val tasks = listOf(buildTask("Task 1"), buildTask("Task 2"))
every { repository.load() } returns tasks

val result = service.loadTasks()

assertThat(result).isNotNull() // ⚠️ weak
```

pitest verdict:

```
>> Generated 12 mutations Killed 7 (58%)   ·   Test strength 70%
SURVIVED  LoadTasksService::loadTasks
          "replaced return value with Collections.emptyList"
```

The mutant makes `loadTasks()` return an **empty list**. `emptyList()` is still non-null, so
`isNotNull()` stays green — the bug slips through. **Survived.**

### ✅ After — assert the actual payload

```kotlin
val result = service.loadTasks()

// Asserting the actual payload (not just non-null / size) is what kills the
// `replaced return value with Collections.emptyList` mutant on `loadTasks()`.
assertThat(result).containsExactlyElementsOf(tasks)
verify { repository.load() }
```

pitest verdict:

```
>> Generated 12 mutations Killed 8 (67%)   ·   Test strength 80%
KILLED    LoadTasksService::loadTasks
          "replaced return value with Collections.emptyList"
```

| Metric               | Before (weak) | After (strong) |
|----------------------|:-------------:|:--------------:|
| Mutation score       | 58 %          | **67 %**       |
| Test strength        | 70 %          | **80 %**       |
| `loadTasks` mutant   | 🔴 SURVIVED   | 🟢 KILLED      |

Same line coverage (100 %) in **both** cases — mutation testing is what told the two apart. That
gap is invisible to Jacoco.

---

## 5. Pitfalls we actually hit

### 5.1 Kotlin synthetics → equivalent (practically unkillable) mutants

The Kotlin compiler emits **synthetic bytecode** that pitest happily mutates but no reasonable
test can kill. These are **equivalent mutants** (they don't change observable behaviour), and they
drag the score down as *noise*. Two of our surviving mutants are exactly this:

1. **`removed call to Intrinsics::checkNotNullExpressionValue`** in `TaskDto.from` (line 14,
   `task.id.value.toString()`).
   Kotlin inserts a runtime null-assertion on the result of `toString()`. Since the value is never
   actually null, deleting the assertion changes nothing observable → **unkillable equivalent
   mutant**.

2. **`removed call to KLogger::debug`** in `LoadTasksController.tasks` (line 18).
   Not strictly a Kotlin synthetic, but the same lesson: logging has no observable behaviour, so
   killing it would require asserting on log output — a *bad* test. Leave it.

The two `NO_COVERAGE` mutants are the `log.debug { "…" }` **message lambdas** — the lambda body is
only evaluated when debug logging is enabled, which it isn't under test. Also noise.

pitest even hints at the root cause on every run:

```
* Project uses kotlin, but the Arcmutate kotlin plugin is not present.
  (https://docs.arcmutate.com/docs/kotlin.html)
```

**Takeaways:**
- With the OSS plugin, **expect a few equivalent mutants** from Kotlin null-checks, `data class`
  synthetics, `when`-exhaustiveness branches, etc. Read survivors critically — *"is this a real
  gap, or an equivalent mutant?"* — before "fixing" them.
- First-class Kotlin support (which filters these) is **commercial**:
  [Arcmutate](https://www.arcmutate.com/) ships a Kotlin plugin. Out of scope for a first spike.
- If a particular equivalent survivor is too noisy, you can filter it, e.g.
  `pitest { avoidCallsTo.set(setOf("io.github.oshai.kotlinlogging")) }` to silence the logging
  mutants — we deliberately left them in here so this document shows the raw, honest output.

### 5.2 JVM 25 → pitest/ASM can't read the newest bytecode

This repo's Gradle toolchain targets **Java 25**. pitest reads/mutates bytecode via **ASM**, and
the OSS toolchain doesn't yet support the Java 25 class-file version (major version 69) — and, more
practically, **no JDK 25 may even be installed** on the spike machine, which fails the build before
pitest is reached:

```
> Cannot find a Java installation on your machine matching:
  {languageVersion=25, …}. Toolchain download repositories have not been configured.
```

**Fix for the spike:** lower the toolchain to a pitest/ASM-friendly JDK (21 works everywhere)
**without editing any file**. The root `build.gradle.kts` now reads the target from a property:

```kotlin
val javaToolchainVersion = (findProperty("javaToolchainVersion") as String? ?: "25").toInt()
java { toolchain { languageVersion.set(JavaLanguageVersion.of(javaToolchainVersion)) } }
// …and the Kotlin jvmTarget is derived the same way.
```

So the repo default stays **25**, and the spike runs with a one-off override:

```bash
./gradlew :examples:spring-for-graphql-fragment-source:pitest -PjavaToolchainVersion=21
```

Once pitest/ASM ship stable Java 25 support (and a JDK 25 is available), drop the override and it
runs on the default toolchain.

---

## TL;DR

- Mutation testing measures **test quality** (would a fault be *caught*), not **execution**
  (did a line *run*) — the gap Jacoco can't see.
- Setup is ~15 lines: the `info.solidsoft.pitest` plugin + `pitest-junit5-plugin`, scoped to one
  module. Report at `build/reports/pitest/index.html`.
- We turned a green-but-weak `isNotNull()` test into a real one by **killing a survived mutant**
  (58 % → 67 % score, 70 % → 80 % strength).
- Kotlin OSS pitest has **equivalent-mutant noise** (synthetics); Java 25 needs a **lowered
  toolchain** for now. Both are expected, both are documented above.
