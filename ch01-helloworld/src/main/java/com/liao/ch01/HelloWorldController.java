package com.liao.ch01;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

/**
 * @author lxf
 * @date 2025/7/22
 */
@RestController
@RequestMapping("/hello")
public class HelloWorldController {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public HelloWorldController(ChatModel chatModel, ChatMemory chatMemory) {
        chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultOptions(ChatOptions.builder()
                        .temperature(0.1).build())
                .build();
        this.chatMemory = chatMemory;
    }

    @GetMapping(value = "/chat")
    public String streamHelloWorld(@RequestParam(value = "query", defaultValue = "你好，很高兴认识你，能简单介绍一下自己吗？") String query) {
        return chatClient.prompt(query)
                .call()
                .content();
    }


    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamHelloWorld(@RequestParam(value = "query", defaultValue = "你好，很高兴认识你，能简单介绍一下自己吗？") String query, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        return chatClient.prompt(query)
                .stream()
                .content();
    }

    @GetMapping(value = "/stream/memory", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamWithMemory(@RequestParam(value = "query", defaultValue = "你好，很高兴认识你，能简单介绍一下自己吗？") String query,
                                         @RequestParam(value = "chatId", defaultValue = "") String chatId,
                                         HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        Consumer<ChatClient.AdvisorSpec> advisorSpecConsumer = a -> a.param(ChatMemory.CONVERSATION_ID, chatId);
        return chatClient.prompt(query)
                // 用于存储聊天记录
                .advisors(messageChatMemoryAdvisor)
                // 可以存放context中的变量
                .advisors(advisorSpecConsumer)
                .stream().content();
    }

}
