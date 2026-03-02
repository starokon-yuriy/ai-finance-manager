package com.ys.ai.aifinancemanager.mcp.client;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema.Implementation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;


@Slf4j
@Configuration
public class McpClientConfiguration {

  @Value("${mcp.client.command:/usr/local/bin/datawrapper-mcp}")
  private String mcpCommand;

  @Value("${mcp.client.enabled:false}")
  private boolean enabled;

  @Value("${datawrapper.access-token:}")
  private String accessToken;

  @Bean
  public McpSyncClient mcpSyncClient() {
    if (!enabled) {
      log.info("MCP client is disabled — creating no-op MCP sync client");
      return createNoOpClient();
    }

    log.info("Creating MCP sync client with stdio transport. Command: {}", mcpCommand);

    String resolvedToken = resolveAccessToken();

    // Build the server parameters with environment variables for the child process
    var serverParams = ServerParameters.builder(mcpCommand)
        .addEnvVar("DATAWRAPPER_ACCESS_TOKEN", resolvedToken != null ? resolvedToken : "")
        .build();

    // Create stdio transport — handles process spawning and JSON-RPC framing
    var transport = new StdioClientTransport(serverParams);

    // Build and initialize the sync client
    var client = McpClient.sync(transport)
        .clientInfo(new Implementation("ai-finance-manager", "1.0.0"))
        .initializationTimeout(Duration.ofSeconds(1))
        .build();

    try {
      client.initialize();
      log.info("MCP sync client initialized successfully");
    } catch (Exception e) {
      log.error("Failed to initialize MCP sync client: {}", e.getMessage(), e);
      // Return the client anyway — it may recover on next call
    }

    return client;
  }

  private String resolveAccessToken() {
    String envToken = System.getenv("DATAWRAPPER_ACCESS_TOKEN");
    if (envToken != null && !envToken.isEmpty()) {
      log.debug("Using DATAWRAPPER_ACCESS_TOKEN from environment");
      return envToken;
    }
    if (accessToken != null && !accessToken.isEmpty()) {
      log.debug("Using DATAWRAPPER_ACCESS_TOKEN from application config");
      return accessToken;
    }
    log.warn("No DATAWRAPPER_ACCESS_TOKEN available — MCP server may reject requests");
    return null;
  }

  private McpSyncClient createNoOpClient() {
    var serverParams = ServerParameters.builder("echo")
        .build();
    var transport = new StdioClientTransport(serverParams);

    return McpClient.sync(transport)
        .clientInfo(new Implementation("ai-finance-manager-noop", "1.0.0"))
        .build();
  }
}
