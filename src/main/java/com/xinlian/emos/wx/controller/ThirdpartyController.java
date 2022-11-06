package com.xinlian.emos.wx.controller;

import com.tencent.cloud.Response;
import com.xinlian.emos.wx.common.util.R;
import com.xinlian.emos.wx.service.BucketService;
import com.xinlian.emos.wx.thirdparty.TxBucketConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@Api
@RequestMapping("thirdparty")
public class ThirdpartyController {

    @Autowired
    private TxBucketConfig bucketConfig;

    @Autowired
    private BucketService bucketService;

    @GetMapping("/getCredential")
    @ApiOperation("获取COS临时上传密钥")
    public R getCredential() {
        Response res = bucketConfig.getCredential();
        return R.ok().put("scrip", res);
    }

    @PostMapping("/fileUpload")
    @ApiOperation("上传文件")
    public R fileUpload(@Valid @RequestParam("key") String key, MultipartFile file) {
        String result = bucketService.uploadFileToBucket(key, file);
        return R.ok().put("key", result);
    }

    @GetMapping("/getFileList")
    @ApiOperation("获取所有文件")
    public R getFileList() {
        ArrayList<HashMap> list = bucketService.list();
        return R.ok().put("result", list);
    }

    @GetMapping("/download")
    @ApiOperation("下载文件")
    public R downloadFile(@Valid @RequestParam("key") String key, @Valid @RequestParam("filePath") String filePath) {
        bucketService.downloadFile(key, filePath);
        return R.ok("下载成功");
    }
}
