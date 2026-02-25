package com.ys.ai.aifinancemanager.mcp.client;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link McpDatawrapperClient} using mocked {@link McpSyncClient}.
 * Tests cover: disabled state, successful chart operations, error handling, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
class McpDatawrapperClientTest {

  @Mock
  private McpSyncClient mcpSyncClient;

  private static final List<Map<String, Object>> SAMPLE_DATA = List.of(
      Map.of("Date", "2026-01-01", "Income", 100, "Expense", 50, "Balance", 50)
  );

  private static final List<Map<String, Object>> SAMPLE_DATA_TWO_ROWS = List.of(
      Map.of("Date", "2026-01-01", "Value", 100),
      Map.of("Date", "2026-01-02", "Value", 200)
  );

  // ─── Disabled state tests ─────────────────────────────────────────────

  @Test
  void shouldReturnNullWhenDisabled() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, false);

    // when/then
    assertThat(client.isEnabled()).isFalse();
    assertThat(client.createChart("line", "Test", SAMPLE_DATA)).isNull();
    assertThat(client.publishChart("abc123")).isNull();
    assertThat(client.updateChart("abc123", SAMPLE_DATA)).isFalse();
  }

  @Test
  void shouldReportEnabledState() {
    // given
    var enabledClient = new McpDatawrapperClient(mcpSyncClient, true);
    var disabledClient = new McpDatawrapperClient(mcpSyncClient, false);

    // then
    assertThat(enabledClient.isEnabled()).isTrue();
    assertThat(disabledClient.isEnabled()).isFalse();
  }

  @Test
  void shouldReturnNullChartIdWhenNoChartCreated() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, false);

    // then
    assertThat(client.getLastChartId()).isNull();
  }

  // ─── Successful chart creation tests ──────────────────────────────────

  @Test
  void createChart_shouldReturnChartIdFromJsonResponse() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, true);
    var result = new CallToolResult(
        List.of(new TextContent("{\"id\": \"Ab12Cd3\"}")),
        false
    );
    when(mcpSyncClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(result);

    // when
    String chartId = client.createChart("line", "Balance Chart", SAMPLE_DATA);

    // then
    assertThat(chartId).isEqualTo("Ab12Cd3");
    assertThat(client.getLastChartId()).isEqualTo("Ab12Cd3");
    verify(mcpSyncClient).callTool(any(McpSchema.CallToolRequest.class));
  }

  @Test
  void createChart_shouldReturnChartIdFromTextResponse() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, true);
    var result = new CallToolResult(
        List.of(new TextContent("Chart created successfully with chart_id: Xy45Mn6")),
        false
    );
    when(mcpSyncClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(result);

    // when
    String chartId = client.createChart("line", "Test", SAMPLE_DATA);

    // then
    assertThat(chartId).isEqualTo("Xy45Mn6");
    verify(mcpSyncClient).callTool(any(McpSchema.CallToolRequest.class));
  }

  @Test
  void createChart_shouldPassCorrectArguments() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, true);
    var result = new CallToolResult(
        List.of(new TextContent("{\"id\": \"test1\"}")),
        false
    );
    when(mcpSyncClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(result);

    // when
    client.createChart("bar", "My Chart", SAMPLE_DATA);

    // then
    var captor = ArgumentCaptor.forClass(McpSchema.CallToolRequest.class);
    verify(mcpSyncClient).callTool(captor.capture());

    McpSchema.CallToolRequest request = captor.getValue();
    assertThat(request.name()).isEqualTo("create_chart");
    assertThat(request.arguments()).containsEntry("chart_type", "bar");
    assertThat(request.arguments()).containsEntry("data", SAMPLE_DATA);
    assertThat(request.arguments()).containsKey("chart_config");

    @SuppressWarnings("unchecked")
    Map<String, Object> chartConfig = (Map<String, Object>) request.arguments().get("chart_config");
    assertThat(chartConfig).containsEntry("title", "My Chart");
  }

  @Test
  void createChart_shouldExtractChartIdFromUrl() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, true);
    var result = new CallToolResult(
        List.of(new TextContent("Published at https://datawrapper.dwcdn.net/Ab1c2D/")),
        false
    );
    when(mcpSyncClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(result);

    // when
    String chartId = client.createChart("line", "Test", SAMPLE_DATA);

    // then
    assertThat(chartId).isEqualTo("Ab1c2D");
    verify(mcpSyncClient).callTool(any(McpSchema.CallToolRequest.class));
  }

  @Test
  void createChart_shouldExtractChartIdFromParentheses() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, true);
    var result = new CallToolResult(
        List.of(new TextContent("Chart created (Kx3Lm9)")),
        false
    );
    when(mcpSyncClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(result);

    // when
    String chartId = client.createChart("line", "Test", SAMPLE_DATA);

    // then
    assertThat(chartId).isEqualTo("Kx3Lm9");
    verify(mcpSyncClient).callTool(any(McpSchema.CallToolRequest.class));
  }

  // ─── Update chart tests ───────────────────────────────────────────────

  @Test
  void updateChart_shouldReturnTrueOnSuccess() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, true);
    var result = new CallToolResult(
        List.of(new TextContent("Chart updated successfully")),
        false
    );
    when(mcpSyncClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(result);

    // when
    boolean updated = client.updateChart("abc123", SAMPLE_DATA);

    // then
    assertThat(updated).isTrue();
    var captor = ArgumentCaptor.forClass(McpSchema.CallToolRequest.class);
    verify(mcpSyncClient).callTool(captor.capture());
    assertThat(captor.getValue().name()).isEqualTo("update_chart");
    assertThat(captor.getValue().arguments()).containsEntry("chart_id", "abc123");
    assertThat(captor.getValue().arguments()).containsEntry("data", SAMPLE_DATA);
  }

  @Test
  void updateChart_shouldReturnFalseOnErrorResult() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, true);
    var result = new CallToolResult(
        List.of(new TextContent("Chart not found")),
        true
    );
    when(mcpSyncClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(result);

    // when
    boolean updated = client.updateChart("nonexistent", SAMPLE_DATA);

    // then
    assertThat(updated).isFalse();
    verify(mcpSyncClient).callTool(any(McpSchema.CallToolRequest.class));
  }

  @Test
  void updateChart_shouldReturnFalseOnNullResult() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, true);
    when(mcpSyncClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(null);

    // when
    boolean updated = client.updateChart("abc123", SAMPLE_DATA);

    // then
    assertThat(updated).isFalse();
    verify(mcpSyncClient).callTool(any(McpSchema.CallToolRequest.class));
  }

  // ─── Publish chart tests ──────��───────────────────────────────────────

  @Test
  void publishChart_shouldReturnEmbedUrl() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, true);
    var result = new CallToolResult(
        List.of(new TextContent("Published: https://datawrapper.dwcdn.net/Ab1c2D/")),
        false
    );
    when(mcpSyncClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(result);

    // when
    String url = client.publishChart("Ab1c2D");

    // then
    assertThat(url).isEqualTo("https://datawrapper.dwcdn.net/Ab1c2D/");
    var captor = ArgumentCaptor.forClass(McpSchema.CallToolRequest.class);
    verify(mcpSyncClient).callTool(captor.capture());
    assertThat(captor.getValue().name()).isEqualTo("publish_chart");
    assertThat(captor.getValue().arguments()).containsEntry("chart_id", "Ab1c2D");
  }

  @Test
  void publishChart_shouldReturnUrlFromJsonResponse() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, true);
    var result = new CallToolResult(
        List.of(new TextContent("{\"publicUrl\": \"https://datawrapper.dwcdn.net/Xy9Mn1/\"}")),
        false
    );
    when(mcpSyncClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(result);

    // when
    String url = client.publishChart("Xy9Mn1");

    // then
    assertThat(url).isEqualTo("https://datawrapper.dwcdn.net/Xy9Mn1/");
    verify(mcpSyncClient).callTool(any(McpSchema.CallToolRequest.class));
  }

  @Test
  void publishChart_shouldReturnNullOnError() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, true);
    var result = new CallToolResult(
        List.of(new TextContent("Publish failed")),
        true
    );
    when(mcpSyncClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(result);

    // when
    String url = client.publishChart("abc123");

    // then
    assertThat(url).isNull();
    verify(mcpSyncClient).callTool(any(McpSchema.CallToolRequest.class));
  }

  @Test
  void publishChart_shouldReturnNullOnNullResult() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, true);
    when(mcpSyncClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(null);

    // when
    String url = client.publishChart("abc123");

    // then
    assertThat(url).isNull();
    verify(mcpSyncClient).callTool(any(McpSchema.CallToolRequest.class));
  }

  // ─── Error handling tests ─────────────────────────────────────────────

  @Test
  void createChart_shouldReturnNullOnException() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, true);
    when(mcpSyncClient.callTool(any(McpSchema.CallToolRequest.class)))
        .thenThrow(new RuntimeException("Connection refused"));

    // when
    String chartId = client.createChart("line", "Test", SAMPLE_DATA);

    // then
    assertThat(chartId).isNull();
    verify(mcpSyncClient).callTool(any(McpSchema.CallToolRequest.class));
  }

  @Test
  void updateChart_shouldReturnFalseOnException() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, true);
    when(mcpSyncClient.callTool(any(McpSchema.CallToolRequest.class)))
        .thenThrow(new RuntimeException("Timeout"));

    // when
    boolean updated = client.updateChart("abc123", SAMPLE_DATA);

    // then
    assertThat(updated).isFalse();
    verify(mcpSyncClient).callTool(any(McpSchema.CallToolRequest.class));
  }

  @Test
  void publishChart_shouldReturnNullOnException() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, true);
    when(mcpSyncClient.callTool(any(McpSchema.CallToolRequest.class)))
        .thenThrow(new RuntimeException("Server error"));

    // when
    String url = client.publishChart("abc123");

    // then
    assertThat(url).isNull();
    verify(mcpSyncClient).callTool(any(McpSchema.CallToolRequest.class));
  }

  @Test
  void createChart_shouldReturnNullWhenErrorInContentText() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, true);
    var result = new CallToolResult(
        List.of(new TextContent("Error: Unauthorized access")),
        false
    );
    when(mcpSyncClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(result);

    // when
    String chartId = client.createChart("line", "Test", SAMPLE_DATA);

    // then
    assertThat(chartId).isNull();
    verify(mcpSyncClient).callTool(any(McpSchema.CallToolRequest.class));
  }

  @Test
  void createChart_shouldReturnNullWhenNoContent() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, true);
    var result = new CallToolResult(List.of(), false);
    when(mcpSyncClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(result);

    // when
    String chartId = client.createChart("line", "Test", SAMPLE_DATA);

    // then
    assertThat(chartId).isNull();
    verify(mcpSyncClient).callTool(any(McpSchema.CallToolRequest.class));
  }

  @Test
  void createChart_shouldReturnNullWhenNullResult() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, true);
    when(mcpSyncClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(null);

    // when
    String chartId = client.createChart("line", "Test", SAMPLE_DATA);

    // then
    assertThat(chartId).isNull();
    verify(mcpSyncClient).callTool(any(McpSchema.CallToolRequest.class));
  }

  // ─── Edge cases ───────────────────────────────────────────────────────

  @Test
  void createChart_shouldHandleEmptyData() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, true);
    var result = new CallToolResult(
        List.of(new TextContent("{\"id\": \"Test1\"}")),
        false
    );
    when(mcpSyncClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(result);

    // when
    String chartId = client.createChart("line", "Test", Collections.emptyList());

    // then
    assertThat(chartId).isEqualTo("Test1");
    var captor = ArgumentCaptor.forClass(McpSchema.CallToolRequest.class);
    verify(mcpSyncClient).callTool(captor.capture());
    assertThat(captor.getValue().arguments()).containsEntry("data", Collections.emptyList());
  }

  @Test
  void publishChart_shouldReturnNullWhenNoUrlInContent() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, true);
    var result = new CallToolResult(
        List.of(new TextContent("Chart published but no URL returned")),
        false
    );
    when(mcpSyncClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(result);

    // when
    String url = client.publishChart("abc123");

    // then
    assertThat(url).isNull();
    verify(mcpSyncClient).callTool(any(McpSchema.CallToolRequest.class));
  }

  @Test
  void createChart_shouldNotCallMcpServerWhenDisabled() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, false);

    // when
    client.createChart("line", "Test", SAMPLE_DATA);

    // then
    verifyNoInteractions(mcpSyncClient);
  }

  @Test
  void updateChart_shouldNotCallMcpServerWhenDisabled() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, false);

    // when
    client.updateChart("abc123", SAMPLE_DATA);

    // then
    verifyNoInteractions(mcpSyncClient);
  }

  @Test
  void publishChart_shouldNotCallMcpServerWhenDisabled() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, false);

    // when
    client.publishChart("abc123");

    // then
    verifyNoInteractions(mcpSyncClient);
  }

  @Test
  void createChart_shouldCacheLastChartId() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, true);
    var result1 = new CallToolResult(
        List.of(new TextContent("{\"id\": \"First1\"}")),
        false
    );
    when(mcpSyncClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(result1);

    // when
    client.createChart("line", "Chart 1", SAMPLE_DATA);

    // then
    assertThat(client.getLastChartId()).isEqualTo("First1");

    // given — second chart should update cache
    var result2 = new CallToolResult(
        List.of(new TextContent("{\"id\": \"Sec2nd\"}")),
        false
    );
    when(mcpSyncClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(result2);

    // when
    client.createChart("bar", "Chart 2", SAMPLE_DATA_TWO_ROWS);

    // then
    assertThat(client.getLastChartId()).isEqualTo("Sec2nd");
    verify(mcpSyncClient, times(2)).callTool(any(McpSchema.CallToolRequest.class));
  }

  // ─── extractChartId / extractEmbedUrl direct tests ────────────────────

  @Test
  void extractChartId_shouldReturnNullForErrorResult() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, false);
    var errorResult = new CallToolResult(
        List.of(new TextContent("Something went wrong")),
        true
    );

    // when
    String chartId = client.extractChartId(errorResult);

    // then
    assertThat(chartId).isNull();
  }

  @Test
  void extractEmbedUrl_shouldReturnNullForErrorResult() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, false);
    var errorResult = new CallToolResult(
        List.of(new TextContent("Error")),
        true
    );

    // when
    String url = client.extractEmbedUrl(errorResult);

    // then
    assertThat(url).isNull();
  }

  @Test
  void extractEmbedUrl_shouldReturnNullForEmptyContent() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, false);
    var emptyResult = new CallToolResult(List.of(), false);

    // when
    String url = client.extractEmbedUrl(emptyResult);

    // then
    assertThat(url).isNull();
  }

  @Test
  void extractChartId_shouldHandleChartIdKey() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, false);
    var result = new CallToolResult(
        List.of(new TextContent("{\"chart_id\": \"XyZ123\"}")),
        false
    );

    // when
    String chartId = client.extractChartId(result);

    // then
    assertThat(chartId).isEqualTo("XyZ123");
  }

  @Test
  void extractChartId_shouldHandleChartIdCamelCase() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient, false);
    var result = new CallToolResult(
        List.of(new TextContent("{\"chartId\": \"Abc456\"}")),
        false
    );

    // when
    String chartId = client.extractChartId(result);

    // then
    assertThat(chartId).isEqualTo("Abc456");
  }
}
