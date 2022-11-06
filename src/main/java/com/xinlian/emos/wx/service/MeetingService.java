package com.xinlian.emos.wx.service;

import com.xinlian.emos.wx.db.pojo.TbMeeting;

import java.util.ArrayList;
import java.util.HashMap;

public interface MeetingService {

    void insertMeeting(TbMeeting entity);

    ArrayList<HashMap> searchMyMeetingListByPage(HashMap param);

    ArrayList<HashMap> searchUnapprovalMeetingListByPage(HashMap param);

    ArrayList<HashMap> searchApprovalMeetingListByPage(HashMap param);

    void deleteMeeting(int id, short status, long creatorId);

    void updateMeetingInfo(HashMap param);

    HashMap searchMeetingById(int id);

    void updateMeetingStatus(HashMap param);
}
