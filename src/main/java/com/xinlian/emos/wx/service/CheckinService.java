package com.xinlian.emos.wx.service;

import com.xinlian.emos.wx.common.util.R;
import com.xinlian.emos.wx.constants.CheckinConstants;

import java.util.ArrayList;
import java.util.HashMap;

public interface CheckinService {
    CheckinConstants validCanCheckIn(int userId, String date);

    void checkin(HashMap param);

    String verifyFaceModel(int userId, String imgUrl);

    void createFaceModel(int userId, String imgUrl);

    HashMap searchTodayCheckin(int userId);

    long searchCheckinDays(int userId);

    ArrayList<HashMap> searchWeekCheckin(HashMap param);

    ArrayList<HashMap> searchMonthCheckin(HashMap param);
}
