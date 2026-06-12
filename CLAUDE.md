# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test

Maven project, **Java 25**, Spring Boot 4.0.5. There is no Maven wrapper — use a system `mvn`.

```bash
mvn test                                              # run all tests
mvn -Dtest=OvalParserImplTest test                    # single test class
mvn -Dtest=OvalParserImplTest#methodName test         # single test method
mvn spring-boot:run                                   # run the REST service (port 8080)
mvn clean package                                      # build the bootable jar
```

Tests are JUnit 5 (`spring-boot-starter-test`). Most are plain unit tests over individual parsers/resolvers/builders; they read fixtures from `src/test/resources/` (`oval.xml`, `xccdf.xml`).

## What this service does

A Spring Boot service that **compiles SCAP content** (an XCCDF benchmark + its OVAL definitions, both XML) into a flattened, execution-ready JSON artifact called an `ExecutionTemplate`. The template tells a downstream agent exactly which OVAL objects to collect on a host, which states to assert, and how rules/definitions combine — with all XCCDF/OVAL cross-references and variables resolved ahead of time.

**Triggers and I/O** (all against LocalStack locally; see touchstone-infra for buckets/queue):
- `messaging/CompileJobPoller` long-polls the `touchstone-compile-jobs` SQS queue. Messages are either a `CompileTemplateRequest` JSON (`{packageId, profileIds?}` — null/empty profileIds = all profiles) or a raw S3 event notification (the raw-content bucket notifies the queue when `packages/<id>/oval.xml` is uploaded → all-profiles compile). Failed jobs are left for redelivery and dead-letter after 3 attempts.
- Content in: `content/S3ContentPackageLoader` reads `packages/<packageId>/{xccdf,oval}.xml` from `touchstone-raw-content`.
- Templates out: `store/S3ExecutionTemplateStore` writes `templates/<packageId>/<profileId>.json` to `touchstone-compiled-templates` (deterministic key, latest wins).
- `POST /api/templates/compile` (`TemplateController`) is a **debug/coverage tool** (synchronous; response surfaces `unsupportedCheckTypes`/warnings) gated by `app.compile-endpoint.enabled`; the poller is gated by `app.compile-job-poller.enabled`.

**The template is a cross-language contract** with the future Go agent: `contracts/execution-template.schema.json` is the formal schema, `contracts/variable-resolution.md` the variable/collection semantics. `ExecutionTemplate.CURRENT_SCHEMA_VERSION` is stamped on every template — bump it on breaking shape changes. Template output is deliberately deterministic (sorted ids at assembly); `TemplateCompileServiceGoldenTest` compares the compiled DISA fixture against `src/test/resources/golden/` and fails on any contract drift (regenerate with `-Dgolden.update=true`).

## Compilation pipeline

`TemplateCompileService.compile()` orchestrates the whole flow. The stages, and the package each lives in:

1. **Parse** (`parser/`) — `XccdfParser` and `OvalParser` turn XML streams into `model/parsed/**` objects. Parsing is delegated to small per-element readers under `parser/reader/{oval,xccdf}/` (e.g. `OvalObjectReader`, `RuleReader`).
2. **Index** (`index/`) — `XccdfIndexBuilder`/`OvalIndexBuilder` build lookup maps (`XccdfIndex`, `OvalIndex`) keyed by id. **Indexes are per-content-package values, not singleton beans** — they are built fresh each compile and passed explicitly.
3. **Resolve** (`resolve/`, `variables/`) — turns the parsed graph into the subset actually needed for one profile, producing `model/resolved/**`:
   - `ProfileResolver` → selected rules for the requested profile.
   - `RuleOvalReferenceResolver` → each rule's OVAL definition references.
   - `ReferencedOvalDefinitionResolver` → the transitive closure of definitions/tests/objects/states reachable from those references — the **evaluation slice** (`ResolvedOvalEvaluationSlice`). Closure logic lives in `OvalDefinitionClosureResolver`, `OvalTestDependencyResolver`, `OvalEvaluationSliceResolver`.
   - `OvalVariableBindingResolver` + `variables/` → resolve XCCDF/OVAL variables into `ResolvedVariableBindings`.
4. **Compile** (`oval/`, `model/compiled/`) —
   - `OvalCheckCompilationService` compiles each test in the slice into a `CompiledOvalCheck` plus its collection objects (`CompiledObjectPlan`) and `CompiledState`s.
   - `OvalDefinitionPlanCompiler` compiles the definition criteria trees into `CompiledOvalDefinitionPlan` (AND/OR/criterion nodes).
   - Local variable plans are compiled by `LocalVariablePlanCompiler`.
5. **Assemble** — `assembleTemplate()` collects everything into one `ExecutionTemplate` (rules, definition plans, objects-by-id, states-by-id, checks, unsupported check types) and returns a `CompileTemplateResponse`.

## OVAL check compiler plugin architecture

This is the largest and most extension-heavy part of the code. Each OVAL **test type** (`registry_test`, `passwordpolicy_test`, `wmi57_test`, …) has its own compiler under `oval/windows/<probe>/`.

- All check compilers implement `OvalCheckCompiler` (`supports(test)` / `compile(...)`) and are injected as a `List<OvalCheckCompiler>` into `OvalCheckCompilationServiceImpl`, which dispatches each test to the first compiler whose `supports()` returns true. Spring auto-discovers them via `@Component`.
- Concrete compilers extend `oval/common/CheckCompilerBase`, implementing just two methods: `supportedTestType()` (the OVAL test-type string) and `compileSimpleObject(...)`. The base class handles the rest: object/state lookup via the index, **set objects** (`ParsedOvalObjectSet` → `OvalSetTask` with operator + child objects), **filters** (`OvalFilterTask`), circular-reference detection, and state compilation.
- `compileSimpleObject` pulls the entities it needs off the `ParsedOvalObject` (e.g. `object.findEntity("hive").resolve()`), builds a probe-specific `*CollectionTask` (extends `CollectionTaskBase`/`SelectorCollectionTaskBase`), and wraps it in a `CompiledObjectPlan`.

**To add support for a new OVAL test type:** create a new package under `oval/windows/<probe>/` (or `oval/independent/<probe>/` for OVAL independent-family types — see `variable`) with a `@Component` compiler extending `CheckCompilerBase` and a `*CollectionTask`. No wiring/registration is needed — the `List<OvalCheckCompiler>` injection picks it up. Tests whose type no compiler supports are recorded in `unsupportedCheckTypes` rather than failing the compile. Every check type used by the seeded CIS Windows benchmarks is currently supported.

## Model layering

`model/` is split by pipeline stage and must not be collapsed:
- `model/parsed/**` — faithful representation of the source XML.
- `model/normalized/**` — normalized intermediate forms.
- `model/resolved/**` — references resolved, scoped to one profile.
- `model/compiled/**` — final execution-ready artifact (`ExecutionTemplate` and friends).

## Current state / gotchas

- Local runs need LocalStack with the buckets/queue provisioned and content seeded — see the private **touchstone-infra** repo (`docker compose up -d`, `terraform -chdir=envs/local apply`, `scripts/seed-content.ps1`). LocalStack Community loses all state on container restart.
- Spring Boot 4 does **not** auto-configure an `ObjectMapper` here; `config/JacksonConfig` defines the contract mapper (ISO-8601 dates). Don't remove it.
- Rule severity/weight is **deliberately not** in the template (decided): the template is agent-facing execution instructions only; the future result service parses raw XCCDF for scoring metadata.
- Variable tailoring/overrides: `compileProfile` passes an empty overrides map to `OvalVariableBindingResolver` — that's the seam where per-org tailoring will plug in. Unbound external variables compile to `UNRESOLVED` + warning (the CIS Controls Assessment Module "survey" profiles do this intentionally; the agent errors those tests).
- `CpeParser` is a `StubCpeParser`.
- Lombok is used throughout (`@RequiredArgsConstructor` for constructor injection, `@Slf4j`, `@Builder`, `@Data`/`@Getter`). New beans follow the constructor-injection-via-`@RequiredArgsConstructor` convention.
- Singleton task lists in compilers use `new ArrayList<>(List.of(task))` on purpose: `CheckCompilerBase.applyFilter()` mutates the list when objects carry OVAL filters.