package com.ys.ai.aifinancemanager.application.service;

import com.ys.ai.aifinancemanager.application.dto.BalanceChartDataResponse.BalanceDataPoint;
import com.ys.ai.aifinancemanager.mcp.client.McpDatawrapperClient;
import com.ys.ai.aifinancemanager.mcp.service.AgentLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatawrapperServiceImpl implements DatawrapperService {

  public static final String CHART_TYPE = "line";
  public static final String CHART_TITLE = "Income vs Expenses Balance";

  public static final String DATAWRAPPER_URL = "https://datawrapper.dwcdn.net/";

  private final McpDatawrapperClient mcpClient;
  private final AgentLogService agentLogService;

  private String cachedChartId;

  @Override
  public String createOrUpdateChart(List<BalanceDataPoint> data,
                                    LocalDate dateFrom, LocalDate dateTo) {
    if (data == null || data.isEmpty()) {
      log.debug("No data points to chart");
      return null;
    }

    var fullTitle = "%s (%s to %s)".formatted(CHART_TITLE, dateFrom, dateTo);
    var records = convertToRecords(data);

    try {
      String chartId = resolveChartId(fullTitle, records);
      if (chartId == null) {
        return null;
      }
      return publishAndLog(chartId, fullTitle, data);
    } catch (Exception e) {
      log.error("Error creating/updating Datawrapper chart: {}", e.getMessage(), e);
      logChartInteraction(fullTitle, data.size(), "Error: " + e.getMessage(),
          false, "Chart creation failed: " + e.getMessage());
      return null;
    }
  }

  private String resolveChartId(String fullTitle, List<Map<String, Object>> records) {
    if (cachedChartId != null) {
      log.info("Updating existing Datawrapper chart: {}", cachedChartId);
      boolean updated = mcpClient.updateChart(cachedChartId, records);
      if (updated) {
        return cachedChartId;
      }
      log.warn("Failed to update chart {}, creating new one", cachedChartId);
    }

    log.info("Creating new Datawrapper chart: {}", fullTitle);
    String chartId = mcpClient.createChart(CHART_TYPE, fullTitle, records);
    if (chartId == null) {
      log.warn("Failed to create chart via MCP");
      return null;
    }
    cachedChartId = chartId;
    return chartId;
  }

  private String publishAndLog(String chartId, String fullTitle, List<BalanceDataPoint> data) {
    String embedUrl = mcpClient.publishChart(chartId);
    if (embedUrl != null) {
      log.info("Chart published successfully. Embed URL: {}", embedUrl);
      logChartInteraction(fullTitle, data.size(),
          "Chart ID: " + chartId + ", Embed URL: " + embedUrl,
          true, "Chart created and published successfully via MCP");
      return embedUrl;
    }

    var fallbackUrl = DATAWRAPPER_URL + chartId + "/";
    log.info("Using fallback embed URL: {}", fallbackUrl);
    logChartInteraction(fullTitle, data.size(),
        "Chart ID: " + chartId + ", Fallback URL: " + fallbackUrl,
        true, "Chart created, using fallback embed URL");
    return fallbackUrl;
  }

  private void logChartInteraction(String fullTitle, int dataPointCount,
                                   String output, boolean success, String summary) {
    agentLogService.logInteraction("createOrUpdateChart",
        "Create/update chart: " + fullTitle,
        "Data points: " + dataPointCount,
        output, success, summary);
  }

  private List<Map<String, Object>> convertToRecords(List<BalanceDataPoint> data) {
    return data.stream()
        .map(point -> {
          var dataRecord = new LinkedHashMap<String, Object>();
          dataRecord.put("Date", point.getDate());
          dataRecord.put("Income", point.getIncome());
          dataRecord.put("Expense", point.getExpense());
          dataRecord.put("Balance", point.getBalance());
          return (Map<String, Object>) dataRecord;
        })
        .toList();
  }
}

