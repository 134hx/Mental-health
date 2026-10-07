package com.example.xinli.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.xinli.common.Result;
import com.example.xinli.entity.Appointment;
import com.example.xinli.entity.TeacherProfile;
import com.example.xinli.entity.TeacherTimeSlot;
import com.example.xinli.entity.User;
import com.example.xinli.mapper.UserMapper;
import com.example.xinli.service.AppointmentService;
import com.example.xinli.service.TeacherProfileService;
import com.example.xinli.service.TeacherTimeSlotService;
import com.example.xinli.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.xinli.dto.AppointmentSaveDTO;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/appointment")
public class AppointmentController {
    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TeacherProfileService teacherProfileService;
    @Autowired
    private TeacherTimeSlotService teacherTimeSlotService;

    /**
     * 学生：获取我的预约列表
     */
    @GetMapping("/myList")
    public Result<List<Appointment>> myList(HttpServletRequest request){
        Long userId = (Long) request.getAttribute("loginUserId");
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getUserId, userId);
        List<Appointment> list = appointmentService.list(wrapper);
        for(Appointment ap : list){
            User teacher = userService.getById(ap.getTeacherId());
            if(teacher != null){
                ap.setTeacherName(teacher.getRealName());
                ap.setTeacherPhone(teacher.getPhone());
            }
            QueryWrapper<TeacherProfile> profileWrapper = new QueryWrapper<>();
            profileWrapper.eq("user_id",ap.getTeacherId());
            TeacherProfile profile = teacherProfileService.getOne(profileWrapper);
            if(profile != null){
                ap.setTeacherAddress(profile.getAddress());
            }
        }
        return Result.success(list);
    }

    /**
     * 老师：查询预约自己的申请
     */
    @GetMapping("/teacherList")
    public Result<List<Appointment>> teacherList(HttpServletRequest request){
        Long userId = (Long) request.getAttribute("loginUserId");
        User user = userService.getById(userId);
        if(user.getRole() == null || user.getRole() != 1){
            return Result.error("无权限");
        }
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getTeacherId, userId);
        List<Appointment> list = appointmentService.list(wrapper);
        return Result.success(list);
    }

    /**
     * 学生新建预约
     * 接收：teacherId，appointDate(yyyy‑MM‑dd)，slotId（选中时间段id），remark
     */
    @PostMapping("/save")
    public Result<Appointment> save(@RequestBody AppointmentSaveDTO dto, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("loginUserId");
        User student = userService.getById(userId);
        if(student.getRole() != null && student.getRole() == 1){
            return Result.error("老师角色不能发起预约");
        }
        if(userId.equals(dto.getTeacherId())){
            return Result.error("不可以预约自己");
        }

        Long slotId = dto.getSlotId();
        if(slotId == null){
            return Result.error("请选择可预约时间段");
        }
        TeacherTimeSlot slot = teacherTimeSlotService.getById(slotId);
        if(slot == null || !slot.getUserId().equals(dto.getTeacherId())){
            return Result.error("时间段非法");
        }

        String appointDateStr = dto.getAppointDate();
        if(appointDateStr == null || appointDateStr.isBlank()){
            return Result.error("请选择预约日期");
        }

        LocalDate localDate;
        if(appointDateStr.contains("T")){
            LocalDateTime isoDt = LocalDateTime.parse(appointDateStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            localDate = isoDt.toLocalDate();
        }else{
            localDate = LocalDate.parse(appointDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        String[] weekArr = {"周一","周二","周三","周四","周五","周六","周日"};
        String realWeek = weekArr[dayOfWeek.getValue()-1];
        if(!slot.getWeekDay().equals(realWeek)){
            return Result.error("该时间段不适用于选中日期的星期");
        }

        String fullTimeStr = localDate + " " + slot.getStartTime() + ":00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime fullDateTime = LocalDateTime.parse(fullTimeStr, formatter);

        //手动new实体，完全清空，不会携带任何前端多余字段
        Appointment appointment = new Appointment();
        appointment.setId(null);
        appointment.setUserId(userId);
        appointment.setTeacherId(dto.getTeacherId());
        appointment.setStatus(0);
        appointment.setAppointTime(fullDateTime);
        appointment.setRemark(dto.getRemark());
        appointment.setAppointDate(appointDateStr);
        appointment.setSlotId(slotId);

        System.out.println("【预约保存】对象:"+appointment);
        boolean ok = appointmentService.save(appointment);
        System.out.println("【预约保存】save返回ok="+ok);

        if(ok){
            return Result.success(appointment);
        }else{
            return Result.error("预约提交失败");
        }
    }

    /**
     * 更新预约状态
     * 学生：status=3 取消
     * 老师：status=1同意 / status=2完成
     */
    @PostMapping("/updateStatus")
    public Result<?> updateStatus(@RequestParam Long id, @RequestParam Integer status, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("loginUserId");
        Appointment ap = appointmentService.getById(id);
        if(ap == null){
            return Result.error("预约记录不存在");
        }
        User loginUser = userService.getById(userId);
        if(loginUser.getRole() == 0){
            //学生只能取消自己的预约
            if(!ap.getUserId().equals(userId)){
                return Result.error("无权操作该预约");
            }
            if(status != 3){
                return Result.error("学生仅支持取消预约");
            }
        }else{
            //老师，只能操作预约自己的记录
            if(!ap.getTeacherId().equals(userId)){
                return Result.error("无权操作该预约");
            }
            if(status !=1 && status !=2){
                return Result.error("老师仅可执行同意或标记完成");
            }
        }
        ap.setStatus(status);
        appointmentService.updateById(ap);
        return Result.success(null);
    }

    /**
     * 【废弃，不再前端调用】获取全部心理老师列表（下拉选择）
     */
    @GetMapping("/getAllTeacher")
    public Result<List<User>> getAllTeacher(){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("role",1);
        List<User> teachers = userMapper.selectList(wrapper);
        return Result.success(teachers);
    }

    // ========== 老师资料卡相关接口 ==========
    /**
     * 学生：获取全部已经发布资料卡的老师资料列表
     */
    @GetMapping("/profile/list")
    public Result<List<TeacherProfile>> getTeacherProfileList(){
        List<TeacherProfile> profileList = teacherProfileService.list();
        for(TeacherProfile p : profileList){
            User user = userService.getById(p.getUserId());
            if(user != null){
                p.setNickname(user.getNickname());
                p.setAvatar(user.getAvatar());
                p.setRealName(user.getRealName());
                p.setGender(user.getSex());
                p.setPhone(user.getPhone());
            }
        }
        return Result.success(profileList);
    }

    /**
     * 老师：获取自己的资料卡（用于编辑回显）
     */
    @GetMapping("/profile/my")
    public Result<TeacherProfile> getMyProfile(HttpServletRequest request){
        Long userId = (Long) request.getAttribute("loginUserId");
        User user = userService.getById(userId);
        if(user.getRole() == null || !user.getRole().equals(1)){
            return Result.error("仅心理老师可操作");
        }
        QueryWrapper<TeacherProfile> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        TeacherProfile profile = teacherProfileService.getOne(wrapper);
        return Result.success(profile);
    }

    /**
     * 老师：保存/编辑自己的资料卡，有就更新，没有就新增
     * 新增：后端校验真实姓名、性别、手机号不为空
     */
    @PostMapping("/profile/save")
    public Result<?> saveProfile(@RequestBody TeacherProfile profile, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("loginUserId");
        User user = userService.getById(userId);
        if(user.getRole() == null || !user.getRole().equals(1)){
            return Result.error("仅心理老师可操作");
        }
        //后端校验：发布资料卡前必须完善个人信息
        if(user.getRealName() == null || user.getRealName().trim().isEmpty()){
            return Result.error("请先完善个人真实姓名");
        }
        if(user.getSex() == null || user.getSex().trim().isEmpty()){
            return Result.error("请先完善个人性别");
        }
        if(user.getPhone() == null || user.getPhone().trim().isEmpty()){
            return Result.error("请先完善个人手机号");
        }
        QueryWrapper<TeacherProfile> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        TeacherProfile exist = teacherProfileService.getOne(wrapper);
        if(exist != null){
            //编辑
            profile.setId(exist.getId());
            profile.setUserId(userId);
            teacherProfileService.updateById(profile);
        }else{
            //新建发布资料卡
            profile.setUserId(userId);
            teacherProfileService.save(profile);
        }
        return Result.success(null);
    }
}
