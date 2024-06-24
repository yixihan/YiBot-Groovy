package com.yixihan.yibot.utils

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.RandomUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.system.SystemUtil
import cn.hutool.system.oshi.OshiUtil
import groovy.util.logging.Slf4j

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * description
 *
 * @author yixihan
 * @date 2024-06-17 09:31
 */
@Slf4j
class ScriptUtils {

    static String runPythonScript(String scriptPath, String... argument) {
        try {
            if (!StrUtil.startWith(scriptPath, "/")) {
                scriptPath = "/" + scriptPath
            }
            InputStream scriptData = ScriptUtils.class.getResourceAsStream(scriptPath)
            if (scriptData == null) {
                log.warn("script not exist")
                return StrUtil.EMPTY
            }

            // 读取.py文件内容并写入临时文件
            Path tempPath = Files.createTempFile("temp_script", ".py")
            Files.copy(scriptData, tempPath, StandardCopyOption.REPLACE_EXISTING)

            // 构建Python命令
            String pyCommand = SystemUtil.getOsInfo().isLinux() ? "python3" : "python"
            List<String> command = [pyCommand, tempPath.toFile().absolutePath]
            command.addAll(argument)
            log.info("command: [${command.join(" ")}]")

            // 使用ProcessBuilder执行命令
            ProcessBuilder processBuilder = new ProcessBuilder(command)
            processBuilder.redirectErrorStream(true)
            Process process = processBuilder.start()

            // 读取Python脚本的输出
            InputStream inputStream = process.getInputStream()
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
            StringBuilder output = new StringBuilder()
            String line
            while ((line = reader.readLine()) != null) {
                output.append(line).append('\n')
            }

            // 等待进程结束并获取退出状态
            int exitCode = process.waitFor()
            // 清理临时文件
            Files.delete(tempPath)

            String outputStr = output.toString().trim()
            if (exitCode == 0) {
                return outputStr
            } else {
                log.warn("Python Script Run Error, ${StrUtil.isBlank(outputStr) ? "Error Code 1" : outputStr}")
                return StrUtil.EMPTY
            }
        } catch (Exception e) {
            log.error("Script Run Error : ${e.message}")
            return StrUtil.EMPTY
        }
    }
}
