package com.neepu.service;

import com.neepu.pojo.Bgm;

import java.util.List;

/**
 * Created by xyzzg on 2018/8/12.
 */
public interface BgmService {

    List<Bgm> queryBgmList();

    Bgm queryBgmById(String bgmId);
}
