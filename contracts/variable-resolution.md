# ExecutionTemplate contract: variable resolution

How an agent must resolve variables in an `ExecutionTemplate`. Semantics follow OVAL 5.11.
Audience: the OVAL engine/evaluator implementation (Go agent). The compiler guarantees the
invariants marked **Guarantee**; the agent implements everything marked **Agent**.

## Where variables appear

Entity selectors (in `objectsById[*].tasks[*].selectors`, `statesById[*].assertions`, and
filter `predicates`) carry an expression that is either a literal or a variable reference:

```json
{
  "field": "value",
  "operation": "equals",
  "datatype": "int",
  "varCheck": "at least one",
  "expression": { "type": "variable", "value": "oval:example:var:1" }
}
```

- `expression.type` is one of `literal`, `variable`, `nil`.
- `varCheck` is present **only** when `expression.type == "variable"` (defaults to `all` in
  source content; the compiler always materializes it).

## The variable table: `variablesById`

One entry per referenced variable:

```json
"oval:example:var:1": {
  "variableId": "oval:example:var:1",
  "datatype": "string",
  "kind": "constant" | "external" | "local",
  "unresolved": true,              // present (true) only when unresolvable
  "unresolvedReason": "...",       // diagnostic, with unresolved
  "values": ["..."],               // constant / bound external
  "expression": { ... }            // local only
}
```

`kind` is the OVAL variable **type** (origin). Whether the agent can resolve it is the
separate `unresolved` flag:

| field | meaning | agent behavior |
|---|---|---|
| `kind: constant` | fixed values from the OVAL | use `values` |
| `kind: external` | value bound from XCCDF | use `values` |
| `kind: local` | computed at scan time | evaluate `expression` (below) |
| `unresolved: true` | could not be resolved/compiled (unbound external, unsupported function, cycle, uncollectable object) | resolving it is an **error** (overrides the above) |

**Guarantee:** every variable id referenced by any selector, assertion, predicate, or nested
`variable_component` has an entry in `variablesById`.

## Resolution algorithm (Agent)

```
resolve(varId) -> ([]string, error)    // memoize per scan
  entry = variablesById[varId]         // missing entry: treat as error (defensive)
  entry.unresolved      -> error
  kind constant/external -> entry.values
  kind local            -> eval(entry.expression)
```

A variable always resolves to an **ordered list of zero or more string values**. Datatype
conversion happens at comparison time using the *selector's* `datatype`, not at resolution
time.

### Expression evaluation: `eval(expr) -> []string`

Expressions are discriminated by `node` (OVAL component/function names):

**`literal_component`** `{ "node": "literal_component", "value": "abc" }`
→ `["abc"]`.

**`variable_component`** `{ "node": "variable_component", "variableId": "oval:...:var:2" }`
→ `resolve(variableId)` (recursive; propagate error).
**Guarantee:** no cycles — the compiler rejects cyclic references as unresolved. Guard anyway.

**`object_component`** `{ "node": "object_component", "objectRef": "oval:...:obj:9", "itemField": "value" }`
→ Collect (or reuse the memoized collection of) `objectsById[objectRef]`, then take the value
of `itemField` from **every** collected item, in collection order.
- **Guarantee:** `objectRef` always has a collection plan in `objectsById`.
- Object collection flag `error` / `not collected` → variable error.
- Object exists but zero items (`does not exist`) → empty list (not an error).
- An item lacking the field entirely → skip that item; multi-valued fields contribute each value.

**`concat`** `{ "node": "concat", "components": [e1, e2, ...] }`
→ The **ordered cartesian product** of the component value lists, joining each combination
left-to-right. Example: `["a","b"] × ["1","2"]` → `["a1","a2","b1","b2"]`.
- Any component error → error.
- Any component empty → empty result.

**`regex_capture`** `{ "node": "regex_capture", "pattern": "^%.*%(.*)$", "component": e }`
→ For each value of `eval(component)`: apply `pattern`; emit the **first capture group** of
the first match; if the pattern does not match, emit the **empty string** for that value.
- Pattern dialect is Perl/PCRE-flavored (OVAL spec). RE2-only engines are insufficient —
  use a PCRE-compatible library (Go: `dlclark/regexp2`).

### Empty and error propagation

- A variable that evaluates to an **error** makes every test whose object/state references it
  evaluate to `error`.
- An **object selector** whose variable resolves to an empty list → the object collection is
  flagged `does not exist` for that dimension (no values to enumerate).
- A **state assertion** whose variable resolves to an empty list → that assertion evaluates
  to `error` (OVAL: a var_ref with no values cannot be checked).

## Applying a variable at comparison time (Agent)

For a selector/assertion with `expression.type == "variable"`:

1. `values = resolve(variableId)`.
2. Convert each value and the item's field value into the selector's `datatype` domain
   (`string`, `int`, `boolean`, `version` — segment-wise numeric, `binary`).
3. Compare the **item value against each variable value** using `operation`
   (equals, not equal, case insensitive equals/not equal, greater than (or equal),
   less than (or equal), bitwise and/or, pattern match).
4. Fold the per-value boolean results with `varCheck`:

| varCheck | result is true when |
|---|---|
| `all` | every comparison is true |
| `at least one` | one or more comparisons are true |
| `none satisfy` | no comparison is true |
| `only one` | exactly one comparison is true |

This per-entity result then feeds the state's entity folding and the test's `check` /
`checkExistence` quantifiers (documented separately in eval-semantics).

## Object selectors with variables (Agent)

When a **collection task selector** (not a state) references a variable — e.g. a registry
`name` with `var_ref` — the agent enumerates/collects using **each** resolved value, i.e. the
object describes the union of items matched by any value, before filters run. `varCheck` does
not apply to collection; it only folds comparisons in states/filters.

## Variable-backed objects: the `independent.variable` task (Agent)

OVAL's independent-family `variable_test` compiles to an object plan whose task collects
nothing from the host — it materializes the resolved variable as items:

```json
{
  "objectId": "oval:example:obj:104",
  "objectType": "variable_object",
  "tasks": [
    { "family": "independent.variable", "variableId": "oval:example:var:104" }
  ]
}
```

- **Agent**: execute by `values = resolve(variableId)`; emit **one item per value**, with a
  single item field `value` holding that value. No OS probe is involved.
- `resolve` error → the object collection errors (dependent tests → `error`).
  Empty value list → zero items (existence result `does not exist`).
- States of type `variable_state` assert against the item's `value` field using the standard
  comparison rules above.
- **Guarantee**: the referenced `variableId` is always present in `variablesById` — the
  compiler follows the `var_ref` entity text of `variable_object`s when building the
  variable closure.

## Compile-time provenance (informative)

- External variables: `kind: external`, bound from XCCDF `Value` elements via the rule's
  check-exports or overridden per compile request. Unbound ones are emitted `unresolved: true`.
- Constant variables: `kind: constant` with `values`.
- Local variables: `kind: local` with an `expression`. Supported nodes today:
  `literal_component`, `concat`, `object_component`, `regex_capture`, `variable_component`.
  Any other OVAL function (split, substring, arithmetic, count, unique, time_difference, begin,
  end, escape_regex) is emitted `unresolved: true` with a warning in `warnings[]` until
  implemented on **both** sides of this contract.