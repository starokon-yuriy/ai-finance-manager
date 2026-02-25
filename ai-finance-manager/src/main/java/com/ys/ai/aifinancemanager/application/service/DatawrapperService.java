package com.ys.ai.aifinancemanager.application.service;

import com.ys.ai.aifinancemanager.application.dto.BalanceChartDataResponse.BalanceDataPoint;

import java.time.LocalDate;
import java.util.List;

public interface DatawrapperService {

  String createOrUpdateChart(List<BalanceDataPoint> data, LocalDate dateFrom, LocalDate dateTo);
}

