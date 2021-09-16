package com.eagle.srb.core.service;

import com.eagle.srb.core.pojo.entity.UserBind;
import com.baomidou.mybatisplus.extension.service.IService;
import com.eagle.srb.core.pojo.vo.UserBindVO;

/**
 * <p>
 * 用户绑定表 服务类
 * </p>
 *
 * @author eagle
 * @since 2021-09-02
 */
public interface UserBindService extends IService<UserBind> {

    String commitBindUser(UserBindVO userBindVO, Long userId);
}
