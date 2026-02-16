package com.ys.ai.aifinancemanager.application.service;

import com.ys.ai.aifinancemanager.application.dto.TransactionExportResponse;
import com.ys.ai.aifinancemanager.application.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class CsvExportServiceImpl implements CsvExportService {

  @Override
  public String exportTransactionsToCsv(TransactionExportResponse exportData) {
    ValidationUtils.validateExportData(exportData);

    log.info("Generating CSV content for {} transactions",
        exportData.getTransactions().size());

    var csv = new StringBuilder();

    csv.append("Transaction ID,Transaction Date,Amount,Category Description,Category Type,Comment\n");

    exportData.getTransactions().forEach(transaction -> {
      csv.append(escapeCsv(String.valueOf(transaction.getIdTransaction()))).append(",");
      csv.append(escapeCsv(transaction.getTransactionDate())).append(",");
      csv.append(escapeCsv(String.valueOf(transaction.getAmount()))).append(",");
      csv.append(escapeCsv(transaction.getCategoryDescription())).append(",");
      csv.append(escapeCsv(transaction.getCategoryType())).append(",");
      csv.append(escapeCsv(transaction.getComment()));
      csv.append("\n");
    });

    log.info("CSV content generated successfully");

    return csv.toString();
  }

  private String escapeCsv(String value) {
    if (value == null) {
      return "";
    }

    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    return value;
  }
}

