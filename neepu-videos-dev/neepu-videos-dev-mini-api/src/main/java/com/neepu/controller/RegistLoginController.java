package com.neepu.controller;

import com.neepu.pojo.Users;
import com.neepu.pojo.vo.UsersVO;
import com.neepu.service.UserService;
import com.neepu.utils.HttpClientUtils;
import com.neepu.utils.IMoocJSONResult;
import com.neepu.utils.MD5Utils;
import com.neepu.utils.WeChatLoginInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
            String url="https://api.weixin.qq.com/sns/jscode2session?appid="+appid+"&secret="+secret+"&js_code="+users.getOpenId()+"&grant_type=authorization_code";
            WeChatLoginInfo jsonObj= HttpClientUtils.httpGet(url);

            //每个用户相对于每个微信应用（公众号或者小程序）的openId 是唯一的
            String openid = jsonObj.getOpenid();
            users.setOpenId(openid);
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


    @ApiOperation(value="微信授权登录", notes="微信登录的接口")
    @ApiImplicitParam(name="jsCode", value="使用 code 换取 openid 和 session_key信息", required=true,
            dataType="String", paramType="form")
    @GetMapping("/wechatLogin")
    public IMoocJSONResult wechatLogin(String jsCode) throws Exception {

        if (StringUtils.isBlank(jsCode)) {
            return IMoocJSONResult.errorMsg("jsCode不得为空...");
        }

        //使用 code 换取 openid 和 session_key 等信息
        String url="https://api.weixin.qq.com/sns/jscode2session?appid="+appid+"&secret="+secret+"&js_code="+jsCode+"&grant_type=authorization_code";
        WeChatLoginInfo jsonObj= HttpClientUtils.httpGet(url);

        //每个用户相对于每个微信应用（公众号或者小程序）的openId 是唯一的
        String openid = jsonObj.getOpenid();
        if (StringUtils.isBlank(openid)) {
            return IMoocJSONResult.ok("服务器开小差了，请重试...");
        }

        // 判断openId对应的用户是否存在
        Users userResult = userService.queryUserForLogin(openid);

        // 3. 返回
        if (userResult != null) {
            userResult.setPassword("");
            UsersVO userVO = setUserRedisToken(userResult);
            return IMoocJSONResult.ok(userVO);
        } else {
            return IMoocJSONResult.errorMsg("您还未绑定账号，请先注册");
        }
    }




    @ApiOperation(value="用户登录", notes="用户登录的接口")
    @ApiImplicitParam(name="user", value="用户类", required=true,
            dataType="Users", paramType="form")
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
