package com.xinlian.emos.wx.constants;

public enum CheckinConstants {
    CHECKIN(10101, "签到"),
    ABSENCE(10102, "迟到"),
    LEAVE_EARLY(10103, "早退"),
    RETROACTIVE(10104, "补签"),
    HOLIDAYS_CHECKIN(10105, "节假日上班"),
    HOLIDAYS_NO_CHECKIN(10106, "节假日不需要考勤"),
    WORKDAYS_NO_CHECKIN(10107, "已请假工作日不考勤"),
    BEFORE_CHECKIN(10108, "没到上班考勤时间"),
    CHECKIN_DONE(10110, "今日已考勤")
    ;

    private int code;
    private String msg;


    CheckinConstants(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
