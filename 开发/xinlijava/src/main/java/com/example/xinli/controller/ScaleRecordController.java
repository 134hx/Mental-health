package com.example.xinli.controller;

import com.example.xinli.common.Result;
import com.example.xinli.entity.ScaleRecord;
import com.example.xinli.service.ScaleRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/scaleRecord")
public class ScaleRecordController {
    @Autowired
    private ScaleRecordService scaleRecordService;

    @PostMapping("/submit")
    public Result<ScaleRecord> submit(HttpServletRequest request,
                                      @RequestParam Long scaleId,
                                      @RequestParam Integer totalScore,
                                      @RequestParam Integer percentScore){
        Long userId = (Long) request.getAttribute("loginUserId");
        ScaleRecord record = scaleRecordService.submitRecord(userId, scaleId, totalScore, percentScore);
        return Result.success(record);
    }

    @GetMapping("/myList")
    public Result<List<ScaleRecord>> myRecordList(HttpServletRequest request){
        Long userId = (Long) request.getAttribute("loginUserId");
        List<ScaleRecord> recordList = scaleRecordService.lambdaQuery()
                .eq(ScaleRecord::getUserId, userId)
                .orderByDesc(ScaleRecord::getCreateTime)
                .list();
        return Result.success(recordList);
    }
}
