package com.yixihan.yibot.comm.builder

import cn.hutool.core.exceptions.ExceptionUtil
import cn.hutool.core.lang.Assert
import cn.hutool.core.map.MapUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.http.HttpRequest
import cn.hutool.http.HttpResponse
import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import com.yixihan.yibot.config.Aria2Config
import com.yixihan.yibot.utils.Bean
import groovy.util.logging.Slf4j

/**
 * Aria2 Builder
 *
 * @author yixihan
 * @date 2024-06-18 10:48
 */
@Slf4j
@SuppressWarnings("unused")
class Aria2Builder {

    Boolean upload
    Boolean download
    Boolean status
    Boolean list
    Boolean health = true

    String url
    String filePath = "bot"
    String fileName
    String fileId

    Long page = 0
    Long pageSize = 10

    private Aria2Builder() {
    }

    static Aria2Builder build() {
        return new Aria2Builder()
    }

    Aria2Builder upload() {
        this.upload = true
        return this
    }

    Aria2Builder download() {
        this.download = true
        return this
    }

    Aria2Builder status() {
        this.status = true
        return this
    }

    Aria2Builder list() {
        this.list = true
        return this
    }

    Aria2Builder health() {
        this.health = true
        return this
    }

    Aria2Builder url(String url) {
        this.url = url
        return this
    }

    Aria2Builder filePath(String filePath) {
        this.filePath = filePath
        return this
    }

    Aria2Builder fileName(String fileName) {
        this.fileName = fileName
        return this
    }

    Aria2Builder fileId(String fileId) {
        this.fileId = fileId
        return this
    }

    Aria2Builder page(Long page) {
        this.page = page
        return this
    }

    Aria2Builder pageSize(Long pageSize) {
        this.pageSize = pageSize
        return this
    }

    String done() {
        if (upload) {
            return uploadFile()
        } else if (download) {
            return downloadFile()
        } else if (list) {
            return listFile()
        } else if (status) {
            return statusFile()
        } else if (health) {
            return aria2Health()
        } else {
            throw ExceptionUtil.wrapRuntime("Invalid Params")
        }
    }

    private String uploadFile() {
        try {
            Assert.isTrue(healthCheck(), "Aria2 Service Error, Please Check")
            HttpResponse response = HttpRequest.post(getRequestUrl())
                    .body(buildUploadBody())
                    .execute()

            if (response.ok) {
                return response.body()
            } else {
                throw ExceptionUtil.wrapRuntime("aria2 request failed")
            }
        } catch (Exception e) {
            log.error("Aria2 Upload Err: ${e.getMessage()}")
            return "Aria2 Upload Err: ${e.getMessage()}"
        }
    }

    private String downloadFile() {
        Assert.isTrue(healthCheck(), "Aria2 Service Error, Please Check")
        return getRequestUrl()
    }

    private String listFile() {
        try {
            Assert.isTrue(healthCheck(), "Aria2 Service Error, Please Check")
            HttpResponse response = HttpRequest.post(getRequestUrl())
                    .body(buildListBody())
                    .execute()

            if (response.ok) {
                return JSONUtil.parseObj(response.body()).getStr("fileList")
            } else {
                throw ExceptionUtil.wrapRuntime("aria2 request failed")
            }
        } catch (Exception e) {
            log.error("Aria2 List Err: ${e.getMessage()}")
            return "Aria2 List Err: ${e.getMessage()}"
        }
    }

    private String statusFile() {
        try {
            Assert.isTrue(healthCheck(), "Aria2 Service Error, Please Check")
            HttpResponse response = HttpRequest.post(getRequestUrl())
                    .body(buildStatusBody())
                    .execute()

            if (response.ok) {
                return response.body()
            } else {
                throw ExceptionUtil.wrapRuntime("aria2 request failed")
            }
        } catch (Exception e) {
            log.error("Aria2 Status Err: ${e.getMessage()}")
            return "Aria2 Status Err: ${e.getMessage()}"
        }
    }

    private String aria2Health() {
        try {
            HttpResponse response = HttpRequest.get(getRequestUrl()).execute()
            if (response.ok) {
                return response.body()
            } else {
                throw ExceptionUtil.wrapRuntime("aria2 request failed")
            }
        } catch (Exception e) {
            log.error("Aria2 Connect Err: ${e.getMessage()}")
            return "Aria2 Connect Err: ${e.getMessage()}"
        }
    }

    private Boolean healthCheck() {
        String data = aria2Health()
        if (StrUtil.isBlank(data) || !JSONUtil.isTypeJSON(data)) {
            return false
        }
        JSONObject healthData = JSONUtil.parseObj(data)
        return healthData.getStr("status") == "UP"
    }

    private String getRequestUrl() {
        Aria2Config config = Bean.get(Aria2Config)
        if (upload) {
            return config.baseUrl + "/" + config.upload
        } else if (download) {
            Assert.notBlank(fileId, "fileId should be provided")
            return StrUtil.format(config.baseUrl + "/" + config.download, fileId)
        } else if (list) {
            return config.baseUrl + "/" + config.list
        } else if (status) {
            return config.baseUrl + "/" + config.status
        } else if (health) {
            return config.baseUrl + "/" + config.health
        } else {
            throw ExceptionUtil.wrapRuntime("Invalid Params")
        }
    }

    String buildUploadBody() {
        Assert.notBlank(url, "fileUrl should be provided")
        Assert.notBlank(filePath, "filePath should be provided")
        Map map = MapUtil.builder()
                .put("fileUrl", url)
                .put("filePath", filePath)
                .build()
        return JSONUtil.toJsonStr(map)
    }

    String buildListBody() {
        Assert.notBlank(filePath, "filePath should be provided")
        Assert.notNull(page, "page should be provided")
        Assert.isTrue(page > 0, "page must be greater than 0")
        Assert.notNull(pageSize, "pageSize should be provided")
        Assert.isTrue(pageSize > 0, "pageSize must be greater than 0")
        Map map = MapUtil.builder()
                .put("fullPath", filePath)
                .put("currentPage", page)
                .put("pageSize", pageSize)
                .build()
        if (StrUtil.isNotBlank(fileName)) {
            map.put("fileName", fileName)
        }
        return JSONUtil.toJsonStr(map)
    }

    String buildStatusBody() {
        Map map = new HashMap()
        if (StrUtil.isNotBlank(fileId)) {
            map.put("fileId", fileId)
        }
        return JSONUtil.toJsonStr(map)
    }
}
