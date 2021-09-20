package com.eagle.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eagle.srb.core.mapper.TransFlowMapper;
import com.eagle.srb.core.mapper.UserInfoMapper;
import com.eagle.srb.core.pojo.bo.TransFlowBO;
import com.eagle.srb.core.pojo.entity.TransFlow;
import com.eagle.srb.core.pojo.entity.UserInfo;
import com.eagle.srb.core.service.TransFlowService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * 交易流水表 服务实现类
 * </p>
 *
 * @author eagle
 * @since 2021-09-02
 */
@Service
public class TransFlowServiceImpl extends ServiceImpl<TransFlowMapper, TransFlow> implements TransFlowService {
    @Resource
    private UserInfoMapper userInfoMapper;

    /**
     * 记录流水号到数据库表trans_flow
     * @param transFlowBO 流水对象
     */
    @Override
    public void saveTransFlow(TransFlowBO transFlowBO) {

        String bindCode = transFlowBO.getBindCode();
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("bind_code", bindCode);
        UserInfo userInfo = userInfoMapper.selectOne(userInfoQueryWrapper);

        TransFlow transFlow = new TransFlow();
        transFlow.setTransAmount(transFlowBO.getAmount());//档次流水涉及金额
        transFlow.setMemo(transFlowBO.getMemo());//短comment
        transFlow.setTransTypeName(transFlowBO.getTransTypeEnum().getTransTypeName());//流失类型比如充值
        transFlow.setTransType(transFlowBO.getTransTypeEnum().getTransType());//流水类型比如充值对应 1
        transFlow.setTransNo(transFlowBO.getAgentBillNo());//流水号
        transFlow.setUserId(userInfo.getId());//用户id
        transFlow.setUserName(userInfo.getName());//用户名
        baseMapper.insert(transFlow);
    }

    @Override
    public boolean isSaveTransFlow(String agentBillNo) {
        QueryWrapper<TransFlow> transFlowQueryWrapper = new QueryWrapper<>();
        transFlowQueryWrapper.eq("trans_no", agentBillNo);
        Integer count = baseMapper.selectCount(transFlowQueryWrapper);
        return count > 0;
    }
}
