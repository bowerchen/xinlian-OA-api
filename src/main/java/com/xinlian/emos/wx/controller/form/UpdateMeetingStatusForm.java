package com.xinlian.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel
public class UpdateMeetingStatusForm {
    private int id;
    private int status;
    private int approveStatus;
}
