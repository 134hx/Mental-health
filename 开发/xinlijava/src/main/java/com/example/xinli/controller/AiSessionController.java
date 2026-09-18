package com.example.xinli.controller;

import com.example.xinli.common.Result;
import com.example.xinli.entity.AiSession;
import com.example.xinli.service.AiSessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/aiSession")
public class AiSessionController {
    @Autowired
    private AiSessionService aiSessionService;

    @PostMapping("/create")
    public Result<AiSession> createSession(HttpServletRequest request, @RequestParam String sessionTitle) {
        Long userId = (Long) request.getAttribute("loginUserId");
        AiSession session = aiSessionService.createSession(userId, sessionTitle);
        return Result.success(session);
    }

    /**
     * 获取我的全部会话列表
     */
    @GetMapping("/myList")
    public Result<List<AiSession>> mySessionList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        List<AiSession> list = aiSessionService.getByUserId(userId);
        return Result.success(list);
    }

    /**
     * 删除会话
     */
    @PostMapping("/delete")
    public Result<String> deleteSession(HttpServletRequest request, @RequestParam Long sessionId) {
        Long userId = (Long) request.getAttribute("loginUserId");
        aiSessionService.removeById(sessionId);
        return Result.success("删除成功");
    }

    /**
     * 修改会话标题
     */
    @PostMapping("/updateTitle")
    public Result<String> updateTitle(HttpServletRequest request,
                                      @RequestParam Long sessionId,
                                      @RequestParam String sessionTitle){
        Long userId = (Long) request.getAttribute("loginUserId");
        AiSession session = new AiSession();
        session.setId(sessionId);
        session.setSessionTitle(sessionTitle);
        aiSessionService.updateById(session);
        return Result.success("修改成功");
    }
}

