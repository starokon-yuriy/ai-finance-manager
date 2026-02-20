---
applyTo: "**/*Test.java"
---

# Unit Test Instructions

**Unit tests verify individual components in isolation using mocks.**

## Mockito Setup

### Standard Unit Test Pattern

```java

@ExtendWith(MockitoExtension.class)
class GetCountryUseCaseImplTest {

  @Mock
  private CountryRepository countryRepository;  // Mock dependencies

  @InjectMocks
  private GetCountryUseCaseImpl countryUseCase;  // Class under test

  @Test
  void shouldReturnCountries() {
    // given
    var language = Language.EN;
    var country = Country.builder().countryCode(1).build();
    var countries = List.of(country);
    when(countryRepository.findAllByLanguage(language)).thenReturn(countries);

    // when
    var actual = countryUseCase.findAllCountriesBy(language);

    // then
    assertThat(actual).containsOnly(country);
    verify(countryRepository).findAllByLanguage(language);
  }
}
```

## Hexagonal Architecture Testing Rules

### Application Layer (Use Cases)

**What to mock:**

- `@Mock` domain repository interfaces (outbound ports)
- `@Mock` other use case interfaces (when composing use cases)

**What NOT to mock:**

- Never mock the use case implementation itself
- Never mock domain entities (use Test Data Builders)

```java

@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseImplTest {

  @Mock
  private OrderRepository orderRepository;  // Mock domain interface

  @Mock
  private ValidateOrderUseCase validateOrderUseCase;  // Mock composed use case

  @InjectMocks
  private CreateOrderUseCaseImpl createOrderUseCase;  // Class under test

  @Test
  void shouldCreateOrder() {
    // given
    var order = OrderTestDataBuilder.order();
    when(orderRepository.save(order)).thenReturn(order);

    // when
    var result = createOrderUseCase.createOrder(order);

    // then
    assertThat(result).isEqualTo(order);
    verify(validateOrderUseCase).validate(order);
    verify(orderRepository).save(order);
  }
}
```

### Infrastructure Layer (Adapters)

**What to mock:**

- `@Mock` Spring Data repositories (JPA/MongoDB)
- `@Mock` MapStruct mappers
- `@Mock` REST clients (for external service adapters)

**What NOT to mock:**

- Never mock the adapter itself

```java

@ExtendWith(MockitoExtension.class)
class OrderRepositoryAdapterTest {

  @Mock
  private OrderEntityRepository entityRepository;  // Mock JPA repository

  @Mock
  private OrderEntityMapper mapper;  // Mock mapper

  @InjectMocks
  private OrderRepositoryAdapter adapter;  // Class under test

  @Test
  void shouldFindOrderById() {
    // given
    var orderId = 1;
    var entity = new OrderEntity();
    entity.setId(orderId);
    var order = Order.builder().id(orderId).build();

    when(entityRepository.findById(orderId)).thenReturn(Optional.of(entity));
    when(mapper.entityToDomain(entity)).thenReturn(order);

    // when
    var result = adapter.findById(orderId);

    // then
    assertThat(result).isPresent().contains(order);
    verify(entityRepository).findById(orderId);
    verify(mapper).entityToDomain(entity);
  }
}
```

### REST Controllers

**What to mock:**

- `@MockBean` use case interfaces (inbound ports)
- `@MockBean` DTO mappers (MapStruct)

**What NOT to mock:**

- Never mock the controller itself
- Never mock MockMvc or ObjectMapper

```java

@WebMvcTest(OrdersController.class)
@Import(OrdersController.class)
class OrdersControllerTest {

  @MockBean
  private CreateOrderUseCase createOrderUseCase;  // Mock use case

  @MockBean
  private OrderDTOMapper orderMapper;  // Mock DTO mapper

  @Autowired
  private MockMvc mockMvc;  // Don't mock - autowired

  @Autowired
  private ObjectMapper objectMapper;  // Don't mock - autowired

  @Test
  void shouldCreateOrder() throws Exception {
    // given
    var order = OrderTestDataBuilder.order();
    var orderDTO = new OrderDTO().orderCode("123");
    var requestDTO = new CreateOrderDTO();
    when(createOrderUseCase.createOrder(any(Order.class))).thenReturn(order);
    when(orderMapper.toDto(order)).thenReturn(orderDTO);

    // when
    var result = mockMvc.perform(post("/v1/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isCreated())
        .andReturn();

    // then
    var actual = objectMapper.readValue(
        result.getResponse().getContentAsString(),
        OrderDTO.class);
    assertThat(actual).isEqualTo(orderDTO);
    verify(createOrderUseCase).createOrder(any(Order.class));
  }
}
```

## Mockito Patterns

### Stubbing Methods

```java
// ✅ Best - use actual objects when possible
var order = OrderTestDataBuilder.order();
when(repository.save(order)).thenReturn(order);
when(repository.findById(1)).thenReturn(Optional.of(order));

// ✅ Good - use typed matchers when object doesn't matter
when(repository.save(any(Order.class))).thenReturn(order);
when(publisher).publish(any(OrderEvent.class));

// ❌ Avoid - generic any() loses type safety
when(repository.save(any())).thenReturn(order);  // What type is any()?
when(publisher).publish(any());  // Less clear

// Stubbing void methods
doNothing().when(publisher).publish(any(OrderEvent.class));

// Stubbing to throw exception
when(repository.findById(999)).thenThrow(new OrderNotFoundException(999));

// Multiple calls
when(repository.findAll())
    .thenReturn(List.of(order1))
    .thenReturn(List.of(order1, order2));
```

### Verification

```java
// ✅ Best - verify with actual objects
verify(repository).save(order);
verify(mapper).toDto(order);

// ✅ Good - verify with typed matchers when needed
verify(repository).save(any(Order.class));
verify(publisher).publish(any(OrderEvent.class));

// ❌ Avoid - generic any() loses type safety
verify(repository).save(any());  // What type?

// Verify number of invocations
verify(repository, times(1)).findById(1);
verify(repository, never()).delete(any(Order.class));
verify(repository, atLeastOnce()).save(any(Order.class));

// Verify no more interactions
verify(repository).save(order);
verifyNoMoreInteractions(repository);

// Verify argument capture (when you need to inspect the argument)
var captor = ArgumentCaptor.forClass(Order.class);
verify(repository).save(captor.capture());
assertThat(captor.getValue().getOrderCode()).isEqualTo(123);
```

### Exception Testing

```java

@Test
void shouldThrowExceptionWhenOrderNotFound() {
  // given
  var orderCode = 999;
  when(repository.findByOrderCode(orderCode)).thenReturn(Optional.empty());

  // when/then
  assertThatThrownBy(() -> useCase.getOrder(orderCode))
      .isInstanceOf(OrderNotFoundException.class)
      .hasMessageContaining("999");

  verify(repository).findByOrderCode(orderCode);
}
```

## Common Anti-Patterns

❌ **Don't mock implementations instead of interfaces:**

```java
// Bad
@Mock
private OrderRepositoryAdapter repositoryAdapter;  // WRONG!

// Good
@Mock
private OrderRepository orderRepository;  // Correct - mock interface
```

❌ **Don't use @InjectMocks on interfaces:**

```java
// Bad
@InjectMocks
private OrderRepository orderRepository;  // WRONG - can't inject into interface

// Good
@InjectMocks
private OrderRepositoryAdapter orderRepositoryAdapter;  // Correct - concrete class
```

❌ **Don't mock everything:**

```java
// Bad - over-mocking
@Mock
private final Integer orderCode;  // WRONG - use real values

// Good
var orderCode = 123;  // Use real primitive/wrapper values
```

❌ **Don't forget to verify:**

```java
// Bad
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

## Key Principles

1. ✅ **Use @ExtendWith(MockitoExtension.class)** for all unit tests
2. ✅ **Mock dependencies (@Mock), inject into class under test (@InjectMocks)**
3. ✅ **Mock at the right layer** - follow hexagonal architecture rules
4. ✅ **Prefer actual objects over any() matchers** - use `any(Type.class)` when needed
5. ✅ **Always verify mock interactions** in the `then` section
6. ✅ **Use Test Data Builders** for domain objects
7. ✅ **Test one scenario per test method** - single responsibility
8. ✅ **Use descriptive test names** that explain the scenario
9. ✅ **Test exception scenarios** with assertThatThrownBy

## Summary

Unit tests verify components in isolation:

- Use Mockito for mocking dependencies
- Mock interfaces, not implementations
- Follow hexagonal architecture testing rules
- Always verify all mock interactions
- Use Test Data Builders for test data
- Test both happy path and error scenarios
