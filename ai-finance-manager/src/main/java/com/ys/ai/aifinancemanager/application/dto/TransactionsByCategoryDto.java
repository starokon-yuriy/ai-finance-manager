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
public class TransactionsByCategoryDto {

  private CategoryDto category;

  private List<TransactionDto> transactions;

  private BigDecimal totalAmount;
}

