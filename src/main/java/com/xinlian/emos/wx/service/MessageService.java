package com.xinlian.emos.wx.service;

import com.xinlian.emos.wx.db.dao.MessageDao;
import com.xinlian.emos.wx.db.pojo.MessageEntity;
import com.xinlian.emos.wx.db.pojo.MessageRefEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;

public interface MessageService {

    String insertMessage(MessageEntity entity);

    List<HashMap> searchMessageByPage(int userId, long start, int length);

    HashMap searchMessageById(String id);

    String insertMessageRef(MessageRefEntity entity);

    long searchUnreadCount(int userId);

    long searchLastCount(int userId);

    long updateUnreadMessage(String id);

    long deleteMessageRefById(String id);

    long deleteUserMessageRef(int userId);
}
