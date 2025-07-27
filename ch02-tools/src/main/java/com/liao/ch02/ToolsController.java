package com.liao.ch02;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lxf
 * @date 2025/7/27
 */
@RequestMapping("/tools")
@RestController
public class ToolsController {

    private final ChatClient chatClient;

    public ToolsController(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel)
                .build();
    }

    @GetMapping("/hello")
    public String hello() {
        return chatClient.prompt().user("hello").call().content();
    }

    @GetMapping(value = "/date", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public String getDate(@RequestParam(value = "query", defaultValue = "获取当前的系统时间") String query) {
        return chatClient.prompt().user(query).tools(new DateTimeTools()).call().content();
    }

}
