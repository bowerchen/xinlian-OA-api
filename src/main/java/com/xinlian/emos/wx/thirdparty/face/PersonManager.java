package com.xinlian.emos.wx.thirdparty.face;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.iai.v20200303.IaiClient;
import com.tencentcloudapi.iai.v20200303.models.*;
import com.xinlian.emos.wx.thirdparty.TxFaceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class PersonManager {

    @Autowired
    private TxFaceConfig txFaceConfig;

    /**
     * 添加人员
     * @param param
     * @return
     * @throws TencentCloudSDKException
     */
    public CreatePersonResponse createPerson(HashMap param) {
        CreatePersonRequest req = new CreatePersonRequest();
        req.setGroupId(param.get("groupId").toString());
        req.setPersonName(param.get("name").toString());
        req.setPersonId(param.get("userId").toString());
        req.setUrl(param.get("imgUrl").toString());
        req.setQualityControl(4L);

        IaiClient client = txFaceConfig.initTxFaceConfig();
        CreatePersonResponse res = null;
        try {
            res = client.CreatePerson(req);
        } catch (TencentCloudSDKException e) {
            e.printStackTrace();
        }

        return res;
    }

    /**
     * 删除人员信息
     * @param userId
     * @return
     */
    public DeletePersonResponse deletePerson(int userId) {
        DeletePersonRequest req = new DeletePersonRequest();
        req.setPersonId(String.valueOf(userId));

        IaiClient client = txFaceConfig.initTxFaceConfig();
        DeletePersonResponse res = null;
        try {
            res = client.DeletePerson(req);
        } catch (TencentCloudSDKException e) {
            e.printStackTrace();
        }
        return res;
    }
}
