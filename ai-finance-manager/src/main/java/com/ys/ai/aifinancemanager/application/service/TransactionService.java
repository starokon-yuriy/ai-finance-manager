package com.ys.ai.aifinancemanager.application.service;

import com.ys.ai.aifinancemanager.application.dto.CategoryDto;
import com.ys.ai.aifinancemanager.application.dto.CreateTransactionRequest;
import com.ys.ai.aifinancemanager.application.dto.TransactionDto;
import com.ys.ai.aifinancemanager.application.dto.TransactionExportResponse;
import com.ys.ai.aifinancemanager.application.dto.TransactionsByTypeResponse;
import com.ys.ai.aifinancemanager.domain.entity.CategoryType;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {

  TransactionDto addTransaction(CreateTransactionRequest request);

  TransactionsByTypeResponse getTransactionsByTypeAndDateRange(
      CategoryType type,
      LocalDate dateFrom,
      LocalDate dateTo
  );

  List<CategoryDto> getAllCategories(CategoryType type);

  TransactionExportResponse exportTransactions(LocalDate dateFrom, LocalDate dateTo);
}

