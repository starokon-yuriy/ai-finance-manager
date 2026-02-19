---
applyTo: "**/*.java"
---

# Common Java Conventions

## Use `var` for Local Variables (MANDATORY)

**Use `var` ONLY when the expression type can be quickly intuitively identified** (e.g., builder, constructor):

```java
// ✅ Good - type is obvious from context
var order = Order.builder().orderCode(123).build();  // Builder pattern - obviously Order
var orders = new ArrayList<Order>();  // Constructor - obviously ArrayList<Order>

// ❌ Bad - type not immediately clear
var country = countryRepository.findById(id);  // What does findById() return?
var materials = materialService.getMaterials(orderId);  // Returns what type of collection?
var result = processOrder(order);  // What does processOrder() return?
var data = process();  // What type is this?
var x = getValue();  // Unclear what type getValue() returns

// ✅ In these cases, use explicit type for clarity
Optional<Country> countryOpt = countryRepository.findById(id);
List<Material> materials = materialService.getMaterials(orderId);
OrderResult result = processOrder(order);
ProcessingResult data = process();
ConfigurationValue x = getValue();
```

**Benefits:**

- **Reduced boilerplate**: Less typing, cleaner code when type is obvious
- **Better focus**: Emphasizes variable names and values over types
- **Easier refactoring**: Type changes automatically propagate
- **Modern Java**: Leverages Java 10+ type inference

**When NOT to use `var`:**

- Method parameters, return types, or field declarations (not supported by Java)
- When the type is not obvious from the right-hand side and explicit type aids understanding
- Generic method calls where return type is ambiguous
- Complex expressions where the resulting type is unclear

## Lombok Annotations (MANDATORY)

### Constructor Injection Pattern

```java
// ✅ Always use @RequiredArgsConstructor with final fields
@Component
@RequiredArgsConstructor
public class GetCountryUseCaseImpl implements GetCountryUseCase {

  private final CountryRepository countryRepository;

  private final ConfigurationsService configurationsService;

  @Override
  public List<Country> findAllCountriesBy(Language language) {
    return countryRepository.findAllByLanguage(language);
  }
}

// ❌ Never use field injection
@Component
public class GetCountryUseCaseImpl {

  @Autowired  // WRONG!
  private CountryRepository countryRepository;
}

// ❌ Never write constructors manually when using Lombok
@Component
public class GetCountryUseCaseImpl {

  private final CountryRepository countryRepository;

  // WRONG - Lombok can generate this
  public GetCountryUseCaseImpl(CountryRepository countryRepository) {
    this.countryRepository = countryRepository;
  }
}
```

### Builder Pattern

```java
// ✅ Use @Builder for domain entities and DTOs
@Builder(toBuilder = true)
public class Order {

  private Integer id;

  private Integer orderCode;

  private OrderType orderType;

  private LocalDateTime createdDate;
}

// Usage
var order = Order.builder()
    .orderCode(123)
    .orderType(OrderType.RMO)
    .createdDate(LocalDateTime.now())
    .build();

// Immutable updates with toBuilder
var updatedOrder = order.toBuilder()
    .updatedDate(LocalDateTime.now())
    .build();
```

### Logging

```java
// ✅ Use @Slf4j for logging
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

  public Order processOrder(Integer orderCode) {
    log.info("Processing order: {}", orderCode);
    log.debug("Order details: {}", order);
    log.error("Failed to process order: {}", orderCode, exception);
    return order;
  }
}

// ❌ Never create logger manually
private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
```

### Data Classes

```java
// ✅ Use @Data for simple data classes (DTOs, entities)
@Data
@Builder
public class OrderEntity {

  private Integer id;

  private Integer orderCode;

  private String section;
}

// ✅ Use @Getter for immutable domain entities
@Getter
@Builder(toBuilder = true)
public class Order {

  private final Integer id;

  private final Integer orderCode;

  private final OrderType orderType;
}
```

## Null Safety

### Optional Usage

```java
// ✅ Use Optional for potentially null return values
public Optional<Order> findByOrderCode(Integer orderCode) {
  return orderRepository.findByOrderCode(orderCode);
}

// ✅ Use Optional properly in call chain
var order = orderRepository.findById(id)
    .orElseThrow(() -> new OrderNotFoundException(id));

// ❌ Never return null when Optional is better
public Order findByOrderCode(Integer orderCode) {
  return order != null ? order : null;  // WRONG - use Optional!
}
```

### Null Checks

```java
// ✅ Use Objects.requireNonNull for validation
public void processOrder(Order order) {
  Objects.requireNonNull(order, "Order cannot be null");
  var orderCode = Objects.requireNonNull(order.getOrderCode(), "Order code cannot be null");
}

// ✅ Use null-safe Optional chaining
var result = Optional.ofNullable(order)
    .map(Order::getOrderCode)
    .map(String::valueOf)
    .orElse("N/A");
```

## Collection Usage

```java
// ✅ Use Collections.emptyList() for empty collections (better performance)
var emptyList = Collections.emptyList();
var emptySet = Collections.emptySet();
var emptyMap = Collections.emptyMap();

// ✅ Use List.of(), Set.of(), Map.of() for non-empty immutable collections
var countries = List.of(country1, country2, country3);
var codes = Set.of(1, 2, 3);
var map = Map.of("key1", "value1", "key2", "value2");

// ❌ Don't use List.of() for empty collections
var emptyList = List.of();  // Works but Collections.emptyList() is preferred

// ✅ Use var with collection constructors for mutable collections
var mutableList = new ArrayList<Order>();
var mutableSet = new HashSet<Integer>();
var mutableMap = new HashMap<String, Order>();

// ❌ Don't use explicit types when var is clearer
List<Order> orders = new ArrayList<>();  // Verbose
var orders = new ArrayList<Order>();     // Better
```

## Stream API

```java
// ✅ Use var with streams
var activeOrders = orders.stream()
        .filter(Order::isActive)
        .map(order -> order.getOrderCode())
        .collect(Collectors.toList());

var ordersByType = orders.stream()
    .collect(Collectors.groupingBy(Order::getOrderType));

// ✅ Use method references when possible
var orderCodes = orders.stream()
    .map(Order::getOrderCode)  // Not: .map(o -> o.getOrderCode())
    .toList();
```

## Exception Handling

```java
// ✅ Throw domain-specific exceptions
throw new OrderNotFoundException(orderCode);
throw new InvalidOrderStateException("Order cannot be processed in state: " + state);

// ✅ Log before throwing when appropriate
log.error("Order {} not found", orderCode);
throw new OrderNotFoundException(orderCode);

// ❌ Don't swallow exceptions
try {
  processOrder(order);
} catch (Exception e) {
  // WRONG - at least log it!
}

// ✅ Log and rethrow or wrap
try {
  processOrder(order);
} catch (DataAccessException e) {
  log.error("Database error processing order {}", orderCode, e);
  throw new OrderProcessingException("Failed to process order", e);
}
```

## String Formatting

```java
// ✅ Use String.format() or formatted() for complex formatting
var message = String.format("Order %d processed at %s", orderCode, timestamp);

var message = "Order %d processed at %s".formatted(orderCode, timestamp);

// ✅ Use StringBuilder for concatenation in loops
var builder = new StringBuilder();
for (var order : orders) {
  builder.append(order.getOrderCode()).append(", ");
}

// ✅ Use text blocks for multi-line strings (Java 15+)
var query = """
    SELECT o.* 
    FROM orders o 
    WHERE o.order_code = ?
    """;
```

## Code Comments (AVOID)

**Code should be self-explanatory. Avoid comments in production code.**

```java
// ❌ Bad - comments explaining what code does
// Get the order by order code
var order = orderRepository.findByOrderCode(orderCode);

// Check if order exists
if (order.isPresent()) {
  // Process the order
  processOrder(order.get());
}

// ✅ Good - self-explanatory code, no comments needed
var order = orderRepository.findByOrderCode(orderCode);
if (order.isPresent()) {
  processOrder(order.get());
}

// ✅ Better - use descriptive method/variable names instead of comments
var order = findOrderOrThrow(orderCode);
processValidatedOrder(order);

// ❌ Bad - commenting out code (delete it instead, use git history)
// var oldImplementation = legacyService.process(order);
var result = newService.process(order);

// ❌ Bad - explaining complex logic with comments
// Calculate total by multiplying quantity by price and adding tax
var total = quantity * price + (quantity * price * taxRate);

// ✅ Good - extract to well-named method
var total = calculateTotalWithTax(quantity, price, taxRate);

private BigDecimal calculateTotalWithTax(int quantity, BigDecimal price, BigDecimal taxRate) {
  var subtotal = price.multiply(BigDecimal.valueOf(quantity));
  var tax = subtotal.multiply(taxRate);
  return subtotal.add(tax);
}
```

**When comments ARE acceptable:**

- Javadoc for public APIs (interfaces, public methods in libraries)
- TODO/FIXME with JIRA ticket reference
- Complex algorithms where the "why" is not obvious
- Structural comments in tests (given-when-then)

```java
// ✅ Acceptable - TODO with ticket reference
// TODO [TRAZAPRO-123]: Refactor to use new validation service

// ✅ Acceptable - explaining WHY, not WHAT
// Use BigDecimal to avoid floating-point precision issues in financial calculations
var total = new BigDecimal("0.00");

// ✅ Acceptable - Javadoc for public API

/**
 * Finds orders by order code and validates business rules.
 *
 * @param orderCode the order code to search for
 * @return validated order
 * @throws OrderNotFoundException if order not found
 */
public Order findAndValidateOrder(Integer orderCode) {
  // implementation
}
```

## Key Principles

1. ✅ **Use `var` when type is obvious from context** (builder, constructor)
2. ✅ **Use @RequiredArgsConstructor for dependency injection** (never @Autowired)
3. ✅ **Use @Builder for entity/DTO construction**
4. ✅ **Use @Slf4j for logging** (never manual logger creation)
5. ✅ **Use Optional for nullable returns** (avoid returning null)
6. ✅ **Use method references over lambdas** when possible
7. ✅ **Use immutable collections** (Collections.emptyList, List.of, Set.of) when appropriate
8. ✅ **Log exceptions before throwing** domain exceptions
9. ✅ **Write self-explanatory code** - avoid comments, use descriptive names instead

## Summary

These common Java conventions apply to ALL Java files in the project:

- Modern Java features (var, Optional, streams, text blocks)
- Lombok for boilerplate reduction
- Constructor injection over field injection
- Immutable collections and builder patterns
- Proper exception handling and logging
