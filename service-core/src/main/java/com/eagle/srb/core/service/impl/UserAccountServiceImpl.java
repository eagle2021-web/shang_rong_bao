package com.eagle.srb.core.service.impl;

import com.eagle.srb.core.pojo.entity.UserAccount;
import com.eagle.srb.core.mapper.UserAccountMapper;
import com.eagle.srb.core.service.UserAccountService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户账户 服务实现类
 * </p>
 *
 * @author eagle
 * @since 2021-09-02
 */
@Service
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements UserAccountService {

}
