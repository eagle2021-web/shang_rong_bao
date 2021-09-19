package com.eagle.srb.core.service;

import com.eagle.srb.core.pojo.entity.BorrowInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.eagle.srb.core.pojo.vo.BorrowInfoApprovalVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 借款信息表 服务类
 * </p>
 *
 * @author eagle
 * @since 2021-09-02
 */
public interface BorrowInfoService extends IService<BorrowInfo> {

    BigDecimal getBorrowAmount(Long userId);

    Integer getStatusByUserId(Long userId);

    void saveBorrowInfo(BorrowInfo borrowInfo, Long userId);

    List<BorrowInfo> selectList();

    Map<String, Object> getBorrowInfoDetail(Long id);

    void approval(BorrowInfoApprovalVO borrowInfoApprovalVO);
}
