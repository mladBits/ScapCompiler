# ScapCompiler

[![CI](https://github.com/mladBits/ScapCompiler/actions/workflows/ci.yml/badge.svg)](https://github.com/mladBits/ScapCompiler/actions/workflows/ci.yml)

**A Spring Boot service that compiles SCAP security content (XCCDF + OVAL XML) into flattened, execution-ready JSON templates for a distributed fleet of endpoint agents.**

This is the compiler component of a larger cloud-based, agent-based SCAP 1.2 compliance-evaluation platform I'm building, designed to scale to tens of thousands of agents. Instead of shipping heavyweight XML parsers and cross-reference resolution logic to every endpoint, all of that work happens **once, server-side** — agents receive a small, self-contained JSON artifact that tells them exactly what to collect and how to evaluate it.

## The problem

SCAP benchmarks (DISA STIGs, CIS Benchmarks) are dense, heavily cross-referenced XML: an XCCDF benchmark selects rules per profile, rules reference OVAL definitions, definitions reference tests, tests reference objects and states, and any of those can reference variables — which may themselves be computed from *other collected objects* at scan time. Traditional scanners re-parse and re-resolve all of this on every host, on every scan.

ScapCompiler does the resolution once and emits an **ExecutionTemplate**: a versioned JSON contract consumed by a lightweight agent (a Go implementation is the next component of the platform). The agent never sees XML.

```
XCCDF + OVAL XML ──► parse ──► index ──► resolve ──► compile ──► ExecutionTemplate (JSON)
```

## How it works

The pipeline is five explicit stages, each with its own model layer (`parsed` → `resolved` → `compiled`) so source fidelity, profile scoping, and execution concerns never bleed into each other:

1. **Parse** — streaming StAX readers turn XCCDF/OVAL XML into faithful `parsed` models (no DOM; benchmark files can be large).
2. **Index** — per-content-package lookup maps keyed by id (deliberately *not* singleton beans).
3. **Resolve** — selects the requested profile's rules, then computes the **transitive closure** of every OVAL definition, test, object, state, and variable reachable from them — the *evaluation slice*. This includes a dedicated variable-closure resolver that follows variable→variable and variable→object reference graphs with cycle detection.
4. **Compile** — each OVAL test is dispatched to a probe-specific compiler plugin; definition criteria trees become AND/OR/criterion ASTs; variables become literal values or runtime evaluation plans.
5. **Assemble** — everything lands in one `ExecutionTemplate` with diagnostics (`unsupportedCheckTypes`, `warnings`) instead of hard failures: one broken check should never kill a 300-rule template.

### Plugin architecture for OVAL probes

Each OVAL test type (`registry_test`, `wmi57_test`, `passwordpolicy_test`, …) is a self-contained package: a compiler extending `CheckCompilerBase` plus a `CollectionTask`. Spring discovers them by interface injection — **adding a new probe requires zero wiring**:

```java
@Component
public class RegistryCheckCompiler extends CheckCompilerBase {
    @Override
    protected String supportedTestType() { return "registry_test"; }

    @Override
    protected ObjectCompilationResult compileSimpleObject(OvalCheckCompileContext ctx, ParsedOvalObject object) {
        // pull hive/key/name entities, emit a RegistryCollectionTask
    }
}
```

The base class handles the hard, shared semantics: set objects (union/intersection/complement), state filters, circular-reference detection, and compiling objects that are referenced only by variables.

### Runtime variables as data, not code

OVAL variables are the trickiest part of SCAP: external variables bound from XCCDF values, constants, and *local* variables computed at scan time from functions like `concat` and `regex_capture` over previously collected objects. The compiler materializes all of this into the template as a declarative expression tree the agent can walk:

```json
{
  "oval:mil.disa.stig.win:var:25334001": {
    "datatype": "string",
    "kind": "PLAN",
    "expression": {
      "function": "concat",
      "components": [
        { "function": "object", "objectRef": "oval:...:obj:20000015", "itemField": "value" },
        { "function": "regex_capture", "pattern": "^%.*%(.*)$",
          "component": { "function": "object", "objectRef": "oval:...:obj:25334002", "itemField": "value" } }
      ]
    }
  }
}
```

The exact agent-side resolution semantics (memoization, cartesian-product concat, `var_check` folding, error propagation) are specified in [`contracts/variable-resolution.md`](contracts/variable-resolution.md) — the template is treated as a **cross-language contract**, since the consuming agent is written in Go.

## Tech stack

| Area | Choice |
| --- | --- |
| Language | Java 25 (sealed interfaces, pattern-matching switch, records) |
| Framework | Spring Boot 4 |
| XML | StAX (Woodstox) streaming parsers |
| JSON | Jackson with polymorphic type discriminators |
| Cloud | AWS SDK v2 (S3/SQS), developed against LocalStack |
| Testing | JUnit 5 — 150 tests, from per-reader unit tests to a full-pipeline test over a real DISA STIG fixture |

## Running it

```bash
mvn test                 # full test suite
mvn spring-boot:run      # REST service on :8080
# POST /api/templates/compile  { "benchmarkId": "...", "profileId": "..." }
```

The repo includes a real Windows STIG XCCDF/OVAL fixture under `src/test/resources/` used by the end-to-end tests.

## The bigger picture

ScapCompiler is component one of four. The platform design (pull-based agent check-in with presigned S3 URLs, agent-side OVAL evaluation with server-side XCCDF scoring, Postgres for fleet/results, SQS-driven result processing):

```
content XML ──► ScapCompiler ──► template in S3
                                      │ presigned GET
        Go agent (Windows service) ◄──┤  collect → evaluate OVAL → partial ARF
                                      │ presigned PUT
        S3 ──► SQS ──► result service ──► XCCDF scoring ──► Postgres / full ARF
                                                  │
                                            React dashboard
```

**Current focus:** hardening the compiler (S3 content loading, template schema versioning + published JSON Schema, golden-file contract tests) before starting the Go agent's evaluation engine.

## Project status

Active, early-stage personal project. Implemented today: the full compile pipeline, 10 Windows OVAL probe compilers, set/filter semantics, complete variable materialization, and graceful degradation for unsupported content. The S3 persistence path and the downstream platform components are in progress — see the roadmap above.
