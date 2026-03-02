package com.ys.ai.aifinancemanager.mcp.client;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Slf4j
@Component
public class McpDatawrapperClient {

  private static final Pattern EMBED_URL_PATTERN = Pattern.compile(
      "https://datawrapper\\.dwcdn\\.net/[\\w]+/?\\d*");
  private static final Pattern CHART_ID_PATTERN = Pattern.compile(
      "\\b([A-Za-z0-9]{5,8})\\b");
  private static final Pattern CHART_ID_IN_PARENS_PATTERN = Pattern.compile(
      "\\(([A-Za-z0-9]{5,8})\\)");
  private static final Pattern CHART_ID_LABELED_PATTERN = Pattern.compile(
      "(?:chart[_ ]?id|id)[:\\s]+['\"]?([A-Za-z0-9]{5,8})['\"]?", Pattern.CASE_INSENSITIVE);

  private static final List<String> CHART_ID_JSON_KEYS = List.of("\"id\"", "\"chart_id\"", "\"chartId\"");
  private static final List<String> URL_JSON_KEYS = List.of("\"url\"", "\"publicUrl\"", "\"embed_url\"", "\"embedUrl\"", "\"public_url\"");

  private static final String LAST_CHART_ID_KEY = "lastChartId";

  private final McpSyncClient mcpSyncClient;
  private final Map<String, String> chartCache = new ConcurrentHashMap<>();

  public McpDatawrapperClient(McpSyncClient mcpSyncClient) {
    this.mcpSyncClient = mcpSyncClient;
    log.info("MCP Datawrapper client initialized");
  }

  public String createChart(String chartType, String title, List<Map<String, Object>> data) {
    try {
      var arguments = Map.of(
          "chart_type", chartType,
          "data", data,
          "chart_config", Map.of("title", title)
      );
      CallToolResult result = callTool("create_chart", arguments);
      if (result != null) {
        String chartId = extractChartId(result);
        if (chartId != null) {
          chartCache.put(LAST_CHART_ID_KEY, chartId);
          log.info("MCP: Chart created with ID: {}", chartId);
        }
        return chartId;
      }
    } catch (Exception e) {
      log.error("MCP: Failed to create chart: {}", e.getMessage(), e);
    }
    return null;
  }

  public boolean updateChart(String chartId, String title, List<Map<String, Object>> data) {
    try {
      var arguments = Map.of(
          "chart_id", chartId,
          "data", data,
          "chart_config", Map.of("title", title)
      );
      CallToolResult result = callTool("update_chart", arguments);
      if (result != null && Boolean.TRUE.equals(result.isError())) {
        logMcpErrorContent(result);
        return false;
      }
      return result != null;
    } catch (Exception e) {
      log.error("MCP: Failed to update chart {}: {}", chartId, e.getMessage(), e);
    }
    return false;
  }

  public String publishChart(String chartId) {
    try {
      CallToolResult result = callTool("publish_chart", Map.of("chart_id", chartId));
      if (result != null) {
        return extractEmbedUrl(result);
      }
    } catch (Exception e) {
      log.error("MCP: Failed to publish chart {}: {}", chartId, e.getMessage(), e);
    }
    return null;
  }

  public String getLastChartId() {
    return chartCache.get(LAST_CHART_ID_KEY);
  }

  @PreDestroy
  public void cleanup() {
    try {
      mcpSyncClient.close();
      log.info("MCP sync client closed");
    } catch (Exception e) {
      log.warn("Error closing MCP sync client: {}", e.getMessage());
    }
  }

  // ─── Response parsing helpers (package-private for testing) ────────────

  String extractChartId(CallToolResult result) {
    log.debug("MCP: Extracting chart ID from result");

    if (Boolean.TRUE.equals(result.isError())) {
      logMcpErrorContent(result);
      return null;
    }

    List<McpSchema.Content> contents = result.content();
    if (contents == null || contents.isEmpty()) {
      log.warn("MCP: Result has no content");
      return null;
    }

    return contents.stream()
        .filter(TextContent.class::isInstance)
        .map(TextContent.class::cast)
        .peek(tc -> log.debug("MCP: Content text element: {}", tc.text()))
        .map(TextContent::text)
        .filter(text -> !text.startsWith("Error"))
        .map(text -> parseJsonValue(text, CHART_ID_JSON_KEYS)
            .or(() -> parseChartIdFromText(text)))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .peek(id -> log.debug("MCP: Extracted chart ID: {}", id))
        .findFirst()
        .orElseGet(() -> {
          log.warn("MCP: Could not extract chart ID from any content element");
          return null;
        });
  }

  String extractEmbedUrl(CallToolResult result) {
    if (Boolean.TRUE.equals(result.isError())) {
      logMcpErrorContent(result);
      return null;
    }

    List<McpSchema.Content> contents = result.content();
    if (contents == null || contents.isEmpty()) {
      return null;
    }

    return contents.stream()
        .filter(TextContent.class::isInstance)
        .map(c -> ((TextContent) c).text())
        .map(text -> parseUrlFromText(text)
            .or(() -> parseJsonValue(text, URL_JSON_KEYS)))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst()
        .orElse(null);
  }

  private CallToolResult callTool(String toolName, Map<String, Object> arguments) {
    return mcpSyncClient.callTool(new McpSchema.CallToolRequest(toolName, arguments));
  }

  private void logMcpErrorContent(CallToolResult result) {
    List<McpSchema.Content> contents = result.content();
    if (contents == null || contents.isEmpty()) {
      log.error("MCP tool returned isError=true with no content");
      return;
    }
    contents.stream()
        .filter(TextContent.class::isInstance)
        .map(TextContent.class::cast)
        .forEach(tc -> log.error("MCP tool error: {}", tc.text()));
  }

  private Optional<String> parseChartIdFromText(String text) {
    var labeledMatcher = CHART_ID_LABELED_PATTERN.matcher(text);
    if (labeledMatcher.find()) {
      return Optional.of(labeledMatcher.group(1));
    }

    var urlMatcher = EMBED_URL_PATTERN.matcher(text);
    if (urlMatcher.find()) {
      Optional<String> idFromUrl = Arrays.stream(urlMatcher.group().split("/"))
          .filter(s -> !s.isEmpty() && s.matches("[A-Za-z0-9]+"))
          .reduce((first, second) -> second);
      if (idFromUrl.isPresent()) {
        return idFromUrl;
      }
    }

    var parensMatcher = CHART_ID_IN_PARENS_PATTERN.matcher(text);
    if (parensMatcher.find()) {
      return Optional.of(parensMatcher.group(1));
    }

    var idMatcher = CHART_ID_PATTERN.matcher(text);
    while (idMatcher.find()) {
      String candidate = idMatcher.group(1);
      if (looksLikeChartId(candidate)) {
        return Optional.of(candidate);
      }
    }
    return Optional.empty();
  }

  private boolean looksLikeChartId(String candidate) {
    if (candidate == null || candidate.length() < 5 || candidate.length() > 8) {
      return false;
    }
    boolean hasLetter = candidate.chars().anyMatch(Character::isLetter);
    boolean hasDigit = candidate.chars().anyMatch(Character::isDigit);
    return !(hasLetter && !hasDigit);
  }

  private Optional<String> parseJsonValue(String text, List<String> keys) {
    if (!text.trim().startsWith("{")) {
      return Optional.empty();
    }
    try {
      return keys.stream()
          .map(key -> extractJsonStringValue(text, key))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .findFirst();
    } catch (Exception ignored) {
      return Optional.empty();
    }
  }

  private Optional<String> extractJsonStringValue(String json, String key) {
    int keyIdx = json.indexOf(key);
    if (keyIdx < 0) {
      return Optional.empty();
    }
    int colonIdx = json.indexOf(':', keyIdx + key.length());
    if (colonIdx < 0) {
      return Optional.empty();
    }
    String rest = json.substring(colonIdx + 1).trim();
    if (!rest.startsWith("\"")) {
      return Optional.empty();
    }
    int endQuote = rest.indexOf('"', 1);
    return endQuote > 1 ? Optional.of(rest.substring(1, endQuote)) : Optional.empty();
  }

  private Optional<String> parseUrlFromText(String text) {
    if (!text.contains("datawrapper.dwcdn.net")) {
      return Optional.empty();
    }
    var matcher = EMBED_URL_PATTERN.matcher(text);
    return matcher.find() ? Optional.of(matcher.group()) : Optional.empty();
  }
}
