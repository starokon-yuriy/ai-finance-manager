package com.ys.ai.aifinancemanager.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {

  private Integer idTransaction;

  private BigDecimal amount;

  private LocalDate transactionDate;

  private CategoryDto category;

  private String comment;
}

