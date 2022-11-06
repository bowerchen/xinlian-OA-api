package com.xinlian.emos.wx.service;

import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.UploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface BucketService {

    // 上传文件
    String uploadFileToBucket(String key, MultipartFile file);

    // 下载文件
    void downloadFile(String key, String filePath);

    // 列出所有文件
    ArrayList<HashMap> list();
}
