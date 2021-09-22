package com.eagle.srb.core.service;

import com.eagle.srb.core.pojo.entity.LendItemReturn;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 标的出借回款记录表 服务类
 * </p>
 *
 * @author eagle
 * @since 2021-09-02
 */
public interface LendItemReturnService extends IService<LendItemReturn> {

    List<LendItemReturn> selectByLendId(Long lendId, Long userId);
}
