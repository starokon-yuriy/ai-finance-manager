package com.ys.ai.aifinancemanager.application.validation;

import com.ys.ai.aifinancemanager.application.dto.CreateTransactionRequest;
import com.ys.ai.aifinancemanager.application.dto.TransactionExportResponse;
import com.ys.ai.aifinancemanager.domain.entity.CategoryType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationUtilsTest {

  @Test
  void validateCreateTransactionRequest_shouldThrowWhenRequestIsNull() {
    assertThrows(NullPointerException.class,
        () -> ValidationUtils.validateCreateTransactionRequest(null));
  }

  @Test
  void validateCreateTransactionRequest_shouldThrowWhenAmountIsNull() {
    var request = CreateTransactionRequest.builder()
        .transactionDate(LocalDate.now())
        .categoryId(1)
        .build();

    assertThrows(NullPointerException.class,
        () -> ValidationUtils.validateCreateTransactionRequest(request));
  }

  @Test
  void validateCreateTransactionRequest_shouldThrowWhenTransactionDateIsNull() {
    var request = CreateTransactionRequest.builder()
        .amount(BigDecimal.TEN)
        .categoryId(1)
        .build();

    assertThrows(NullPointerException.class,
        () -> ValidationUtils.validateCreateTransactionRequest(request));
  }

  @Test
  void validateCreateTransactionRequest_shouldThrowWhenCategoryIdIsNull() {
    var request = CreateTransactionRequest.builder()
        .amount(BigDecimal.TEN)
        .transactionDate(LocalDate.now())
        .build();

    assertThrows(NullPointerException.class,
        () -> ValidationUtils.validateCreateTransactionRequest(request));
  }

  @Test
  void validateCreateTransactionRequest_shouldPassWhenAllFieldsAreValid() {
    var request = CreateTransactionRequest.builder()
        .amount(BigDecimal.TEN)
        .transactionDate(LocalDate.now())
        .categoryId(1)
        .comment("Test comment")
        .build();

    assertDoesNotThrow(() -> ValidationUtils.validateCreateTransactionRequest(request));
  }

  @Test
  void validateDateRange_shouldThrowWhenDateFromIsNull() {
    assertThrows(NullPointerException.class,
        () -> ValidationUtils.validateDateRange(null, LocalDate.now()));
  }

  @Test
  void validateDateRange_shouldThrowWhenDateToIsNull() {
    assertThrows(NullPointerException.class,
        () -> ValidationUtils.validateDateRange(LocalDate.now(), null));
  }

  @Test
  void validateDateRange_shouldThrowWhenDateFromIsAfterDateTo() {
    var dateFrom = LocalDate.of(2024, 2, 1);
    var dateTo = LocalDate.of(2024, 1, 1);

    assertThrows(IllegalArgumentException.class,
        () -> ValidationUtils.validateDateRange(dateFrom, dateTo));
  }

  @Test
  void validateDateRange_shouldPassWhenDateFromIsBeforeDateTo() {
    var dateFrom = LocalDate.of(2024, 1, 1);
    var dateTo = LocalDate.of(2024, 2, 1);

    assertDoesNotThrow(() -> ValidationUtils.validateDateRange(dateFrom, dateTo));
  }

  @Test
  void validateDateRange_shouldPassWhenDateFromEqualsDateTo() {
    var date = LocalDate.of(2024, 1, 1);

    assertDoesNotThrow(() -> ValidationUtils.validateDateRange(date, date));
  }

  @Test
  void validateCategoryType_shouldThrowWhenTypeIsNull() {
    assertThrows(NullPointerException.class,
        () -> ValidationUtils.validateCategoryType(null));
  }

  @Test
  void validateCategoryType_shouldPassWhenTypeIsValid() {
    assertDoesNotThrow(() -> ValidationUtils.validateCategoryType(CategoryType.EXPENSES));
    assertDoesNotThrow(() -> ValidationUtils.validateCategoryType(CategoryType.INCOMES));
  }

  @Test
  void validateTransactionTypeAndDateRange_shouldThrowWhenTypeIsNull() {
    var dateFrom = LocalDate.of(2024, 1, 1);
    var dateTo = LocalDate.of(2024, 2, 1);

    assertThrows(NullPointerException.class,
        () -> ValidationUtils.validateTransactionTypeAndDateRange(null, dateFrom, dateTo));
  }

  @Test
  void validateTransactionTypeAndDateRange_shouldThrowWhenDateRangeIsInvalid() {
    var dateFrom = LocalDate.of(2024, 2, 1);
    var dateTo = LocalDate.of(2024, 1, 1);

    assertThrows(IllegalArgumentException.class,
        () -> ValidationUtils.validateTransactionTypeAndDateRange(CategoryType.EXPENSES, dateFrom, dateTo));
  }

  @Test
  void validateTransactionTypeAndDateRange_shouldPassWhenAllParametersAreValid() {
    var dateFrom = LocalDate.of(2024, 1, 1);
    var dateTo = LocalDate.of(2024, 2, 1);

    assertDoesNotThrow(
        () -> ValidationUtils.validateTransactionTypeAndDateRange(CategoryType.EXPENSES, dateFrom, dateTo));
  }

  @Test
  void validateExportData_shouldThrowWhenExportDataIsNull() {
    assertThrows(NullPointerException.class,
        () -> ValidationUtils.validateExportData(null));
  }

  @Test
  void validateExportData_shouldPassWhenExportDataIsValid() {
    var exportData = TransactionExportResponse.builder()
        .transactions(Collections.emptyList())
        .build();

    assertDoesNotThrow(() -> ValidationUtils.validateExportData(exportData));
  }

  @Test
  void constructor_shouldThrowUnsupportedOperationException() throws Exception {
    // given
    var constructor = ValidationUtils.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    // when
    var exception = assertThrows(InvocationTargetException.class, constructor::newInstance);

    // then
    assertInstanceOf(UnsupportedOperationException.class, exception.getCause());
  }
}

