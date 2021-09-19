package com.eagle.srb.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eagle.srb.core.pojo.entity.BorrowInfo;

import java.util.List;

/**
 * <p>
 * 借款信息表 Mapper 接口
 * </p>
 *
 * @author eagle
 * @since 2021-09-02
 */
public interface BorrowInfoMapper extends BaseMapper<BorrowInfo> {

    List<BorrowInfo> selectBorrowInfoList();
}
