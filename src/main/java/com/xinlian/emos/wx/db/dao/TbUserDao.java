package com.xinlian.emos.wx.db.dao;

import com.xinlian.emos.wx.db.pojo.TbUser;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Mapper
public interface TbUserDao {

    boolean haveRootUser();

    int insert(HashMap param);

    Integer searchIdByOpenId(String openId);

    HashMap searchUserSummary(int userId);

    Set<String> searchUserPermissions(int userId);

    TbUser searchById(int userId);

    String searchUserHiredate(int userId);

    ArrayList<HashMap> searchUserGroupByDept(String keyword);

    ArrayList<HashMap> searchMembers(List param);

    HashMap searchUserInfo(int userId);

    int searchDeptManagerId(int userId);

    int searchGmId();
}