package com.neepu.service;

import com.neepu.pojo.Comments;
import com.neepu.pojo.Videos;
import com.neepu.utils.PagedResult;

import java.util.List;

/**
 * Created by xyzzg on 2018/8/12.
 */
public interface VideoService {

    String saveVideo(Videos video);

    void updateVideo(String videoId,String coverPath);

    //分页查询视频列表
    PagedResult getAllVideos(Videos video,Integer isSaveRecord,Integer page,Integer pageSize, Integer type, List<String> videoIds);

    /**
     * @Description: 查询我喜欢的视频列表
     */
    public PagedResult queryMyLikeVideos(String userId, Integer page, Integer pageSize);

    /**
     * @Description: 查询我关注的人的视频列表
     */
    public PagedResult queryMyFollowVideos(String userId, Integer page, Integer pageSize);

    /**
     * @Description: 获取热搜词列表
     */
    public List<String> getHotwords();

    /**
     * @Description: 用户喜欢/点赞视频
     */
    public void userLikeVideo(String userId, String videoId, String videoCreaterId);

    /**
     * @Description: 用户不喜欢/取消点赞视频
     */
    public void userUnLikeVideo(String userId, String videoId, String videoCreaterId);

    /**
     * @Description: 用户留言
     */
    public void saveComment(Comments comment);

    /**
     * @Description: 留言分页
     */
    public PagedResult getAllComments(String videoId, Integer page, Integer pageSize);


}
