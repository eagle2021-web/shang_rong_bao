package com.eagle.srb.oss.service;

import java.io.InputStream;

/**
 * @author eagle2020
 * @date 2021/09/16
 */
public interface FileService {

    /**
     * 文件上传至阿里云
     */
    String upload(InputStream inputStream, String module, String fileName);

    void removeFile(String url);
}
