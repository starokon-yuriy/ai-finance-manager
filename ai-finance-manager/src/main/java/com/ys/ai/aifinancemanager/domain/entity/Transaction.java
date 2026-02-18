package com.ys.ai.aifinancemanager.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "TRANSACTIONS")
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID_TRANSACTION")
  private Integer idTransaction;

  @Column(name = "AMOUNT", nullable = false, precision = 10, scale = 2)
  private BigDecimal amount;

  @Column(name = "TRANSACTION_DATE", nullable = false)
  private LocalDate transactionDate;

  @ManyToOne
  @JoinColumn(name = "ID_CATEGORY")
  private Category category;

  @Column(name = "COMMENT")
  private String comment;
}

