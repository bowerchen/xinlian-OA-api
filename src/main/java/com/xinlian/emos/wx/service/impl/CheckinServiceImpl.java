package com.xinlian.emos.wx.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateRange;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.tencentcloudapi.iai.v20200303.models.CreatePersonResponse;
import com.tencentcloudapi.iai.v20200303.models.VerifyFaceResponse;
import com.xinlian.emos.wx.common.util.R;
import com.xinlian.emos.wx.constants.CheckinConstants;
import com.xinlian.emos.wx.constants.SystemConstants;
import com.xinlian.emos.wx.config.shiro.JwtUtil;
import com.xinlian.emos.wx.db.dao.*;
import com.xinlian.emos.wx.db.pojo.TbCheckin;
import com.xinlian.emos.wx.db.pojo.TbFaceModel;
import com.xinlian.emos.wx.db.pojo.TbUser;
import com.xinlian.emos.wx.exception.EmosException;
import com.xinlian.emos.wx.service.CheckinService;
import com.xinlian.emos.wx.thirdparty.face.PersonManager;
import com.xinlian.emos.wx.thirdparty.face.VerifyFace;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Service
@Slf4j
@Scope("prototype")
public class CheckinServiceImpl implements CheckinService {

    @Autowired
    private SystemConstants constants;

    @Autowired
    private TbHolidaysDao holidaysDao;

    @Autowired
    private TbWorkdayDao workdayDao;

    @Autowired
    private TbCheckinDao checkinDao;

    @Autowired
    private TbFaceModelDao faceModelDao;

    @Autowired
    private TbUserDao userDao;

    @Autowired
    private PersonManager personManager;

    @Autowired
    private VerifyFace verifyFace;

    @Autowired
    private TbCityDao cityDao;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${tx.groupId}")
    private String groupId;

    /**
     * 判断能否签到
     * @param userId
     * @param date
     * @return
     */
    @Override
    public CheckinConstants validCanCheckIn(int userId, String date) {
        boolean bool_1 = holidaysDao.searchTodayIsHolidays() != null ? true : false;
        boolean bool_2 = workdayDao.searchTodayIsWorkdays() != null ? true : false;
        String type = "工作日";
        if (DateUtil.date().isWeekend()) {
            type = "节假日";
        }
        if (bool_1) {
            type = "节假日";
        } else if (bool_2) {
            type = "工作日";
        }

        if (type.equals("节假日")) {
            return CheckinConstants.HOLIDAYS_NO_CHECKIN;
        } else {
            DateTime now = DateUtil.date();

            // 上班签到时间
            String startAM = DateUtil.today() + " " + constants.attendanceStartTime; // 上班 6:00
            String AM = DateUtil.today() + " " + constants.attendanceTime; // 上班 8:30
            DateTime attendanceStart = DateUtil.parse(startAM);
            DateTime attendance = DateUtil.parse(AM);

            // 下班签到时间
            String PM = DateUtil.today() + " " + constants.closingTime; // 下班 17:30
            DateTime closing = DateUtil.parse(PM);

            TbCheckin checkin = checkinDao.haveCheckin(userId);

            CheckinConstants msg = null;
            if (now.isBefore(attendanceStart)) {
                msg = CheckinConstants.BEFORE_CHECKIN;
            } else if (now.isBefore(attendance)) {
                boolean bool = checkin.getCheckinTime() != null ? true : false;
                msg = bool ? CheckinConstants.CHECKIN_DONE : CheckinConstants.CHECKIN;
            } else if (now.isAfter(attendance) && now.isBefore(closing)) {

                if (checkin == null) {
                    msg = CheckinConstants.ABSENCE;
                } else {
                    if (checkin.getCheckinTime() != null) {
                        msg = CheckinConstants.LEAVE_EARLY;
                    } else {
                        msg = CheckinConstants.CHECKIN_DONE;
                    }

                    if (checkin.getCheckoutTime() != null) {
                        msg = CheckinConstants.CHECKIN_DONE;
                    }
                }

            } else if (now.isAfter(closing)) {
                if (checkin == null) {
                    msg = CheckinConstants.ABSENCE;
                } else {
                    boolean bool = checkin.getCheckoutTime() != null ? true : false;
                    msg = bool ? CheckinConstants.CHECKIN_DONE : CheckinConstants.CHECKIN;
                }
            } else {
                msg = CheckinConstants.CHECKIN_DONE;
            }
            return msg;
        }
    }

    /**
     * 签到
     * @param param
     */
    @Transactional
    @Override
    public void checkin(HashMap param) {
        Date d1 = DateUtil.date();
        Date d2 = DateUtil.parse(DateUtil.today() + " " + constants.attendanceTime);
        Date d3 = DateUtil.parse(DateUtil.today() + " " + constants.closingTime);

        int status = 1;
        if (d1.compareTo(d2) <= 0) {
            status = 1;
        } else if (d1.compareTo(d2) > 0 && d1.compareTo(d3) < 0) {
            status = 2;
        }

        int userId = (Integer) param.get("userId");
        // 查询疫情风险地区, 默认低风险
        int risk = 1;
        String city = (String) param.get("city");
        String district = (String) param.get("district");

        if (!StrUtil.isBlank(city) && !StrUtil.isBlank(district)) {
            // 获取城市编码
            String code = cityDao.searchCode(city);
            try {
                String url = "http://m." + code + ".bendibao.com/news/yqdengji?qu=" + district;
                Document document = Jsoup.connect(url).get();
                Elements listContent = document.getElementsByClass("list-content");
                if (listContent.size() > 0) {
                    Element element = listContent.get(0);
                    String result = element.select("p:last-child").text();
                    if ("高风险".equals(result)) {
                        risk = 3;
                        // TODO 发送警告通知
                    } else if ("中风险".equals(result)) {
                        risk = 2;
                    }
                }
            } catch (Exception e) {
                log.error("执行异常", e);
                throw new EmosException("获取风险等级失败", 500);
            }

            //保存签到记录
            String address = (String) param.get("address");
            String country = (String) param.get("country");
            String province = (String) param.get("province");

            TbCheckin checkin = new TbCheckin();
            checkin.setUserId(userId);
            checkin.setAddress(address);
            checkin.setCountry(country);
            checkin.setProvince(province);
            checkin.setCity(city);
            checkin.setRisk(risk);
            checkin.setDistrict(district);
            checkin.setStatus((byte) status);
            checkin.setDate(DateUtil.today());

            long count = checkinDao.searchIfCheckinDay(userId);
            if (count == 0) {
                checkinDao.insert(checkin);
            }

            TbCheckin haveCheckin = checkinDao.haveCheckin(userId);

            if (haveCheckin.getCheckinTime() == null) {
                checkin.setCheckinTime(d1);
                checkinDao.updateCheckinTime(checkin);
            }

            if (haveCheckin.getCheckinTime() != null && haveCheckin.getCheckoutTime() == null) {
                checkin.setCheckinTime(null);
                checkin.setCheckoutTime(d1);
                checkinDao.updateCheckinTime(checkin);
            }

        }
    }

    /**
     * 人脸识别
     * @param userId
     * @param imgUrl
     * @return
     */
    @Override
    public String verifyFaceModel(int userId, String imgUrl) {
        String faceModel = faceModelDao.searchFaceModel(userId);
        if (StrUtil.isBlank(faceModel)) {
            throw new EmosException("不存在人脸模型");
        } else {
            // 开始人脸识别
            HashMap param = new HashMap<>();
            param.put("userId", userId);
            param.put("imgUrl", imgUrl);
            VerifyFaceResponse res = verifyFace.faceCompare(param);

            if (res.getIsMatch() && res.getScore() > 85) {
                return "人脸识别通过";
            } else {
                throw new EmosException("人脸识别不通过，请重新识别");
            }
        }
    }

    /**
     * 创建人脸模型
     * @param userId
     * @param imgUrl
     */
    @Override
    public void createFaceModel(int userId, String imgUrl) {
        // 获取姓名
        TbUser user = userDao.searchById(userId);

        HashMap<String, Object> param = new HashMap<>();
        param.put("groupId", groupId);
        param.put("userId", userId);
        param.put("name", user.getNickname());
        param.put("imgUrl", imgUrl);

        CreatePersonResponse res = personManager.createPerson(param);
        TbFaceModel faceModel = new TbFaceModel();
        faceModel.setUserId(userId);
        faceModel.setFaceModel(res.getFaceId());

        faceModelDao.insert(faceModel);
    }

    /**
     * 查询总签到天数
     * @param userId
     * @return
     */
    @Override
    public long searchCheckinDays(int userId) {
        long days = checkinDao.searchCheckinDays(userId);
        return days;
    }


    /**
     * 查询今天签到信息
     * @param userId
     * @return
     */
    @Override
    public HashMap searchTodayCheckin(int userId) {
        HashMap map = checkinDao.searchTodayCheckin(userId);
        return map;
    }


    /**
     * 查询一周签到信息
     * @param param
     * @return
     */
    @Override
    public ArrayList<HashMap> searchWeekCheckin(HashMap param) {
        ArrayList<HashMap> checkinList = checkinDao.searchWeekCheckin(param);
        ArrayList<String> holidaysList = holidaysDao.searchWorkdayInRange(param);
        ArrayList<String> workdayList = workdayDao.searchWorkdayInRange(param);

        DateTime startDate = DateUtil.parseDate(param.get("startDate").toString());
        DateTime endDate = DateUtil.parseDate(param.get("endDate").toString());
        DateRange range = DateUtil.range(startDate, endDate, DateField.DAY_OF_MONTH);

        ArrayList<HashMap> list = new ArrayList<>();
        range.forEach(one -> {
            String date = one.toString("yyyy-MM-dd");
            String type = "工作日";
            if (one.isWeekend()) {
                type = "节假日";
            }
            if (holidaysList != null && holidaysList.contains(date)) {
                type = "节假日";
            } else if (workdayList != null && workdayList.contains(date)) {
                type = "工作日";
            }

            String status = "";
            if (type.equals("工作日") && DateUtil.compare(one, DateUtil.date()) <= 0) {
                status = "缺勤";
                boolean flag = false;
                for (HashMap<String, String> map : checkinList) {
                    if (map.containsValue(date)) {
                        status = map.get("status");
                        flag = true;
                        break;
                    }
                }
                DateTime endTime = DateUtil.parse(DateUtil.today() + " " + constants.attendanceEndTime);
                String today = DateUtil.today();
                if (date.equals(today) && DateUtil.date().isBefore(endTime) && flag == false) {
                    status = "";
                }
            }
            HashMap map = new HashMap();
            map.put("date", date);
            map.put("status", status);
            map.put("type", type);
            map.put("day", one.dayOfWeekEnum().toChinese("周"));
            list.add(map);
        });
        return list;
    }

    @Override
    public ArrayList<HashMap> searchMonthCheckin(HashMap param) {
        return this.searchWeekCheckin(param);
    }
}
