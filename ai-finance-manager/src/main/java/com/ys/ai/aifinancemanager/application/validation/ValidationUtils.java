package com.ys.ai.aifinancemanager.application.validation;

import com.ys.ai.aifinancemanager.application.dto.CreateTransactionRequest;
import com.ys.ai.aifinancemanager.application.dto.TransactionExportResponse;
import com.ys.ai.aifinancemanager.domain.entity.CategoryType;

import java.time.LocalDate;
import java.util.Objects;

public final class ValidationUtils {

  private ValidationUtils() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  public static void validateCreateTransactionRequest(CreateTransactionRequest request) {
    Objects.requireNonNull(request, "Transaction request cannot be null");
    Objects.requireNonNull(request.getAmount(), "Amount cannot be null");
    Objects.requireNonNull(request.getTransactionDate(), "Transaction date cannot be null");
    Objects.requireNonNull(request.getCategoryId(), "Category cannot be null");
  }

  public static void validateDateRange(LocalDate dateFrom, LocalDate dateTo) {
    Objects.requireNonNull(dateFrom, "Date from cannot be null");
    Objects.requireNonNull(dateTo, "Date to cannot be null");

    if (dateFrom.isAfter(dateTo)) {
      throw new IllegalArgumentException("Date from must be before or equal to date to");
    }
  }

  public static void validateCategoryType(CategoryType type) {
    Objects.requireNonNull(type, "Category type cannot be null");
  }

  public static void validateTransactionTypeAndDateRange(
      CategoryType type,
      LocalDate dateFrom,
      LocalDate dateTo) {
    validateCategoryType(type);
    validateDateRange(dateFrom, dateTo);
  }

  public static void validateExportData(TransactionExportResponse exportData) {
    Objects.requireNonNull(exportData, "Export data cannot be null");
  }
}

