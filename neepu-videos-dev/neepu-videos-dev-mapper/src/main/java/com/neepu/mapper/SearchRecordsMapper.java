package com.neepu.mapper;

import com.neepu.pojo.SearchRecords;
import com.neepu.utils.MyMapper;

import java.util.List;

public interface SearchRecordsMapper extends MyMapper<SearchRecords> {

    List<String> getHotwords();
}