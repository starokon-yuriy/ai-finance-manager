package com.ys.ai.aifinancemanager.application.service;

import com.ys.ai.aifinancemanager.application.dto.CategoryDto;
import com.ys.ai.aifinancemanager.application.dto.CreateTransactionRequest;
import com.ys.ai.aifinancemanager.application.dto.TransactionDto;
import com.ys.ai.aifinancemanager.application.dto.TransactionExportResponse;
import com.ys.ai.aifinancemanager.application.dto.TransactionExportResponse.TransactionExportDetail;
import com.ys.ai.aifinancemanager.application.dto.TransactionsByTypeResponse;
import com.ys.ai.aifinancemanager.application.dto.TransactionsByTypeResponse.CategoryTransactionSummary;
import com.ys.ai.aifinancemanager.application.mapper.CategoryMapper;
import com.ys.ai.aifinancemanager.application.mapper.TransactionMapper;
import com.ys.ai.aifinancemanager.application.validation.ValidationUtils;
import com.ys.ai.aifinancemanager.domain.entity.Category;
import com.ys.ai.aifinancemanager.domain.entity.CategoryType;
import com.ys.ai.aifinancemanager.domain.entity.Transaction;
import com.ys.ai.aifinancemanager.domain.repository.CategoryRepository;
import com.ys.ai.aifinancemanager.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

  private final TransactionRepository transactionRepository;

  private final CategoryRepository categoryRepository;

  private final TransactionMapper transactionMapper;

  private final CategoryMapper categoryMapper;

  @Override
  @Transactional
  public TransactionDto addTransaction(CreateTransactionRequest request) {
    ValidationUtils.validateCreateTransactionRequest(request);

    log.info("Adding new transaction: categoryId={}, amount={}", request.getCategoryId(), request.getAmount());

    var category = categoryRepository.findById(request.getCategoryId())
        .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + request.getCategoryId()));

    var transaction = Transaction.builder()
        .amount(request.getAmount())
        .transactionDate(request.getTransactionDate())
        .category(category)
        .comment(request.getComment())
        .build();

    var savedTransaction = transactionRepository.save(transaction);
    log.info("Transaction added successfully with id: {}", savedTransaction.getIdTransaction());

    return transactionMapper.toDto(savedTransaction);
  }

  @Override
  @Transactional(readOnly = true)
  public TransactionsByTypeResponse getTransactionsByTypeAndDateRange(
      CategoryType type,
      LocalDate dateFrom,
      LocalDate dateTo) {
    ValidationUtils.validateTransactionTypeAndDateRange(type, dateFrom, dateTo);

    log.info("Fetching {} transactions between {} and {}", type, dateFrom, dateTo);

    var transactions = transactionRepository.findByCategoryTypeAndTransactionDateBetween(
        type, dateFrom, dateTo);
    log.info("Found {} transactions of type {}", transactions.size(), type);

    // Group transactions by category
    var transactionsByCategory = transactions.stream()
        .collect(Collectors.groupingBy(
            Transaction::getCategory
        ));

    // Build category summaries
    var categorySummaries = transactionsByCategory.entrySet().stream()
        .map(this::toCategoryTransactionSummary)
        .toList();

    // Calculate total amount across all categories
    var totalAmount = transactions.stream()
        .map(Transaction::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    log.info("Grouped transactions into {} categories with total amount: {}",
        categorySummaries.size(), totalAmount);

    return TransactionsByTypeResponse.builder()
        .categorySummaries(categorySummaries)
        .totalAmount(totalAmount)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public List<CategoryDto> getAllCategories(CategoryType type) {
    ValidationUtils.validateCategoryType(type);

    log.info("Fetching categories of type: {}", type);

    var categories = categoryRepository.findByType(type);
    log.info("Found {} categories of type {}", categories.size(), type);

    return categories.stream()
        .map(categoryMapper::toDto)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public TransactionExportResponse exportTransactions(LocalDate dateFrom, LocalDate dateTo) {
    ValidationUtils.validateDateRange(dateFrom, dateTo);

    log.info("Exporting transactions for period {} to {}", dateFrom, dateTo);

    var transactions = transactionRepository.findByTransactionDateBetween(dateFrom, dateTo);
    log.info("Found {} transactions to export", transactions.size());

    var transactionDetails = transactions.stream()
        .sorted(getTransactionComparator())
        .map(TransactionServiceImpl::toExportDetails)
        .toList();

    log.info("Export data prepared: {} transactions sorted by category type", transactionDetails.size());

    return TransactionExportResponse.builder()
        .transactions(transactionDetails)
        .build();
  }

  private CategoryTransactionSummary toCategoryTransactionSummary(Entry<Category, List<Transaction>> entry) {
    var categoryDto = categoryMapper.toDto(entry.getKey());
    var transactionDtos = transactionMapper.toDtoList(entry.getValue());
    var categoryTotal = entry.getValue().stream()
        .map(Transaction::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return CategoryTransactionSummary.builder()
        .category(categoryDto)
        .transactions(transactionDtos)
        .categoryTotal(categoryTotal)
        .build();
  }

  private static TransactionExportDetail toExportDetails(Transaction transaction) {
    return TransactionExportDetail.builder()
        .idTransaction(transaction.getIdTransaction())
        .transactionDate(transaction.getTransactionDate().toString())
        .amount(transaction.getAmount())
        .categoryDescription(transaction.getCategory().getDescription())
        .categoryType(transaction.getCategory().getType().toString())
        .comment(transaction.getComment() != null ? transaction.getComment() : "")
        .build();
  }

  private static @NonNull Comparator<Transaction> getTransactionComparator() {
    return (t1, t2) -> {
      // Sort by CategoryType: INCOMES first, then EXPENSES
      var type1 = t1.getCategory() != null ? t1.getCategory().getType() : CategoryType.EXPENSES;
      var type2 = t2.getCategory() != null ? t2.getCategory().getType() : CategoryType.EXPENSES;

      if (type1 == CategoryType.INCOMES && type2 == CategoryType.EXPENSES) {
        return -1;
      } else if (type1 == CategoryType.EXPENSES && type2 == CategoryType.INCOMES) {
        return 1;
      }
      return 0;
    };
  }
}
