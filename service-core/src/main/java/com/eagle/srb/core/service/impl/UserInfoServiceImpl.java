package com.eagle.srb.core.service.impl;

import com.eagle.srb.core.pojo.entity.UserInfo;
import com.eagle.srb.core.mapper.UserInfoMapper;
import com.eagle.srb.core.service.UserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户基本信息 服务实现类
 * </p>
 *
 * @author eagle
 * @since 2021-09-02
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

}
