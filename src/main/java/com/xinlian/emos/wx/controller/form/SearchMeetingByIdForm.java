package com.xinlian.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@ApiModel
public class SearchMeetingByIdForm {
    @NotNull
    @Min(1)
    private int id;
}