package com.neepu.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.neepu.enums.VideoStatusEnum;
import com.neepu.mapper.*;
import com.neepu.pojo.*;
import com.neepu.pojo.vo.CommentsVO;
import com.neepu.pojo.vo.VideosVO;
import com.neepu.service.VideoService;
import com.neepu.utils.PagedResult;
import com.neepu.utils.TimeAgoUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

/**
 * Created by lx on 2019/3/01.
 */
@Service
public class VideoServiceImpl implements VideoService {

    @Autowired
    private VideosMapper videosMapper;
    @Autowired
    private Sid sid;
    @Autowired
    private VideosMapperCustom videosMapperCustom;
    @Autowired
    private SearchRecordsMapper searchRecordsMapper;
    @Autowired
    private  CommentsMapperCustom commentMapperCustom;
    @Autowired
    private UsersLikeVideosMapper usersLikeVideosMapper;
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private CommentsMapper commentMapper;


    @Autowired
    public RedisTemplate<String, String> redisTemplate;

    /**
     *  Propagation.REQUIRED
     * 假如当前正要执行的事务不在另外一个事务里，那么就起一个新的事务
     *
     * 如果ServiceA.methodA已经起了事务，这时调用ServiceB.methodB，ServiceB.methodB看到自己已经运行在ServiceA.methodA的事务内部，就不再起新的事务。
     * 这时只有外部事务并且他们是共用的，所以这时ServiceA.methodA或者ServiceB.methodB无论哪个发生异常methodA和methodB作为一个整体都将一起回滚。
     *
     * @param video
     * @return
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String saveVideo(Videos video) {

        String id = sid.nextShort();
        video.setId(id);
        /**
         * 上传视频后需要经过管理员审核
         */
        //video.setStatus(VideoStatusEnum.AUDIT.getValue());


        video.setStatus(VideoStatusEnum.SUCCESS.getValue());

        videosMapper.insertSelective(video);
        return id;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateVideo(String videoId,String coverPath) {

        Videos videos = new Videos();
        videos.setId(videoId);
        videos.setCoverPath(coverPath);
        videosMapper.updateByPrimaryKeySelective(videos);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public PagedResult getAllVideos(Videos video,Integer isSaveRecord,Integer page,Integer pageSize, Integer type, List<String> videoIds) {

        //保存热搜词
        String desc = video.getVideoDesc();
        String userId = video.getUserId();

        if (isSaveRecord != null && isSaveRecord == 1){
            SearchRecords searchRecords = new SearchRecords();
            String id = sid.nextShort();
            searchRecords.setId(id);
            searchRecords.setContent(desc);
            searchRecordsMapper.insert(searchRecords);
        }

        PageHelper.startPage(page,pageSize);
        List<VideosVO> list = videosMapperCustom.queryAllVideos(desc,userId,videoIds);

        PageInfo<VideosVO> pageInfo = new PageInfo <>(list);

        PagedResult pagedResult = new PagedResult();
        pagedResult.setPage(page);
        pagedResult.setTotal(pageInfo.getPages());
        pagedResult.setRows(list);
        pagedResult.setRecords(pageInfo.getTotal());
        return pagedResult;
    }

    @Override
    public List <String> getHotwords() {
        return searchRecordsMapper.getHotwords();
    }

    //支持当前事务，如果当前没有事务，就以非事务方式执行
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedResult queryMyLikeVideos(String userId, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        List<VideosVO> list = videosMapperCustom.queryMyLikeVideos(userId);

        PageInfo<VideosVO> pageList = new PageInfo<>(list);

        PagedResult pagedResult = new PagedResult();
        pagedResult.setTotal(pageList.getPages());
        pagedResult.setRows(list);
        pagedResult.setPage(page);
        pagedResult.setRecords(pageList.getTotal());

        return pagedResult;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedResult queryMyFollowVideos(String userId, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        List<VideosVO> list = videosMapperCustom.queryMyFollowVideos(userId);

        PageInfo<VideosVO> pageList = new PageInfo<>(list);

        PagedResult pagedResult = new PagedResult();
        pagedResult.setTotal(pageList.getPages());
        pagedResult.setRows(list);
        pagedResult.setPage(page);
        pagedResult.setRecords(pageList.getTotal());

        return pagedResult;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void userLikeVideo(String userId, String videoId, String videoCreaterId) {
        // 1. 保存用户和视频的喜欢点赞关联关系表
        String likeId = sid.nextShort();
        UsersLikeVideos ulv = new UsersLikeVideos();
        ulv.setId(likeId);
        ulv.setUserId(userId);
        ulv.setVideoId(videoId);
        usersLikeVideosMapper.insert(ulv);

        // 2. 视频喜欢数量累加
        videosMapperCustom.addVideoLikeCount(videoId);

        // 3. 用户受喜欢数量的累加
        usersMapper.addReceiveLikeCount(videoCreaterId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void userUnLikeVideo(String userId, String videoId, String videoCreaterId) {
        // 1. 删除用户和视频的喜欢点赞关联关系表

        Example example = new Example(UsersLikeVideos.class);
        Example.Criteria criteria = example.createCriteria();

        criteria.andEqualTo("userId", userId);
        criteria.andEqualTo("videoId", videoId);

        usersLikeVideosMapper.deleteByExample(example);

        // 2. 视频喜欢数量累减
        videosMapperCustom.reduceVideoLikeCount(videoId);

        // 3. 用户受喜欢数量的累减
        usersMapper.reduceReceiveLikeCount(videoCreaterId);

    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveComment(Comments comment) {
        String id = sid.nextShort();
        comment.setId(id);
        comment.setCreateTime(new Date());
        commentMapper.insert(comment);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedResult getAllComments(String videoId, Integer page, Integer pageSize) {

        PageHelper.startPage(page, pageSize);

        List<CommentsVO> list = commentMapperCustom.queryComments(videoId);

        for (CommentsVO c : list) {
            String timeAgo = TimeAgoUtils.format(c.getCreateTime());
            c.setTimeAgoStr(timeAgo);
        }

        PageInfo<CommentsVO> pageList = new PageInfo<>(list);

        PagedResult grid = new PagedResult();
        grid.setTotal(pageList.getPages());
        grid.setRows(list);
        grid.setPage(page);
        grid.setRecords(pageList.getTotal());

        return grid;
    }
}
