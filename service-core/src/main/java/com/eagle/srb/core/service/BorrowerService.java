package com.eagle.srb.core.service;

import com.eagle.srb.core.pojo.entity.Borrower;
import com.baomidou.mybatisplus.extension.service.IService;
import com.eagle.srb.core.pojo.vo.BorrowerVO;

/**
 * <p>
 * 借款人 服务类
 * </p>
 *
 * @author eagle
 * @since 2021-09-02
 */
public interface BorrowerService extends IService<Borrower> {

    void saveBorrowerVOByUserId(BorrowerVO borrowerVO, Long userId);

    Integer getStatusByUserId(Long userId);
}
