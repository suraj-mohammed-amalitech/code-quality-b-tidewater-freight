# Tidewater Freight — Refactoring Rationale

## 1. Key architectural decisions

### 1.1 Characterisation tests first

Before changing anything, `ShippingCalculatorCharacterisationTest` was written
against the starter prices. Every subsequent refactor ran against those tests.
A price that moved during a refactor was a bug, not a style change.

### 1.2 `ShipmentRequest` — validation at the boundary

The original `calculateCost(String, double, String)` accepted nulls, blank
zones, negative weights, and free-text service levels without complaint — all
silently handled by the catch-all.

`ShipmentRequest` is a self-validating record that rejects invalid data on
construction. The calculator receives a `ShipmentRequest` and never sees bad
data. This is the single responsibility principle applied to validation: one
class owns the rule "what is a valid shipment?", and it enforces it once.

### 1.3 `ServiceLevel` enum — no free strings

`"express"`, `"OVERNIGHT"`, `"Express"` are typos the original priced as
STANDARD silently. The `ServiceLevel` enum makes those impossible to write as
Java code. `ServiceLevel.fromText(String)` is provided for callers that receive
raw text at system boundaries (CSV, HTTP), and it throws
`InvalidShipmentException` with the offending value in the message.

### 1.4 `ZoneRate` — rate and fee live together

The original had the per-kg rate in `InMemoryRateRepository` and the flat fee
in an `if/else` chain in `ShippingCalculator`. Adding a new zone meant editing
both — the classic shotgun surgery smell. Miss one and the zone silently has no
fee.

`ZoneRate` holds both together. A zone cannot exist in the repository without
its fee. The split-rules defect is structurally impossible.

### 1.5 One class per pricing rule

`BasePriceRule`, `ZoneFeeRule`, `HeavyItemSurchargeRule`, `ServiceLevelRule` —
each does one job, is independently testable, and has no knowledge of any other
rule. The calculator runs the list in order. Adding a new surcharge (e.g. a
weekend fee) means a new class, with zero edits to any existing rule.

`ServiceLevelRule` must be last: the business rule is that express ×1.5 applies
to the total including fees and surcharges. `DefaultPricingRulesTest` verifies
the order explicitly.

### 1.6 `rates.properties` — zones as config, not code

Zone rates live in `src/main/resources/rates.properties`, one line per zone:

```properties
DOMESTIC      = 5.00, 0.00
REGIONAL      = 8.00, 10.00
INTERNATIONAL = 15.00, 25.00
```

Adding ARCTIC is one new line. No Java changes at all. `ShippingCalculator`,
every rule class, and every existing test are untouched.

### 1.7 Sub-packages by feature

```
exception/   ShippingException hierarchy
model/       ServiceLevel, ShipmentRequest, ZoneRate
rate/        RateRepository interface + InMemoryRateRepository
pricing/     PricingRule interface + four rule classes + DefaultPricingRules
```

Lower packages never import higher ones. The calculator at the root uses the
interfaces from `rate/` and `pricing/`; it never names a concrete class.

### 1.8 `mvn verify` gates

The build fails if:
- Line or branch coverage drops below 90% (JaCoCo)
- PIT mutation kill rate drops below 95%

`ShippingException` is abstract rather than concrete so callers are always
forced to catch a specific, named subtype or the base — there is no way to
accidentally throw a raw `ShippingException` with a vague message, which is
the same mistake the original code made with its catch-all.

Demo is excluded from both — it is a console entry point with no logic. These
gates mean a future change that breaks test quality fails the build
automatically.

---

## 2. How the design absorbs the new zone

The `NewZone` test group in `ShippingCalculatorTest` already demonstrates this:

```java
// ARCTIC 10 kg STANDARD: 20*10 + 40 = 240.00
// ARCTIC 30 kg STANDARD: 20*30 + 40 = 640.00 (no surcharge at boundary)
// ARCTIC 35 kg EXPRESS:  (20*35 + 40 + 20) * 1.5 = 760 * 1.5 = 1140.00
```

To add ARCTIC in production: one line in `rates.properties`. Nothing else
changes.

---

## 3. What was kept, fixed, or rewritten

| Artifact | Decision | Reason |
|---|---|---|
| `RateRepository` interface | Fixed — returns `ZoneRate`, not `double` | Rate and fee must travel together |
| `InMemoryRateRepository` | Rewritten | Loads from `rates.properties`, `ZoneRate`, defensive copy, factory method |
| `ShippingCalculator` | Rewritten | Accepts `ShipmentRequest`, runs rule pipeline, no zone/level ifs, no swallowed exception |
| `Demo.java` | Updated | Uses `ShipmentRequest` + `ServiceLevel`, catches `ShippingException` |
| Zone if/else chain | Deleted | Replaced by `ZoneFeeRule` reading from `ZoneRate` |
| `ZoneFeePolicy` / `MapZoneFeePolicy` | Deleted | Superseded by `ZoneRate` + `ZoneFeeRule` |
| `ShippingCalculationException` | Deleted | Replaced by `ShippingException` hierarchy |
| All three original tests | Rewritten | None could fail (see ANALYSIS.md §4) |

New classes:
- `ShippingException`, `UnknownZoneException`, `InvalidShipmentException`, `RateUnavailableException`
- `ServiceLevel`, `ShipmentRequest`, `ZoneRate`
- `PricingRule`, `BasePriceRule`, `ZoneFeeRule`, `HeavyItemSurchargeRule`, `ServiceLevelRule`, `DefaultPricingRules`

---

## 4. The improvement beyond the brief — `mvn verify` gates

The brief fixes this code once. Without automated gates, a future change could
undo the work: a new rule without a test, a hollow assertion, a catch-all added
back. The gates make that impossible:

- **JaCoCo ≥ 90%** — untested code paths fail the build
- **PIT ≥ 90%** — hollow tests that don't kill mutants fail the build

Both gates run on `mvn verify`. A developer who adds code without adequate tests
gets a build failure before they can merge.

---

## 5. Verified test and mutation results

PIT was run on both the starter code and the refactored code:

| Metric | Starter (before) | Refactored (after) |
|---|---|---|
| Tests | 3 (none could fail) | 77, all passing |
| Mutations generated | 18 | 46 |
| Mutations killed | 3 (17%) | **46 (100%)** |
| Line coverage | 20/33 (61%) | 108/116 (93%) |
| Test strength | 27% | **100%** |

The starter tests killed 17% of mutants — green build, broken code. The
refactored suite kills 100% of mutants with 100% test strength.

---

## 6. What the AI got wrong — for the team

1. **Swallowing exceptions**: `catch (Exception e) { return 0; }` turns every failure into valid-looking data
2. **Mocking the class under test**: the express test stubbed `ShippingCalculator` and asserted the stub — no production code ran
3. **Assertions that cannot fail**: `assertNotNull` on a primitive double autoboxed to `Double` can never be null
4. **Weak assertions**: `assertTrue(cost > 0)` passes for any positive number, right or wrong
5. **Creating a seam then bypassing it**: `RateRepository` interface existed but `new InMemoryRateRepository()` hardwired it
6. **Free strings for domain concepts**: `"EXPRESS"`, `"DOMESTIC"` — typos compile and silently produce wrong prices
7. **Split rules**: rate in one place, fee in another — a new zone requires editing two classes

A green build is not evidence the code is correct. Read what each assertion actually proves.
