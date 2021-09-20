package com.eagle.srb.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eagle.srb.core.enums.TransTypeEnum;
import com.eagle.srb.core.hfb.FormHelper;
import com.eagle.srb.core.hfb.HfbConst;
import com.eagle.srb.core.hfb.RequestHelper;
import com.eagle.srb.core.mapper.UserAccountMapper;
import com.eagle.srb.core.mapper.UserInfoMapper;
import com.eagle.srb.core.pojo.bo.TransFlowBO;
import com.eagle.srb.core.pojo.entity.UserAccount;
import com.eagle.srb.core.pojo.entity.UserInfo;
import com.eagle.srb.core.service.TransFlowService;
import com.eagle.srb.core.service.UserAccountService;
import com.eagle.srb.core.util.LendNoUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户账户 服务实现类
 * </p>
 *
 * @author eagle
 * @since 2021-09-02
 */
@Service
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements UserAccountService {

    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private TransFlowService transFlowService;

    @Override
    public String commitCharge(BigDecimal chargeAmt, Long userId) {
        //获取充值人绑定协议号
        UserInfo userInfo = userInfoMapper.selectById(userId);
        String bindCode = userInfo.getBindCode();

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentBillNo", LendNoUtils.getChargeNo());//生成一个流水号
        paramMap.put("bindCode", bindCode);//原来绑定borrower时的bindCode
        paramMap.put("chargeAmt", chargeAmt);//充值金额
        paramMap.put("feeAmt", new BigDecimal("0"));//冻结金额
        paramMap.put("notifyUrl", HfbConst.RECHARGE_NOTIFY_URL);//同步通知url
        paramMap.put("returnUrl", HfbConst.RECHARGE_RETURN_URL);//返回地址url
        paramMap.put("timestamp", RequestHelper.getTimestamp());//
        paramMap.put("sign", RequestHelper.getSign(paramMap));


        return FormHelper.buildForm(HfbConst.RECHARGE_URL, paramMap);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String notify(Map<String, Object> paramMap) {


        String agentBillNo = (String)paramMap.get("agentBillNo");

        //幂等性判断？标准 =  //判断交易流水是否存在
        boolean isSave = transFlowService.isSaveTransFlow(agentBillNo);
        if(isSave){
            log.warn("幂等性返回");
            return "success";
        }

        //账户处理
        String bindCode = (String)paramMap.get("bindCode");
        String chargeAmt = (String)paramMap.get("chargeAmt");
        baseMapper.updateAccount(bindCode, new BigDecimal(chargeAmt), new BigDecimal(0));

        //记录账户流水
        TransFlowBO transFlowBO = new TransFlowBO(
                agentBillNo,
                bindCode,
                new BigDecimal(chargeAmt),
                TransTypeEnum.RECHARGE,
                "充值啦");

        transFlowService.saveTransFlow(transFlowBO);

        return "success";
    }
}
