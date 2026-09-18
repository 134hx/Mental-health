package com.example.xinli.controller;

import com.example.xinli.common.Result;
import com.example.xinli.entity.AiMessage;
import com.example.xinli.service.AiMessageService;
import com.example.xinli.util.AiChatUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/aiMessage")
public class AiMessageController {
    @Autowired
    private AiMessageService aiMessageService;
    @Autowired
    private AiChatUtil aiChatUtil;

    @PostMapping("/send")
    public Result<AiMessage> sendMsg(HttpServletRequest request,
                                     @RequestParam Long sessionId,
                                     @RequestParam String content) {
        Long userId = (Long) request.getAttribute("loginUserId");

        // 1、保存用户消息入库
        AiMessage userMsg = new AiMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(content);
        aiMessageService.save(userMsg);

        // 2、读取本会话全部历史消息（包含刚刚新增的user消息，用于上下文）
        List<AiMessage> history = aiMessageService.getBySessionId(sessionId);

        // 3、调用大模型，带上全部上下文
        String aiContent;
        try {
            aiContent = aiChatUtil.chatWithHistory(history);
        } catch (Exception e) {
            e.printStackTrace();
            aiContent = "AI服务出现异常，请稍后重新发送。";
        }

        // 4、保存AI回复入库
        AiMessage aiMsg = new AiMessage();
        aiMsg.setSessionId(sessionId);
        aiMsg.setRole("assistant");
        aiMsg.setContent(aiContent);
        aiMessageService.save(aiMsg);

        return Result.success(aiMsg);
    }

    /**
     * 获取会话全部历史消息
     */
    @GetMapping("/list")
    public Result<List<AiMessage>> getMessageList(@RequestParam Long sessionId) {
        List<AiMessage> list = aiMessageService.getBySessionId(sessionId);
        return Result.success(list);
    }
}

