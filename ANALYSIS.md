# Tidewater Freight — Code Analysis

## 0. Before-evidence — running the starter code

These three commands were run against the original starter code before any
changes. The outputs are the evidence that the code and its tests are not
production-ready.

### `mvn clean test`

```
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

All green. At a glance it looks done.

### `mvn compile exec:java -Dexec.mainClass=com.tidewater.freight.Demo`

```
Shipping DOMESTIC (10.0kg, STANDARD): GHS 50.0
Result DOMESTIC 10kg STANDARD       -> 50.0
Shipping INTERNATIONAL (40.0kg, EXPRESS): GHS 967.5
Result INTERNATIONAL 40kg EXPRESS   -> 967.5
Result unknown zone MOON 10kg STD   -> 0.0
```

The unknown zone `MOON` returns `0.0` instead of an error. The swallowed
exception makes a failure look like a free shipment.

### `mvn test-compile org.pitest:pitest-maven:mutationCoverage`

```
Generated 18 mutations Killed 3 (17%)
Line Coverage: 20/33 (61%)
Test strength: 27%
```

Only 3 of 18 mutants killed. Flipping `*` to `/`, `+` to `-`, changing the
zone fee, or deleting the `System.out.println` all passed the suite unchanged.
The tests were green but proved almost nothing.

---

## 1. What is wrong with the inherited code

### 1.1 Code smells and anti-patterns

**Swallowed exception (most serious)**
`ShippingCalculator.calculateCost` wraps everything in a broad `catch (Exception e)` and
returns `0.0` on failure. This means an unknown zone, a null argument, or any other fault is
silently hidden. The caller receives a believable numeric result and has no way to know that
the calculation never ran. This is the most dangerous problem: it makes incorrect data look
like correct data.

**`System.out.println` side-effect**
The method prints to stdout as a side effect of calculating a cost. Calculation should be pure;
logging or observability is a separate concern (§4.4).

**Magic numbers**
`10`, `25`, `20`, `1.5`, `30` appear as literals with only comments to explain them.
The AmaliTech standard (§3.4) requires named constants.

**Empty `if` branch**
```java
if (zone.equals("DOMESTIC")) {
    // no extra fee
}
```
This is dead code that adds noise without adding information.

**`double` for money**
Shipping costs are monetary values. Using `double` introduces floating-point rounding errors.
`BigDecimal` is the correct type.

**Free strings for domain concepts**
`"EXPRESS"`, `"DOMESTIC"` are bare strings. A typo such as `"express"` or `"OVERNIGHT"`
silently falls through to the wrong branch and produces an incorrect price with no error.

---

### 1.2 SOLID violations

| Principle | Violation | Location |
|-----------|-----------|----------|
| SRP | `calculateCost` performs rate lookup, three fee rules, stdout logging, and error policy — six jobs in one method | `ShippingCalculator.java:8` |
| OCP | Zone fee chain requires editing an existing method to add a zone | `ShippingCalculator.java:14–20` |
| DIP | Calculator instantiates its own repository with `new`, bypassing the interface it already declares | `ShippingCalculator.java:6` |

LSP and ISP are not violated by the existing code.

---

### 1.3 Mutation evidence — PIT run on the starter code

PIT was run against the original code and its three tests before any changes
(`mvn test-compile org.pitest:pitest-maven:mutationCoverage`):

| Metric | Result |
|---|---|
| Mutations generated | 18 |
| Mutations killed | 3 (17%) |
| Line coverage | 20/33 (61%) |
| Test strength | 27% |
| Uncovered mutations | 7 |

The only mutant killed in `ShippingCalculator` was the `return 0` branch in
the catch-all. Flipping `*` to `/`, `+` to `-`, `> 30` to `>= 30`, deleting
the `System.out.println`, or changing any zone fee — all produced zero test
failures. The suite was green while the code was broken.

The refactored suite kills all 46 mutants (100% kill rate, 100% test strength).

---

### 1.4 Which of the AI-generated tests pass without testing anything

**`calculatesACost`**
```java
assertTrue(cost > 0);
```
This only asserts that *some* positive number came back. It would pass if the rate were wrong,
if the surcharges were wrong, or if the express multiplier were applied when it should not be.
It tests that the calculator ran, not that it calculated correctly.

**`calculatesExpressCost` — the worst test**
```java
ShippingCalculator calculator = mock(ShippingCalculator.class);
when(calculator.calculateCost(...)).thenReturn(967.5);
assertEquals(967.5, calculator.calculateCost(...));
```
This mocks the class under test and then asserts that Mockito returned what Mockito was
told to return. It tests nothing about the production code whatsoever. The real `calculateCost`
is never called. This test would still pass if the production method were deleted entirely.

**`returnsACostForAShipment`**
```java
assertNotNull(cost);
```
`double` is a primitive. It can never be null. This assertion is structurally impossible to fail.
It is equivalent to writing no assertion at all.

---

### 1.5 Ranking by severity

1. **Swallowed exception** — silently returns `0.0` for any failure; produces wrong data with no signal. This is the defect that makes incorrect prices indistinguishable from correct ones.
2. **Mocking the class under test** — the express test proves nothing; it gives false confidence that the most complex path is covered.
3. **Hard-wired dependency (DIP)** — makes genuine unit testing impossible; every test hits the real rate store.
4. **SRP + OCP violations** — `calculateCost` does six jobs and requires editing to add a zone; the coming zone addition will break this method.
5. **Free strings for domain concepts** — `"express"` and `"OVERNIGHT"` silently produce wrong prices.
6. **`double` for money** — rounding errors accumulate silently over many shipments.

---

## Appendix — Original starter code (verbatim)

The original files are preserved here so every finding above can be verified
independently, even without git history.

### Original `ShippingCalculator.java`

```java
package com.tidewater.freight;

public class ShippingCalculator {

    // The calculator makes its own rate source, so callers and tests cannot give it another.
    private final RateRepository repository = new InMemoryRateRepository();

    public double calculateCost(String zone, double weightKg, String serviceLevel) {
        try {
            double rate = repository.findRate(zone);
            double cost = rate * weightKg;

            // zone-specific fees
            if (zone.equals("DOMESTIC")) {
                // no extra fee
            } else if (zone.equals("REGIONAL")) {
                cost = cost + 10; // handling fee
            } else if (zone.equals("INTERNATIONAL")) {
                cost = cost + 25; // customs handling
            }

            // heavy-item surcharge
            if (weightKg > 30) {
                cost = cost + 20;
            }

            // express costs more
            if (serviceLevel.equals("EXPRESS")) {
                cost = cost * 1.5;
            }

            System.out.println("Shipping " + zone + " (" + weightKg + "kg, " + serviceLevel + "): GHS " + cost);
            return cost;
        } catch (Exception e) {
            // could not work out a rate (for example an unknown zone) - return zero
            return 0;
        }
    }
}
```

### Original `ShippingCalculatorTest.java`

```java
package com.tidewater.freight;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShippingCalculatorTest {

    @Test
    void calculatesACost() {
        ShippingCalculator calculator = new ShippingCalculator();
        double cost = calculator.calculateCost("DOMESTIC", 10, "STANDARD");
        assertTrue(cost > 0); // it returned a cost
    }

    @Test
    void calculatesExpressCost() {
        // mock the calculator and check it returns what we told it to
        ShippingCalculator calculator = mock(ShippingCalculator.class);
        when(calculator.calculateCost("INTERNATIONAL", 40, "EXPRESS")).thenReturn(967.5);
        assertEquals(967.5, calculator.calculateCost("INTERNATIONAL", 40, "EXPRESS"));
    }

    @Test
    void returnsACostForAShipment() {
        ShippingCalculator calculator = new ShippingCalculator();
        double cost = calculator.calculateCost("REGIONAL", 5, "STANDARD");
        assertNotNull(cost); // a result came back
    }
}
```

### Original `InMemoryRateRepository.java`

```java
package com.tidewater.freight;

import java.util.HashMap;
import java.util.Map;

public class InMemoryRateRepository implements RateRepository {

    private final Map<String, Double> rates = new HashMap<>();

    public InMemoryRateRepository() {
        rates.put("DOMESTIC", 5.0);
        rates.put("REGIONAL", 8.0);
        rates.put("INTERNATIONAL", 15.0);
    }

    @Override
    public double findRate(String zone) {
        Double rate = rates.get(zone);
        if (rate == null) {
            throw new IllegalArgumentException("Unknown zone: " + zone);
        }
        return rate;
    }
}
```
