package com.eagle.srb.core.service.impl;

import com.eagle.srb.core.pojo.entity.UserLoginRecord;
import com.eagle.srb.core.mapper.UserLoginRecordMapper;
import com.eagle.srb.core.service.UserLoginRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户登录记录表 服务实现类
 * </p>
 *
 * @author eagle
 * @since 2021-09-02
 */
@Service
public class UserLoginRecordServiceImpl extends ServiceImpl<UserLoginRecordMapper, UserLoginRecord> implements UserLoginRecordService {

}
