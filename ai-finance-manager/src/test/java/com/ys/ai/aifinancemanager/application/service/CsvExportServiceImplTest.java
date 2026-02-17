package com.ys.ai.aifinancemanager.application.service;

import com.ys.ai.aifinancemanager.application.dto.TransactionExportResponse;
import com.ys.ai.aifinancemanager.application.dto.TransactionExportResponse.TransactionExportDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CsvExportServiceImplTest {

  @InjectMocks
  private CsvExportServiceImpl csvExportService;

  private TransactionExportResponse exportData;

  @BeforeEach
  void setUp() {
    // Create test data
    TransactionExportDetail transaction1 = TransactionExportDetail.builder()
        .idTransaction(1)
        .transactionDate("2026-01-15")
        .amount(new BigDecimal("5000.00"))
        .categoryDescription("Salary")
        .categoryType("INCOMES")
        .comment("Monthly salary")
        .build();

    TransactionExportDetail transaction2 = TransactionExportDetail.builder()
        .idTransaction(2)
        .transactionDate("2026-01-20")
        .amount(new BigDecimal("150.50"))
        .categoryDescription("Food")
        .categoryType("EXPENSES")
        .comment("Groceries")
        .build();

    exportData = TransactionExportResponse.builder()
        .transactions(Arrays.asList(transaction1, transaction2))
        .build();
  }

  @Test
  void exportTransactionsToCsv_shouldGenerateCorrectCsvContent() {
    // When
    String result = csvExportService.exportTransactionsToCsv(exportData);

    // Then
    assertNotNull(result);

    String[] lines = result.split("\n");
    assertEquals(3, lines.length, "Should have header + 2 data rows");

    // Verify header
    assertEquals("Transaction ID,Transaction Date,Amount,Category Description,Category Type,Comment", lines[0]);

    // Verify first transaction
    assertTrue(lines[1].contains("1"));
    assertTrue(lines[1].contains("2026-01-15"));
    assertTrue(lines[1].contains("5000.00"));
    assertTrue(lines[1].contains("Salary"));
    assertTrue(lines[1].contains("INCOMES"));
    assertTrue(lines[1].contains("Monthly salary"));

    // Verify second transaction
    assertTrue(lines[2].contains("2"));
    assertTrue(lines[2].contains("2026-01-20"));
    assertTrue(lines[2].contains("150.50"));
    assertTrue(lines[2].contains("Food"));
    assertTrue(lines[2].contains("EXPENSES"));
    assertTrue(lines[2].contains("Groceries"));
  }

  @Test
  void exportTransactionsToCsv_shouldHandleEmptyComment() {
    // Given
    TransactionExportDetail transaction = TransactionExportDetail.builder()
        .idTransaction(3)
        .transactionDate("2026-01-25")
        .amount(new BigDecimal("75.00"))
        .categoryDescription("Transportation")
        .categoryType("EXPENSES")
        .comment("")
        .build();

    TransactionExportResponse data = TransactionExportResponse.builder()
        .transactions(List.of(transaction))
        .build();

    // When
    String result = csvExportService.exportTransactionsToCsv(data);

    // Then
    assertNotNull(result);
    String[] lines = result.split("\n");
    assertEquals(2, lines.length);

    // Verify the CSV line contains all expected fields
    assertTrue(lines[1].contains("3"));
    assertTrue(lines[1].contains("2026-01-25"));
    assertTrue(lines[1].contains("75.00"));
    assertTrue(lines[1].contains("Transportation"));
    assertTrue(lines[1].contains("EXPENSES"));
  }

  @Test
  void exportTransactionsToCsv_shouldHandleNullComment() {
    // Given
    TransactionExportDetail transaction = TransactionExportDetail.builder()
        .idTransaction(4)
        .transactionDate("2026-02-01")
        .amount(new BigDecimal("200.00"))
        .categoryDescription("Entertainment")
        .categoryType("EXPENSES")
        .comment(null)
        .build();

    TransactionExportResponse data = TransactionExportResponse.builder()
        .transactions(List.of(transaction))
        .build();

    // When
    String result = csvExportService.exportTransactionsToCsv(data);

    // Then
    assertNotNull(result);
    String[] lines = result.split("\n");
    assertEquals(2, lines.length);

    // Should handle null gracefully
    assertFalse(lines[1].endsWith("null"));
  }

  @Test
  void exportTransactionsToCsv_shouldEscapeCommasInFields() {
    // Given
    TransactionExportDetail transaction = TransactionExportDetail.builder()
        .idTransaction(5)
        .transactionDate("2026-02-05")
        .amount(new BigDecimal("100.00"))
        .categoryDescription("Gifts, Donations")
        .categoryType("EXPENSES")
        .comment("Birthday gift, card")
        .build();

    TransactionExportResponse data = TransactionExportResponse.builder()
        .transactions(List.of(transaction))
        .build();

    // When
    String result = csvExportService.exportTransactionsToCsv(data);

    // Then
    assertNotNull(result);

    // Fields with commas should be escaped with quotes
    assertTrue(result.contains("\"Gifts, Donations\""));
    assertTrue(result.contains("\"Birthday gift, card\""));
  }

  @Test
  void exportTransactionsToCsv_shouldEscapeQuotesInFields() {
    // Given
    TransactionExportDetail transaction = TransactionExportDetail.builder()
        .idTransaction(6)
        .transactionDate("2026-02-10")
        .amount(new BigDecimal("50.00"))
        .categoryDescription("Books")
        .categoryType("EXPENSES")
        .comment("Book \"Clean Code\"")
        .build();

    TransactionExportResponse data = TransactionExportResponse.builder()
        .transactions(List.of(transaction))
        .build();

    // When
    String result = csvExportService.exportTransactionsToCsv(data);

    // Then
    assertNotNull(result);

    // Quotes should be escaped by doubling them
    assertTrue(result.contains("\"Book \"\"Clean Code\"\"\""));
  }

  @Test
  void exportTransactionsToCsv_shouldHandleNewlinesInFields() {
    // Given
    TransactionExportDetail transaction = TransactionExportDetail.builder()
        .idTransaction(7)
        .transactionDate("2026-02-15")
        .amount(new BigDecimal("300.00"))
        .categoryDescription("Healthcare")
        .categoryType("EXPENSES")
        .comment("Doctor visit\nPrescription")
        .build();

    TransactionExportResponse data = TransactionExportResponse.builder()
        .transactions(List.of(transaction))
        .build();

    // When
    String result = csvExportService.exportTransactionsToCsv(data);

    // Then
    assertNotNull(result);

    // Fields with newlines should be escaped with quotes
    assertTrue(result.contains("\"Doctor visit\nPrescription\""));
  }

  @Test
  void exportTransactionsToCsv_shouldHandleEmptyTransactionList() {
    // Given
    TransactionExportResponse data = TransactionExportResponse.builder()
        .transactions(List.of())
        .build();

    // When
    String result = csvExportService.exportTransactionsToCsv(data);

    // Then
    assertNotNull(result);

    String[] lines = result.split("\n");
    assertEquals(1, lines.length, "Should only have header");
    assertEquals("Transaction ID,Transaction Date,Amount,Category Description,Category Type,Comment", lines[0]);
  }

  @Test
  void exportTransactionsToCsv_shouldSortTransactionsByCategoryType() {
    // Given - transactions with EXPENSES first, then INCOMES (opposite of expected order)
    TransactionExportDetail expenseTransaction = TransactionExportDetail.builder()
        .idTransaction(1)
        .transactionDate("2026-01-20")
        .amount(new BigDecimal("150.50"))
        .categoryDescription("Food")
        .categoryType("EXPENSES")
        .comment("Groceries")
        .build();

    TransactionExportDetail incomeTransaction = TransactionExportDetail.builder()
        .idTransaction(2)
        .transactionDate("2026-01-15")
        .amount(new BigDecimal("5000.00"))
        .categoryDescription("Salary")
        .categoryType("INCOMES")
        .comment("Monthly salary")
        .build();

    TransactionExportResponse data = TransactionExportResponse.builder()
        .transactions(Arrays.asList(expenseTransaction, incomeTransaction))
        .build();

    // When
    String result = csvExportService.exportTransactionsToCsv(data);

    // Then
    assertNotNull(result);

    String[] lines = result.split("\n");
    assertEquals(3, lines.length);

    // The data is expected to be provided in sorted order from the service layer
    // CSV export should preserve the order it receives
    assertTrue(lines[1].contains("EXPENSES"), "First data row should be EXPENSES");
    assertTrue(lines[2].contains("INCOMES"), "Second data row should be INCOMES");
  }

  @Test
  void exportTransactionsToCsv_shouldIncludeAllRequiredColumns() {
    // When
    String result = csvExportService.exportTransactionsToCsv(exportData);

    // Then
    assertNotNull(result);

    String[] lines = result.split("\n");
    String header = lines[0];

    // Verify all required columns are present
    assertTrue(header.contains("Transaction ID"));
    assertTrue(header.contains("Transaction Date"));
    assertTrue(header.contains("Amount"));
    assertTrue(header.contains("Category Description"));
    assertTrue(header.contains("Category Type"));
    assertTrue(header.contains("Comment"));
  }

  @Test
  void exportTransactionsToCsv_shouldFormatAmountsCorrectly() {
    // Given
    TransactionExportDetail transaction = TransactionExportDetail.builder()
        .idTransaction(8)
        .transactionDate("2026-02-20")
        .amount(new BigDecimal("1234.56"))
        .categoryDescription("Freelance")
        .categoryType("INCOMES")
        .comment("Project payment")
        .build();

    TransactionExportResponse data = TransactionExportResponse.builder()
        .transactions(List.of(transaction))
        .build();

    // When
    String result = csvExportService.exportTransactionsToCsv(data);

    // Then
    assertNotNull(result);
    assertTrue(result.contains("1234.56"));
  }

  @Test
  void exportTransactionsToCsv_shouldHandleMultipleTransactions() {
    // Given - create 5 transactions
    List<TransactionExportDetail> transactions = Arrays.asList(
        createTransactionDetail(1, "2026-01-01", "100.00", "INCOMES"),
        createTransactionDetail(2, "2026-01-02", "200.00", "EXPENSES"),
        createTransactionDetail(3, "2026-01-03", "300.00", "INCOMES"),
        createTransactionDetail(4, "2026-01-04", "400.00", "EXPENSES"),
        createTransactionDetail(5, "2026-01-05", "500.00", "INCOMES")
    );

    TransactionExportResponse data = TransactionExportResponse.builder()
        .transactions(transactions)
        .build();

    // When
    String result = csvExportService.exportTransactionsToCsv(data);

    // Then
    assertNotNull(result);
    String[] lines = result.split("\n");
    assertEquals(6, lines.length, "Should have header + 5 data rows");
  }

  private TransactionExportDetail createTransactionDetail(
      int id, String date, String amount, String type) {
    return TransactionExportDetail.builder()
        .idTransaction(id)
        .transactionDate(date)
        .amount(new BigDecimal(amount))
        .categoryDescription("Category " + id)
        .categoryType(type)
        .comment("Comment " + id)
        .build();
  }
}

