package com.eagle.srb.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.eagle.common.result.R;
import com.eagle.srb.core.pojo.entity.UserInfo;
import com.eagle.srb.core.pojo.query.UserInfoQuery;
import com.eagle.srb.core.pojo.vo.LoginVO;
import com.eagle.srb.core.pojo.vo.RegisterVO;
import com.eagle.srb.core.pojo.vo.UserInfoVO;

/**
 * <p>
 * 用户基本信息 服务类
 * </p>
 *
 * @author eagle
 * @since 2021-09-02
 */
public interface UserInfoService extends IService<UserInfo> {

    void register(RegisterVO registerVO);

    UserInfoVO login(LoginVO loginVO, String ip);


    IPage<UserInfo> listPage(Page<UserInfo> pageParam, UserInfoQuery userInfoQuery);

    void lock(Long id, Integer status);


    R checkMobile(String mobile);
}
