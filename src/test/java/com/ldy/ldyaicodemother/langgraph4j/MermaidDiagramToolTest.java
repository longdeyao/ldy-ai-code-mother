package com.ldy.ldyaicodemother.langgraph4j;

import com.ldy.ldyaicodemother.langgraph4j.enums.ImageCategoryEnum;
import com.ldy.ldyaicodemother.langgraph4j.resource.ImageResource;
import com.ldy.ldyaicodemother.langgraph4j.tools.MermaidDiagramTool;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MermaidDiagramToolTest {

    @Resource
    private MermaidDiagramTool mermaidDiagramTool;

    @Test
    void testGenerateMermaidDiagram() {
        // 测试生成 Mermaid 架构图
        String mermaidCode = """
                flowchart LR
                    Start([开始]) --> Input[输入数据]
                    Input --> Process[处理数据]
                    Process --> Decision{是否有效?}
                    Decision -->|是| Output[输出结果]
                    Decision -->|否| Error[错误处理]
                    Output --> End([结束])
                    Error --> End
                """;
        String description = "简单系统架构图";
        List<ImageResource> diagrams = mermaidDiagramTool.generateMermaidDiagram(mermaidCode, description);
        assertNotNull(diagrams);
        assertFalse(diagrams.isEmpty(), "Mermaid 架构图生成失败，请确认已安装 mmdc 且 Chrome 可用");
        ImageResource firstDiagram = diagrams.get(0);
        assertEquals(ImageCategoryEnum.ARCHITECTURE, firstDiagram.getCategory());
        assertEquals(description, firstDiagram.getDescription());
        assertNotNull(firstDiagram.getUrl());
        assertTrue(firstDiagram.getUrl().startsWith("http"));
        System.out.println("生成了架构图: " + firstDiagram.getUrl());
    }
}
