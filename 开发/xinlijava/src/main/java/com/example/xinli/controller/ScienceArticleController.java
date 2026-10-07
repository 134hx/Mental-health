package com.example.xinli.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.xinli.common.Result;
import com.example.xinli.entity.ScienceArticle;
import com.example.xinli.entity.User;
import com.example.xinli.service.ScienceArticleService;
import com.example.xinli.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/science/article")
public class ScienceArticleController {

    @Autowired
    private ScienceArticleService scienceArticleService;
    @Autowired
    private UserService userService;

    // 列表（所有登录用户可见）
    @GetMapping("/list")
    public Result<List<ScienceArticle>> list(@RequestParam(required = false) String keyword) {
        List<ScienceArticle> list = scienceArticleService.list(
                new LambdaQueryWrapper<ScienceArticle>().orderByDesc(ScienceArticle::getCreateTime)
        );
        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = keyword.trim().toLowerCase();
            list = list.stream()
                    .filter(a -> (a.getTitle() != null && a.getTitle().toLowerCase().contains(k))
                            || (a.getSummary() != null && a.getSummary().toLowerCase().contains(k)))
                    .toList();
        }
        return Result.success(list);
    }

    // 详情
    @GetMapping("/get")
    public Result<ScienceArticle> get(Long id) {
        ScienceArticle article = scienceArticleService.getById(id);
        if (article == null) return Result.error("文章不存在");
        return Result.success(article);
    }

    // 新增（仅老师）
    @PostMapping("/add")
    public Result<?> add(@RequestBody ScienceArticle article, HttpServletRequest request) {
        Long loginUserId = (Long) request.getAttribute("loginUserId");
        User loginUser = userService.getById(loginUserId);
        if (loginUser == null || loginUser.getRole() != 1) {
            return Result.error("权限不足，仅老师可以发布科普文章");
        }
        article.setUserId(loginUserId);
        scienceArticleService.save(article);
        return Result.success(null);
    }

    // 删除（仅本人）
    @PostMapping("/delete")
    public Result<?> delete(@RequestParam Long id, HttpServletRequest request) {
        Long loginUserId = (Long) request.getAttribute("loginUserId");
        ScienceArticle article = scienceArticleService.getById(id);
        if (article == null) return Result.error("文章不存在");
        if (article.getUserId() == null || !article.getUserId().equals(loginUserId)) {
            return Result.error("只能删除自己发布的文章");
        }
        scienceArticleService.removeById(id);
        return Result.success(null);
    }
}