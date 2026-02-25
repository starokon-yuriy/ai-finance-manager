package com.ys.ai.aifinancemanager.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceChartDataResponse {

  private String chartEmbedUrl;

  private List<BalanceDataPoint> balanceData;

  private BigDecimal totalIncome;

  private BigDecimal totalExpense;

  private BigDecimal netBalance;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BalanceDataPoint {
    private String date;
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal balance;
  }
}

