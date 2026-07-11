package com.ldy.ldyaicodemother.langgraph4j.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import com.ldy.ldyaicodemother.exception.BusinessException;
import com.ldy.ldyaicodemother.exception.ErrorCode;
import com.ldy.ldyaicodemother.langgraph4j.enums.ImageCategoryEnum;
import com.ldy.ldyaicodemother.langgraph4j.resource.ImageResource;
import com.ldy.ldyaicodemother.manager.CosManager;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MermaidDiagramTool {

    @Resource
    private CosManager cosManager;
    
    @Tool("将 Mermaid 代码转换为架构图图片，用于展示系统结构和技术关系")
    public List<ImageResource> generateMermaidDiagram(@P("Mermaid 图表代码") String mermaidCode,
                                                      @P("架构图描述") String description) {
        if (StrUtil.isBlank(mermaidCode)) {
            return new ArrayList<>();
        }
        try {
            // 转换为SVG图片
            File diagramFile = convertMermaidToSvg(mermaidCode);
            // 上传到COS
            String keyName = String.format("/mermaid/%s/%s",
                    RandomUtil.randomString(5), diagramFile.getName());
            String cosUrl = cosManager.uploadFile(keyName, diagramFile);
            // 清理临时文件
            FileUtil.del(diagramFile);
            if (StrUtil.isNotBlank(cosUrl)) {
                return Collections.singletonList(ImageResource.builder()
                        .category(ImageCategoryEnum.ARCHITECTURE)
                        .description(description)
                        .url(cosUrl)
                        .build());
            }
        } catch (Exception e) {
            log.error("生成架构图失败: {}", e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    /**
     * 将Mermaid代码转换为SVG图片
     */
    private File convertMermaidToSvg(String mermaidCode) {
        File tempInputFile = FileUtil.createTempFile("mermaid_input_", ".mmd", true);
        File tempOutputFile = new File(FileUtil.getTmpDir(),
                "mermaid_output_" + RandomUtil.randomString(8) + ".svg");
        File puppeteerConfigFile = null;
        try {
            FileUtil.writeUtf8String(mermaidCode, tempInputFile);
            puppeteerConfigFile = createPuppeteerConfigFile();
            String command = SystemUtil.getOsInfo().isWindows() ? "mmdc.cmd" : "mmdc";
            List<String> cmd = new ArrayList<>();
            cmd.add(command);
            cmd.add("-i");
            cmd.add(tempInputFile.getAbsolutePath());
            cmd.add("-o");
            cmd.add(tempOutputFile.getAbsolutePath());
            cmd.add("-b");
            cmd.add("transparent");
            if (puppeteerConfigFile != null) {
                cmd.add("-p");
                cmd.add(puppeteerConfigFile.getAbsolutePath());
            }
            String cliOutput = executeMermaidCli(cmd);
            if (!tempOutputFile.exists() || tempOutputFile.length() == 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                        "Mermaid CLI 执行失败: " + StrUtil.blankToDefault(cliOutput, "未生成 SVG 文件"));
            }
            return tempOutputFile;
        } finally {
            FileUtil.del(tempInputFile);
            if (puppeteerConfigFile != null) {
                FileUtil.del(puppeteerConfigFile);
            }
        }
    }

    /**
     * mmdc 依赖 Puppeteer 启动浏览器；Windows 上内置 Chromium 常启动失败，需指定系统 Chrome。
     */
    private File createPuppeteerConfigFile() {
        String chromePath = resolveChromeExecutable();
        if (StrUtil.isBlank(chromePath)) {
            return null;
        }
        File configFile = FileUtil.createTempFile("puppeteer_config_", ".json", true);
        String json = String.format(
                "{\"executablePath\":\"%s\",\"args\":[\"--no-sandbox\"]}",
                chromePath.replace("\\", "\\\\")
        );
        FileUtil.writeString(json, configFile, StandardCharsets.UTF_8);
        return configFile;
    }

    private String resolveChromeExecutable() {
        List<String> candidates = new ArrayList<>();
        if (SystemUtil.getOsInfo().isWindows()) {
            candidates.add("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");
            candidates.add("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");
            String localAppData = System.getenv("LOCALAPPDATA");
            if (StrUtil.isNotBlank(localAppData)) {
                candidates.add(localAppData + "\\Google\\Chrome\\Application\\chrome.exe");
            }
        } else if (SystemUtil.getOsInfo().isMac()) {
            candidates.add("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome");
        } else {
            candidates.add("/usr/bin/google-chrome");
            candidates.add("/usr/bin/chromium-browser");
            candidates.add("/usr/bin/chromium");
        }
        for (String path : candidates) {
            if (FileUtil.exist(path)) {
                return path;
            }
        }
        return null;
    }

    private String executeMermaidCli(List<String> cmd) {
        try {
            Process process = new ProcessBuilder(cmd)
                    .redirectErrorStream(true)
                    .start();
            boolean finished = process.waitFor(120, TimeUnit.SECONDS);
            String output = IoUtil.read(process.getInputStream(), StandardCharsets.UTF_8);
            if (!finished) {
                process.destroyForcibly();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Mermaid CLI 执行超时");
            }
            if (process.exitValue() != 0) {
                log.error("Mermaid CLI 退出码 {}，输出: {}", process.exitValue(), output);
            }
            return output;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Mermaid CLI 执行异常: " + e.getMessage());
        }
    }


    /*@Slf4j
    @Component
    public class MermaidDiagramTool {

        @Resource
        private CosManager cosManager;

        @Tool("将 Mermaid 代码转换为架构图图片，用于展示系统结构和技术关系")
        public List<ImageResource> generateMermaidDiagram(@P("Mermaid 图表代码") String mermaidCode,
                                                          @P("架构图描述") String description) {
            if (StrUtil.isBlank(mermaidCode)) {
                return new ArrayList<>();
            }
            try {
                // 转换为SVG图片
                File diagramFile = convertMermaidToSvg(mermaidCode);
                // 上传到COS
                String keyName = String.format("/mermaid/%s/%s",
                        RandomUtil.randomString(5), diagramFile.getName());
                String cosUrl = cosManager.uploadFile(keyName, diagramFile);
                // 清理临时文件
                FileUtil.del(diagramFile);
                if (StrUtil.isNotBlank(cosUrl)) {
                    return Collections.singletonList(ImageResource.builder()
                            .category(ImageCategoryEnum.ARCHITECTURE)
                            .description(description)
                            .url(cosUrl)
                            .build());
                }
            } catch (Exception e) {
                log.error("生成架构图失败: {}", e.getMessage(), e);
            }
            return new ArrayList<>();
        }

        *//**
         * 将Mermaid代码转换为SVG图片
         *//*
        private File convertMermaidToSvg(String mermaidCode) {
            // 创建临时输入文件
            File tempInputFile = FileUtil.createTempFile("mermaid_input_", ".mmd", true);
            FileUtil.writeUtf8String(mermaidCode, tempInputFile);
            // 创建临时输出文件
            File tempOutputFile = FileUtil.createTempFile("mermaid_output_", ".svg", true);
            // 根据操作系统选择命令
            String command = SystemUtil.getOsInfo().isWindows() ? "mmdc.cmd" : "mmdc";
            // 构建命令
            String cmdLine = String.format("%s -i %s -o %s -b transparent",
                    command,
                    tempInputFile.getAbsolutePath(),
                    tempOutputFile.getAbsolutePath()
            );
            // 执行命令
            RuntimeUtil.execForStr(cmdLine);
            // 检查输出文件
            if (!tempOutputFile.exists() || tempOutputFile.length() == 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Mermaid CLI 执行失败");
            }
            // 清理输入文件，保留输出文件供上传使用
            FileUtil.del(tempInputFile);
            return tempOutputFile;
        }
    }*/

}
