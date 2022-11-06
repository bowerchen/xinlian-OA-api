package com.xinlian.emos.wx.db.dao;

import com.xinlian.emos.wx.db.pojo.SysConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysConfigDao {

    List<SysConfig> selectAllParam();
}