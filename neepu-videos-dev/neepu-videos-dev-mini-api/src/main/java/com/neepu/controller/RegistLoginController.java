package com.neepu.controller;

import com.neepu.pojo.Users;
import com.neepu.pojo.vo.UsersVO;
import com.neepu.service.UserService;
import com.neepu.utils.IMoocJSONResult;
import com.neepu.utils.MD5Utils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 *
 */
@RestController
@Api(value = "用户注册登陆的接口",tags = {"注册与登陆的controller"})
public class RegistLoginController extends BasicController{

    @Autowired
    private UserService userService;

    @PostMapping("/regist")
    @ApiOperation(value = "用户注册",notes = "注册用滴")
    public IMoocJSONResult regist(@RequestBody Users users) throws Exception {
        //判断用户名密码不为空
        if (StringUtils.isBlank(users.getUsername()) || StringUtils.isBlank(users.getPassword())){
            return IMoocJSONResult.errorMsg("用户名或密码不能为空");
        }
        //用户名存在？
        boolean usernameflag = userService.queryUsernameIsExist(users.getUsername());

        //保存用户
        if (!usernameflag){
            users.setNickname(users.getUsername());
            users.setPassword(MD5Utils.getMD5Str(users.getPassword()));
            users.setFansCounts(0);
            users.setReceiveLikeCounts(0);
            users.setFollowCounts(0);
            userService.saveUser(users);
        } else {
            return IMoocJSONResult.errorMsg("用户名已存在");
        }
        users.setPassword("");

        UsersVO usersVO = setUserRedisToken(users);
        return IMoocJSONResult.ok(usersVO);
    }

    public UsersVO setUserRedisToken(Users userRedisToken){
        String uniqueToken = UUID.randomUUID().toString();
        redis.set(USER_REDIS_SESSION + ":" + userRedisToken.getId() , uniqueToken, 1000*60*30);

        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(userRedisToken,usersVO);
        usersVO.setUserToken(uniqueToken);
        return usersVO;
    }

    @ApiOperation(value="用户登录", notes="用户登录的接口")
    @PostMapping("/login")
    public IMoocJSONResult login(@RequestBody Users user) throws Exception {
        String username = user.getUsername();
        String password = user.getPassword();
//		Thread.sleep(3000);
        // 1. 判断用户名和密码必须不为空
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return IMoocJSONResult.ok("用户名或密码不能为空...");
        }

        // 2. 判断用户是否存在
        Users userResult = userService.queryUserForLogin(username,
                MD5Utils.getMD5Str(user.getPassword()));

        // 3. 返回
        if (userResult != null) {
            userResult.setPassword("");
            UsersVO userVO = setUserRedisToken(userResult);
            return IMoocJSONResult.ok(userVO);
        } else {
            return IMoocJSONResult.errorMsg("用户名或密码不正确, 请重试...");
        }
    }

    @ApiOperation(value="用户注销", notes="用户注销的接口")
    @ApiImplicitParam(name="userId", value="用户id", required=true,
            dataType="String", paramType="query")
    @PostMapping("/logout")
    public IMoocJSONResult logout(String userId) throws Exception {
        redis.del(USER_REDIS_SESSION + ":" + userId);
        return IMoocJSONResult.ok();
    }
}
