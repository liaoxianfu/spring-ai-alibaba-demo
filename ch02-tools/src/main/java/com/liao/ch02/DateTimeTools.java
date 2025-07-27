package com.liao.ch02;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * @author lxf
 * @date 2025/7/27
 */
@Slf4j
@Component
public class DateTimeTools {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Tool(description = "获取当前日期时间")
    public String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        String formatDateTime = getFormatDateTime(now);
        log.info("getCurrentDateTime:{}", formatDateTime);
        return formatDateTime;
    }

    @Tool(description = "调整日期时间，让用户更方便")
    public String adJustDateTime(@ToolParam(description = "被调整的时间") String localDateTime,
                                 @ToolParam(description = "调整的数") int amount,
                                 @ToolParam(description = "调整的单位") ChronoUnit unit) {
        log.info("adJustDateTime {}:{}:{}", localDateTime, amount, unit);
        LocalDateTime parse = LocalDateTime.parse(localDateTime, formatter);
        LocalDateTime plus = parse.plus(amount, unit);
        String formatDateTime = getFormatDateTime(plus);
        log.info("getFormatDateTime:{}", formatDateTime);
        return formatDateTime;
    }

    private String getFormatDateTime(@ToolParam(description = "被格式化的时间") LocalDateTime localDateTime) {
        log.info("{}", localDateTime);
        return localDateTime.format(formatter);
    }
}
