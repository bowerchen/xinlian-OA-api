package com.xinlian.emos.wx.db.dao;

import com.xinlian.emos.wx.db.pojo.TbCheckin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.shiro.crypto.hash.Hash;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbCheckinDao {

    // 检索当天签到时间
    TbCheckin haveCheckin(int userId);

    void insert(TbCheckin tbCheckin);

    // 查看签完到的信息
    HashMap searchTodayCheckin(int userId);

    // 查看所有的签到记录
    long searchCheckinDays(int userId);

    // 查看当前是否有签到记录
    long searchIfCheckinDay(int userId);

    // 查看一周签到记录信息
    ArrayList<HashMap> searchWeekCheckin(HashMap param);

    // 更新签到、签出时间
    void updateCheckinTime(TbCheckin checkin);
}