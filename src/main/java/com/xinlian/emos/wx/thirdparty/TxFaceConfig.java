package com.xinlian.emos.wx.thirdparty;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.iai.v20200303.IaiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TxFaceConfig {

    @Value("${tx.secretId}")
    private String secret_id;

    @Value("${tx.secretKey}")
    private String secret_key;

    public IaiClient initTxFaceConfig() {

        // 创建实例
        Credential cred = new Credential(secret_id, secret_key);

        // 设置http代理
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("iai.tencentcloudapi.com");

        // 实例化Client
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);

        IaiClient client = new IaiClient(cred, "ap-guangzhou", clientProfile);

        return client;
    }
}
