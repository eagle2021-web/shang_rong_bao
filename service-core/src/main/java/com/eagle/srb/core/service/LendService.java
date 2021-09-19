package com.eagle.srb.core.service;

import com.eagle.srb.core.pojo.entity.BorrowInfo;
import com.eagle.srb.core.pojo.entity.Lend;
import com.baomidou.mybatisplus.extension.service.IService;
import com.eagle.srb.core.pojo.vo.BorrowInfoApprovalVO;

/**
 * <p>
 * 标的准备表 服务类
 * </p>
 *
 * @author eagle
 * @since 2021-09-02
 */
public interface LendService extends IService<Lend> {

    void createLend(BorrowInfoApprovalVO borrowInfoApprovalVO, BorrowInfo borrowInfo);
}
