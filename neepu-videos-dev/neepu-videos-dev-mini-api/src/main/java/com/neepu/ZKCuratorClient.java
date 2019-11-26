package com.neepu;

import com.neepu.cofig.ResourceConfig;
import com.neepu.enums.BGMOperatorTypeEnum;
import com.neepu.utils.JsonUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by xyzzg on 2018/8/19.
 */
@Component
public class ZKCuratorClient {

    //zk客户端
    private CuratorFramework client = null;
    private final static Logger log = LoggerFactory.getLogger(ZKCuratorClient.class);

    //public static final String ZOOKEEPER_SERVER = "120.79.143.66:2181";

    //@Autowired
    //private BgmService bgmService;

    @Autowired
    private ResourceConfig resourceConfig;

    public void init(){
        if (client != null){
            return;
        }
        //重试策略
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,5);
        //创建zk客户端
        client = CuratorFrameworkFactory.builder()
                .connectString(resourceConfig.getZookeeperServer())
                .sessionTimeoutMs(10000)
                .retryPolicy(retryPolicy).namespace("admin").build();
        //启动客户端
        client.start();

        /**
         * zk监听节点
         */
        try {
//            String testNodeData = new String(client.getData().forPath("/bgm/180819A5SX6YN5D4"));
//            //success
//            log.info("测试节点数据：{}",testNodeData);
            addChildWatch("/bgm");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void addChildWatch(String nodePath) throws Exception {

        final PathChildrenCache cache = new PathChildrenCache(client,nodePath,true);
        cache.start();
        //获取监听器列表，添加自己的监听器
        cache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client,PathChildrenCacheEvent event) throws Exception {

                if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_ADDED)){
                    log.info("监听到事件CHILD_ADDED");
                    //1.从数据库查询bgm对象，获取路径path
                    String path = event.getData().getPath();
                    String opertorObjStr = new String(event.getData().getData());
                    Map<String,String> map = JsonUtils.jsonToPojo(opertorObjStr,Map.class);
                    String opertorType = map.get("operType");
                    String songPath = map.get("path");

//                    String arr[] =path.split("/");
//                    String bgmId = arr[arr.length - 1];

//                    Bgm bgm = bgmService.queryBgmById(bgmId);
//                    if (bgm == null){
//                        return;
//                    }

                    //bgm所在的相对路径
                    //String songPath = bgm.getPath();

                    //2.定义保存到本地的bgm路径
                    String filePath = resourceConfig.getFileSpace() + songPath;

                    //3.定义下载的路径（播放url）
                    String arrPath[] = songPath.split("\\\\");
                    String finalPath = "";
                    //处理url的斜杠
                    for (int i = 0 ; i<arrPath.length ; i++){
                        if (StringUtils.isNoneBlank(arrPath[i])){
                            finalPath += "/";
                            finalPath += URLEncoder.encode(arrPath[i],"UTF-8");
                        }
                    }
                    String bgmUrl = resourceConfig.getBgmServer() +finalPath;

                    if (opertorType.equals(BGMOperatorTypeEnum.ADD.type)){
                        //下载bgm到springboot服务器
                        URL url = new URL(bgmUrl);
                        File file = new File(filePath);
                        FileUtils.copyURLToFile(url,file);
                        client.delete().forPath(path);
                    } else if (opertorType.equals(BGMOperatorTypeEnum.DELETE.type)){
                        File file = new File(filePath);
                        FileUtils.forceDelete(file);
                        client.delete().forPath(path);
                    }

                }
            }
        });
    }
}
