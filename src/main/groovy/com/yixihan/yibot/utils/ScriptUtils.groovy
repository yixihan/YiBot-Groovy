package com.yixihan.yibot.utils

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.StrUtil
import groovy.util.logging.Slf4j

import java.nio.charset.StandardCharsets

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
            File path = FileUtil.file(scriptPath)
            // 构建Python命令
            List<String> command = ["python", path.getAbsolutePath()]
            command.addAll(argument)

            // 使用ProcessBuilder执行命令
            ProcessBuilder processBuilder = new ProcessBuilder(command)
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
