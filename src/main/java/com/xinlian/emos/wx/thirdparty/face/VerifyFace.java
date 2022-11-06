package com.xinlian.emos.wx.thirdparty.face;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.iai.v20200303.IaiClient;
import com.tencentcloudapi.iai.v20200303.models.VerifyFaceRequest;
import com.tencentcloudapi.iai.v20200303.models.VerifyFaceResponse;
import com.xinlian.emos.wx.exception.EmosException;
import com.xinlian.emos.wx.thirdparty.TxFaceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class VerifyFace {

    @Autowired
    private TxFaceConfig txFaceConfig;

    public VerifyFaceResponse faceCompare(HashMap param) {
        VerifyFaceRequest req = new VerifyFaceRequest();
        req.setUrl(param.get("imgUrl").toString());
        req.setPersonId(param.get("userId").toString());

        IaiClient client = txFaceConfig.initTxFaceConfig();
        VerifyFaceResponse res = null;
        try {
            res = client.VerifyFace(req);
        } catch (TencentCloudSDKException e) {
            throw new EmosException(e.getMessage());
        }
        return res;
    }
}
