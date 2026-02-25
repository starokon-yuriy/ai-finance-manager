package com.ys.ai.aifinancemanager.application.service;

import com.ys.ai.aifinancemanager.application.dto.BalanceChartDataResponse;
import com.ys.ai.aifinancemanager.application.dto.BalanceChartDataResponse.BalanceDataPoint;
import com.ys.ai.aifinancemanager.application.validation.ValidationUtils;
import com.ys.ai.aifinancemanager.domain.entity.CategoryType;
import com.ys.ai.aifinancemanager.domain.entity.Transaction;
import com.ys.ai.aifinancemanager.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceChartServiceImpl implements BalanceChartService {

  private final TransactionRepository transactionRepository;
  private final DatawrapperService datawrapperService;

  @Override
  @Transactional(readOnly = true)
  public BalanceChartDataResponse getBalanceChartData(LocalDate dateFrom, LocalDate dateTo) {
    ValidationUtils.validateDateRange(dateFrom, dateTo);

    log.info("Generating balance chart data from {} to {}", dateFrom, dateTo);

    var incomeTransactions = transactionRepository.findByCategoryTypeAndTransactionDateBetween(
        CategoryType.INCOMES, dateFrom, dateTo);
    var expenseTransactions = transactionRepository.findByCategoryTypeAndTransactionDateBetween(
        CategoryType.EXPENSES, dateFrom, dateTo);

    log.info("Found {} income and {} expense transactions", incomeTransactions.size(), expenseTransactions.size());

    var balanceData = buildBalanceTimeSeries(dateFrom, dateTo, incomeTransactions, expenseTransactions);

    var totalIncome = incomeTransactions.stream()
        .map(Transaction::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    var totalExpense = expenseTransactions.stream()
        .map(Transaction::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    var netBalance = totalIncome.subtract(totalExpense);

    // Try to create Datawrapper chart if service is configured
    String chartEmbedUrl = null;
    try {
      chartEmbedUrl = datawrapperService.createOrUpdateChart(
          "Income vs Expenses Balance", balanceData, dateFrom, dateTo);
      log.info("Datawrapper chart created/updated. Embed URL: {}", chartEmbedUrl);
    } catch (Exception e) {
      log.warn("Failed to create Datawrapper chart (service may not be configured): {}", e.getMessage());
    }

    return BalanceChartDataResponse.builder()
        .chartEmbedUrl(chartEmbedUrl)
        .balanceData(balanceData)
        .totalIncome(totalIncome)
        .totalExpense(totalExpense)
        .netBalance(netBalance)
        .build();
  }

  private List<BalanceDataPoint> buildBalanceTimeSeries(
      LocalDate dateFrom,
      LocalDate dateTo,
      List<Transaction> incomeTransactions,
      List<Transaction> expenseTransactions) {

    long daysBetween = ChronoUnit.DAYS.between(dateFrom, dateTo);
    boolean aggregateByMonth = daysBetween > 31;

    // Group incomes by date
    Map<String, BigDecimal> incomeByDate = groupTransactionsByDate(incomeTransactions, aggregateByMonth);
    // Group expenses by date
    Map<String, BigDecimal> expenseByDate = groupTransactionsByDate(expenseTransactions, aggregateByMonth);

    // Merge all dates and build time series
    TreeMap<String, BalanceDataPoint> timeSeriesMap = new TreeMap<>();

    // Initialize all dates in range
    if (aggregateByMonth) {
      var current = dateFrom.withDayOfMonth(1);
      var end = dateTo.withDayOfMonth(1);
      while (!current.isAfter(end)) {
        var key = current.toString().substring(0, 7); // YYYY-MM
        timeSeriesMap.put(key, BalanceDataPoint.builder()
            .date(key)
            .income(BigDecimal.ZERO)
            .expense(BigDecimal.ZERO)
            .balance(BigDecimal.ZERO)
            .build());
        current = current.plusMonths(1);
      }
    } else {
      var current = dateFrom;
      while (!current.isAfter(dateTo)) {
        var key = current.toString();
        timeSeriesMap.put(key, BalanceDataPoint.builder()
            .date(key)
            .income(BigDecimal.ZERO)
            .expense(BigDecimal.ZERO)
            .balance(BigDecimal.ZERO)
            .build());
        current = current.plusDays(1);
      }
    }

    // Fill in actual values
    incomeByDate.forEach((date, amount) -> {
      var point = timeSeriesMap.get(date);
      if (point != null) {
        point.setIncome(amount);
      }
    });

    expenseByDate.forEach((date, amount) -> {
      var point = timeSeriesMap.get(date);
      if (point != null) {
        point.setExpense(amount);
      }
    });

    // Calculate running balance
    BigDecimal runningBalance = BigDecimal.ZERO;
    List<BalanceDataPoint> result = new ArrayList<>();
    for (var point : timeSeriesMap.values()) {
      runningBalance = runningBalance.add(point.getIncome()).subtract(point.getExpense());
      point.setBalance(runningBalance);
      result.add(point);
    }

    log.info("Built time series with {} data points (aggregated by {})",
        result.size(), aggregateByMonth ? "month" : "day");

    return result;
  }

  private Map<String, BigDecimal> groupTransactionsByDate(
      List<Transaction> transactions, boolean aggregateByMonth) {
    return transactions.stream()
        .collect(Collectors.groupingBy(
            t -> {
              if (aggregateByMonth) {
                return t.getTransactionDate().toString().substring(0, 7);
              } else {
                return t.getTransactionDate().toString();
              }
            },
            Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
        ));
  }
}

