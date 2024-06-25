package com.yixihan.yibot.utils

import cn.hutool.core.io.FileUtil
import cn.hutool.core.lang.Assert
import cn.hutool.core.util.ObjUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.system.SystemUtil
import groovy.util.logging.Slf4j

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
/**
 * 脚本运行 工具类
 *
 * @author yixihan
 * @date 2024-06-17 09:31
 */
@Slf4j
class ScriptUtils {

    static String runPythonScript(String scriptPath, String... argument) {
        // 读取脚本, 生成临时文件
        Path tempPath = buildTempFile(scriptPath, "py")

        // 构建Python命令
        List<String> command = [SystemUtil.getOsInfo().isLinux() ? "python3" : "python"]

        // 执行
        return runScript(command, tempPath, argument)
    }

    static String runScript(List<String> command, Path scriptPath, String... argument) {
        // 参数校验
        Assert.notEmpty(command)
        Assert.isTrue(ObjUtil.isNotNull(scriptPath) && FileUtil.exists(scriptPath, false))
        return runScript(command, scriptPath.toFile(), argument)
    }

    static String runScript(List<String> command, File scriptFile, String... argument) {
        Assert.notEmpty(command)
        Assert.isTrue(ObjUtil.isNotNull(scriptFile) && FileUtil.exist(scriptFile))
        try {
            // 构建命令
            command.add(scriptFile.absolutePath)
            command.addAll(argument)
            log.info("command: [${command.join(" ")}]")

            // 使用ProcessBuilder执行命令
            ProcessBuilder processBuilder = new ProcessBuilder(command)
            processBuilder.redirectErrorStream(true)
            Process process = processBuilder.start()

            // 读取脚本的输出
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
            FileUtil.del(scriptFile)

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

    static Path buildTempFile(String scriptPath, String suffix) {
        Path tempFile
        try {
            if (StrUtil.isBlank(scriptPath)) {
                log.warn("scriptPath is Blank")
                return null
            }
            scriptPath = !StrUtil.startWith(scriptPath, "/") ? "/" + scriptPath : scriptPath
            InputStream scriptData = ScriptUtils.class.getResourceAsStream(scriptPath)
            if (scriptData == null) {
                log.warn("script not exist")
                return null
            }

            // 读取文件内容并写入临时文件
            suffix = !StrUtil.startWith(suffix, ".") ? "." + suffix : suffix
            tempFile = Files.createTempFile("temp_script", suffix)
            Files.copy(scriptData, tempFile, StandardCopyOption.REPLACE_EXISTING)
            return tempFile
        } catch (Exception e) {
            log.error("Build Temp File Error, ${e.message}")
            return null
        }
    }
}
