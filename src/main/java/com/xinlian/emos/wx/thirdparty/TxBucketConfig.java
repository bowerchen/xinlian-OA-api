package com.xinlian.emos.wx.thirdparty;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.endpoint.EndpointBuilder;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.Transfer;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.TransferManagerConfiguration;
import com.qcloud.cos.transfer.TransferProgress;
import com.tencent.cloud.CosStsClient;
import com.tencent.cloud.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.TreeMap;

@Component
public class TxBucketConfig {

    @Value("${tx.secretId}")
    private String secret_id;

    @Value("${tx.secretKey}")
    private String secret_key;

    @Value("${tx.bucket}")
    private String bucket;

    @Value("${tx.region}")
    private String region;

    @Value("${tx.appId}")
    private String appId;

    public Response getCredential(){

        TreeMap<String, Object> config = new TreeMap<>();
        config.put("secretId", secret_id);
        config.put("secretKey", secret_key);

        // 设置临时密钥有效时长 -> 30分钟
        config.put("durationSeconds", 1800);

        // 设置bucket
        config.put("bucket", bucket);

        // 设置 bucket所在地区
        config.put("region", region);

        config.put("allowPrefixes",  new String[]{
            "*"
        });

        // 密钥权限列表
        String[] allowActions = new String[]{
            // 简单上传
            "name/cos:PutObject",
            // 表单上传、小程序上传
            "name/cos:PostObject",
            // 分块上传
            "name/cos:InitiateMultipartUpload",
            "name/cos:ListMultipartUploads",
            "name/cos:ListParts",
            "name/cos:UploadPart",
            "name/cos:CompleteMultipartUpload"
        };
        config.put("allowActions", allowActions);

        Response res = null;
        try {
            res = CosStsClient.getCredential(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public TransferManager createTransferManager() {
        COSClient cosClient = createCOSClient();
        TransferManager transferManager = new TransferManager(cosClient);
        return transferManager;
    }

    public void shutdownTransferManager(TransferManager transferManager) {
        transferManager.shutdownNow(true);
    }

    public COSClient createCOSClient() {
        COSCredentials cred = new BasicCOSCredentials(secret_id, secret_key);

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setRegion(new Region(region));

        // 设置读取超时
        clientConfig.setSocketTimeout(30 * 1000);
        clientConfig.setConnectionTimeout(30 * 1000);

        return new COSClient(cred, clientConfig);
    }

    public void shutdownCosClient(COSClient cosClient) {
        cosClient.shutdown();
    }

    public void showTransferProgress(Transfer transfer) {
        System.out.println(transfer.getDescription());

        while(transfer.isDone() == false) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return;
            }

            TransferProgress progress = transfer.getProgress();
            long sofar  = progress.getBytesTransferred();
            long total = progress.getTotalBytesToTransfer();
            double pct = progress.getPercentTransferred();
            System.out.printf("上传进度： [%d / %d] = %.02f%%\n", sofar, total, pct);
        }

        System.out.println(transfer.getState());
    }
}


