package com.xinlian.emos.wx.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONUtil;
import com.xinlian.emos.wx.common.util.R;
import com.xinlian.emos.wx.config.shiro.JwtUtil;
import com.xinlian.emos.wx.controller.form.*;
import com.xinlian.emos.wx.db.pojo.TbMeeting;
import com.xinlian.emos.wx.exception.EmosException;
import com.xinlian.emos.wx.service.MeetingService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/meeting")
public class MeetingController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MeetingService meetingService;

    @PostMapping("/searchMyMeetingListByPage")
    @ApiOperation("查询会议列表分页数据")
    public R searchMyMeetingListByPage(@Valid @RequestBody SearchMyMeetingListByPageForm form, @RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        int page = form.getPage();
        int length = form.getLength();
        long start = (page - 1) * length;
        HashMap map = new HashMap<>();
        map.put("userId", userId);
        map.put("start", start);
        map.put("length", length);
        ArrayList<HashMap> list = meetingService.searchMyMeetingListByPage(map);
        return R.ok().put("result", list);
    }

    @PostMapping("/searchUnapprovalMeetingListByPage")
    @ApiOperation("会议未审批列表")
    public R searchUnapprovalMeetingListByPage(@Valid @RequestBody SearchMyMeetingListByPageForm form) {
        int page = form.getPage();
        int length = form.getLength();
        long start = (page - 1) * length;
        HashMap map = new HashMap<>();
        map.put("start", start);
        map.put("length", length);
        ArrayList<HashMap> list = meetingService.searchUnapprovalMeetingListByPage(map);
        return R.ok().put("result", list);
    }

    @PostMapping("/searchApprovalMeetingListByPage")
    @ApiOperation("会议已审批列表")
    public R searchApprovalMeetingListByPage(@Valid @RequestBody SearchMyMeetingListByPageForm form) {
        int page = form.getPage();
        int length = form.getLength();
        long start = (page - 1) * length;
        HashMap map = new HashMap<>();
        map.put("start", start);
        map.put("length", length);
        ArrayList<HashMap> list = meetingService.searchApprovalMeetingListByPage(map);
        return R.ok().put("result", list);
    }

    @PostMapping("/insertMeeting")
    @ApiOperation("添加会议")
    public R insertMeeting(@Valid @RequestBody InsertMeetingForm form, @RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        if (form.getType() == 2 && (form.getPlace() == null || form.getPlace().length() == 0)) {
            throw new EmosException("线下会议不能为空");
        }

        DateTime d1 = DateUtil.parse(form.getDate() + " " + form.getStart() + ":00");
        DateTime d2 = DateUtil.parse(form.getDate() + " " + form.getEnd() + ":00");
        if (d2.isBeforeOrEquals(d1)) {
            throw new EmosException("结束时间必须大于开始时间");
        }
        if (!JSONUtil.isJsonArray(form.getMembers())) {
            throw new EmosException("members不是JSON数组");
        }
        TbMeeting meeting = new TbMeeting();
        meeting.setUuid(UUID.randomUUID().toString(true));
        meeting.setTitle(form.getTitle());
        meeting.setCreatorId((long) userId);
        meeting.setDate(form.getDate());
        meeting.setPlace(form.getPlace());
        meeting.setStart(form.getStart()+":00");
        meeting.setEnd(form.getEnd() + ":00");
        meeting.setType((short) form.getType());
        meeting.setMembers(form.getMembers());
        meeting.setDesc(form.getDesc());
        meeting.setStatus((short) 1);
        meetingService.insertMeeting(meeting);
        return R.ok().put("result", "success");
    }

    @PostMapping("/updateMeetingInfo")
    @ApiOperation("更新会议")
    public R updateMeetingInfo(@Valid @RequestBody UpdateMeetingInfoForm form) {

        if (form.getType() == 2 && (form.getPlace() == null || form.getType() == null)) {
            throw new EmosException("线下会议地点不能为空");
        }

        DateTime start = DateUtil.parse(form.getDate() + " " + form.getStart());
        DateTime end = DateUtil.parse(form.getDate() + " " + form.getEnd());
        if (end.isBeforeOrEquals(start)) {
            throw new EmosException("结束时间必须大于开始时间");
        }
        if (!JSONUtil.isJsonArray((form.getMembers()))) {
            throw new EmosException("members不是JSON数组");
        }

        HashMap param = new HashMap();
        param.put("title", form.getTitle());
        param.put("date", form.getDate());
        param.put("place", form.getPlace());
        param.put("start", form.getStart() + ":00");
        param.put("end", form.getEnd() + ":00");
        param.put("type", form.getType());
        param.put("members", form.getMembers());
        param.put("id", form.getId());
        param.put("desc", form.getDesc());
        meetingService.updateMeetingInfo(param);
        return R.ok();
    }

    @PostMapping("/updateMeetingStatus")
    @ApiOperation("更新会议审批状态")
    public R updateMeetingStatus(@Valid @RequestBody UpdateMeetingStatusForm form, @RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        HashMap meeting = meetingService.searchMeetingById(form.getId());
        int status = (int) meeting.get("status");
        Long creatorId = (Long) meeting.get("creatorId");
        if (status > 1) {
            throw new EmosException("会议已审批");
        }
        if (meeting.containsKey("approveId") || meeting.containsKey("approveStatus")) {
            throw new EmosException("会议已审批");
        }

        HashMap param = new HashMap();
        param.put("id", form.getId());
        param.put("status", form.getStatus());
        param.put("approveId", userId);
        param.put("approveStatus", form.getApproveStatus());
        param.put("creatorId", creatorId);
        meetingService.updateMeetingStatus(param);
        return R.ok();
    }

    @PostMapping("/searchMeetingById")
    @ApiOperation("根据会议ID查找记录")
    public R searchMeetingById(@Valid @RequestBody SearchMeetingByIdForm form) {
        HashMap map = meetingService.searchMeetingById(form.getId());
        return R.ok().put("result", map);
    }

    @PostMapping("/deleteMeeting")
    @ApiOperation("删除会议")
    public R deleteMeetingById(@Valid @RequestBody DeleteMeetingForm form) {
        meetingService.deleteMeeting(form.getId(), form.getStatus(), form.getCreatorId());
        return R.ok().put("result", "success");
    }
}
