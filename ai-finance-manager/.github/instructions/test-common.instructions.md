---
applyTo: "**/*Test.java,**/*IT.java"
---

# Common Test Instructions

**These rules apply to ALL tests (unit and integration).**

## MANDATORY Test Structure

**All tests MUST use given-when-then structure with comments:**

```java

@Test
void shouldSaveSettings() {
  // given
  var settings = AdminSettings.builder().build();
  when(repository.save(settings)).thenReturn(settings);

  // when
  var result = adapter.saveSettings(settings);

  // then
  assertNotNull(result);
  verify(repository).save(settings);
}
```

**This is the ONLY exception to the "no comments" rule** - given-when-then comments are mandatory in tests for structure and readability.

## Test Data Builders (MANDATORY)

**Always use Test Data Builders when available** instead of manually constructing objects:

```java
// ✅ Good
var material = MaterialTestDataBuilder.material();
var order = OrderTestDataBuilder.order();
var assignment = AssignmentTestDataBuilder.assignment();

// ❌ Bad
Order order = new Order();
order.setOrderCode("123");
order.setSection(new Section());
```

**Available Test Data Builders:**

- `MaterialTestDataBuilder`
- `OrderTestDataBuilder`
- `AssignmentTestDataBuilder`
- `SourcingTestDataBuilder`
- `FinalProductOrderTestDataBuilder`
- `PackagingConfigurationTestDataBuilder`

## Building OpenAPI DTOs

**For OpenAPI-generated DTOs, use constructor with fluent method chaining:**

```java
// ✅ Good - constructor with method chaining
var orderDTO = new OrderDTO()
    .orderCode("123")
    .orderType("RMO")
    .supplierId(456);

var requestDTO = new CreateOrderDetailsDTO()
    .orderCode("789")
    .section("RM")
    .materials(List.of(materialDTO));

// ❌ Bad - manual setters
var orderDTO = new OrderDTO();
orderDTO.setOrderCode("123");
orderDTO.setOrderType("RMO");
orderDTO.setSupplierId(456);
```

## Assertion Libraries

**Always verify all mock interactions in the `then` section:**

```java
// then
assertNotNull(result);
verify(repository).save(order);
verify(mapper).toDto(entity);
verifyNoMoreInteractions(repository);
```

## Test Method Naming

Use descriptive names that explain the scenario:

```java
// ✅ Good - describes behavior
@Test
void shouldReturnCountriesWhenLanguageIsEnglish()

@Test
void shouldThrowExceptionWhenMaterialNotFound()

@Test
void shouldValidateOrderSuccessfullyWhenAllDataIsPresent()

// ✅ Acceptable for simple cases
@Test
void findAllCountriesByTest()

// ❌ Bad - unclear intent
@Test
void test1()

@Test
void testOrder()
```

## Common Anti-Patterns to AVOID

❌ **Don't manually construct domain objects:**

```java
// Bad
Order order = new Order();
order.setOrderCode("123");
order.setSection(new Section());

// Good
var order = OrderTestDataBuilder.order();
```

❌ **Don't omit given-when-then comments** - they are MANDATORY

❌ **Don't use explicit types** - follow var usage rules from java-common.instructions.md

❌ **Don't forget to verify mocks:**

```java
// Bad - no verification
@Test
void shouldSaveOrder() {
  // given
  var order = OrderTestDataBuilder.order();
  when(repository.save(order)).thenReturn(order);

  // when
  var result = useCase.save(order);

  // then
  assertNotNull(result);
  // MISSING: verify(repository).save(order);
}
```

## Key Reminders

1. ✅ **Use given-when-then structure with comments** (mandatory)
2. ✅ **Use Test Data Builders** - never manually construct domain objects
3. ✅ **Verify all mock interactions** in the `then` section
4. ✅ **Use descriptive test method names** that explain the scenario
5. ✅ **Follow hexagonal architecture testing rules** (see unit-test.instructions.md)
