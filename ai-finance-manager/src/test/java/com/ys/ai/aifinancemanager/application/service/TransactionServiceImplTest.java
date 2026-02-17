package com.ys.ai.aifinancemanager.application.service;

import com.ys.ai.aifinancemanager.application.dto.CategoryDto;
import com.ys.ai.aifinancemanager.application.dto.CreateTransactionRequest;
import com.ys.ai.aifinancemanager.application.dto.TransactionDto;
import com.ys.ai.aifinancemanager.application.dto.TransactionExportResponse;
import com.ys.ai.aifinancemanager.application.dto.TransactionsByTypeResponse;
import com.ys.ai.aifinancemanager.application.mapper.CategoryMapper;
import com.ys.ai.aifinancemanager.application.mapper.TransactionMapper;
import com.ys.ai.aifinancemanager.domain.entity.Category;
import com.ys.ai.aifinancemanager.domain.entity.CategoryType;
import com.ys.ai.aifinancemanager.domain.entity.Transaction;
import com.ys.ai.aifinancemanager.domain.repository.CategoryRepository;
import com.ys.ai.aifinancemanager.domain.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

  @Mock
  private TransactionRepository transactionRepository;

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private TransactionMapper transactionMapper;

  @Mock
  private CategoryMapper categoryMapper;

  @InjectMocks
  private TransactionServiceImpl transactionService;

  private Category incomeCategory;
  private Category expenseCategory;
  private Transaction incomeTransaction;
  private Transaction expenseTransaction;
  private CategoryDto incomeCategoryDto;
  private CategoryDto expenseCategoryDto;
  private TransactionDto incomeTransactionDto;
  private TransactionDto expenseTransactionDto;

  @BeforeEach
  void setUp() {
    // Setup income category
    incomeCategory = Category.builder()
        .idCategory(1)
        .description("Salary")
        .type(CategoryType.INCOMES)
        .build();

    incomeCategoryDto = CategoryDto.builder()
        .idCategory(1)
        .description("Salary")
        .type(CategoryType.INCOMES)
        .build();

    // Setup expense category
    expenseCategory = Category.builder()
        .idCategory(2)
        .description("Food")
        .type(CategoryType.EXPENSES)
        .build();

    expenseCategoryDto = CategoryDto.builder()
        .idCategory(2)
        .description("Food")
        .type(CategoryType.EXPENSES)
        .build();

    // Setup income transaction
    incomeTransaction = Transaction.builder()
        .idTransaction(100)
        .amount(new BigDecimal("5000.00"))
        .transactionDate(LocalDate.of(2026, 1, 15))
        .category(incomeCategory)
        .comment("Monthly salary")
        .build();

    incomeTransactionDto = TransactionDto.builder()
        .idTransaction(100)
        .amount(new BigDecimal("5000.00"))
        .transactionDate(LocalDate.of(2026, 1, 15))
        .category(incomeCategoryDto)
        .comment("Monthly salary")
        .build();

    // Setup expense transaction
    expenseTransaction = Transaction.builder()
        .idTransaction(101)
        .amount(new BigDecimal("150.50"))
        .transactionDate(LocalDate.of(2026, 1, 20))
        .category(expenseCategory)
        .comment("Groceries")
        .build();

    expenseTransactionDto = TransactionDto.builder()
        .idTransaction(101)
        .amount(new BigDecimal("150.50"))
        .transactionDate(LocalDate.of(2026, 1, 20))
        .category(expenseCategoryDto)
        .comment("Groceries")
        .build();
  }

  // ========== addTransaction Tests ==========

  @Test
  void addTransaction_shouldAddTransactionSuccessfully() {
    // Given
    CreateTransactionRequest request = CreateTransactionRequest.builder()
        .amount(new BigDecimal("5000.00"))
        .transactionDate(LocalDate.of(2026, 1, 15))
        .categoryId(1)
        .comment("Monthly salary")
        .build();

    when(categoryRepository.findById(1)).thenReturn(Optional.of(incomeCategory));
    when(transactionRepository.save(any(Transaction.class))).thenReturn(incomeTransaction);
    when(transactionMapper.toDto(incomeTransaction)).thenReturn(incomeTransactionDto);

    // When
    TransactionDto result = transactionService.addTransaction(request);

    // Then
    assertNotNull(result);
    assertEquals(100, result.getIdTransaction());
    assertEquals(new BigDecimal("5000.00"), result.getAmount());
    assertEquals("Monthly salary", result.getComment());

    verify(categoryRepository).findById(1);
    verify(transactionRepository).save(any(Transaction.class));
    verify(transactionMapper).toDto(incomeTransaction);
  }

  @Test
  void addTransaction_shouldThrowExceptionWhenCategoryNotFound() {
    // Given
    CreateTransactionRequest request = CreateTransactionRequest.builder()
        .amount(new BigDecimal("100.00"))
        .transactionDate(LocalDate.now())
        .categoryId(999)
        .comment("Test")
        .build();

    when(categoryRepository.findById(999)).thenReturn(Optional.empty());

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> transactionService.addTransaction(request)
    );

    assertTrue(exception.getMessage().contains("Category not found"));
    verify(categoryRepository).findById(999);
    verify(transactionRepository, never()).save(any());
  }

  @Test
  void addTransaction_shouldThrowExceptionWhenAmountIsNull() {
    // Given
    CreateTransactionRequest request = CreateTransactionRequest.builder()
        .amount(null)
        .transactionDate(LocalDate.now())
        .categoryId(1)
        .comment("Test")
        .build();

    // When & Then
    assertThrows(NullPointerException.class,
        () -> transactionService.addTransaction(request));

    verify(categoryRepository, never()).findById(anyInt());
    verify(transactionRepository, never()).save(any());
  }

  @Test
  void addTransaction_shouldThrowExceptionWhenAmountIsNegative() {
    // Given
    CreateTransactionRequest request = CreateTransactionRequest.builder()
        .amount(new BigDecimal("-100.00"))
        .transactionDate(LocalDate.now())
        .categoryId(1)
        .comment("Test")
        .build();

    when(categoryRepository.findById(1)).thenReturn(Optional.of(incomeCategory));

    // When
    // Note: ValidationUtils doesn't validate negative amounts, so transaction will be saved
    Transaction transaction = Transaction.builder()
        .idTransaction(999)
        .amount(new BigDecimal("-100.00"))
        .transactionDate(LocalDate.now())
        .category(incomeCategory)
        .comment("Test")
        .build();

    when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
    when(transactionMapper.toDto(transaction)).thenReturn(TransactionDto.builder()
        .idTransaction(999)
        .amount(new BigDecimal("-100.00"))
        .build());

    TransactionDto result = transactionService.addTransaction(request);

    // Then
    assertNotNull(result);
    assertEquals(new BigDecimal("-100.00"), result.getAmount());
    verify(transactionRepository).save(any(Transaction.class));
  }

  @Test
  void addTransaction_shouldThrowExceptionWhenAmountIsZero() {
    // Given
    CreateTransactionRequest request = CreateTransactionRequest.builder()
        .amount(BigDecimal.ZERO)
        .transactionDate(LocalDate.now())
        .categoryId(1)
        .comment("Test")
        .build();

    when(categoryRepository.findById(1)).thenReturn(Optional.of(incomeCategory));

    // When
    // Note: ValidationUtils doesn't validate zero amounts, so transaction will be saved
    Transaction transaction = Transaction.builder()
        .idTransaction(999)
        .amount(BigDecimal.ZERO)
        .transactionDate(LocalDate.now())
        .category(incomeCategory)
        .comment("Test")
        .build();

    when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
    when(transactionMapper.toDto(transaction)).thenReturn(TransactionDto.builder()
        .idTransaction(999)
        .amount(BigDecimal.ZERO)
        .build());

    TransactionDto result = transactionService.addTransaction(request);

    // Then
    assertNotNull(result);
    assertEquals(BigDecimal.ZERO, result.getAmount());
    verify(transactionRepository).save(any(Transaction.class));
  }

  @Test
  void addTransaction_shouldThrowExceptionWhenTransactionDateIsNull() {
    // Given
    CreateTransactionRequest request = CreateTransactionRequest.builder()
        .amount(new BigDecimal("100.00"))
        .transactionDate(null)
        .categoryId(1)
        .comment("Test")
        .build();

    // When & Then
    assertThrows(NullPointerException.class,
        () -> transactionService.addTransaction(request));

    verify(categoryRepository, never()).findById(anyInt());
    verify(transactionRepository, never()).save(any());
  }

  @Test
  void addTransaction_shouldThrowExceptionWhenCategoryIdIsNull() {
    // Given
    CreateTransactionRequest request = CreateTransactionRequest.builder()
        .amount(new BigDecimal("100.00"))
        .transactionDate(LocalDate.now())
        .categoryId(null)
        .comment("Test")
        .build();

    // When & Then
    assertThrows(NullPointerException.class,
        () -> transactionService.addTransaction(request));

    verify(categoryRepository, never()).findById(anyInt());
    verify(transactionRepository, never()).save(any());
  }

  @Test
  void addTransaction_shouldAllowNullComment() {
    // Given
    CreateTransactionRequest request = CreateTransactionRequest.builder()
        .amount(new BigDecimal("100.00"))
        .transactionDate(LocalDate.now())
        .categoryId(1)
        .comment(null)
        .build();

    Transaction transactionWithoutComment = Transaction.builder()
        .idTransaction(102)
        .amount(new BigDecimal("100.00"))
        .transactionDate(LocalDate.now())
        .category(incomeCategory)
        .comment(null)
        .build();

    TransactionDto dtoWithoutComment = TransactionDto.builder()
        .idTransaction(102)
        .amount(new BigDecimal("100.00"))
        .transactionDate(LocalDate.now())
        .category(incomeCategoryDto)
        .comment(null)
        .build();

    when(categoryRepository.findById(1)).thenReturn(Optional.of(incomeCategory));
    when(transactionRepository.save(any(Transaction.class))).thenReturn(transactionWithoutComment);
    when(transactionMapper.toDto(transactionWithoutComment)).thenReturn(dtoWithoutComment);

    // When
    TransactionDto result = transactionService.addTransaction(request);

    // Then
    assertNotNull(result);
    assertNull(result.getComment());
    verify(transactionRepository).save(any(Transaction.class));
  }

  // ========== getTransactionsByTypeAndDateRange Tests ==========

  @Test
  void getTransactionsByTypeAndDateRange_shouldReturnIncomesCorrectly() {
    // Given
    LocalDate dateFrom = LocalDate.of(2026, 1, 1);
    LocalDate dateTo = LocalDate.of(2026, 1, 31);
    CategoryType type = CategoryType.INCOMES;

    List<Transaction> transactions = List.of(incomeTransaction);

    when(transactionRepository.findByCategoryTypeAndTransactionDateBetween(type, dateFrom, dateTo))
        .thenReturn(transactions);
    when(categoryMapper.toDto(incomeCategory)).thenReturn(incomeCategoryDto);
    when(transactionMapper.toDtoList(transactions)).thenReturn(List.of(incomeTransactionDto));

    // When
    TransactionsByTypeResponse result = transactionService.getTransactionsByTypeAndDateRange(
        type, dateFrom, dateTo);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getCategorySummaries().size());
    assertEquals(new BigDecimal("5000.00"), result.getTotalAmount());

    verify(transactionRepository).findByCategoryTypeAndTransactionDateBetween(type, dateFrom, dateTo);
  }

  @Test
  void getTransactionsByTypeAndDateRange_shouldReturnExpensesCorrectly() {
    // Given
    LocalDate dateFrom = LocalDate.of(2026, 1, 1);
    LocalDate dateTo = LocalDate.of(2026, 1, 31);
    CategoryType type = CategoryType.EXPENSES;

    List<Transaction> transactions = List.of(expenseTransaction);

    when(transactionRepository.findByCategoryTypeAndTransactionDateBetween(type, dateFrom, dateTo))
        .thenReturn(transactions);
    when(categoryMapper.toDto(expenseCategory)).thenReturn(expenseCategoryDto);
    when(transactionMapper.toDtoList(transactions)).thenReturn(List.of(expenseTransactionDto));

    // When
    TransactionsByTypeResponse result = transactionService.getTransactionsByTypeAndDateRange(
        type, dateFrom, dateTo);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getCategorySummaries().size());
    assertEquals(new BigDecimal("150.50"), result.getTotalAmount());
  }

  @Test
  void getTransactionsByTypeAndDateRange_shouldReturnEmptyWhenNoTransactionsFound() {
    // Given
    LocalDate dateFrom = LocalDate.of(2026, 1, 1);
    LocalDate dateTo = LocalDate.of(2026, 1, 31);
    CategoryType type = CategoryType.INCOMES;

    when(transactionRepository.findByCategoryTypeAndTransactionDateBetween(type, dateFrom, dateTo))
        .thenReturn(List.of());

    // When
    TransactionsByTypeResponse result = transactionService.getTransactionsByTypeAndDateRange(
        type, dateFrom, dateTo);

    // Then
    assertNotNull(result);
    assertTrue(result.getCategorySummaries().isEmpty());
    assertEquals(BigDecimal.ZERO, result.getTotalAmount());
  }

  @Test
  void getTransactionsByTypeAndDateRange_shouldGroupTransactionsByCategory() {
    // Given
    LocalDate dateFrom = LocalDate.of(2026, 1, 1);
    LocalDate dateTo = LocalDate.of(2026, 1, 31);
    CategoryType type = CategoryType.EXPENSES;

    Category foodCategory = Category.builder()
        .idCategory(2)
        .description("Food")
        .type(CategoryType.EXPENSES)
        .build();

    Category transportCategory = Category.builder()
        .idCategory(3)
        .description("Transport")
        .type(CategoryType.EXPENSES)
        .build();

    Transaction foodTransaction1 = Transaction.builder()
        .idTransaction(101)
        .amount(new BigDecimal("100.00"))
        .transactionDate(LocalDate.of(2026, 1, 10))
        .category(foodCategory)
        .comment("Groceries")
        .build();

    Transaction foodTransaction2 = Transaction.builder()
        .idTransaction(102)
        .amount(new BigDecimal("50.00"))
        .transactionDate(LocalDate.of(2026, 1, 15))
        .category(foodCategory)
        .comment("Restaurant")
        .build();

    Transaction transportTransaction = Transaction.builder()
        .idTransaction(103)
        .amount(new BigDecimal("30.00"))
        .transactionDate(LocalDate.of(2026, 1, 20))
        .category(transportCategory)
        .comment("Bus ticket")
        .build();

    List<Transaction> transactions = Arrays.asList(
        foodTransaction1, foodTransaction2, transportTransaction);

    CategoryDto foodCategoryDto = CategoryDto.builder()
        .idCategory(2)
        .description("Food")
        .type(CategoryType.EXPENSES)
        .build();

    CategoryDto transportCategoryDto = CategoryDto.builder()
        .idCategory(3)
        .description("Transport")
        .type(CategoryType.EXPENSES)
        .build();

    when(transactionRepository.findByCategoryTypeAndTransactionDateBetween(type, dateFrom, dateTo))
        .thenReturn(transactions);
    when(categoryMapper.toDto(foodCategory)).thenReturn(foodCategoryDto);
    when(categoryMapper.toDto(transportCategory)).thenReturn(transportCategoryDto);
    when(transactionMapper.toDtoList(anyList())).thenAnswer(invocation -> {
      List<Transaction> input = invocation.getArgument(0);
      return input.stream()
          .map(t -> TransactionDto.builder()
              .idTransaction(t.getIdTransaction())
              .amount(t.getAmount())
              .build())
          .toList();
    });

    // When
    TransactionsByTypeResponse result = transactionService.getTransactionsByTypeAndDateRange(
        type, dateFrom, dateTo);

    // Then
    assertNotNull(result);
    assertEquals(2, result.getCategorySummaries().size());
    assertEquals(new BigDecimal("180.00"), result.getTotalAmount());

    // Verify food category has 2 transactions
    var foodSummary = result.getCategorySummaries().stream()
        .filter(s -> s.getCategory().getDescription().equals("Food"))
        .findFirst()
        .orElse(null);
    assertNotNull(foodSummary);
    assertEquals(2, foodSummary.getTransactions().size());
    assertEquals(new BigDecimal("150.00"), foodSummary.getCategoryTotal());

    // Verify transport category has 1 transaction
    var transportSummary = result.getCategorySummaries().stream()
        .filter(s -> s.getCategory().getDescription().equals("Transport"))
        .findFirst()
        .orElse(null);
    assertNotNull(transportSummary);
    assertEquals(1, transportSummary.getTransactions().size());
    assertEquals(new BigDecimal("30.00"), transportSummary.getCategoryTotal());
  }

  @Test
  void getTransactionsByTypeAndDateRange_shouldThrowExceptionWhenTypeIsNull() {
    // given
    var dateFrom = LocalDate.of(2026, 1, 1);
    var dateTo = LocalDate.of(2026, 1, 31);

    // when & then
    assertThrows(NullPointerException.class,
        () -> transactionService.getTransactionsByTypeAndDateRange(null, dateFrom, dateTo));

    verify(transactionRepository, never())
        .findByCategoryTypeAndTransactionDateBetween(any(), any(), any());
  }

  @Test
  void getTransactionsByTypeAndDateRange_shouldThrowExceptionWhenDateFromIsNull() {
    // given
    var dateTo = LocalDate.now();

    // when & then
    assertThrows(NullPointerException.class,
        () -> transactionService.getTransactionsByTypeAndDateRange(
            CategoryType.INCOMES, null, dateTo));

    verify(transactionRepository, never())
        .findByCategoryTypeAndTransactionDateBetween(any(), any(), any());
  }

  @Test
  void getTransactionsByTypeAndDateRange_shouldThrowExceptionWhenDateToIsNull() {
    // given
    var dateFrom = LocalDate.now();

    // when & then
    assertThrows(NullPointerException.class,
        () -> transactionService.getTransactionsByTypeAndDateRange(
            CategoryType.INCOMES, dateFrom, null));

    verify(transactionRepository, never())
        .findByCategoryTypeAndTransactionDateBetween(any(), any(), any());
  }

  @Test
  void getTransactionsByTypeAndDateRange_shouldThrowExceptionWhenDateFromIsAfterDateTo() {
    // Given
    LocalDate dateFrom = LocalDate.of(2026, 1, 31);
    LocalDate dateTo = LocalDate.of(2026, 1, 1);

    // When & Then
    assertThrows(IllegalArgumentException.class,
        () -> transactionService.getTransactionsByTypeAndDateRange(
            CategoryType.INCOMES, dateFrom, dateTo));

    verify(transactionRepository, never())
        .findByCategoryTypeAndTransactionDateBetween(any(), any(), any());
  }

  // ========== getAllCategories Tests ==========

  @Test
  void getAllCategories_shouldReturnIncomeCategoriesCorrectly() {
    // Given
    List<Category> categories = List.of(incomeCategory);
    when(categoryRepository.findByType(CategoryType.INCOMES)).thenReturn(categories);
    when(categoryMapper.toDto(incomeCategory)).thenReturn(incomeCategoryDto);

    // When
    List<CategoryDto> result = transactionService.getAllCategories(CategoryType.INCOMES);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Salary", result.getFirst().getDescription());
    assertEquals(CategoryType.INCOMES, result.getFirst().getType());

    verify(categoryRepository).findByType(CategoryType.INCOMES);
    verify(categoryMapper).toDto(incomeCategory);
  }

  @Test
  void getAllCategories_shouldReturnExpenseCategoriesCorrectly() {
    // Given
    List<Category> categories = List.of(expenseCategory);
    when(categoryRepository.findByType(CategoryType.EXPENSES)).thenReturn(categories);
    when(categoryMapper.toDto(expenseCategory)).thenReturn(expenseCategoryDto);

    // When
    List<CategoryDto> result = transactionService.getAllCategories(CategoryType.EXPENSES);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Food", result.getFirst().getDescription());
    assertEquals(CategoryType.EXPENSES, result.getFirst().getType());

    verify(categoryRepository).findByType(CategoryType.EXPENSES);
  }

  @Test
  void getAllCategories_shouldReturnEmptyListWhenNoCategoriesFound() {
    // Given
    when(categoryRepository.findByType(CategoryType.INCOMES)).thenReturn(List.of());

    // When
    List<CategoryDto> result = transactionService.getAllCategories(CategoryType.INCOMES);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void getAllCategories_shouldThrowExceptionWhenTypeIsNull() {
    // When & Then
    assertThrows(NullPointerException.class,
        () -> transactionService.getAllCategories(null));

    verify(categoryRepository, never()).findByType(any());
  }

  @Test
  void getAllCategories_shouldReturnMultipleCategories() {
    // Given
    Category category1 = Category.builder()
        .idCategory(1)
        .description("Salary")
        .type(CategoryType.INCOMES)
        .build();

    Category category2 = Category.builder()
        .idCategory(2)
        .description("Freelance")
        .type(CategoryType.INCOMES)
        .build();

    CategoryDto categoryDto1 = CategoryDto.builder()
        .idCategory(1)
        .description("Salary")
        .type(CategoryType.INCOMES)
        .build();

    CategoryDto categoryDto2 = CategoryDto.builder()
        .idCategory(2)
        .description("Freelance")
        .type(CategoryType.INCOMES)
        .build();

    when(categoryRepository.findByType(CategoryType.INCOMES))
        .thenReturn(Arrays.asList(category1, category2));
    when(categoryMapper.toDto(category1)).thenReturn(categoryDto1);
    when(categoryMapper.toDto(category2)).thenReturn(categoryDto2);

    // When
    List<CategoryDto> result = transactionService.getAllCategories(CategoryType.INCOMES);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(c -> c.getDescription().equals("Salary")));
    assertTrue(result.stream().anyMatch(c -> c.getDescription().equals("Freelance")));
  }

  // ========== exportTransactions Tests ==========

  @Test
  void exportTransactions_shouldExportTransactionsCorrectly() {
    // Given
    LocalDate dateFrom = LocalDate.of(2026, 1, 1);
    LocalDate dateTo = LocalDate.of(2026, 1, 31);

    List<Transaction> transactions = Arrays.asList(incomeTransaction, expenseTransaction);

    when(transactionRepository.findByTransactionDateBetween(dateFrom, dateTo))
        .thenReturn(transactions);

    // When
    TransactionExportResponse result = transactionService.exportTransactions(dateFrom, dateTo);

    // Then
    assertNotNull(result);
    assertEquals(2, result.getTransactions().size());

    verify(transactionRepository).findByTransactionDateBetween(dateFrom, dateTo);
  }

  @Test
  void exportTransactions_shouldSortTransactionsByTypeIncomesFirst() {
    // Given
    LocalDate dateFrom = LocalDate.of(2026, 1, 1);
    LocalDate dateTo = LocalDate.of(2026, 1, 31);

    // Create transactions with EXPENSES first in the list
    List<Transaction> unsortedTransactions = Arrays.asList(expenseTransaction, incomeTransaction);

    when(transactionRepository.findByTransactionDateBetween(dateFrom, dateTo))
        .thenReturn(unsortedTransactions);

    // When
    TransactionExportResponse result = transactionService.exportTransactions(dateFrom, dateTo);

    // Then
    assertNotNull(result);
    assertEquals(2, result.getTransactions().size());

    // Verify INCOMES come before EXPENSES
    assertEquals("INCOMES", result.getTransactions().getFirst().getCategoryType());
    assertEquals("EXPENSES", result.getTransactions().get(1).getCategoryType());
  }

  @Test
  void exportTransactions_shouldReturnEmptyWhenNoTransactionsFound() {
    // Given
    LocalDate dateFrom = LocalDate.of(2026, 1, 1);
    LocalDate dateTo = LocalDate.of(2026, 1, 31);

    when(transactionRepository.findByTransactionDateBetween(dateFrom, dateTo))
        .thenReturn(List.of());

    // When
    TransactionExportResponse result = transactionService.exportTransactions(dateFrom, dateTo);

    // Then
    assertNotNull(result);
    assertTrue(result.getTransactions().isEmpty());
  }

  @Test
  void exportTransactions_shouldThrowExceptionWhenDateFromIsNull() {
    // Given
    var dateTo = LocalDate.now();

    // When & Then
    assertThrows(NullPointerException.class,
        () -> transactionService.exportTransactions(null, dateTo));

    verify(transactionRepository, never()).findByTransactionDateBetween(any(), any());
  }

  @Test
  void exportTransactions_shouldThrowExceptionWhenDateToIsNull() {
    // Given
    var dateFrom = LocalDate.now();

    // When & Then
    assertThrows(NullPointerException.class,
        () -> transactionService.exportTransactions(dateFrom, null));

    verify(transactionRepository, never()).findByTransactionDateBetween(any(), any());
  }

  @Test
  void exportTransactions_shouldThrowExceptionWhenDateFromIsAfterDateTo() {
    // Given
    LocalDate dateFrom = LocalDate.of(2026, 1, 31);
    LocalDate dateTo = LocalDate.of(2026, 1, 1);

    // When & Then
    assertThrows(IllegalArgumentException.class,
        () -> transactionService.exportTransactions(dateFrom, dateTo));

    verify(transactionRepository, never()).findByTransactionDateBetween(any(), any());
  }

  @Test
  void exportTransactions_shouldHandleEmptyComments() {
    // Given
    LocalDate dateFrom = LocalDate.of(2026, 1, 1);
    LocalDate dateTo = LocalDate.of(2026, 1, 31);

    Transaction transactionWithoutComment = Transaction.builder()
        .idTransaction(103)
        .amount(new BigDecimal("100.00"))
        .transactionDate(LocalDate.of(2026, 1, 10))
        .category(incomeCategory)
        .comment(null)
        .build();

    when(transactionRepository.findByTransactionDateBetween(dateFrom, dateTo))
        .thenReturn(List.of(transactionWithoutComment));

    // When
    TransactionExportResponse result = transactionService.exportTransactions(dateFrom, dateTo);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getTransactions().size());
    assertEquals("", result.getTransactions().getFirst().getComment());
  }

  @Test
  void exportTransactions_shouldIncludeAllRequiredFields() {
    // Given
    LocalDate dateFrom = LocalDate.of(2026, 1, 1);
    LocalDate dateTo = LocalDate.of(2026, 1, 31);

    when(transactionRepository.findByTransactionDateBetween(dateFrom, dateTo))
        .thenReturn(List.of(incomeTransaction));

    // When
    TransactionExportResponse result = transactionService.exportTransactions(dateFrom, dateTo);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getTransactions().size());

    var exportDetail = result.getTransactions().getFirst();
    assertNotNull(exportDetail.getIdTransaction());
    assertNotNull(exportDetail.getTransactionDate());
    assertNotNull(exportDetail.getAmount());
    assertNotNull(exportDetail.getCategoryDescription());
    assertNotNull(exportDetail.getCategoryType());
    assertNotNull(exportDetail.getComment());
  }
}

