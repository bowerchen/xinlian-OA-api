package com.xinlian.emos.wx;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.xinlian.emos.wx.db.pojo.MessageEntity;
import com.xinlian.emos.wx.db.pojo.MessageRefEntity;
import com.xinlian.emos.wx.db.pojo.TbMeeting;
import com.xinlian.emos.wx.service.MeetingService;
import com.xinlian.emos.wx.service.MessageService;
import com.xinlian.emos.wx.thirdparty.TxBucketConfig;
import com.xinlian.emos.wx.thirdparty.face.PersonManager;
import com.xinlian.emos.wx.thirdparty.face.VerifyFace;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

@SpringBootTest
class EmosWxApiApplicationTests {

    @Autowired
    private MessageService messageService;

    @Autowired
    private MeetingService meetingService;

    @Test
    void contextLoads() {
        for (int i = 0; i <= 100; i++) {
            MessageEntity message = new MessageEntity();
            message.setUuid(IdUtil.simpleUUID());
            message.setSenderId(0l);
            message.setSenderName("系统消息");
            message.setMsg("这是第" + i + "条测试消息");
            message.setSendTime(new Date());
            String id = messageService.insertMessage(message);

            MessageRefEntity messageRef = new MessageRefEntity();
            messageRef.setMessageId(id);
            messageRef.setReceiverId(26);
            messageRef.setLastFlag(true);
            messageRef.setReadFlag(false);
            messageService.insertMessageRef(messageRef);
        }
    }

    @Test
    void createMeetingData() {
        for (int i = 1; i < 100; i++) {
            TbMeeting meeting = new TbMeeting();
            meeting.setId((long) i);
            meeting.setUuid(IdUtil.simpleUUID());
            meeting.setTitle("测试会议"+i);
            meeting.setCreatorId(26L);
            meeting.setDate(DateUtil.today());
            meeting.setPlace("线上会议室");
            meeting.setStart("8:30");
            meeting.setEnd("10:30");
            meeting.setType((short) 1);
            meeting.setMembers("[15, 16]");
            meeting.setDesc("会议研讨EMOS项目上线测试");
            meeting.setInstanceId(IdUtil.simpleUUID());
            meeting.setStatus((short) 3);
            meetingService.insertMeeting(meeting);
        }
    }
}
