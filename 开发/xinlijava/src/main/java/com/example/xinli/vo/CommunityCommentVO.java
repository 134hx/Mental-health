package com.example.xinli.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.time.LocalDateTime;

public class CommunityCommentVO {
    private Long id;
    private Long postId;
    private Long userId;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private Integer role;
    private String showNickname;
    private String showAvatar;
    private String realName;
    // 新增：老师资料字段
    private String gender;
    private String goodAt;
    private String introduction;
    private String address;

    private Boolean isMine;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }
    public String getShowNickname() { return showNickname; }
    public void setShowNickname(String showNickname) { this.showNickname = showNickname; }
    public String getShowAvatar() { return showAvatar; }
    public void setShowAvatar(String showAvatar) { this.showAvatar = showAvatar; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    //新增4个老师资料get/set
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getGoodAt() { return goodAt; }
    public void setGoodAt(String goodAt) { this.goodAt = goodAt; }
    public String getIntroduction() { return introduction; }
    public void setIntroduction(String introduction) { this.introduction = introduction; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Boolean getIsMine() { return isMine; }
    public void setIsMine(Boolean mine) { isMine = mine; }
}
