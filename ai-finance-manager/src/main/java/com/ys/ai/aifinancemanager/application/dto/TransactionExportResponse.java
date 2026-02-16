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
public class TransactionExportResponse {

  private List<TransactionExportDetail> transactions;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TransactionExportDetail {
    private Integer idTransaction;
    private String transactionDate;
    private BigDecimal amount;
    private String categoryDescription;
    private String categoryType;
    private String comment;
  }
}

