package com.xinlian.emos.wx.controller.form;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DeleteMeetingForm {

    @NotNull
    private int id;

    @NotNull
    private Long creatorId;

    @NotNull
    private Short status;
}
