package com.xinlian.emos.wx.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONArray;
import com.xinlian.emos.wx.db.dao.TbMeetingDao;
import com.xinlian.emos.wx.db.dao.TbUserDao;
import com.xinlian.emos.wx.db.pojo.MessageEntity;
import com.xinlian.emos.wx.db.pojo.TbMeeting;
import com.xinlian.emos.wx.exception.EmosException;
import com.xinlian.emos.wx.service.MeetingService;
import com.xinlian.emos.wx.task.MessageTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Service
@Slf4j
public class MeetingServiceImpl implements MeetingService {

    @Autowired
    private TbMeetingDao meetingDao;

    @Autowired
    private TbUserDao userDao;

    @Value("${emos.code}")
    private String code;

    @Value("${emos.receiveNotify}")
    private String receiveNotify;

    @Value("${workflow.url}")
    private String workflowUrl;

    @Autowired
    private MessageTask messageTask;

    private ArrayList list;

    @Override
    public void insertMeeting(TbMeeting entity) {
        int row = meetingDao.insertMeeting(entity);
        if (row != 1) {
            throw new EmosException("会议添加失败");
        }
        meetingTaskMessage(entity.getId(), "系统通知", "会议申请已提交, 正在审批中");
    }

    @Override
    public ArrayList<HashMap> searchMyMeetingListByPage(HashMap param) {
        ArrayList<HashMap> list = meetingDao.searchMyMeetingListByPage(param);
        String date = null;
        ArrayList resultList = new ArrayList();
        HashMap resultMap = null;
        JSONArray array = null;
        for (HashMap map : list) {
            String temp = map.get("date").toString();
            if (!temp.equals(date)) {
                date = temp;
                resultMap = new HashMap();
                resultMap.put("date", date);
                array = new JSONArray();
                resultMap.put("list", array);
                resultList.add(resultMap);
            }
            array.put(map);
        }
        return resultList;
    }

    @Override
    public ArrayList<HashMap> searchUnapprovalMeetingListByPage(HashMap param) {
        ArrayList<HashMap> list = meetingDao.searchUnapprovalMeetingListByPage(param);
        return list;
    }

    @Override
    public ArrayList<HashMap> searchApprovalMeetingListByPage(HashMap param) {
        ArrayList<HashMap> list = meetingDao.searchApprovalMeetingListByPage(param);
        return list;
    }

    @Override
    public void deleteMeeting(int id, short status, long creatorId) {
        if (status >= 2) {
            throw new EmosException("会议已经审批, 不能取消");
        } else {
            int i = meetingDao.deleteByMeetingId(id);
            if (i != 1) {
                throw new EmosException("会议取消失败");
            } else {
                meetingTaskMessage(creatorId, "系统通知", "您已取消会议申请");
            }
        }
    }

    @Override
    public void updateMeetingInfo(HashMap param) {
        int id = (int) param.get("id");
        HashMap map = meetingDao.searchMeetingById(id);
        int status = (int) map.get("status");
        if (status != 1) {
            throw new EmosException("该会议已审核, 不能更改");
        }
        int row = meetingDao.updateMeetingInfo(param);
        if (row != 1) {
            throw new EmosException("会议更新失败");
        }
    }

    @Override
    public HashMap searchMeetingById(int id) {
        HashMap map = meetingDao.searchMeetingById(id);
        ArrayList<HashMap> list= meetingDao.searchMeetingMembers(id);
        map.put("members", list);
        return map;
    }

    @Override
    public void updateMeetingStatus(HashMap param) {
        int row = meetingDao.updateMeetingStatus(param);
        if (row != 1) {
            throw new EmosException("会议审批失败");
        }
        Long creatorId = (Long) param.get("creatorId");
        int approveStatus = (int) param.get("approveStatus");
        if (approveStatus == 0) {
            meetingTaskMessage(creatorId, "会议审批", "您的会议申请审批不通过");
        } else if (approveStatus == 1) {
            meetingTaskMessage(creatorId, "会议审批", "您的会议申请审批已通过");
        }
    }

    private void meetingTaskMessage(Long id, String name, String content) {
        MessageEntity message = new MessageEntity();
        message.setSenderId(id);
        message.setSenderName(name);
        message.setUuid(IdUtil.simpleUUID());
        message.setSendTime(new Date());
        message.setMsg(content);
        messageTask.sendAsync(id + "", message);
    }
}
