package com.example.xinli.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.xinli.common.Md5Util;
import com.example.xinli.common.TokenManager;
import com.example.xinli.entity.User;
import com.example.xinli.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public User register(User user) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername()).or().eq("phone",user.getPhone());
        Long count = userMapper.selectCount(wrapper);
        if(count > 0){
            throw new RuntimeException("用户名或手机号已被注册");
        }
        user.setPassword(Md5Util.encrypt(user.getPassword()));
        userMapper.insert(user);
        return user;
    }


    /**
     * loginKey：可以是username 或者 phone
     */
    public String login(String loginKey, String password) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", loginKey).or().eq("phone", loginKey);
        User user = userMapper.selectOne(wrapper);
        if(user == null){
            throw new RuntimeException("账号/手机号不存在");
        }
        if(!user.getPassword().equals(Md5Util.encrypt(password))){
            throw new RuntimeException("密码错误");
        }
        return TokenManager.createToken(user.getId());
    }

    public User getById(Long id){
        return userMapper.selectById(id);
    }
    public User getOne(QueryWrapper<User> wrapper){
        return userMapper.selectOne(wrapper);
    }
    public void updateById(User user){
        userMapper.updateById(user);
    }
}

