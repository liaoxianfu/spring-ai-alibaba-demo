package com.liao.ch01;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author lxf
 * @date 2025/7/22
 */
@Slf4j
@RestController
@RequestMapping("/hello")
public class HelloWorldController {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public HelloWorldController(ChatModel chatModel, @Qualifier("jdbcChatMemory") ChatMemory chatMemory, MyLoggerAdvisor myLoggerAdvisor) {

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
        Map<String, Object> map = Map.of("endContent", "喵喵~");
        response.setCharacterEncoding("UTF-8");
        PromptTemplate build = PromptTemplate.builder().template("你是一名白发红眼猫儿的猫娘,会{endContent}叫")
                .variables(map)
                .build();

        return chatClient.prompt(query)
                .system(build.render())
                .advisors(p -> p.params(map))
                .advisors(new MiaoAdvisor())
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

    @GetMapping(value = "structOutput")
    public List<Person> structOutput() {
        ParameterizedTypeReference<List<Person>> type =
                new ParameterizedTypeReference<>() {
                };

        return chatClient.prompt()
                .user("小明今年10岁，小红比小明大2岁")
                .system(SystemPromptTemplate.builder()
                        .template("请将输入的句子中的年龄进行解析并返回一个json对象")
                        .build().render())
                .call()
                .entity(type);
    }

    @GetMapping(value = "structOutput2")
    public List<Person> structOutput2() {
        // 定义结构化解析类型
        var type = new ParameterizedTypeReference<List<Person>>() {
        };
        BeanOutputConverter<List<Person>> beanOutputConverter = new BeanOutputConverter<>(type);
        // 获取Format的Prompt
        String format = beanOutputConverter.getFormat();
        // 用户输入加上Format的提示词
        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template("小明今年10岁，小红比小明大2岁 \n {format}")
                .variables(Map.of("format", format))
                .build();
        // 获取LLM输出
        String content = chatClient.prompt()
                .user(promptTemplate.render())
                .call()
                .content();
        log.info("{}", content);
        if (Strings.isBlank(content)) {
            return List.of();
        }
        // 转换为bean
        return beanOutputConverter.convert(content);
    }

    @GetMapping(value = "structOutputMap")
    public Map<String, Integer> structOutputMap() {
        var type = new ParameterizedTypeReference<Map<String, Integer>>() {
        };
        return chatClient.prompt()
                .user("需要购买苹果两个，西瓜3个，番茄4个")
                .system("请将内容转换为map 其中key是名称，value是数量")
                .call()
                .entity(type);

    }

    @GetMapping(value = "structOutputList")
    public List<String> structOutputList() {
        var type = new ListOutputConverter(new DefaultConversionService());
        return chatClient.prompt()
                .user("需要购买苹果两个，西瓜3个，番茄4个")
                .system("请将内容转换为list 其中元素是名称")
                .call()
                .entity(type);

    }
}
