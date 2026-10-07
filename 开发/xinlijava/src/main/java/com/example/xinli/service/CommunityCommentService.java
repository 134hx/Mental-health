package com.example.xinli.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.xinli.entity.CommunityComment;
import com.example.xinli.entity.TeacherProfile;
import com.example.xinli.entity.User;
import com.example.xinli.mapper.CommunityCommentMapper;
import com.example.xinli.service.TeacherProfileService;
import com.example.xinli.service.UserService;
import com.example.xinli.vo.CommunityCommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommunityCommentService extends ServiceImpl<CommunityCommentMapper, CommunityComment> {

    @Autowired
    private UserService userService;
    @Autowired
    private TeacherProfileService teacherProfileService;

    private static final String ANONYMOUS_NICK_PREFIX = "匿名";

    private String getAnonymousNick(Long userId){
        long code = Math.abs(userId.hashCode()) % 10000;
        return ANONYMOUS_NICK_PREFIX + "#" + String.format("%04d", code);
    }

    /**
     * 根据帖子id查询评论VO列表
     * @param postId 帖子id
     * @param loginUserId 当前登录用户id，标记是否自己的评论
     */
    public List<CommunityCommentVO> listByPostId(Long postId, Long loginUserId) {
        LambdaQueryWrapper<CommunityComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityComment::getPostId, postId);
        wrapper.orderByAsc(CommunityComment::getCreateTime);
        List<CommunityComment> commentList = baseMapper.selectList(wrapper);

        return commentList.stream().map(comment -> {
            CommunityCommentVO vo = new CommunityCommentVO();
            vo.setId(comment.getId());
            vo.setPostId(comment.getPostId());
            vo.setUserId(comment.getUserId());
            vo.setContent(comment.getContent());
            vo.setCreateTime(comment.getCreateTime());
            // 修复：字段名 isMine
            vo.setIsMine(comment.getUserId().equals(loginUserId));

            User user = userService.getById(comment.getUserId());
            if(user != null){
                vo.setRole(user.getRole());
                if(user.getRole() == 0){
                    //学生匿名
                    vo.setShowNickname(getAnonymousNick(user.getId()));
                    vo.setShowAvatar(null);
                }else{
                    //老师：填充用户+教师档案信息
                    vo.setShowNickname(user.getNickname());
                    vo.setShowAvatar(user.getAvatar());
                    vo.setRealName(user.getRealName());
                    vo.setGender(user.getSex());
                    //查询老师档案
                    TeacherProfile profile = teacherProfileService.getOne(
                            new LambdaQueryWrapper<TeacherProfile>().eq(TeacherProfile::getUserId, user.getId())
                    );
                    if(profile != null){
                        vo.setGoodAt(profile.getGoodAt());
                        vo.setIntroduction(profile.getIntro());
                        vo.setAddress(profile.getAddress());
                    }
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }
}
