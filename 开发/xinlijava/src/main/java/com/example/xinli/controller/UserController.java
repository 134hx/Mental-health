package com.example.xinli.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.xinli.common.Md5Util;
import com.example.xinli.common.Result;
import com.example.xinli.common.TokenManager;
import com.example.xinli.entity.User;
import com.example.xinli.mapper.UserMapper;
import com.example.xinli.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 新增注入 Mapper
    @Autowired
    private UserMapper userMapper;

    @PostMapping("/register")
    public Result<User> register(@RequestBody User user){
        try {
            User saved = userService.register(user);
            saved.setPassword(null);
            return Result.success(saved);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<Map<String,Object>> login(@RequestBody User loginUser){
        try {
            String token = userService.login(loginUser.getUsername(), loginUser.getPassword());
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("username", loginUser.getUsername()).or().eq("phone", loginUser.getUsername());
            User user = userService.getOne(wrapper);
            if(user == null){
                return Result.error("用户不存在");
            }
            user.setPassword(null);
            Map<String,Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            return Result.success(data);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/saveRole")
    public Result<?> saveRole(@RequestBody User user, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("loginUserId");
        User dbUser = userService.getById(userId);
        dbUser.setRole(user.getRole());
        userService.updateById(dbUser);
        return Result.success(null);
    }

    @GetMapping("/info")
    public Result<User> getInfo(HttpServletRequest request){
        Long userId = (Long) request.getAttribute("loginUserId");
        User user = userService.getById(userId);
        user.setPassword(null);
        return Result.success(user);
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("token") String token){
        TokenManager.remove(token);
        return Result.success(null);
    }

    @PostMapping("/updateInfo")
    public Result<?> updateInfo(@RequestBody User user, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("loginUserId");
        user.setId(userId);
        user.setUsername(null);
        user.setPassword(null);
        userService.updateById(user);
        return Result.success(null);
    }

    @PostMapping("/checkAccount")
    public Result<?> checkAccount(@RequestBody Map<String,String> body){
        String account = body.get("account");
        QueryWrapper<User> wrapper=new QueryWrapper<>();
        wrapper.eq("username",account).or().eq("phone",account);
        Long count=userMapper.selectCount(wrapper);
        if(count==0){
            return Result.error("该账号/手机号未注册");
        }
        return Result.success(null);
    }

    @PostMapping("/resetPassword")
    public Result<?> resetPassword(@RequestBody Map<String,String> body){
        String account=body.get("account");
        String newPwd=body.get("newPassword");
        QueryWrapper<User> wrapper=new QueryWrapper<>();
        wrapper.eq("username",account).or().eq("phone",account);
        User user=userService.getOne(wrapper);
        if(user==null){
            return Result.error("账号/手机号不存在，请重新输入");
        }
        user.setPassword(Md5Util.encrypt(newPwd));
        userService.updateById(user);
        return Result.success(null);
    }
}
