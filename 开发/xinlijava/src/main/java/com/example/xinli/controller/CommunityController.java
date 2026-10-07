package com.example.xinli.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.xinli.common.Result;
import com.example.xinli.entity.CommunityPost;
import com.example.xinli.entity.CommunityComment;
import com.example.xinli.entity.TeacherProfile;
import com.example.xinli.entity.User;
import com.example.xinli.service.CommunityPostService;
import com.example.xinli.service.CommunityCommentService;
import com.example.xinli.service.TeacherProfileService;
import com.example.xinli.service.UserService;
import com.example.xinli.vo.CommunityPostVO;
import com.example.xinli.vo.CommunityCommentVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.xinli.util.ContentSafetyUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/community")
public class CommunityController {
    @Autowired
    private CommunityPostService communityPostService;
    @Autowired
    private CommunityCommentService communityCommentService;
    @Autowired
    private UserService userService;
    @Autowired
    private TeacherProfileService teacherProfileService;

    private static final String ANONYMOUS_AVATAR = "/vo/niming.png";
    private static final String ANONYMOUS_NICK_PREFIX = "匿名";

    private String getAnonymousNick(Long userId){
        long code = Math.abs(userId.hashCode()) % 10000;
        return ANONYMOUS_NICK_PREFIX + "#" + String.format("%04d", code);
    }

    @GetMapping("/post/list")
    public Result<List<CommunityPostVO>> postList(HttpServletRequest request){
        Long loginUserId = (Long) request.getAttribute("loginUserId");
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityPost::getIsDeleted,0);
        wrapper.orderByDesc(CommunityPost::getCreateTime);
        List<CommunityPost> postList = communityPostService.list(wrapper);
        List<CommunityPostVO> voList = new ArrayList<>();
        for (CommunityPost post : postList) {
            CommunityPostVO vo = new CommunityPostVO();
            BeanUtils.copyProperties(post,vo);
            //标记是否是我自己的帖子
            vo.setIsMine(post.getUserId().equals(loginUserId));

            User user = userService.getById(post.getUserId());
            if(user == null) continue;
            vo.setUserId(post.getUserId());
            vo.setRole(user.getRole());
            if(user.getRole() == 0){
                vo.setShowAvatar(ANONYMOUS_AVATAR);
                vo.setShowNickname(getAnonymousNick(post.getUserId()));
            }else{
                String userAvatar = user.getAvatar();
                vo.setShowAvatar(userAvatar);
                vo.setAvatar(userAvatar);
                vo.setShowNickname(user.getNickname());
                vo.setRealName(user.getRealName());
                vo.setGender(user.getSex());
                TeacherProfile profile = teacherProfileService.getOne(
                        new LambdaQueryWrapper<TeacherProfile>().eq(TeacherProfile::getUserId,user.getId())
                );
                if(profile != null){
                    vo.setGoodAt(profile.getGoodAt());
                    vo.setAddress(profile.getAddress());
                    vo.setIntroduction(profile.getIntro());
                }
            }
            voList.add(vo);
        }
        return Result.success(voList);
    }

    @PostMapping("/post/save")
    public Result<?> savePost(@RequestBody CommunityPost post, HttpServletRequest request){
        Long loginUserId = (Long) request.getAttribute("loginUserId");
        //高危检测：标题+内容一起检测
        String totalText = (post.getTitle()+" "+post.getContent()).trim();
        ContentSafetyUtil.CheckResult checkRes = ContentSafetyUtil.check(totalText);
        if(checkRes.isUnsafe()){
            return Result.error(checkRes.getMsg());
        }

        post.setUserId(loginUserId);
        post.setIsDeleted(0);
        communityPostService.save(post);
        return Result.success(null);
    }

    //=======评论接口========
    @GetMapping("/comment/list")
    public Result<List<CommunityCommentVO>> getCommentList(Long postId, HttpServletRequest request){
        Long loginUserId = (Long) request.getAttribute("loginUserId");
        List<CommunityCommentVO> voList = communityCommentService.listByPostId(postId, loginUserId);
        return Result.success(voList);
    }

    @PostMapping("/comment/add")
    public Result<?> addComment(@RequestBody CommunityComment comment, HttpServletRequest request){
        Long loginUserId = (Long) request.getAttribute("loginUserId");
        //评论内容高危检测
        ContentSafetyUtil.CheckResult checkRes = ContentSafetyUtil.check(comment.getContent());
        if(checkRes.isUnsafe()){
            return Result.error(checkRes.getMsg());
        }

        comment.setUserId(loginUserId);
        comment.setCreateTime(LocalDateTime.now());
        communityCommentService.save(comment);
        return Result.success(null);
    }

    @PostMapping("/comment/delete/{id}")
    public Result<?> deleteComment(@PathVariable Long id, HttpServletRequest request){
        Long loginUserId = (Long) request.getAttribute("loginUserId");
        CommunityComment comment = communityCommentService.getById(id);
        if(comment == null || !comment.getUserId().equals(loginUserId)){
            return Result.error("无权删除该评论");
        }
        communityCommentService.removeById(id);
        return Result.success(null);
    }

    @PostMapping("/post/delete/{id}")
    public Result<?> deletePost(@PathVariable Long id, HttpServletRequest request){
        Long loginUserId = (Long) request.getAttribute("loginUserId");
        CommunityPost post = communityPostService.getById(id);
        if(post == null){
            return Result.error("帖子不存在");
        }
        // 只能帖子本人删除
        if(!post.getUserId().equals(loginUserId)){
            return Result.error("无权删除该帖子");
        }
        //逻辑删除，设置isDeleted=1
        post.setIsDeleted(1);
        communityPostService.updateById(post);
        return Result.success(null);
    }

}
