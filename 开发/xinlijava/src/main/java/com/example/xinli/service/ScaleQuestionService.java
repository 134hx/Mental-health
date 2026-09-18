package com.example.xinli.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.xinli.entity.ScaleQuestion;
import com.example.xinli.mapper.ScaleQuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ScaleQuestionService {
    @Autowired
    private ScaleQuestionMapper scaleQuestionMapper;

    // 根据量表id查询全部题目，按sort排序
    public List<ScaleQuestion> getByScaleId(Long scaleId){
        QueryWrapper<ScaleQuestion> wrapper = new QueryWrapper<>();
        wrapper.eq("scale_id",scaleId);
        wrapper.orderByAsc("sort");
        return scaleQuestionMapper.selectList(wrapper);
    }
}
