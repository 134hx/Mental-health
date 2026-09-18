package com.example.xinli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("scale_question")
public class ScaleQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long scaleId;
    private String questionContent;
    private String optionJson;
    private Integer sort;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getScaleId() { return scaleId; }
    public void setScaleId(Long scaleId) { this.scaleId = scaleId; }
    public String getQuestionContent() { return questionContent; }
    public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }
    public String getOptionJson() { return optionJson; }
    public void setOptionJson(String optionJson) { this.optionJson = optionJson; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
