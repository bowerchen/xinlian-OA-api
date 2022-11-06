package com.xinlian.emos.wx.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.xinlian.emos.wx.common.util.R;
import com.xinlian.emos.wx.constants.CheckinConstants;
import com.xinlian.emos.wx.constants.SystemConstants;
import com.xinlian.emos.wx.config.shiro.JwtUtil;
import com.xinlian.emos.wx.controller.form.CheckinForm;
import com.xinlian.emos.wx.controller.form.SearchMonthCheckinForm;
import com.xinlian.emos.wx.exception.EmosException;
import com.xinlian.emos.wx.service.CheckinService;
import com.xinlian.emos.wx.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/checkin")
@Api("签到模块Web接口")
@Slf4j
public class CheckinController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CheckinService checkinService;

    @Autowired
    private SystemConstants constants;

    @Autowired
    private UserService userService;

    @GetMapping("/validCanCheckIn")
    @ApiOperation("查看用户今天是否可以签到")
    public R validCanCheckIn(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        CheckinConstants con = checkinService.validCanCheckIn(userId, DateUtil.today());
        return R.ok(con.getCode(), con.getMsg());
    }

    @PostMapping("/checkin")
    @ApiOperation("签到")
    public R checkin(@Valid @RequestBody CheckinForm form, @RequestHeader("token") String token) {
        HashMap<String, Object> param = new HashMap<>();
        int userId = jwtUtil.getUserId(token);
        param.put("userId", userId);
        param.put("city", form.getCity());
        param.put("district", form.getDistrict());
        param.put("address", form.getAddress());
        param.put("country", form.getCountry());
        param.put("province", form.getProvince());
        checkinService.checkin(param);
        return R.ok("签到成功");
    }

    @GetMapping("/createFaceModel")
    @ApiOperation("创建人脸模型")
    public R createFaceModel(@RequestHeader("token") String token, @RequestParam("photo") String imgUrl) {
        int userId = jwtUtil.getUserId(token);
        checkinService.createFaceModel(userId, imgUrl);
        return R.ok("人脸建模成功");
    }

    @GetMapping("/validFaceModel")
    public R validFaceModel(@RequestHeader("token") String token, @RequestParam("photo") String imgUrl) {
        int userId = jwtUtil.getUserId(token);
        String faceModel = checkinService.verifyFaceModel(userId, imgUrl);
        return R.ok(faceModel);
    }

    @GetMapping("/searchTodayCheckin")
    @ApiOperation("查询用户当前签到数据")
    public R seartchTodayCheckin(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);

        HashMap map = checkinService.searchTodayCheckin(userId);
        map.put("attendanceTime", constants.attendanceTime);
        map.put("closingTime", constants.closingTime);
        long days = checkinService.searchCheckinDays(userId);
        map.put("checkinDays", days);

        DateTime hiredate = DateUtil.parse(userService.searchUserHiredate(userId));
        DateTime startDate = DateUtil.beginOfWeek(DateUtil.date());
        if (startDate.isBefore(hiredate)) {
            startDate = hiredate;
        }
        DateTime endDate = DateUtil.endOfWeek(DateUtil.date());
        HashMap param = new HashMap();
        param.put("startDate", startDate.toString());
        param.put("endDate", endDate.toString());
        param.put("userId", userId);
        ArrayList<HashMap> list = checkinService.searchWeekCheckin(param);
        map.put("weekCheckin", list);
        return R.ok().put("result", map);
    }

    @PostMapping("/searchMonthCheckin")
    @ApiOperation("查询用户某月签到数据")
    public R searchMonthCheckin(@Valid @RequestBody SearchMonthCheckinForm form, @RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        DateTime hiredate = DateUtil.parse(userService.searchUserHiredate(userId));
        String month = form.getMonth() < 10 ? "0" + form.getMonth() : form.getMonth().toString();
        DateTime startDate = DateUtil.parse(form.getYear() + "-" + month + "-01");
        if (startDate.isBefore(DateUtil.beginOfMonth(hiredate))) {
            throw new EmosException("只能查询考勤之后日期的数据");
        }
        if (startDate.isBefore(hiredate)) {
            startDate = hiredate;
        }
        DateTime endDate = DateUtil.endOfMonth(startDate);
        HashMap param = new HashMap();
        param.put("userId", userId);
        param.put("startDate", startDate.toString());
        param.put("endDate", endDate.toString());
        ArrayList<HashMap> list = checkinService.searchMonthCheckin(param);
        int sum_1 = 0, sum_2 = 0, sum_3 = 0;
        for (HashMap<String, String> one : list) {
            String type = one.get("type");
            String status = one.get("status");
            if ("工作日".equals(type)) {
                if ("正常".equals(status)) {
                    sum_1++;
                } else if ("迟到".equals(status)) {
                    sum_2++;
                } else if ("缺勤".equals(status)) {
                    sum_3++;
                }
            }
        }
        return R.ok().put("list", list).put("sum_1", sum_1).put("sum_2", sum_2).put("sum_3", sum_3);
    }
}
