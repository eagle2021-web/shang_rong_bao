package com.eagle.srb.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eagle.srb.core.pojo.bo.TransFlowBO;
import com.eagle.srb.core.pojo.entity.TransFlow;

/**
 * <p>
 * 交易流水表 服务类
 * </p>
 *
 * @author eagle
 * @since 2021-09-02
 */
public interface TransFlowService extends IService<TransFlow> {
    void saveTransFlow(TransFlowBO transFlowBO);

    boolean isSaveTransFlow(String agentBillNo);
}
