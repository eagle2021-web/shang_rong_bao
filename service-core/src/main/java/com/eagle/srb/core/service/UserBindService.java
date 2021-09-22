package com.eagle.srb.core.service;

import com.eagle.srb.core.pojo.entity.UserBind;
import com.baomidou.mybatisplus.extension.service.IService;
import com.eagle.srb.core.pojo.vo.UserBindVO;

import java.util.Map;

/**
 * <p>
 * 用户绑定表 服务类
 * </p>
 *
 * @author eagle
 * @since 2021-09-02
 */
public interface UserBindService extends IService<UserBind> {

    /**
     *
     * @param userBindVO 前端传过来的对象
     * @param userId 登录用户的id
     * @return 返回给前端html字符串
     */
    String commitBindUser(UserBindVO userBindVO, Long userId);

    void notify(Map<String, Object> paramMap);

    String getBindCodeByUserId(Long investUserId);
}
