package com.xinlian.emos.wx.service.impl;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.*;
import com.qcloud.cos.transfer.Download;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.Upload;
import com.xinlian.emos.wx.service.BucketService;
import com.xinlian.emos.wx.thirdparty.TxBucketConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class BucketServiceImpl  implements BucketService {

    @Autowired
    private TxBucketConfig bucketConfig;

    @Value("${tx.bucket}")
    private String bucket;

    private String BASIC_URL = "https://xinlian-sign-1255543036.cos.ap-guangzhou.myqcloud.com";

    @Override
    public String uploadFileToBucket(String key, MultipartFile file) {

        TransferManager transferManager = bucketConfig.createTransferManager();
        UploadResult uploadResult = null;
        String result = "";
        try {
            InputStream inputStream = file.getInputStream();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, inputStream, metadata);

            Upload upload = transferManager.upload(putObjectRequest);
            bucketConfig.showTransferProgress(upload);
            uploadResult = upload.waitForUploadResult();
            result = BASIC_URL + "/" + uploadResult.getKey();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        bucketConfig.shutdownTransferManager(transferManager);
        return result;
    }

    @Override
    public void downloadFile(String key, String filePath) {
        TransferManager transferManager = bucketConfig.createTransferManager();

        File downloadFile = new File(filePath);
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, key);
        try {
            Download download = transferManager.download(getObjectRequest, downloadFile);
            download.waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bucketConfig.shutdownTransferManager(transferManager);
    }

    @Override
    public ArrayList<HashMap> list() {
        ArrayList<HashMap> list = new ArrayList<>();
        COSClient cosClient = bucketConfig.createCOSClient();
        DecimalFormat format = new DecimalFormat("0.00");
        int MB = 1024 * 1024;

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.setPrefix("/document");
        listObjectsRequest.setBucketName(bucket);
        listObjectsRequest.setMaxKeys(1000);

        // 保存列出的结果
        ObjectListing objectListing = null;
        do {
            try {
                objectListing = cosClient.listObjects(listObjectsRequest);
            } catch (CosClientException e) {
                e.printStackTrace();
            }

            List<COSObjectSummary> objectSummaries = objectListing.getObjectSummaries();
            for (COSObjectSummary objectSummary : objectSummaries) {
                HashMap<String, Object> map = new HashMap<>();
                String size = format.format(objectSummary.getSize() / (float) MB) + "MB";
                String name = objectSummary.getKey().replace("document/", "");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                map.put("key", BASIC_URL + "/" + objectSummary.getKey());
                map.put("size", size);
                map.put("name", name);
                map.put("time", dateFormat.format(objectSummary.getLastModified()));
                if (objectSummary.getSize() != 0) {
                    list.add(map);
                }
            }
            String nextMarker = objectListing.getNextMarker();
            listObjectsRequest.setMarker(nextMarker);
        } while(objectListing.isTruncated());
        bucketConfig.shutdownCosClient(cosClient);
        return list;
    }
}
