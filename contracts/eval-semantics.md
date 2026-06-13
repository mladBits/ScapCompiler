# ExecutionTemplate contract: evaluation semantics

How an agent turns collected host data into rule results. This is the third
contract document, alongside `execution-template.schema.json` (shape) and
`variable-resolution.md` (variables). Scope: the operators and values the
schema can express and that observed CIS/DISA content uses — not a full OVAL
5.11 re-derivation. Where this narrows OVAL, it is noted.

Audience: the OVAL evaluator (Go agent, `internal/oval`). The compiler
guarantees the **Guarantee** items; the agent implements the **Agent** items.

## Result values

Evaluation is multi-valued (OVAL result enumeration):

| value | meaning |
|---|---|
| `true` | the constraint holds |
| `false` | the constraint does not hold |
| `error` | evaluation could not complete (collection failed, bad comparison) |
| `unknown` | a dependency was unknown |
| `not applicable` | excluded from the result |
| `not evaluated` | never computed |

**Negation** flips `true`↔`false`; every other value negates to itself (you
cannot negate an error).

## Operator folding (`AND`, `OR`, `ONE`, `XOR`)

Used by definition criteria groups and by a test's `stateOperator`. Folding a
set of child results (empty set → `not applicable`):

- **AND**: any `false` → `false`; else any `error` → `error`; else any
  `unknown` → `unknown`; else any `not evaluated` → `not evaluated`; else any
  `true` → `true`; else `not applicable`.
- **OR**: any `true` → `true`; else `error` → `unknown` → `not evaluated`
  precedence; else any `false` → `false`; else `not applicable`.
- **ONE**: ≥2 `true` → `false`; else indeterminate (`error`/`unknown`/`not
  evaluated`) propagates in that precedence; else exactly one `true` → `true`;
  else `false`.
- **XOR**: indeterminate propagates first; else odd count of `true` → `true`,
  even → `false`.

## Comparison (one item value vs one state value)

`Compare(datatype, operation, itemValue, stateValue) -> bool | error`.

**Operations**: `equals`, `not equal`, `case insensitive equals`,
`case insensitive not equal`, `greater than`, `greater than or equal`,
`less than`, `less than or equal`, `pattern match`.

**Datatypes** (comparison domain):

- `string` — exact (or case-folded for the case-insensitive ops); ordering is
  lexical.
- `int` — parsed as 64-bit signed (a registry DWORD `4294967295` exceeds
  int32, so int64 is required); compared numerically.
- `boolean` — `true`/`1` vs `false`/`0`, normalized then compared (0/1).
- `version` — split on `.`,`-`,`,`,`:`; segments compared **numerically when
  both are numeric** (so `10 > 9`), else lexically; missing segments are `0`
  (`10.0` == `10.0.0`).
- `float` — parsed as 64-bit float.
- `record` — not compared directly; see record handling below.
- An unrecognized datatype degrades to `string` comparison (defined, coarse).

A parse failure (e.g. non-numeric `int`) → comparison error → the test result
is `error`.

**`pattern match`**: the state value is a regular expression and the item
value is tested against it. **The OVAL spec mandates PCRE (Perl 5 Compatible
Regular Expressions)** for pattern match — Go's stdlib RE2 is insufficient (it
rejects constructs the spec allows, e.g. the negative lookahead `(?!…)` that
appears in DISA content). The agent uses a PCRE engine (Go: `dlclark/regexp2`)
with a match timeout to bound catastrophic backtracking. A
`TestGoldenPatternsCompile` contract test compiles every pattern in the golden
template as a content-sanity check.

## var_check folding

When a selector's expression is a variable (multiple values), the item value is
compared against **each** variable value, and the per-value booleans fold by
`varCheck`:

| varCheck | true when |
|---|---|
| `all` | every comparison true |
| `at least one` | ≥1 true |
| `none satisfy` | 0 true |
| `only one` | exactly 1 true |

(Full per-value evaluation and datatype domain detail: `variable-resolution.md`.)

## State evaluation (does one item satisfy one state)

A state is a set of assertions (selectors); the item satisfies the state iff
**all** assertions hold (AND).

For one scalar assertion:
- Resolve the expected value(s): `literal` → one value; `variable` → the
  variable's values (folded by `varCheck`); `nil` → a presence assertion
  (satisfied iff the item field has no value).
- If the item lacks the field entirely → `false`.
- If the item field is multi-valued, the assertion must hold for **all** of its
  values (OVAL `entity_check` is not carried in the contract; `all` is the
  default — **narrowing**, revisit if content needs other entity_checks).
- A comparison error makes the assertion (and the state) `error`.

**Record assertions** (`datatype: record`, since schema 1.1.0): the selector
carries nested `fields` instead of an `expression`. The intended semantics are
that each nested field selector asserts on a record sub-field, AND-combined.
How a collected record maps to item fields is finalized with the WMI probe
(agent milestone M7); **until then the agent fails closed**, evaluating any
record assertion to `error` rather than guessing the mapping.

## Test evaluation (one OVAL test)

Inputs: the collected object (a `Collection` with a flag + items), the `check`
quantifier, `checkExistence`, `stateOperator`, and the referenced states.

1. Collection flag `error` → `error`; `not collected` → `not evaluated`.
2. **Existence result** from the item count and `checkExistence`:
   - `at_least_one_exists` / `all_exist` → `true` iff ≥1 item (single-object
     tests: "all exist" reduces to "≥1 present" — **narrowing**).
   - `any_exist` → always `true`.
   - `none_exist` → `true` iff 0 items.
   - `only_one_exists` → `true` iff exactly 1 item.
3. No states referenced → the test result **is** the existence result.
4. Existence not `true`, or zero items → the existence result stands.
5. Otherwise, for each item: evaluate it against every referenced state, fold
   those per-state results by `stateOperator`. Fold the per-item results by
   `check`:
   - `all` → AND, `at least one` → OR, `only one` → ONE,
     `none satisfy` → negate(OR).
6. Final test result = AND(existence result, state result).

## Definition criteria

A definition's `criteria` is a tree:
- **group** (`operator` + `children`) → fold child results by the operator;
- **criterion leaf** (`testId`) → the test's result; `supported: false` →
  `error` (the check never compiled);
- **extend-definition leaf** (`definitionId`) → the referenced definition's
  result.
`negate` applies to any node after it is computed.

**Rule result** = the fold of the rule's `ovalDefinitionIds` results. *(Single
definition per rule is the common case; for multiple, AND is the proposed fold
— confirm against scoring needs before relying on it.)*

## Guarantees / narrowings summary

- **Guarantee** (compiler): every referenced object/state/check/definition is
  present (except `supported:false` criteria and provenance-only filter
  stateRefs); record entities carry their nested field assertions;
  `stateOperator` is always set (AND default).
- **Narrowing** (agent, revisit when content demands): `entity_check` fixed to
  `all`; `all_exist` treated as `at_least_one_exists` for single objects;
  multi-definition rule fold proposed as AND.
