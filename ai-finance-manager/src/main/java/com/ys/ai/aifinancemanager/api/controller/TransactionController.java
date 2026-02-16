package com.ys.ai.aifinancemanager.api.controller;

import com.ys.ai.aifinancemanager.application.dto.CategoryDto;
import com.ys.ai.aifinancemanager.application.dto.CreateTransactionRequest;
import com.ys.ai.aifinancemanager.application.dto.TransactionDto;
import com.ys.ai.aifinancemanager.application.dto.TransactionsByTypeResponse;
import com.ys.ai.aifinancemanager.application.service.CsvExportService;
import com.ys.ai.aifinancemanager.application.service.TransactionService;
import com.ys.ai.aifinancemanager.domain.entity.CategoryType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/finance")
@RequiredArgsConstructor
public class TransactionController {

  private final TransactionService transactionService;

  private final CsvExportService csvExportService;

  @PostMapping("/transactions")
  public ResponseEntity<TransactionDto> addTransaction(@RequestBody CreateTransactionRequest request) {
    log.info("REST request to add transaction: {}", request);
    var result = transactionService.addTransaction(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @GetMapping("/transactions")
  public ResponseEntity<TransactionsByTypeResponse> getTransactions(
      @RequestParam CategoryType type,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
    log.info("REST request to get {} transactions between {} and {}", type, dateFrom, dateTo);
    var result = transactionService.getTransactionsByTypeAndDateRange(type, dateFrom, dateTo);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/categories")
  public ResponseEntity<List<CategoryDto>> getAllCategories(@RequestParam CategoryType type) {
    log.info("REST request to get categories of type: {}", type);
    var result = transactionService.getAllCategories(type);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/transactions/export")
  public ResponseEntity<String> exportTransactionsToCsv(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
    log.info("REST request to export transactions to CSV between {} and {}", dateFrom, dateTo);

    var exportData = transactionService.exportTransactions(dateFrom, dateTo);
    var csvContent = csvExportService.exportTransactionsToCsv(exportData);

    var filename = String.format("transactions_%s_%s.csv",
        dateFrom.format(DateTimeFormatter.ISO_DATE),
        dateTo.format(DateTimeFormatter.ISO_DATE));

    var headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("text/csv"));
    headers.setContentDispositionFormData("attachment", filename);
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

    log.info("Exporting CSV file: {}", filename);

    return new ResponseEntity<>(csvContent, headers, HttpStatus.OK);
  }
}
