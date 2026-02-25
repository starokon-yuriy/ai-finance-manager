package com.ys.ai.aifinancemanager.mcp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class AgentLogService {

  private static final Path LOG_FILE = Path.of("agent_log.txt");
  private static final DateTimeFormatter TIMESTAMP_FMT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");


  public void logInteraction(String action, String prompt, String contextSnapshot,
                             String agentOutput, boolean accepted, String reason) {
    String timestamp = LocalDateTime.now().format(TIMESTAMP_FMT);
    String status = accepted ? "ACCEPTED" : "REJECTED";

    StringBuilder entry = new StringBuilder();
    entry.append("=".repeat(80)).append("\n");
    entry.append("[").append(timestamp).append("] ACTION: ").append(action).append("\n");
    entry.append("PROMPT: ").append(prompt).append("\n");
    entry.append("CONTEXT SNAPSHOT: ").append(truncate(contextSnapshot, 500)).append("\n");
    entry.append("MODEL: datawrapper-mcp (stdio JSON-RPC 2.0)\n");
    entry.append("AGENT OUTPUT: ").append(truncate(agentOutput, 500)).append("\n");
    entry.append("STATUS: ").append(status).append("\n");
    entry.append("REASON: ").append(reason).append("\n");
    entry.append("=".repeat(80)).append("\n\n");

    try {
      Files.writeString(LOG_FILE, entry.toString(),
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE,
          StandardOpenOption.APPEND);
      log.debug("Agent interaction logged: {} - {} - {}", timestamp, action, status);
    } catch (IOException e) {
      log.error("Failed to write to agent_log.txt: {}", e.getMessage(), e);
    }
  }

  private String truncate(String s, int maxLen) {
    if (s == null) return "null";
    if (s.length() <= maxLen) return s;
    return s.substring(0, maxLen) + "...[truncated]";
  }
}

