package com.property.config;

import com.property.property.interfaces.mcp.PropertyMcpTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class McpConfig {

    @Bean
    ToolCallbackProvider propertyToolCallbacks(PropertyMcpTool propertyMcpTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(propertyMcpTool)
                .build();
    }
}
