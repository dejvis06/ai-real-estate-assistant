package com.reservation.config;

import com.reservation.reservation.interfaces.mcp.ReservationMcpTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class McpConfig {

    @Bean
    ToolCallbackProvider reservationToolCallbacks(ReservationMcpTool reservationMcpTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(reservationMcpTool)
                .build();
    }
}
