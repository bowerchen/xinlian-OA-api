package com.xinlian.emos.wx.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xinlian.emos.wx.db.dao.TbDeptDao;
import com.xinlian.emos.wx.db.dao.TbUserDao;
import com.xinlian.emos.wx.db.pojo.MessageEntity;
import com.xinlian.emos.wx.db.pojo.TbUser;
import com.xinlian.emos.wx.exception.EmosException;
import com.xinlian.emos.wx.service.UserService;
import com.xinlian.emos.wx.task.MessageTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@Scope("prototype")
public class UserServiceImpl implements UserService {

    @Value("${wx.app-id}")
    private String appId;

    @Value("${wx.app-secret}")
    private String appSecret;

    @Autowired
    private TbUserDao userDao;

    @Autowired
    private TbDeptDao deptDao;

    @Autowired
    private MessageTask messageTask;

    private String getOpenId(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        HashMap map = new HashMap();
        map.put("appid", appId);
        map.put("secret", appSecret);
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        String response = HttpUtil.post(url, map);
        JSONObject json = JSONUtil.parseObj(response);
        String openId = json.getStr("openid");
        if (openId == null || openId.length() == 0) {
            throw new RuntimeException("临时登录凭证错误");
        }
        return openId;
    }

    @Override
    public int registerUser(String registerCode, String code, String nickname, String photo) {
        Integer id = 0;
        if (registerCode.equals("000000")) {
            boolean root = userDao.haveRootUser();
            if (!root) {
                String openId = getOpenId(code);
                HashMap param = new HashMap();
                param.put("openId", openId);
                param.put("nickname", nickname);
                param.put("role", "[0]");
                param.put("status", 1);
                param.put("createTime", new Date());
                param.put("root", true);
                userDao.insert(param);
                id = userDao.searchIdByOpenId(openId);
            } else {
                throw new EmosException("无法绑定超级管理员账号");
            }
        } else {
            String openId = getOpenId(code);
            HashMap param = new HashMap();
            param.put("openId", openId);
            param.put("nickname", nickname);
            param.put("role", "[3]");
            param.put("status", 1);
            param.put("createTime", new Date());
            param.put("root", false);
            userDao.insert(param);
            id = userDao.searchIdByOpenId(openId);
        }

        MessageEntity entity = new MessageEntity();
        entity.setSenderId(0l);
        entity.setSenderName("系统消息");
        entity.setUuid(IdUtil.simpleUUID());
        entity.setSenderPhoto(photo);
        entity.setSendTime(new Date());
        entity.setMsg("欢迎您注册, 请及时更新个人信息");
        messageTask.sendAsync(id + "", entity);

        return id;
    }

    @Override
    public Set<String> searchUserPermissions(int userId) {
        Set<String> permissions = userDao.searchUserPermissions(userId);
        return permissions;
    }

    @Override
    public Integer login(String code) {
        String openId = getOpenId(code);
        Integer userId = userDao.searchIdByOpenId(openId);
        if (userId == null) {
            throw new EmosException("账号未注册");
        }
        return userId;
    }

    @Override
    public TbUser searchById(int userId) {
        TbUser user = userDao.searchById(userId);
        return user;
    }

    @Override
    public String searchUserHiredate(int userId) {
        String hiredate = userDao.searchUserHiredate(userId);
        return hiredate;
    }

    @Override
    public HashMap searchUserSummary(int userId) {
        HashMap map = userDao.searchUserSummary(userId);
        return map;
    }

    @Override
    public ArrayList<HashMap> searchUserGroupByDept(String keyword) {
        ArrayList<HashMap> list_1 = deptDao.searchDeptMembers(keyword);
        ArrayList<HashMap> list_2 = userDao.searchUserGroupByDept(keyword);
        for (HashMap map_1 : list_1) {
            long deptId = (long) map_1.get("id");
            ArrayList members = new ArrayList<>();
            for (HashMap map_2 : list_2) {
                long id = (long) map_2.get("deptId");
                if (deptId == id) {
                    members.add(map_2);
                }
            }
            map_1.put("members", members);
        }
        return list_1;
    }

    @Override
    public ArrayList<HashMap> searchMembers(List param) {
        ArrayList<HashMap> list = userDao.searchMembers(param);
        return list;
    }
}
