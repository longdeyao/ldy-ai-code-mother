package com.ldy.ldyaicodemother.ai;

import com.ldy.ldyaicodemother.ai.model.HtmlCodeResult;
import com.ldy.ldyaicodemother.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("做随机睡前小故事的小工具(适用人群：女朋友)");
        Assertions.assertNotNull(result);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode("做随机脑筋急转弯的小工具(适用人群：女朋友)");
        Assertions.assertNotNull(result);
    }
}