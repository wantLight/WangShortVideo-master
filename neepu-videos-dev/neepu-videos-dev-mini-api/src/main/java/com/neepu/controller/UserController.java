package com.neepu.controller;

import com.neepu.pojo.Users;
import com.neepu.pojo.UsersReport;
import com.neepu.pojo.vo.PublisherVideo;
import com.neepu.pojo.vo.UsersVO;
import com.neepu.service.UserService;
import com.neepu.utils.IMoocJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;


@RestController
@Api(value = "用户相关业务接口",tags = {"用户相关业务的controller"})
@RequestMapping("/user")
public class UserController extends BasicController{

    @Autowired
    private UserService userService;

    @PostMapping("/uploadFace")
    @ApiOperation(value = "用户上传头像",notes = "用户上传头像接口")
    @ApiImplicitParam(name = "userId",value = "用户Id",required = true,dataType = "String",paramType = "query")
    public IMoocJSONResult uploadFace(String userId,
                                      @RequestParam("file")MultipartFile[] files) throws Exception {
        //System.out.print("处理中...");
        if (StringUtils.isBlank(userId)) {
            return IMoocJSONResult.errorMsg("用户id不能为空...");
        }

        // 文件保存的命名空间
        //String fileSpace = "D:/neepu_videos_dev";
        // 保存到数据库中的相对路径
        String uploadPathDB = "/" + userId + "/face";

        FileOutputStream fileOutputStream = null;
        InputStream inputStream;
        try {
            if (files != null && files.length > 0) {

                String fileName = files[0].getOriginalFilename();
                if (StringUtils.isNotBlank(fileName)) {
                    // 文件上传的最终保存路径
                    String finalFacePath = FILE_SPACE + uploadPathDB + "/" + fileName;
                    // 设置数据库保存的路径
                    uploadPathDB += ("/" + fileName);

                    File outFile = new File(finalFacePath);
                    if (outFile.getParentFile() != null || !outFile.getParentFile().isDirectory()) {
                        // 创建父文件夹
                        outFile.getParentFile().mkdirs();
                    }
                    fileOutputStream = new FileOutputStream(outFile);
                    inputStream = files[0].getInputStream();
                    IOUtils.copy(inputStream, fileOutputStream);
                }
            } else {
                return IMoocJSONResult.errorMsg("上传出错...");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return IMoocJSONResult.errorMsg("上传出错...");
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        }
        Users user = new Users();
        user.setId(userId);
        user.setFaceImage(uploadPathDB);
        userService.updateUserInfo(user);

        return IMoocJSONResult.ok(uploadPathDB);
    }


    @PostMapping("/query")
    @ApiOperation(value = "查询用户信息",notes = "查询用户信息接口")
    @ApiImplicitParam(name = "userId",value = "用户Id",required = true,dataType = "String",paramType = "query")
    public IMoocJSONResult quary(String userId){

        if (StringUtils.isBlank(userId)) {
            return IMoocJSONResult.errorMsg("用户id不能为空...");
        }

        Users userInfo = userService.queryUserInfo(userId);
        UsersVO usersVO = new UsersVO();
        //快速，便捷！
        BeanUtils.copyProperties(userInfo,usersVO);
        return IMoocJSONResult.ok(usersVO);
    }

    @PostMapping("/queryPublisher")
    @ApiOperation(value = "查询视频发布者",notes = "查询视频发布者信息接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "loginUserId", value = "登录用户Id", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "videoId", value = "视频Id", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "publishUserId", value = "发布者Id", required = true, dataType = "String", paramType = "query")
    })
    public IMoocJSONResult queryPublisher(String loginUserId, String videoId,
                                          String publishUserId) throws Exception {

        if (StringUtils.isBlank(publishUserId)) {
            return IMoocJSONResult.errorMsg("id为空啦！");
        }

        // 1. 查询视频发布者的信息
        Users userInfo = userService.queryUserInfo(publishUserId);
        UsersVO publisher = new UsersVO();
        BeanUtils.copyProperties(userInfo, publisher);

        // 2. 查询当前登录者和视频的点赞关系
        boolean userLikeVideo = userService.isUserLikeVideo(loginUserId, videoId);

        PublisherVideo bean = new PublisherVideo();
        bean.setPublisher(publisher);
        bean.setUserLikeVideo(userLikeVideo);

        return IMoocJSONResult.ok(bean);
    }

    @PostMapping("/beyourfans")
    @ApiOperation(value = "关注",notes = "老铁，这不关注一波？")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "登录用户Id", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "fanId", value = "关注发布者Id", required = true, dataType = "String", paramType = "query")
    })
    public IMoocJSONResult beyourfans(String userId, String fanId) throws Exception {

        if (StringUtils.isBlank(userId) || StringUtils.isBlank(fanId)) {
            return IMoocJSONResult.errorMsg("id为空啦！");
        }

        userService.saveUserFanRelation(userId, fanId);

        return IMoocJSONResult.ok("关注成功...");
    }

    @PostMapping("/dontbeyourfans")
    @ApiOperation(value = "关注",notes = "这就取关了嘛，QAQ")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "登录用户Id", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "fanId", value = "关注发布者Id", required = true, dataType = "String", paramType = "query")
    })
    public IMoocJSONResult dontbeyourfans(String userId, String fanId) throws Exception {

        if (StringUtils.isBlank(userId) || StringUtils.isBlank(fanId)) {
            return IMoocJSONResult.errorMsg("");
        }

        userService.deleteUserFanRelation(userId, fanId);

        return IMoocJSONResult.ok("取消关注成功...");
    }

    @PostMapping("/reportUser")
    @ApiOperation(value = "举报视频",notes = "举报信息接口")
    @ApiImplicitParam(name = "userId",value = "用户Id",required = true,dataType = "UsersReport",paramType = "query")
    public IMoocJSONResult reportUser(@RequestBody UsersReport usersReport) throws Exception {

        // 保存举报信息
        userService.reportUser(usersReport);

        return IMoocJSONResult.errorMsg("举报成功...有你平台变得更美好...");
    }


}
