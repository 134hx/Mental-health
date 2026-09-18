package com.example.xinli.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.xinli.entity.AiMessage;
import com.example.xinli.mapper.AiMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AiMessageService {

    @Autowired
    private AiMessageMapper aiMessageMapper;

    public void save(AiMessage aiMessage){
        aiMessageMapper.insert(aiMessage);
    }

    // 根据会话id获取全部聊天消息
    public List<AiMessage> getBySessionId(Long sessionId){
        QueryWrapper<AiMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id",sessionId);
        wrapper.orderByAsc("create_time");
        return aiMessageMapper.selectList(wrapper);
    }
}
