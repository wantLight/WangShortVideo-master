package com.neepu.utils;

import com.alibaba.fastjson.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;

/**
 * @author xyzzg
 * @version 1.0
 * @date 2019-12-3 17:15
 */
public class HttpClientUtils {

    // 日志记录
    private static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

    private static RequestConfig requestConfig;

    static
    {
        // 设置请求和传输超时时
        requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();
    }



    /**
     * get请求
     * @param url 路径
     * @return
     */
    public static WeChatLoginInfo httpGet(String url)
    {
        // get请求返回结果
        WeChatLoginInfo weChatLoginInfo = null;
        CloseableHttpClient client = HttpClients.createDefault();
        // get请求
        HttpGet request = new HttpGet(url);
        request.setConfig(requestConfig);
        try
        {
            CloseableHttpResponse response = client.execute(request);

            // 请求成功，并得到相应
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
            {
                // 读取服务器返回过来的json字符串数�?
                HttpEntity entity = response.getEntity();
                String strResult = EntityUtils.toString(entity, "utf-8");
                // 把json字符串转换成json对象
                weChatLoginInfo = JSONObject.parseObject(strResult,WeChatLoginInfo.class);
            }
            else
            {
                logger.error("get请求提交失败:" + url);
            }
        }
        catch (IOException e)
        {
            logger.error("get请求提交失败:" + url, e);
        }
        finally
        {
            request.releaseConnection();
        }
        return weChatLoginInfo;
    }

    /**
     * 每个用户相对于每个微信应用（公众号或者小程序）的openId 是唯一的
     *
     * @param args
     */
    public static void main(String[] args) {
        //url中的  appid 和  secret 开发者会给你  这相当于你小程序的ID和密码       js_code 也会给你  js_code是用微信开发者工具调用方法获得
        String  appid="wx5a52fe5a3602ee99";//你小程序Id
        String secret="49638f838cd5d2b73d61617dc90efddd";//填入你小程序的secret
        String code="081O2uxd1EE9Dz0hhTwd1qOqxd1O2uxS";//用微信开发者工具获取到的code

        //使用 code 换取 openid 和 session_key 等信息
        String url="https://api.weixin.qq.com/sns/jscode2session?appid="+appid+"&secret="+secret+"&js_code="+code+"&grant_type=authorization_code";
        WeChatLoginInfo jsonObj=HttpClientUtils.httpGet(url);
        System.out.println(jsonObj);
        //打印结果 {"openid":"ooIr25XVkA_JK8VnGdI2XtTVRdqg","session_key":"h7EQyL+8P0Kvr58kgsqJfw=="}
    }

}
