package com.xinlian.emos.wx.db.dao;

import com.xinlian.emos.wx.db.pojo.TbMeeting;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbMeetingDao {

    ArrayList<HashMap> searchMyMeetingListByPage(HashMap param);

    ArrayList<HashMap> searchUnapprovalMeetingListByPage(HashMap param);

    ArrayList<HashMap> searchApprovalMeetingListByPage(HashMap param);

    int insertMeeting(TbMeeting entity);

    boolean searchMeetingMembersInSameDept(String uuid);

    HashMap searchMeetingById(int id);

    ArrayList<HashMap> searchMeetingMembers(int id);

    int updateMeetingInstanceId(HashMap param);

    int updateMeetingStatus(HashMap param);

    int deleteByMeetingId(int id);

    int updateMeetingInfo(HashMap param);
}