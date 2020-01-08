package com.neepu.cofig;

/**
 * @author xyzzg
 * @version 1.0
 * @date 2019-10-8 18:45
 */
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lulu
 * @Date 2019/6/7 18:27
 */
@Configuration
public class EsConfig {


    @Bean
    public RestHighLevelClient client(){
        RestHighLevelClient client=new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("120.79.143.66",9200,"http")
                        //这里如果要用client去访问其他节点，就添加进去
                )
        );
        return client;
    }
}

