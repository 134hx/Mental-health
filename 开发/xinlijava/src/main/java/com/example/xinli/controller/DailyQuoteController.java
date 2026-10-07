package com.example.xinli.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.xinli.common.Result;
import com.example.xinli.entity.DailyQuoteRecord;
import com.example.xinli.service.DailyQuoteRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dailyQuote")
public class DailyQuoteController {

    @Autowired
    private DailyQuoteRecordService dailyQuoteRecordService;

    // 语录池，后端维护
    private static final List<String> QUOTES = List.of(
            "不必成为别人，你已是足够好的自己。",
            "此刻的你，已经尽力了。",
            "允许自己慢一点，也没关系。",
            "情绪像天气，来了会走，不必抓着不放。",
            "你不需要时刻坚强，偶尔脆弱也是权利。",
            "呼吸，是一切平静的入口。",
            "把注意力放回当下，未来会慢慢展开。",
            "对自己温柔一点，你正在努力。",
            "不是所有事都要解决，有些只需要被看见。",
            "今天，让自己休息一下。",
            "你已经比昨天更勇敢了一点。",
            "此刻的呼吸，就是家。",
            "慢下来，也是前进。",
            "接纳自己，是一生的功课。",
            "你值得被温柔以待，包括你自己。"
    );

    /**
     * 获取今天的抽取结果（不抽，只查询）
     * 返回 { drawn: true/false, quote: "..." }
     */
    @GetMapping("/today")
    public Result<Map<String, Object>> today(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        LocalDate today = LocalDate.now();

        DailyQuoteRecord record = dailyQuoteRecordService.getOne(
                new LambdaQueryWrapper<DailyQuoteRecord>()
                        .eq(DailyQuoteRecord::getUserId, userId)
                        .eq(DailyQuoteRecord::getQuoteDate, today)
                        .last("LIMIT 1")
        );

        Map<String, Object> data = new HashMap<>();
        if (record != null) {
            data.put("drawn", true);
            data.put("quote", record.getQuoteText());
        } else {
            data.put("drawn", false);
            data.put("quote", null);
        }
        return Result.success(data);
    }

    /**
     * 抽取今日一句
     * - 如果今天已经抽过，直接返回原有记录（幂等）
     * - 如果没抽过，随机抽一句，写入数据库
     * 返回 { drawn: true, quote: "..." }
     */
    @PostMapping("/draw")
    public Result<Map<String, Object>> draw(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        LocalDate today = LocalDate.now();

        // 先查今天有没有抽过
        DailyQuoteRecord exist = dailyQuoteRecordService.getOne(
                new LambdaQueryWrapper<DailyQuoteRecord>()
                        .eq(DailyQuoteRecord::getUserId, userId)
                        .eq(DailyQuoteRecord::getQuoteDate, today)
                        .last("LIMIT 1")
        );

        Map<String, Object> data = new HashMap<>();
        if (exist != null) {
            // 已经抽过，返回原来的（防止刷新重抽）
            data.put("drawn", true);
            data.put("quote", exist.getQuoteText());
            return Result.success(data);
        }

        // 没抽过 → 随机抽一句
        String quote = QUOTES.get((int) (Math.random() * QUOTES.size()));

        DailyQuoteRecord record = new DailyQuoteRecord();
        record.setUserId(userId);
        record.setQuoteText(quote);
        record.setQuoteDate(today);

        try {
            dailyQuoteRecordService.save(record);
        } catch (Exception e) {
            // 唯一索引冲突（并发场景）：查一次返回已有的
            DailyQuoteRecord again = dailyQuoteRecordService.getOne(
                    new LambdaQueryWrapper<DailyQuoteRecord>()
                            .eq(DailyQuoteRecord::getUserId, userId)
                            .eq(DailyQuoteRecord::getQuoteDate, today)
                            .last("LIMIT 1")
            );
            if (again != null) {
                data.put("drawn", true);
                data.put("quote", again.getQuoteText());
                return Result.success(data);
            }
            return Result.error("抽取失败，请重试");
        }

        data.put("drawn", true);
        data.put("quote", quote);
        return Result.success(data);
    }
}