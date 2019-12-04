package com.neepu.controller;

import com.neepu.utils.RedisOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * 父类Controller - 主要为了定义一些常量
 *
 * 路径未修改前，服务器会在tomcat/bin/D:neepu_dev/下创建？
 *
 * lx_videos_prod
 *
 */
@RestController
public class BasicController {


    @Autowired
    public RedisOperator redis;

    public static final String USER_REDIS_SESSION = "user-redis-session";

    //public static final String FFMPEG_EXE = "D:\\ffmpeg\\bin\\ffmpeg.exe";

    public static final String FFMPEG_EXE = "/usr/bin/ffmpeg";

    //public static final String FILE_SPACE = "D:\\lx_videos_prod";

    public static final String FILE_SPACE = "/home/lx_videos_prod";

    public static final Integer PAGE_SIZE = 5;

    //public static final String UPLOAD_PATHDB = "";

    //你小程序Id
    public static final String  appid="wx5a52fe5a3602ee99";

    //填入你小程序的secret
    public static final String secret="49638f838cd5d2b73d61617dc90efddd";
}
