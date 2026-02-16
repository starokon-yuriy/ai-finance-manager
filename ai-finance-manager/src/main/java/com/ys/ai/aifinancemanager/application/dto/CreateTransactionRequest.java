package com.ys.ai.aifinancemanager.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequest {

  private BigDecimal amount;

  private LocalDate transactionDate;

  private Integer categoryId;

  private String comment;
}

