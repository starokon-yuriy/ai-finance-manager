package com.ys.ai.aifinancemanager.mcp.client;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * Diagnostic test to list tools exposed by the datawrapper-mcp server.
 * Disabled by default — run manually to inspect the schema.
 */
class McpToolDiscoveryTest {

  @Test
  @Disabled("Manual diagnostic — requires running datawrapper-mcp server")
  void listAvailableTools() throws Exception {
    ServerParameters serverParams = ServerParameters.builder("/Users/ystaro/.local/bin/datawrapper-mcp")
        .build();
    var transport = new StdioClientTransport(serverParams);
    McpSyncClient client = McpClient.sync(transport)
        .clientInfo(new McpSchema.Implementation("tool-lister", "1.0.0"))
        .requestTimeout(Duration.ofSeconds(15))
        .build();
    client.initialize();

    var toolsResult = client.listTools();
    for (var tool : toolsResult.tools()) {
      System.out.println("=== Tool: " + tool.name() + " ===");
      System.out.println("Description: " + tool.description());
      System.out.println("Schema: " + tool.inputSchema());
      System.out.println();
    }

    client.close();
  }
}

