package com.example.xinli.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.xinli.common.Result;
import com.example.xinli.entity.PsychVideo;
import com.example.xinli.entity.User;
import com.example.xinli.service.PsychVideoService;
import com.example.xinli.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/psych/video")
public class PsychVideoController {

    @Autowired
    private PsychVideoService psychVideoService;
    @Autowired
    private UserService userService;

    @Value("${web.upload.path}")
    private String uploadRootPath;

    private String getVideoRootFolder(){
        return uploadRootPath + File.separator + "video" + File.separator;
    }

    // 给单条视频填充发布者信息
    private void fillUploader(PsychVideo video) {
        if (video == null || video.getUserId() == null) return;
        User u = userService.getById(video.getUserId());
        if (u != null) {
            String name = (u.getRealName() != null && !u.getRealName().isBlank())
                    ? u.getRealName()
                    : u.getNickname();
            video.setUploaderName(name);
            video.setUploaderAvatar(u.getAvatar());
        }
    }

    /**
     * 查询视频列表，可按标题/发布者模糊搜索
     * @param keyword 可选。匹配视频标题，或发布者（realName/nickname）
     */
    @GetMapping("/list")
    public Result<List<PsychVideo>> list(@RequestParam(required = false) String keyword) {
        // 先查全部，倒序
        List<PsychVideo> list = psychVideoService.list(
                new LambdaQueryWrapper<PsychVideo>().orderByDesc(PsychVideo::getCreateTime)
        );

        // 填充发布者
        for (PsychVideo v : list) fillUploader(v);

        // 有 keyword 时，按 标题 或 发布者 模糊匹配（大小写不敏感）
        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = keyword.trim().toLowerCase();
            list = list.stream()
                    .filter(v -> {
                        boolean titleHit = v.getTitle() != null
                                && v.getTitle().toLowerCase().contains(k);
                        boolean uploaderHit = v.getUploaderName() != null
                                && v.getUploaderName().toLowerCase().contains(k);
                        return titleHit || uploaderHit;
                    })
                    .toList();
        }
        return Result.success(list);
    }

    //根据id获取单条视频
    @GetMapping("/get")
    public Result<PsychVideo> get(Long id) {
        PsychVideo video = psychVideoService.getById(id);
        if (video == null) {
            return Result.error("视频不存在");
        }
        fillUploader(video);
        return Result.success(video);
    }

    //新增视频数据库记录（仅老师）
    @PostMapping("/add")
    public Result<?> add(@RequestBody PsychVideo psychVideo, HttpServletRequest request) {
        Long loginUserId = (Long) request.getAttribute("loginUserId");
        User loginUser = userService.getById(loginUserId);
        if (loginUser == null || loginUser.getRole() != 1) {
            return Result.error("权限不足，仅老师可以新增视频");
        }
        psychVideo.setUserId(loginUserId);
        psychVideoService.save(psychVideo);
        return Result.success(null);
    }

    //删除视频（仅本人可删）
    @PostMapping("/delete")
    public Result<?> delete(@RequestParam Long id, HttpServletRequest request) {
        Long loginUserId = (Long) request.getAttribute("loginUserId");
        User loginUser = userService.getById(loginUserId);
        if (loginUser == null || loginUser.getRole() != 1) {
            return Result.error("权限不足，仅老师可以删除视频");
        }
        PsychVideo video = psychVideoService.getById(id);
        if (video == null) {
            return Result.error("视频不存在");
        }
        if (video.getUserId() == null || !video.getUserId().equals(loginUserId)) {
            return Result.error("只能删除自己上传的视频");
        }
        String url = video.getUrl();
        if (url != null && url.startsWith("/video/")) {
            String[] parts = url.split("/");
            if (parts.length >= 4) {
                String folderUuid = parts[2];
                File folder = new File(getVideoRootFolder(), folderUuid);
                deleteDir(folder);
            }
        }
        psychVideoService.removeById(id);
        return Result.success(null);
    }

    private void deleteDir(File dir) {
        if (dir == null || !dir.exists()) return;
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File c : children) deleteDir(c);
            }
        }
        dir.delete();
    }

    @PostMapping("/createFolder")
    public Result<String> createFolder(HttpServletRequest request) {
        Long loginUserId = (Long) request.getAttribute("loginUserId");
        User loginUser = userService.getById(loginUserId);
        if (loginUser == null || loginUser.getRole() != 1) {
            return Result.error("权限不足，仅老师可以操作");
        }
        String folderUuid = UUID.randomUUID().toString();
        File folder = new File(getVideoRootFolder(), folderUuid);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return Result.success(folderUuid);
    }

    @PostMapping("/uploadFile")
    public Result<String> uploadFile(@RequestParam MultipartFile file,
                                     @RequestParam String folderUuid,
                                     HttpServletRequest request) {
        Long loginUserId = (Long) request.getAttribute("loginUserId");
        User loginUser = userService.getById(loginUserId);
        if (loginUser == null || loginUser.getRole() != 1) {
            return Result.error("权限不足，仅老师可以上传视频");
        }
        try {
            File targetFolder = new File(getVideoRootFolder(), folderUuid);
            if (!targetFolder.exists()) {
                return Result.error("文件夹不存在，请先调用创建文件夹接口");
            }
            File destFile = new File(targetFolder, "movie.mp4");
            file.transferTo(destFile);
            String url = "/video/" + folderUuid + "/movie.mp4";
            return Result.success(url);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("视频文件上传失败");
        }
    }

    @PostMapping("/uploadPoster")
    public Result<String> uploadPoster(@RequestParam MultipartFile file,
                                       @RequestParam String folderUuid,
                                       HttpServletRequest request) {
        Long loginUserId = (Long) request.getAttribute("loginUserId");
        User loginUser = userService.getById(loginUserId);
        if (loginUser == null || loginUser.getRole() != 1) {
            return Result.error("权限不足，仅老师可以上传封面");
        }
        try {
            File targetFolder = new File(getVideoRootFolder(), folderUuid);
            if (!targetFolder.exists()) {
                return Result.error("文件夹不存在，请先调用创建文件夹接口");
            }
            File destFile = new File(targetFolder, "poster.jpg");
            file.transferTo(destFile);
            String url = "/video/" + folderUuid + "/poster.jpg";
            return Result.success(url);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("封面上传失败");
        }
    }
}