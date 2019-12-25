package com.neepu.controller;

import com.google.common.collect.Lists;
import com.neepu.enums.VideoStatusEnum;
import com.neepu.pojo.Bgm;
import com.neepu.pojo.Comments;
import com.neepu.pojo.Videos;
import com.neepu.service.BgmService;
import com.neepu.service.VideoService;
import com.neepu.utils.*;
import io.swagger.annotations.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * TODO private static final Object you;
 *
 * FIXME NullPointerException
 *
 */
@RestController
@RequestMapping("/video")
@Api(value = "视频相关业务接口",tags = {"视频相关业务的controller"})
public class VideoController extends BasicController{

    @Autowired
    private BgmService bgmService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private EsUtil esUtil;



    @PostMapping(value = "/upload",headers = "content-type=multipart/form-data")
    @ApiOperation(value = "用户上传视频",notes = "用户上传视频接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId",value = "用户Id",required = true,dataType = "String",paramType = "form"),
            @ApiImplicitParam(name = "bgmId",value = "背景音乐Id",required = false,dataType = "String",paramType = "form"),
            @ApiImplicitParam(name = "videoSeconds",value = "背景音乐播放长度",required = true,dataType = "double",paramType = "form"),
            @ApiImplicitParam(name = "videoWidth",value = "视频宽度",required = true,dataType = "int",paramType = "form"),
            @ApiImplicitParam(name = "videoHeight",value = "视频高度",required = true,dataType = "int",paramType = "form"),
            @ApiImplicitParam(name = "desc",value = "视频描述",required = false,dataType = "String",paramType = "form"),
            @ApiImplicitParam(name = "latitude",value = "视频经度",required = false,dataType = "double",paramType = "form"),
            @ApiImplicitParam(name = "longitude",value = "视频维度",required = false,dataType = "double",paramType = "form")
    })
    public IMoocJSONResult upload(String userId,String bgmId,double videoSeconds,int videoWidth,String coverImg,
                                  int videoHeight,String desc,double latitude,double longitude,
                                  @ApiParam(value = "短视频",required = true)
                                  MultipartFile file) throws Exception {

        if (StringUtils.isBlank(userId)) {
            return IMoocJSONResult.errorMsg("用户id不能为空...");
        }

        // 文件保存的命名空间
		//String FILE_SPACE = "D:/neepu_videos_dev";
        // 保存到数据库中的相对路径
        String uploadPathDB = "/" + userId + "/video";
        String coverPathDB = "/" + userId + "/video";

        FileOutputStream fileOutputStream = null;
        InputStream inputStream = null;
        // 文件上传的最终保存路径
        String finalVideoPath = "";
        try {
            if (file != null) {

                String fileName = file.getOriginalFilename();
                // abc.mp4 根据'.'拆分字符串，拆解需求
                String arrayFilenameItem[] =  fileName.split("\\.");
                String fileNamePrefix = "";
                for (int i = 0 ; i < arrayFilenameItem.length-1 ; i ++) {
                    fileNamePrefix += arrayFilenameItem[i];
                }
                // fix bug: 解决小程序端OK，PC端不OK的bug，原因：PC端和小程序端对临时视频的命名不同
//				String fileNamePrefix = fileName.split("\\.")[0];

                if (StringUtils.isNotBlank(fileName)) {

                    finalVideoPath = FILE_SPACE + uploadPathDB + "/" + fileName;
                    // 设置数据库保存的路径
                    uploadPathDB += ("/" + fileName);
                    coverPathDB = coverPathDB + "/" + fileNamePrefix + ".jpg";

                    File outFile = new File(finalVideoPath);
                    if (outFile.getParentFile() != null || !outFile.getParentFile().isDirectory()) {
                        // 创建父文件夹
                        outFile.getParentFile().mkdirs();
                    }

                    fileOutputStream = new FileOutputStream(outFile);
                    inputStream = file.getInputStream();
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

        // 判断bgmId是否为空，如果不为空，
        // 那就查询bgm的信息，并且合并视频，生产新的视频
        if (StringUtils.isNotBlank(bgmId)) {
            Bgm bgm = bgmService.queryBgmById(bgmId);
            String mp3InputPath = FILE_SPACE + bgm.getPath();

            MergeVideoMp3 tool = new MergeVideoMp3(FFMPEG_EXE);
            String videoInputPath = finalVideoPath;

            String videoOutputName = UUID.randomUUID().toString() + ".mp4";
            uploadPathDB = "/" + userId + "/video" + "/" + videoOutputName;
            finalVideoPath = FILE_SPACE + uploadPathDB;
            tool.convertor(videoInputPath, mp3InputPath, videoSeconds, finalVideoPath);
        }
        System.out.println("uploadPathDB=" + uploadPathDB);
        System.out.println("finalVideoPath=" + finalVideoPath);

        if (StringUtils.isBlank(coverImg)){
            // 对视频进行截图
            FetchVideoCover videoInfo = new FetchVideoCover(FFMPEG_EXE);
            videoInfo.getCover(finalVideoPath, FILE_SPACE + coverPathDB);
        } else {
            coverPathDB = coverImg;
        }


        // 保存视频信息到数据库
        Videos video = new Videos();
        video.setAudioId(bgmId);
        video.setUserId(userId);
        video.setVideoSeconds((float)videoSeconds);
        video.setVideoHeight(videoHeight);
        video.setVideoWidth(videoWidth);
        video.setVideoDesc(desc);
        video.setVideoPath(uploadPathDB);
        video.setCoverPath(coverPathDB);
        video.setStatus(VideoStatusEnum.SUCCESS.value);
        video.setCreateTime(new Date());

        //wgs84坐标
        video.setLatitude(latitude);
        video.setLongitude(longitude);

        String videoId = videoService.saveVideo(video);


        /**
         * 如果视频有经纬度则使用redis-geo
         */
        if (latitude != 0 && longitude != 0){

            Point point = new Point(longitude,latitude);
            redisTemplate.opsForGeo().add(KEY_PREFIX_GEO,point,videoId);
        }

        return IMoocJSONResult.ok(videoId);
    }

    @ApiOperation(value="上传封面", notes="上传封面的接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name="userId", value="用户id", required=true,
                    dataType="String", paramType="form")
    })
    @PostMapping(value="/uploadCover", headers="content-type=multipart/form-data")
    public IMoocJSONResult uploadCover(String userId,
                                       @ApiParam(value="视频封面", required=true)
                                               MultipartFile file) throws Exception {


        // 文件保存的命名空间
		//String FILE_SPACE = "D:/neepu_videos_dev";
        // 保存到数据库中的相对路径
        String uploadPathDB = "/" + userId + "/video";

        FileOutputStream fileOutputStream = null;
        InputStream inputStream;
        // 文件上传的最终保存路径
        String finalCoverPath = "";
        try {
            if (file != null) {

                String fileName = file.getOriginalFilename();
                if (StringUtils.isNotBlank(fileName)) {

                    finalCoverPath = FILE_SPACE + uploadPathDB + "/" + fileName;
                    // 设置数据库保存的路径
                    uploadPathDB += ("/" + fileName);

                    File outFile = new File(finalCoverPath);
                    if (outFile.getParentFile() != null || !outFile.getParentFile().isDirectory()) {
                        // 创建父文件夹
                        outFile.getParentFile().mkdirs();
                    }

                    fileOutputStream = new FileOutputStream(outFile);
                    inputStream = file.getInputStream();
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

        //videoService.updateVideo(videoId, uploadPathDB);

        return IMoocJSONResult.ok(uploadPathDB);
    }

    //paramType：参数放在哪个地方
    @ApiOperation(value="展示全部", notes="小程序视频首页")
    @ApiImplicitParams({
            @ApiImplicitParam(name="video", value="视频提交", required=true,
                    dataType="Videos", paramType="form"),
            @ApiImplicitParam(name="isSaveRecord", value="1 需要保存热搜词", required=true,
                    dataType="Integer", paramType="form"),
            @ApiImplicitParam(name="type", value="1 按附近排序", required=true,
                    dataType="Integer", paramType="form")
    })
    @PostMapping(value="/showAll")
    public IMoocJSONResult showAll(@RequestBody Videos video,Integer isSaveRecord,
                                   Integer page, Integer type ,Double latitude,Double longitude) throws Exception {

        if (page == null){
            page = 1;
        }
        List<String> videoIds = Lists.newArrayList();

        /**
         * 使用redis-geo类型查询附近的人发送的视频
         */
        if (type.equals(1) && latitude != null && !latitude.equals(0)){
            GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();
            //设置geo查询参数
            RedisGeoCommands.GeoRadiusCommandArgs geoRadiusArgs = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs();
            //查询返回结果包括距离和坐标
            geoRadiusArgs = geoRadiusArgs.includeCoordinates().includeDistance();
            //按查询出的坐标距离中心坐标的距离进行排序
            geoRadiusArgs.sortAscending();
            //限制查询数量
            geoRadiusArgs.limit(100);
            //redis 查询附近5km人发的视频
            GeoResults<RedisGeoCommands.GeoLocation<String>> radiusGeo = geoOps.radius(
                    KEY_PREFIX_GEO,
                    new Circle(new Point(longitude,latitude), new Distance(5, RedisGeoCommands.DistanceUnit.KILOMETERS)),
                    geoRadiusArgs);


            for (GeoResult<RedisGeoCommands.GeoLocation<String>> geoResult : radiusGeo) {
                //这里可以取到附近的视频id
                videoIds.add(geoResult.getContent().getName());
            }
        }

        /**
         * 是否有搜索词传入 - 有的话走es进行分词搜索
         */
        if (StringUtils.isNoneBlank(video.getVideoDesc())){
            //进入es处理搜索请求
            // 在gcmc中搜索带有 "x" 字样的
            MatchQueryBuilder builder = new MatchQueryBuilder("content", video.getVideoDesc());
            // 设置搜索，可以是任何类型的 QueryBuilder
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(builder).minScore(3);
            List<String> idList = esUtil.projectSearch(EsUtil.INDEX_NAME, searchSourceBuilder);
            //求交集
            videoIds = videoIds.stream().filter(item -> idList.contains(item)).collect(Collectors.toList());
        }



        PagedResult result = videoService.getAllVideos(video, isSaveRecord,page, PAGE_SIZE, type, videoIds);
        return IMoocJSONResult.ok(result);
    }

    /**
     * @Description: 我关注的人发的视频
     */
    @ApiOperation(value="展示关注的人视频", notes="我关注的人发的视频")
    @PostMapping("/showMyFollow")
    public IMoocJSONResult showMyFollow(String userId, Integer page) throws Exception {

        if (StringUtils.isBlank(userId)) {
            return IMoocJSONResult.ok();
        }

        if (page == null) {
            page = 1;
        }

        int pageSize = 6;

        PagedResult videosList = videoService.queryMyFollowVideos(userId, page, pageSize);

        return IMoocJSONResult.ok(videosList);
    }

    /**
     * @Description: 我收藏(点赞)过的视频列表
     */
    @ApiOperation(value="收藏(点赞)过的视频", notes="我收藏(点赞)过的视频列表")
    @PostMapping("/showMyLike")
    public IMoocJSONResult showMyLike(String userId, Integer page, Integer pageSize) throws Exception {

        if (StringUtils.isBlank(userId)) {
            return IMoocJSONResult.ok();
        }

        if (page == null) {
            page = 1;
        }

        if (pageSize == null) {
            pageSize = 6;
        }

        PagedResult videosList = videoService.queryMyLikeVideos(userId, page, pageSize);

        return IMoocJSONResult.ok(videosList);
    }

    @ApiOperation(value="查询热搜词", notes="小程序视频查询热搜词")
    @PostMapping(value="/hot")
    public IMoocJSONResult hot() throws Exception {
        return IMoocJSONResult.ok(videoService.getHotwords());
    }

    @ApiOperation(value="关注", notes="小程序视频关注某用户")
    @PostMapping(value="/userLike")
    public IMoocJSONResult userLike(String userId, String videoId, String videoCreaterId)
            throws Exception {
        videoService.userLikeVideo(userId, videoId, videoCreaterId);
        return IMoocJSONResult.ok();
    }

    @ApiOperation(value="取消关注", notes="小程序视频取消关注某用户")
    @PostMapping(value="/userUnLike")
    public IMoocJSONResult userUnLike(String userId, String videoId, String videoCreaterId) throws Exception {
        videoService.userUnLikeVideo(userId, videoId, videoCreaterId);
        return IMoocJSONResult.ok();
    }

    @ApiOperation(value="保存评论", notes="保存小程序视频评论")
    @PostMapping("/saveComment")
    public IMoocJSONResult saveComment(@RequestBody Comments comment,
                                       String fatherCommentId, String toUserId) throws Exception {

        comment.setFatherCommentId(fatherCommentId);
        comment.setToUserId(toUserId);

        videoService.saveComment(comment);
        return IMoocJSONResult.ok();
    }

    @ApiOperation(value="分页得到视频评论", notes="小程序视频分页得到视频评论")
    @PostMapping("/getVideoComments")
    public IMoocJSONResult getVideoComments(String videoId, Integer page, Integer pageSize) throws Exception {

        if (StringUtils.isBlank(videoId)) {
            return IMoocJSONResult.ok();
        }

        // 分页查询视频列表，时间顺序倒序排序
        if (page == null) {
            page = 1;
        }

        if (pageSize == null) {
            pageSize = 10;
        }

        PagedResult list = videoService.getAllComments(videoId, page, pageSize);

        return IMoocJSONResult.ok(list);
    }







}
