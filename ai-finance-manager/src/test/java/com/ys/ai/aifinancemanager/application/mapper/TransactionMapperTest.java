package com.ys.ai.aifinancemanager.application.mapper;

import com.ys.ai.aifinancemanager.application.dto.CategoryDto;
import com.ys.ai.aifinancemanager.application.dto.TransactionDto;
import com.ys.ai.aifinancemanager.domain.entity.Category;
import com.ys.ai.aifinancemanager.domain.entity.CategoryType;
import com.ys.ai.aifinancemanager.domain.entity.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TransactionMapperTest {

  @Autowired
  private TransactionMapper transactionMapper;

  @Test
  void toDto_shouldMapTransactionEntityToDtoCorrectly() {
    // Given
    Category category = Category.builder()
        .idCategory(1)
        .description("Salary")
        .type(CategoryType.INCOMES)
        .build();

    Transaction transaction = Transaction.builder()
        .idTransaction(100)
        .amount(new BigDecimal("5000.00"))
        .transactionDate(LocalDate.of(2026, 1, 15))
        .category(category)
        .comment("Monthly salary")
        .build();

    // When
    TransactionDto result = transactionMapper.toDto(transaction);

    // Then
    assertNotNull(result);
    assertEquals(transaction.getIdTransaction(), result.getIdTransaction());
    assertEquals(transaction.getAmount(), result.getAmount());
    assertEquals(transaction.getTransactionDate(), result.getTransactionDate());
    assertEquals(transaction.getComment(), result.getComment());

    assertNotNull(result.getCategory());
    assertEquals(category.getIdCategory(), result.getCategory().getIdCategory());
    assertEquals(category.getDescription(), result.getCategory().getDescription());
    assertEquals(category.getType(), result.getCategory().getType());
  }

  @Test
  void toDto_shouldHandleNullCategory() {
    // Given
    Transaction transaction = Transaction.builder()
        .idTransaction(101)
        .amount(new BigDecimal("150.50"))
        .transactionDate(LocalDate.of(2026, 1, 20))
        .category(null)
        .comment("Test transaction")
        .build();

    // When
    TransactionDto result = transactionMapper.toDto(transaction);

    // Then
    assertNotNull(result);
    assertEquals(101, result.getIdTransaction());
    assertEquals(new BigDecimal("150.50"), result.getAmount());
    assertNull(result.getCategory());
  }

  @Test
  void toDto_shouldHandleNullComment() {
    // Given
    Category category = Category.builder()
        .idCategory(2)
        .description("Food")
        .type(CategoryType.EXPENSES)
        .build();

    Transaction transaction = Transaction.builder()
        .idTransaction(102)
        .amount(new BigDecimal("75.00"))
        .transactionDate(LocalDate.of(2026, 1, 25))
        .category(category)
        .comment(null)
        .build();

    // When
    TransactionDto result = transactionMapper.toDto(transaction);

    // Then
    assertNotNull(result);
    assertEquals(102, result.getIdTransaction());
    assertNull(result.getComment());
  }

  @Test
  void toDto_shouldReturnNullWhenInputIsNull() {
    // When
    TransactionDto result = transactionMapper.toDto(null);

    // Then
    assertNull(result);
  }

  @Test
  void toEntity_shouldMapTransactionDtoToEntityCorrectly() {
    // Given
    CategoryDto categoryDto = CategoryDto.builder()
        .idCategory(1)
        .description("Salary")
        .type(CategoryType.INCOMES)
        .build();

    TransactionDto dto = TransactionDto.builder()
        .idTransaction(100)
        .amount(new BigDecimal("5000.00"))
        .transactionDate(LocalDate.of(2026, 1, 15))
        .category(categoryDto)
        .comment("Monthly salary")
        .build();

    // When
    Transaction result = transactionMapper.toEntity(dto);

    // Then
    assertNotNull(result);
    assertEquals(dto.getIdTransaction(), result.getIdTransaction());
    assertEquals(dto.getAmount(), result.getAmount());
    assertEquals(dto.getTransactionDate(), result.getTransactionDate());
    assertEquals(dto.getComment(), result.getComment());

    // Note: category is ignored in mapping as per @Mapping(target = "category", ignore = true)
    assertNull(result.getCategory());
  }

  @Test
  void toEntity_shouldIgnoreCategoryMapping() {
    // Given
    CategoryDto categoryDto = CategoryDto.builder()
        .idCategory(3)
        .description("Transportation")
        .type(CategoryType.EXPENSES)
        .build();

    TransactionDto dto = TransactionDto.builder()
        .idTransaction(103)
        .amount(new BigDecimal("50.00"))
        .transactionDate(LocalDate.of(2026, 2, 1))
        .category(categoryDto)
        .comment("Bus ticket")
        .build();

    // When
    Transaction result = transactionMapper.toEntity(dto);

    // Then
    assertNotNull(result);
    assertNull(result.getCategory(), "Category should be ignored in toEntity mapping");
  }

  @Test
  void toEntity_shouldReturnNullWhenInputIsNull() {
    // When
    Transaction result = transactionMapper.toEntity(null);

    // Then
    assertNull(result);
  }

  @Test
  void toDtoList_shouldMapListOfTransactionsCorrectly() {
    // Given
    Category category1 = Category.builder()
        .idCategory(1)
        .description("Salary")
        .type(CategoryType.INCOMES)
        .build();

    Category category2 = Category.builder()
        .idCategory(2)
        .description("Food")
        .type(CategoryType.EXPENSES)
        .build();

    Transaction transaction1 = Transaction.builder()
        .idTransaction(100)
        .amount(new BigDecimal("5000.00"))
        .transactionDate(LocalDate.of(2026, 1, 15))
        .category(category1)
        .comment("Salary")
        .build();

    Transaction transaction2 = Transaction.builder()
        .idTransaction(101)
        .amount(new BigDecimal("150.50"))
        .transactionDate(LocalDate.of(2026, 1, 20))
        .category(category2)
        .comment("Groceries")
        .build();

    List<Transaction> transactions = Arrays.asList(transaction1, transaction2);

    // When
    List<TransactionDto> result = transactionMapper.toDtoList(transactions);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());

    TransactionDto dto1 = result.getFirst();
    assertEquals(100, dto1.getIdTransaction());
    assertEquals(new BigDecimal("5000.00"), dto1.getAmount());
    assertEquals("Salary", dto1.getComment());

    TransactionDto dto2 = result.get(1);
    assertEquals(101, dto2.getIdTransaction());
    assertEquals(new BigDecimal("150.50"), dto2.getAmount());
    assertEquals("Groceries", dto2.getComment());
  }

  @Test
  void toDtoList_shouldHandleEmptyList() {
    // Given
    List<Transaction> transactions = List.of();

    // When
    List<TransactionDto> result = transactionMapper.toDtoList(transactions);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void toDtoList_shouldReturnNullWhenInputIsNull() {
    // When
    List<TransactionDto> result = transactionMapper.toDtoList(null);

    // Then
    assertNull(result);
  }

  @Test
  void toDtoList_shouldHandleListWithNullElements() {
    // Given
    Category category = Category.builder()
        .idCategory(1)
        .description("Test")
        .type(CategoryType.INCOMES)
        .build();

    Transaction transaction = Transaction.builder()
        .idTransaction(100)
        .amount(new BigDecimal("100.00"))
        .transactionDate(LocalDate.now())
        .category(category)
        .comment("Test")
        .build();

    List<Transaction> transactions = Arrays.asList(transaction, null);

    // When
    List<TransactionDto> result = transactionMapper.toDtoList(transactions);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertNotNull(result.get(0));
    assertNull(result.get(1));
  }

  @Test
  void toDto_shouldMapExpenseTransactionCorrectly() {
    // Given
    Category category = Category.builder()
        .idCategory(5)
        .description("Entertainment")
        .type(CategoryType.EXPENSES)
        .build();

    Transaction transaction = Transaction.builder()
        .idTransaction(200)
        .amount(new BigDecimal("89.99"))
        .transactionDate(LocalDate.of(2026, 2, 10))
        .category(category)
        .comment("Concert tickets")
        .build();

    // When
    TransactionDto result = transactionMapper.toDto(transaction);

    // Then
    assertNotNull(result);
    assertEquals(200, result.getIdTransaction());
    assertEquals(new BigDecimal("89.99"), result.getAmount());
    assertEquals(LocalDate.of(2026, 2, 10), result.getTransactionDate());
    assertEquals("Concert tickets", result.getComment());
    assertEquals(CategoryType.EXPENSES, result.getCategory().getType());
  }
}

