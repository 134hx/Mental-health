package com.example.xinli.controller;

import com.example.xinli.common.Result;
import com.example.xinli.entity.User;
import com.example.xinli.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.UUID;

@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${web.upload.path}")
    private String uploadPath;

    @Autowired
    private UserService userService;

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file, HttpServletRequest request) throws Exception{
        Long userId = (Long) request.getAttribute("loginUserId");
        if(userId == null){
            return Result.error("请先登录");
        }
        // 根据id查询当前登录用户，拿到username账号
        User loginUser = userService.getById(userId);
        if(loginUser == null){
            return Result.error("用户不存在");
        }
        String username = loginUser.getUsername();

        // 【windows文件名过滤】把windows不允许的字符替换成下划线
        username = username.replaceAll("[\\\\/:*?\"<>| ]","_");

        // 用户专属文件夹 upload/user/username
        File userDir = new File(uploadPath, "avatar/" + username);
        if(!userDir.exists()){
            userDir.mkdirs();
        }

        String original = file.getOriginalFilename();
        String suffix = original.substring(original.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString().replace("-","") + suffix;

        File destFile = new File(userDir, fileName);
        file.transferTo(destFile);

        // 返回前端访问路径
        String returnUrl = "upload/avatar/" + username + "/" + fileName;
        System.out.println("磁盘保存路径：" + destFile.getAbsolutePath());
        System.out.println("返回路径：" + returnUrl);
        return Result.success(returnUrl);
    }
}


