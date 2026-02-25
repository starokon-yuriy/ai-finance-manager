package com.ys.ai.aifinancemanager.application.service;

import com.ys.ai.aifinancemanager.application.dto.BalanceChartDataResponse;
import com.ys.ai.aifinancemanager.domain.entity.Category;
import com.ys.ai.aifinancemanager.domain.entity.CategoryType;
import com.ys.ai.aifinancemanager.domain.entity.Transaction;
import com.ys.ai.aifinancemanager.domain.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceChartServiceImplTest {

  @Mock
  private TransactionRepository transactionRepository;

  @Mock
  private DatawrapperService datawrapperService;

  @InjectMocks
  private BalanceChartServiceImpl balanceChartService;

  @Test
  void getBalanceChartData_shouldReturnEmptyDataForNoTransactions() {
    LocalDate dateFrom = LocalDate.of(2026, 1, 1);
    LocalDate dateTo = LocalDate.of(2026, 1, 31);

    when(transactionRepository.findByCategoryTypeAndTransactionDateBetween(
        eq(CategoryType.INCOMES), eq(dateFrom), eq(dateTo)))
        .thenReturn(Collections.emptyList());
    when(transactionRepository.findByCategoryTypeAndTransactionDateBetween(
        eq(CategoryType.EXPENSES), eq(dateFrom), eq(dateTo)))
        .thenReturn(Collections.emptyList());
    when(datawrapperService.createOrUpdateChart(any(), any(), any()))
        .thenReturn(null);

    BalanceChartDataResponse result = balanceChartService.getBalanceChartData(dateFrom, dateTo);

    assertThat(result).isNotNull();
    assertThat(result.getTotalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(result.getTotalExpense()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(result.getNetBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(result.getBalanceData()).hasSize(31); // daily for <=31 days range
  }

  @Test
  void getBalanceChartData_shouldCalculateCorrectBalance() {
    LocalDate dateFrom = LocalDate.of(2026, 1, 1);
    LocalDate dateTo = LocalDate.of(2026, 1, 5);

    Category incomeCategory = Category.builder()
        .idCategory(1).description("Salary").type(CategoryType.INCOMES).build();
    Category expenseCategory = Category.builder()
        .idCategory(2).description("Food").type(CategoryType.EXPENSES).build();

    List<Transaction> incomes = List.of(
        Transaction.builder()
            .idTransaction(1).amount(new BigDecimal("1000"))
            .transactionDate(LocalDate.of(2026, 1, 1)).category(incomeCategory).build(),
        Transaction.builder()
            .idTransaction(2).amount(new BigDecimal("500"))
            .transactionDate(LocalDate.of(2026, 1, 3)).category(incomeCategory).build()
    );

    List<Transaction> expenses = List.of(
        Transaction.builder()
            .idTransaction(3).amount(new BigDecimal("200"))
            .transactionDate(LocalDate.of(2026, 1, 2)).category(expenseCategory).build()
    );

    when(transactionRepository.findByCategoryTypeAndTransactionDateBetween(
        eq(CategoryType.INCOMES), eq(dateFrom), eq(dateTo)))
        .thenReturn(incomes);
    when(transactionRepository.findByCategoryTypeAndTransactionDateBetween(
        eq(CategoryType.EXPENSES), eq(dateFrom), eq(dateTo)))
        .thenReturn(expenses);
    when(datawrapperService.createOrUpdateChart(any(), any(), any()))
        .thenReturn(null);

    BalanceChartDataResponse result = balanceChartService.getBalanceChartData(dateFrom, dateTo);

    assertThat(result.getTotalIncome()).isEqualByComparingTo(new BigDecimal("1500"));
    assertThat(result.getTotalExpense()).isEqualByComparingTo(new BigDecimal("200"));
    assertThat(result.getNetBalance()).isEqualByComparingTo(new BigDecimal("1300"));
    assertThat(result.getBalanceData()).hasSize(5);

    // Verify running balance: day1=1000, day2=800, day3=1300, day4=1300, day5=1300
    assertThat(result.getBalanceData().get(0).getBalance()).isEqualByComparingTo(new BigDecimal("1000"));
    assertThat(result.getBalanceData().get(1).getBalance()).isEqualByComparingTo(new BigDecimal("800"));
    assertThat(result.getBalanceData().get(2).getBalance()).isEqualByComparingTo(new BigDecimal("1300"));
  }

  @Test
  void getBalanceChartData_shouldAggregateByMonthForLargeRange() {
    LocalDate dateFrom = LocalDate.of(2026, 1, 1);
    LocalDate dateTo = LocalDate.of(2026, 6, 30);

    when(transactionRepository.findByCategoryTypeAndTransactionDateBetween(
        eq(CategoryType.INCOMES), eq(dateFrom), eq(dateTo)))
        .thenReturn(Collections.emptyList());
    when(transactionRepository.findByCategoryTypeAndTransactionDateBetween(
        eq(CategoryType.EXPENSES), eq(dateFrom), eq(dateTo)))
        .thenReturn(Collections.emptyList());
    when(datawrapperService.createOrUpdateChart(any(), any(), any()))
        .thenReturn(null);

    BalanceChartDataResponse result = balanceChartService.getBalanceChartData(dateFrom, dateTo);

    assertThat(result.getBalanceData()).hasSize(6); // 6 months
    assertThat(result.getBalanceData().get(0).getDate()).isEqualTo("2026-01");
    assertThat(result.getBalanceData().get(5).getDate()).isEqualTo("2026-06");
  }

  @Test
  void getBalanceChartData_shouldIncludeDatawrapperUrl_whenAvailable() {
    LocalDate dateFrom = LocalDate.of(2026, 1, 1);
    LocalDate dateTo = LocalDate.of(2026, 1, 5);

    when(transactionRepository.findByCategoryTypeAndTransactionDateBetween(any(), any(), any()))
        .thenReturn(Collections.emptyList());
    when(datawrapperService.createOrUpdateChart(any(), any(), any()))
        .thenReturn("https://datawrapper.dwcdn.net/abc123/");

    BalanceChartDataResponse result = balanceChartService.getBalanceChartData(dateFrom, dateTo);

    assertThat(result.getChartEmbedUrl()).isEqualTo("https://datawrapper.dwcdn.net/abc123/");
  }

  @Test
  void getBalanceChartData_shouldHandleDatawrapperFailureGracefully() {
    LocalDate dateFrom = LocalDate.of(2026, 1, 1);
    LocalDate dateTo = LocalDate.of(2026, 1, 5);

    when(transactionRepository.findByCategoryTypeAndTransactionDateBetween(any(), any(), any()))
        .thenReturn(Collections.emptyList());
    when(datawrapperService.createOrUpdateChart(any(), any(), any()))
        .thenThrow(new RuntimeException("API error"));

    BalanceChartDataResponse result = balanceChartService.getBalanceChartData(dateFrom, dateTo);

    assertThat(result).isNotNull();
    assertThat(result.getChartEmbedUrl()).isNull();
    assertThat(result.getBalanceData()).isNotEmpty();
  }

  @Test
  void getBalanceChartData_shouldThrowForInvalidDateRange() {
    LocalDate dateFrom = LocalDate.of(2026, 2, 1);
    LocalDate dateTo = LocalDate.of(2026, 1, 1);

    assertThatThrownBy(() -> balanceChartService.getBalanceChartData(dateFrom, dateTo))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
