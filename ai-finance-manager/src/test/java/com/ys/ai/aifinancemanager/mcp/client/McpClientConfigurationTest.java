package com.ys.ai.aifinancemanager.mcp.client;

import io.modelcontextprotocol.client.McpSyncClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link McpClientConfiguration}.
 * Validates configuration bean creation in disabled state.
 */
class McpClientConfigurationTest {

  @Test
  void mcpSyncClient_shouldCreateNoOpClientWhenDisabled() {
    // given
    var config = new McpClientConfiguration();
    setField(config, "enabled", false);
    setField(config, "mcpCommand", "echo");
    setField(config, "accessToken", "");

    // when
    McpSyncClient client = config.mcpSyncClient();

    // then
    assertThat(client).isNotNull();
    assertThat(client.getClientInfo()).isNotNull();
    assertThat(client.getClientInfo().name()).isEqualTo("ai-finance-manager-noop");
  }

  @Test
  void mcpSyncClient_noOpClientShouldNotBeInitialized() {
    // given
    var config = new McpClientConfiguration();
    setField(config, "enabled", false);
    setField(config, "mcpCommand", "echo");
    setField(config, "accessToken", "");

    // when
    McpSyncClient client = config.mcpSyncClient();

    // then
    assertThat(client.isInitialized()).isFalse();
  }

  private void setField(Object target, String fieldName, Object value) {
    try {
      var field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(target, value);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set field " + fieldName, e);
    }
  }
}
