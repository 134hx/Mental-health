package com.example.xinli.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class CommunityPostVO {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private Boolean isMine;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private Integer role; //0学生 1老师
    private String showAvatar;
    private String showNickname;

    //老师资料，弹窗预约使用
    private String realName;
    private String gender;
    private String avatar;
    private String goodAt;
    private String address;
    private String introduction; //老师简介

    //get set
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }
    public String getShowAvatar() { return showAvatar; }
    public void setShowAvatar(String showAvatar) { this.showAvatar = showAvatar; }
    public String getShowNickname() { return showNickname; }
    public void setShowNickname(String showNickname) { this.showNickname = showNickname; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getGoodAt() { return goodAt; }
    public void setGoodAt(String goodAt) { this.goodAt = goodAt; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getIntroduction() { return introduction; }
    public void setIntroduction(String introduction) { this.introduction = introduction; }
    public Boolean getIsMine() {
        return isMine;
    }
    public void setIsMine(Boolean mine) {
        isMine = mine;
    }
}
