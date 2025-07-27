package com.liao.ch01;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * @author lxf
 * @date 2025/7/26
 */
@Slf4j
@Component
public class MiaoAdvisor implements BaseAdvisor {
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String orDefault = (String) chatClientRequest.context().getOrDefault("endContent", "喵喵喵");
        String systemPrompt = chatClientRequest.prompt().getSystemMessage().getText();
        Prompt prompt = chatClientRequest.prompt()
                .augmentSystemMessage(systemPrompt + String.format("你需要以 %s 结尾", orDefault));
        log.info("{}", prompt.getContents());
        // 修改Request
        return ChatClientRequest.builder()
                .prompt(prompt)
                .context(chatClientRequest.context())
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
