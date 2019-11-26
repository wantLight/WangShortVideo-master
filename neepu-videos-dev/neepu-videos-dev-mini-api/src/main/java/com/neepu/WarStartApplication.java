package com.neepu;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 * Created by xyzzg on 2018/8/28.
 */
public class WarStartApplication extends SpringBootServletInitializer{
    /**
     * 继承SpringBootServletInitializer，相当于使用web.xml的形式去启动部署
     * @param builder
     * @return
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        //使用web.xml运行应用程序指向ImoocVideosDevMiniApiApplication，最后启动springboot
        return builder.sources(ImoocVideosDevMiniApiApplication.class);
    }
}
