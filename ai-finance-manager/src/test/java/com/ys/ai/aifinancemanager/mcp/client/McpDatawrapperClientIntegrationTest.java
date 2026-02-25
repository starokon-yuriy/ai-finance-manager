package com.ys.ai.aifinancemanager.mcp.client;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class McpDatawrapperClientIntegrationTest {

  @Mock
  private McpSyncClient mcpSyncClient;

  private static final List<Map<String, Object>> SAMPLE_DATA = List.of(
      Map.of("Date", "2026-01-01", "Value", 100)
  );

  private static final List<Map<String, Object>> UPDATED_DATA = List.of(
      Map.of("Date", "2026-01-01", "Value", 200),
      Map.of("Date", "2026-01-02", "Value", 300)
  );

  @Test
  void shouldPerformFullChartWorkflow_createUpdatePublish() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient);

    var createResult = new CallToolResult(
        List.of(new TextContent("{\"id\": \"Wf1234\"}")),
        false
    );
    var updateResult = new CallToolResult(
        List.of(new TextContent("Chart updated")),
        false
    );
    var publishResult = new CallToolResult(
        List.of(new TextContent("Published: https://datawrapper.dwcdn.net/Wf1234/")),
        false
    );

    when(mcpSyncClient.callTool(any(io.modelcontextprotocol.spec.McpSchema.CallToolRequest.class)))
        .thenReturn(createResult)
        .thenReturn(updateResult)
        .thenReturn(publishResult);

    // when — create
    String chartId = client.createChart("line", "Balance Chart", SAMPLE_DATA);

    // then — chart created
    assertThat(chartId).isEqualTo("Wf1234");
    assertThat(client.getLastChartId()).isEqualTo("Wf1234");

    // when — update
    boolean updated = client.updateChart(chartId, UPDATED_DATA);

    // then — chart updated
    assertThat(updated).isTrue();

    // when — publish
    String embedUrl = client.publishChart(chartId);

    // then — chart published with embed URL
    assertThat(embedUrl).isEqualTo("https://datawrapper.dwcdn.net/Wf1234/");
    Mockito.verify(mcpSyncClient, Mockito.times(3)).callTool(any(io.modelcontextprotocol.spec.McpSchema.CallToolRequest.class));
  }

  @Test
  void shouldHandleWorkflowFailureGracefully() {
    // given
    var client = new McpDatawrapperClient(mcpSyncClient);

    when(mcpSyncClient.callTool(any(io.modelcontextprotocol.spec.McpSchema.CallToolRequest.class)))
        .thenThrow(new RuntimeException("Server unreachable"));

    // when
    String chartId = client.createChart("line", "Test", SAMPLE_DATA);

    // then
    assertThat(chartId).isNull();
    assertThat(client.getLastChartId()).isNull();
  }

}
