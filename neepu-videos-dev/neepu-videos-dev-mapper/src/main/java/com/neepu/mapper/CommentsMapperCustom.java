package com.neepu.mapper;

import java.util.List;

import com.neepu.pojo.Comments;
import com.neepu.pojo.vo.CommentsVO;
import com.neepu.utils.MyMapper;

public interface CommentsMapperCustom extends MyMapper<Comments> {
	
	public List<CommentsVO> queryComments(String videoId);
}