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

A Spring Boot REST app that **compiles SCAP content** (an XCCDF benchmark + its OVAL definitions, both XML) into a flattened, execution-ready JSON artifact called an `ExecutionTemplate`. The single endpoint is `POST /api/templates/compile` (`TemplateController` → `TemplateCompileService`). The template tells a downstream agent exactly which OVAL objects to collect on a host, which states to assert, and how rules/definitions combine — with all XCCDF/OVAL cross-references and variables resolved ahead of time.

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

**To add support for a new OVAL test type:** create a new package under `oval/windows/<probe>/` with a `@Component` compiler extending `CheckCompilerBase` and a `*CollectionTask`. No wiring/registration is needed — the `List<OvalCheckCompiler>` injection picks it up. Tests whose type no compiler supports are recorded in `unsupportedCheckTypes` rather than failing the compile.

## Model layering

`model/` is split by pipeline stage and must not be collapsed:
- `model/parsed/**` — faithful representation of the source XML.
- `model/normalized/**` — normalized intermediate forms.
- `model/resolved/**` — references resolved, scoped to one profile.
- `model/compiled/**` — final execution-ready artifact (`ExecutionTemplate` and friends).

## Current state / gotchas

This is an early, in-progress codebase. Several things are stubbed or hardcoded — do not treat them as the intended design:

- `TemplateCompileService.compile()` **ignores the request's benchmark/profile content source** and reads `xccdf.xml`/`oval.xml` from hardcoded absolute paths under `src/test/resources/`, then writes the result to `./test.json` (see the root-level `test.json`). The `ContentPackageLoader`/S3 path is commented out.
- AWS SDK (S3/SQS/STS via `config/AwsClientConfig`) and `ContentPackageLoader` exist for the intended "load content package from S3, persist artifact to S3" flow, but persistence is not yet wired in.
- `CpeParser` is a `StubCpeParser`.
- Lombok is used throughout (`@RequiredArgsConstructor` for constructor injection, `@Slf4j`, `@Builder`, `@Data`/`@Getter`). New beans follow the constructor-injection-via-`@RequiredArgsConstructor` convention.