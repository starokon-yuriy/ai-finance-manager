package com.ys.ai.aifinancemanager.application.service;

import com.ys.ai.aifinancemanager.application.dto.TransactionExportResponse;

public interface CsvExportService {

  String exportTransactionsToCsv(TransactionExportResponse exportData);
}

