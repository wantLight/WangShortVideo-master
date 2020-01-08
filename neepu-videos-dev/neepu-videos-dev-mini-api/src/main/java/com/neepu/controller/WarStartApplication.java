package com.neepu.controller;

import com.neepu.NeepuVideosDevMiniApiApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * @author xyzzg
 * @version 1.0
 * @date 2019-12-23 18:23
 */
public class WarStartApplication extends SpringBootServletInitializer {
    /**
     * 继承SpringBootServletInitializer，相当于使用web.xml的形式去启动部署
     * @param builder
     * @return
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        //使用web.xml运行应用程序指向ImoocVideosDevMiniApiApplication，最后启动springboot
        return builder.sources(NeepuVideosDevMiniApiApplication.class);
    }

}
