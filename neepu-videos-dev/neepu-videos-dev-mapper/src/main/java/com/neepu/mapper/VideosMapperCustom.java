package com.neepu.mapper;

import com.neepu.pojo.Videos;
import com.neepu.pojo.vo.VideosVO;
import com.neepu.utils.MyMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

//区分自定义mapper与自动生成mapper区别
public interface VideosMapperCustom extends MyMapper<Videos> {

    /**
     * @Description: 条件查询所有视频列表
     */
     List<VideosVO> queryAllVideos(@Param("videoDesc") String videoDesc,
                                         @Param("userId") String userId,
                                         @Param("videoIds") List<String> videoIds
                                         );

    /**
     * @Description: 查询关注的视频
     */
     List<VideosVO> queryMyFollowVideos(String userId);

    /**
     * @Description: 查询点赞视频
     */
     List<VideosVO> queryMyLikeVideos(@Param("userId") String userId);

    /**
     * @Description: 对视频喜欢的数量进行累加
     */
     void addVideoLikeCount(String videoId);

    /**
     * @Description: 对视频喜欢的数量进行累减
     */
     void reduceVideoLikeCount(String videoId);
}