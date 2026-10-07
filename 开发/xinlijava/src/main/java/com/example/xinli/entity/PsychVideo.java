package com.example.xinli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@TableName("psych_video")
public class PsychVideo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private String poster;
    private String url;
    private Long userId;

    // 关键：让 LocalDateTime 按 "yyyy-MM-dd HH:mm:ss" 输出，避免带 T
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    // 非数据库字段：发布者信息
    @TableField(exist = false)
    private String uploaderName;
    @TableField(exist = false)
    private String uploaderAvatar;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getUploaderName() { return uploaderName; }
    public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }
    public String getUploaderAvatar() { return uploaderAvatar; }
    public void setUploaderAvatar(String uploaderAvatar) { this.uploaderAvatar = uploaderAvatar; }
}