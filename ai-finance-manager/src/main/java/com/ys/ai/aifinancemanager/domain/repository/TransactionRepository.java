package com.ys.ai.aifinancemanager.domain.repository;

import com.ys.ai.aifinancemanager.domain.entity.CategoryType;
import com.ys.ai.aifinancemanager.domain.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

  List<Transaction> findByTransactionDateBetween(LocalDate startDate, LocalDate endDate);

  @Query("SELECT t FROM Transaction t WHERE t.category.type = :categoryType " +
      "AND t.transactionDate BETWEEN :startDate AND :endDate")
  List<Transaction> findByCategoryTypeAndTransactionDateBetween(
      @Param("categoryType") CategoryType categoryType,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );
}

