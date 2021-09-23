package com.eagle.srb.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eagle.srb.core.pojo.entity.LendReturn;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 还款记录表 服务类
 * </p>
 *
 * @author eagle
 * @since 2021-09-02
 */
public interface LendReturnService extends IService<LendReturn> {

    List<LendReturn> selectByLendId(Long lendId);

    String commitReturn(Long lendReturnId, Long userId);

    void notify(Map<String, Object> paramMap);
}
