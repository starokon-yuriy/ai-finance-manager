package com.ys.ai.aifinancemanager.api.controller;

import com.ys.ai.aifinancemanager.application.dto.CategoryDto;
import com.ys.ai.aifinancemanager.application.dto.CreateTransactionRequest;
import com.ys.ai.aifinancemanager.application.dto.TransactionDto;
import com.ys.ai.aifinancemanager.application.dto.TransactionExportResponse;
import com.ys.ai.aifinancemanager.application.dto.TransactionExportResponse.TransactionExportDetail;
import com.ys.ai.aifinancemanager.application.dto.TransactionsByTypeResponse;
import com.ys.ai.aifinancemanager.application.dto.TransactionsByTypeResponse.CategoryTransactionSummary;
import com.ys.ai.aifinancemanager.application.service.CsvExportService;
import com.ys.ai.aifinancemanager.application.service.TransactionService;
import com.ys.ai.aifinancemanager.domain.entity.CategoryType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link TransactionController}.
 *
 * <p>Uses {@code @WebMvcTest} to test only the web layer with mocked service
 * dependencies, following the
 * <a href="https://spring.io/guides/gs/testing-web/">Spring Boot Testing the Web Layer</a>
 * guide.</p>
 */
@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

  private static final String BASE_URL = "/api/v1/finance";

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private TransactionService transactionService;

  @MockitoBean
  private CsvExportService csvExportService;

  // ========== POST /transactions ==========

  @Test
  void addTransaction_shouldReturnCreatedWithTransactionDto() throws Exception {
    var requestJson = """
        {
            "amount": 5000.00,
            "transactionDate": "2026-02-01",
            "categoryId": 1,
            "comment": "Monthly salary"
        }
        """;

    var expectedDto = TransactionDto.builder()
        .idTransaction(100)
        .amount(new BigDecimal("5000.00"))
        .transactionDate(LocalDate.of(2026, 2, 1))
        .category(CategoryDto.builder()
            .idCategory(1)
            .description("Salary")
            .type(CategoryType.INCOMES)
            .build())
        .comment("Monthly salary")
        .build();

    when(transactionService.addTransaction(any(CreateTransactionRequest.class)))
        .thenReturn(expectedDto);

    mockMvc.perform(post(BASE_URL + "/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.idTransaction", is(100)))
        .andExpect(jsonPath("$.amount", is(5000.00)))
        .andExpect(jsonPath("$.transactionDate", is("2026-02-01")))
        .andExpect(jsonPath("$.category.idCategory", is(1)))
        .andExpect(jsonPath("$.category.description", is("Salary")))
        .andExpect(jsonPath("$.category.type", is("INCOMES")))
        .andExpect(jsonPath("$.comment", is("Monthly salary")));

    verify(transactionService).addTransaction(any(CreateTransactionRequest.class));
  }

  @Test
  void addTransaction_shouldReturnCreatedWithNullComment() throws Exception {
    var requestJson = """
        {
            "amount": 150.50,
            "transactionDate": "2026-02-05",
            "categoryId": 2
        }
        """;

    var expectedDto = TransactionDto.builder()
        .idTransaction(101)
        .amount(new BigDecimal("150.50"))
        .transactionDate(LocalDate.of(2026, 2, 5))
        .category(CategoryDto.builder()
            .idCategory(2)
            .description("Food & Groceries")
            .type(CategoryType.EXPENSES)
            .build())
        .comment(null)
        .build();

    when(transactionService.addTransaction(any(CreateTransactionRequest.class)))
        .thenReturn(expectedDto);

    mockMvc.perform(post(BASE_URL + "/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.idTransaction", is(101)))
        .andExpect(jsonPath("$.comment").doesNotExist());
  }

  @Test
  void addTransaction_shouldPropagateExceptionWhenServiceThrows() {
    var requestJson = """
        {
            "amount": 100.00,
            "transactionDate": "2026-02-10",
            "categoryId": 999,
            "comment": "Test"
        }
        """;

    when(transactionService.addTransaction(any(CreateTransactionRequest.class)))
        .thenThrow(new IllegalArgumentException("Category not found with id: 999"));

    var exception = assertThrows(Exception.class, () ->
        mockMvc.perform(post(BASE_URL + "/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson)));

    assertThat(exception).rootCause()
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Category not found with id: 999");
  }

  // ========== GET /transactions ==========

  @Test
  void getTransactions_shouldReturnOkWithTransactionsByTypeResponse() throws Exception {
    var categorySummary = CategoryTransactionSummary.builder()
        .category(CategoryDto.builder()
            .idCategory(1)
            .description("Salary")
            .type(CategoryType.INCOMES)
            .build())
        .transactions(List.of(
            TransactionDto.builder()
                .idTransaction(100)
                .amount(new BigDecimal("5000.00"))
                .transactionDate(LocalDate.of(2026, 2, 1))
                .category(CategoryDto.builder()
                    .idCategory(1)
                    .description("Salary")
                    .type(CategoryType.INCOMES)
                    .build())
                .comment("Monthly salary")
                .build()
        ))
        .categoryTotal(new BigDecimal("5000.00"))
        .build();

    var response = TransactionsByTypeResponse.builder()
        .categorySummaries(List.of(categorySummary))
        .totalAmount(new BigDecimal("5000.00"))
        .build();

    when(transactionService.getTransactionsByTypeAndDateRange(
        CategoryType.INCOMES,
        LocalDate.of(2026, 2, 1),
        LocalDate.of(2026, 2, 28)))
        .thenReturn(response);

    mockMvc.perform(get(BASE_URL + "/transactions")
            .param("type", "INCOMES")
            .param("dateFrom", "2026-02-01")
            .param("dateTo", "2026-02-28"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.categorySummaries", hasSize(1)))
        .andExpect(jsonPath("$.categorySummaries[0].category.description", is("Salary")))
        .andExpect(jsonPath("$.categorySummaries[0].transactions", hasSize(1)))
        .andExpect(jsonPath("$.categorySummaries[0].categoryTotal", is(5000.00)))
        .andExpect(jsonPath("$.totalAmount", is(5000.00)));

    verify(transactionService).getTransactionsByTypeAndDateRange(
        CategoryType.INCOMES,
        LocalDate.of(2026, 2, 1),
        LocalDate.of(2026, 2, 28));
  }

  @Test
  void getTransactions_shouldReturnOkWithExpensesType() throws Exception {
    var foodSummary = CategoryTransactionSummary.builder()
        .category(CategoryDto.builder()
            .idCategory(2)
            .description("Food & Groceries")
            .type(CategoryType.EXPENSES)
            .build())
        .transactions(List.of(
            TransactionDto.builder()
                .idTransaction(101)
                .amount(new BigDecimal("250.50"))
                .transactionDate(LocalDate.of(2026, 2, 5))
                .category(CategoryDto.builder()
                    .idCategory(2)
                    .description("Food & Groceries")
                    .type(CategoryType.EXPENSES)
                    .build())
                .comment("Supermarket shopping")
                .build()
        ))
        .categoryTotal(new BigDecimal("250.50"))
        .build();

    var transportSummary = CategoryTransactionSummary.builder()
        .category(CategoryDto.builder()
            .idCategory(3)
            .description("Transportation")
            .type(CategoryType.EXPENSES)
            .build())
        .transactions(List.of(
            TransactionDto.builder()
                .idTransaction(102)
                .amount(new BigDecimal("50.00"))
                .transactionDate(LocalDate.of(2026, 2, 7))
                .category(CategoryDto.builder()
                    .idCategory(3)
                    .description("Transportation")
                    .type(CategoryType.EXPENSES)
                    .build())
                .comment("Gas station")
                .build()
        ))
        .categoryTotal(new BigDecimal("50.00"))
        .build();

    var response = TransactionsByTypeResponse.builder()
        .categorySummaries(List.of(foodSummary, transportSummary))
        .totalAmount(new BigDecimal("300.50"))
        .build();

    when(transactionService.getTransactionsByTypeAndDateRange(
        CategoryType.EXPENSES,
        LocalDate.of(2026, 2, 1),
        LocalDate.of(2026, 2, 28)))
        .thenReturn(response);

    mockMvc.perform(get(BASE_URL + "/transactions")
            .param("type", "EXPENSES")
            .param("dateFrom", "2026-02-01")
            .param("dateTo", "2026-02-28"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.categorySummaries", hasSize(2)))
        .andExpect(jsonPath("$.categorySummaries[0].category.description", is("Food & Groceries")))
        .andExpect(jsonPath("$.categorySummaries[1].category.description", is("Transportation")))
        .andExpect(jsonPath("$.totalAmount", is(300.50)));
  }

  @Test
  void getTransactions_shouldReturnOkWithEmptyResultWhenNoTransactionsFound() throws Exception {
    var response = TransactionsByTypeResponse.builder()
        .categorySummaries(List.of())
        .totalAmount(BigDecimal.ZERO)
        .build();

    when(transactionService.getTransactionsByTypeAndDateRange(
        CategoryType.INCOMES,
        LocalDate.of(2026, 3, 1),
        LocalDate.of(2026, 3, 31)))
        .thenReturn(response);

    mockMvc.perform(get(BASE_URL + "/transactions")
            .param("type", "INCOMES")
            .param("dateFrom", "2026-03-01")
            .param("dateTo", "2026-03-31"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.categorySummaries", hasSize(0)))
        .andExpect(jsonPath("$.totalAmount", is(0)));
  }

  @Test
  void getTransactions_shouldReturnBadRequestWhenTypeIsMissing() throws Exception {
    mockMvc.perform(get(BASE_URL + "/transactions")
            .param("dateFrom", "2026-02-01")
            .param("dateTo", "2026-02-28"))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(transactionService);
  }

  @Test
  void getTransactions_shouldReturnBadRequestWhenDateFromIsMissing() throws Exception {
    mockMvc.perform(get(BASE_URL + "/transactions")
            .param("type", "INCOMES")
            .param("dateTo", "2026-02-28"))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(transactionService);
  }

  @Test
  void getTransactions_shouldReturnBadRequestWhenDateToIsMissing() throws Exception {
    mockMvc.perform(get(BASE_URL + "/transactions")
            .param("type", "INCOMES")
            .param("dateFrom", "2026-02-01"))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(transactionService);
  }

  @Test
  void getTransactions_shouldReturnBadRequestWhenTypeIsInvalid() throws Exception {
    mockMvc.perform(get(BASE_URL + "/transactions")
            .param("type", "INVALID_TYPE")
            .param("dateFrom", "2026-02-01")
            .param("dateTo", "2026-02-28"))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(transactionService);
  }

  @Test
  void getTransactions_shouldReturnBadRequestWhenDateFormatIsInvalid() throws Exception {
    mockMvc.perform(get(BASE_URL + "/transactions")
            .param("type", "INCOMES")
            .param("dateFrom", "01-02-2026")
            .param("dateTo", "2026-02-28"))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(transactionService);
  }

  // ========== GET /categories ==========

  @Test
  void getAllCategories_shouldReturnOkWithIncomeCategories() throws Exception {
    var categories = List.of(
        CategoryDto.builder()
            .idCategory(1)
            .description("Salary")
            .type(CategoryType.INCOMES)
            .build(),
        CategoryDto.builder()
            .idCategory(8)
            .description("Other Income")
            .type(CategoryType.INCOMES)
            .build()
    );

    when(transactionService.getAllCategories(CategoryType.INCOMES)).thenReturn(categories);

    mockMvc.perform(get(BASE_URL + "/categories")
            .param("type", "INCOMES"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].idCategory", is(1)))
        .andExpect(jsonPath("$[0].description", is("Salary")))
        .andExpect(jsonPath("$[0].type", is("INCOMES")))
        .andExpect(jsonPath("$[1].idCategory", is(8)))
        .andExpect(jsonPath("$[1].description", is("Other Income")))
        .andExpect(jsonPath("$[1].type", is("INCOMES")));

    verify(transactionService).getAllCategories(CategoryType.INCOMES);
  }

  @Test
  void getAllCategories_shouldReturnOkWithExpenseCategories() throws Exception {
    var categories = List.of(
        CategoryDto.builder()
            .idCategory(2)
            .description("Food & Groceries")
            .type(CategoryType.EXPENSES)
            .build(),
        CategoryDto.builder()
            .idCategory(3)
            .description("Transportation")
            .type(CategoryType.EXPENSES)
            .build(),
        CategoryDto.builder()
            .idCategory(4)
            .description("Entertainment")
            .type(CategoryType.EXPENSES)
            .build()
    );

    when(transactionService.getAllCategories(CategoryType.EXPENSES)).thenReturn(categories);

    mockMvc.perform(get(BASE_URL + "/categories")
            .param("type", "EXPENSES"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[0].description", is("Food & Groceries")))
        .andExpect(jsonPath("$[1].description", is("Transportation")))
        .andExpect(jsonPath("$[2].description", is("Entertainment")));

    verify(transactionService).getAllCategories(CategoryType.EXPENSES);
  }

  @Test
  void getAllCategories_shouldReturnOkWithEmptyListWhenNoCategoriesFound() throws Exception {
    when(transactionService.getAllCategories(CategoryType.INCOMES)).thenReturn(List.of());

    mockMvc.perform(get(BASE_URL + "/categories")
            .param("type", "INCOMES"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  void getAllCategories_shouldReturnBadRequestWhenTypeIsMissing() throws Exception {
    mockMvc.perform(get(BASE_URL + "/categories"))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(transactionService);
  }

  @Test
  void getAllCategories_shouldReturnBadRequestWhenTypeIsInvalid() throws Exception {
    mockMvc.perform(get(BASE_URL + "/categories")
            .param("type", "INVALID"))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(transactionService);
  }

  // ========== GET /transactions/export ==========

  @Test
  void exportTransactionsToCsv_shouldReturnCsvFileWithCorrectHeaders() throws Exception {
    var exportData = TransactionExportResponse.builder()
        .transactions(List.of(
            TransactionExportDetail.builder()
                .idTransaction(1)
                .transactionDate("2026-02-01")
                .amount(new BigDecimal("5000.00"))
                .categoryDescription("Salary")
                .categoryType("INCOMES")
                .comment("Monthly salary")
                .build(),
            TransactionExportDetail.builder()
                .idTransaction(2)
                .transactionDate("2026-02-05")
                .amount(new BigDecimal("250.50"))
                .categoryDescription("Food & Groceries")
                .categoryType("EXPENSES")
                .comment("Supermarket shopping")
                .build()
        ))
        .build();

    var csvContent = """
        Transaction ID,Transaction Date,Amount,Category Description,Category Type,Comment
        1,2026-02-01,5000.00,Salary,INCOMES,Monthly salary
        2,2026-02-05,250.50,Food & Groceries,EXPENSES,Supermarket shopping
        """;

    when(transactionService.exportTransactions(
        LocalDate.of(2026, 2, 1),
        LocalDate.of(2026, 2, 28)))
        .thenReturn(exportData);
    when(csvExportService.exportTransactionsToCsv(exportData))
        .thenReturn(csvContent);

    mockMvc.perform(get(BASE_URL + "/transactions/export")
            .param("dateFrom", "2026-02-01")
            .param("dateTo", "2026-02-28"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("text/csv"))
        .andExpect(header().string("Content-Disposition",
            "form-data; name=\"attachment\"; filename=\"transactions_2026-02-01_2026-02-28.csv\""))
        .andExpect(header().string("Cache-Control",
            "must-revalidate, post-check=0, pre-check=0"))
        .andExpect(content().string(csvContent));

    verify(transactionService).exportTransactions(
        LocalDate.of(2026, 2, 1),
        LocalDate.of(2026, 2, 28));
    verify(csvExportService).exportTransactionsToCsv(exportData);
  }

  @Test
  void exportTransactionsToCsv_shouldReturnCsvWithEmptyTransactions() throws Exception {
    var exportData = TransactionExportResponse.builder()
        .transactions(List.of())
        .build();

    var csvContent = "Transaction ID,Transaction Date,Amount,Category Description,Category Type,Comment\n";

    when(transactionService.exportTransactions(
        LocalDate.of(2026, 3, 1),
        LocalDate.of(2026, 3, 31)))
        .thenReturn(exportData);
    when(csvExportService.exportTransactionsToCsv(exportData))
        .thenReturn(csvContent);

    mockMvc.perform(get(BASE_URL + "/transactions/export")
            .param("dateFrom", "2026-03-01")
            .param("dateTo", "2026-03-31"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("text/csv"))
        .andExpect(content().string(csvContent));
  }

  @Test
  void exportTransactionsToCsv_shouldReturnCorrectFilenameInContentDisposition() throws Exception {
    var exportData = TransactionExportResponse.builder()
        .transactions(List.of())
        .build();

    when(transactionService.exportTransactions(
        LocalDate.of(2026, 1, 15),
        LocalDate.of(2026, 6, 30)))
        .thenReturn(exportData);
    when(csvExportService.exportTransactionsToCsv(exportData))
        .thenReturn("header\n");

    mockMvc.perform(get(BASE_URL + "/transactions/export")
            .param("dateFrom", "2026-01-15")
            .param("dateTo", "2026-06-30"))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Disposition",
            "form-data; name=\"attachment\"; filename=\"transactions_2026-01-15_2026-06-30.csv\""));
  }

  @Test
  void exportTransactionsToCsv_shouldReturnBadRequestWhenDateFromIsMissing() throws Exception {
    mockMvc.perform(get(BASE_URL + "/transactions/export")
            .param("dateTo", "2026-02-28"))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(transactionService);
    verifyNoInteractions(csvExportService);
  }

  @Test
  void exportTransactionsToCsv_shouldReturnBadRequestWhenDateToIsMissing() throws Exception {
    mockMvc.perform(get(BASE_URL + "/transactions/export")
            .param("dateFrom", "2026-02-01"))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(transactionService);
    verifyNoInteractions(csvExportService);
  }

  @Test
  void exportTransactionsToCsv_shouldReturnBadRequestWhenDateFormatIsInvalid() throws Exception {
    mockMvc.perform(get(BASE_URL + "/transactions/export")
            .param("dateFrom", "2026/02/01")
            .param("dateTo", "2026-02-28"))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(transactionService);
    verifyNoInteractions(csvExportService);
  }

  @Test
  void exportTransactionsToCsv_shouldPropagateExceptionWhenServiceThrows() {
    when(transactionService.exportTransactions(
        LocalDate.of(2026, 2, 1),
        LocalDate.of(2026, 2, 28)))
        .thenThrow(new IllegalArgumentException("Date from must be before or equal to date to"));

    var exception = assertThrows(Exception.class, () ->
        mockMvc.perform(get(BASE_URL + "/transactions/export")
            .param("dateFrom", "2026-02-01")
            .param("dateTo", "2026-02-28")));

    assertThat(exception).rootCause()
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Date from must be before or equal to date to");
  }
}
