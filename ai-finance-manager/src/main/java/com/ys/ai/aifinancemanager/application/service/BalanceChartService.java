package com.ys.ai.aifinancemanager.application.service;

import com.ys.ai.aifinancemanager.application.dto.BalanceChartDataResponse;

import java.time.LocalDate;

public interface BalanceChartService {

  BalanceChartDataResponse getBalanceChartData(LocalDate dateFrom, LocalDate dateTo);
}

