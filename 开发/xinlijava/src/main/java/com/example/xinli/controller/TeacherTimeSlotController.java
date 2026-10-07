package com.example.xinli.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.xinli.common.Result;
import com.example.xinli.entity.TeacherTimeSlot;
import com.example.xinli.service.TeacherTimeSlotService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/teacherSlot")
public class TeacherTimeSlotController {

    @Autowired
    private TeacherTimeSlotService teacherTimeSlotService;

    /**
     * 老师：获取自己全部时间段
     */
    @GetMapping("/my")
    public Result<List<TeacherTimeSlot>> getMySlot(HttpServletRequest request){
        Long userId = (Long) request.getAttribute("loginUserId");
        QueryWrapper<TeacherTimeSlot> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        List<TeacherTimeSlot> list = teacherTimeSlotService.list(wrapper);
        return Result.success(list);
    }

    /**
     * 老师：批量保存时间段（先删除旧，再插入新）
     */
    @PostMapping("/saveBatch")
    public Result<?> saveBatch(@RequestBody List<TeacherTimeSlot> slotList, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("loginUserId");
        QueryWrapper<TeacherTimeSlot> delWrapper = new QueryWrapper<>();
        delWrapper.eq("user_id",userId);
        teacherTimeSlotService.remove(delWrapper);
        for(TeacherTimeSlot slot : slotList){
            slot.setUserId(userId);
            slot.setId(null);
        }
        if(!slotList.isEmpty()){
            teacherTimeSlotService.saveBatch(slotList);
        }
        return Result.success(null);
    }

    /**
     * 根据老师id，获取该老师所有时间段（学生预约弹窗使用）
     */
    @GetMapping("/getByTeacherId/{teacherId}")
    public Result<List<TeacherTimeSlot>> getByTeacherId(@PathVariable Long teacherId){
        QueryWrapper<TeacherTimeSlot> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",teacherId);
        List<TeacherTimeSlot> list = teacherTimeSlotService.list(wrapper);
        return Result.success(list);
    }
}
