package com.example.xinli.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.xinli.entity.AiSession;
import com.example.xinli.mapper.AiSessionMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AiSessionService extends ServiceImpl<AiSessionMapper, AiSession> {

    // 创建新会话
    public AiSession createSession(Long userId, String title) {
        AiSession session = new AiSession();
        session.setUserId(userId);
        session.setSessionTitle(title);
        this.save(session);
        return session;
    }

    // 查询该用户全部会话
    public List<AiSession> getByUserId(Long userId) {
        QueryWrapper<AiSession> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return this.list(wrapper);
    }
}
