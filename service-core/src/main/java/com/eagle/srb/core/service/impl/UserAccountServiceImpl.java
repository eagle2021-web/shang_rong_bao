package com.eagle.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eagle.common.exception.Assert;
import com.eagle.common.result.ResponseEnum;
import com.eagle.srb.base.dto.SmsDTO;
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
import com.eagle.srb.core.service.UserBindService;
import com.eagle.srb.core.service.UserInfoService;
import com.eagle.srb.core.util.LendNoUtils;
import com.eagle.srb.rabbitutil.constant.MQConst;
import com.eagle.srb.rabbitutil.service.MQService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements UserAccountService {

    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private TransFlowService transFlowService;
    @Resource
    private UserAccountService userAccountService;
    @Resource
    private UserBindService userBindService;
    @Resource
    private UserInfoService userInfoService;
    @Resource
    private MQService mqService;

    /**
     * 确认充值，向hfb提交表单
     * @param chargeAmt 充值金额
     * @param userId 用户id
     * @return 表单
     */
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
        paramMap.put("timestamp", RequestHelper.getTimestamp());//时间戳
        paramMap.put("sign", RequestHelper.getSign(paramMap));//签名

        return FormHelper.buildForm(HfbConst.RECHARGE_URL, paramMap);
    }

    /**
     * 处理充值通知
     *
     * @param paramMap 参数列表
     * @return 处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String notify(Map<String, Object> paramMap) {


        String agentBillNo = (String) paramMap.get("agentBillNo");

        //幂等性判断？标准 =  //判断交易流水是否存在
        boolean isSave = transFlowService.isSaveTransFlow(agentBillNo);
        if (isSave) {
            log.warn("幂等性返回");
            return "success";
        }

        //账户处理
        String bindCode = (String) paramMap.get("bindCode");
        String chargeAmt = (String) paramMap.get("chargeAmt");
        baseMapper.updateAccount(bindCode, new BigDecimal(chargeAmt), new BigDecimal(0));

        //记录账户流水
        TransFlowBO transFlowBO = new TransFlowBO(
                agentBillNo,
                bindCode,
                new BigDecimal(chargeAmt),
                TransTypeEnum.RECHARGE,
                "充值啦");

        transFlowService.saveTransFlow(transFlowBO);

        //发消息
        String mobile = userInfoService.getMobileByBindCode(bindCode);
        SmsDTO smsDTO = new SmsDTO();
        smsDTO.setMobile(mobile);
        smsDTO.setMessage("充值成功");
        mqService.sendMessage(
                MQConst.EXCHANGE_TOPIC_SMS,//交换机
                MQConst.ROUTING_SMS_ITEM,//路由
                smsDTO
        );
        return "success";
    }

    @Override
    public BigDecimal getAccount(Long userId) {

        QueryWrapper<UserAccount> userAccountQueryWrapper = new QueryWrapper<>();
        userAccountQueryWrapper.eq("user_id", userId);
        UserAccount userAccount = baseMapper.selectOne(userAccountQueryWrapper);
        return userAccount.getAmount();
    }

    /**
     * 提现确认
     *
     * @param fetchAmt 金额
     * @param userId   用户id
     * @return 返回表单
     */
    @Override
    public String commitWithdraw(BigDecimal fetchAmt, Long userId) {
        //用户账户余额
        BigDecimal amount = userAccountService.getAccount(userId);
        Assert.isTrue(amount.doubleValue() >= fetchAmt.doubleValue(),
                ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR);

        String bindCode = userBindService.getBindCodeByUserId(userId);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentBillNo", LendNoUtils.getWithdrawNo());
        paramMap.put("bindCode", bindCode);
        paramMap.put("fetchAmt", fetchAmt);
        paramMap.put("feeAmt", new BigDecimal(0));
        paramMap.put("notifyUrl", HfbConst.WITHDRAW_NOTIFY_URL);
        paramMap.put("returnUrl", HfbConst.WITHDRAW_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);

        //构建自动提交表单
        return FormHelper.buildForm(HfbConst.WITHDRAW_URL, paramMap);
    }

    @Override
    public void notifyWithdraw(Map<String, Object> paramMap) {

        //幂等判断
        log.info("提现成功");
        String agentBillNo = (String) paramMap.get("agentBillNo");
        boolean result = transFlowService.isSaveTransFlow(agentBillNo);
        if (result) {
            log.warn("幂等性返回");
            return;
        }

        //账户同步
        String bindCode = (String) paramMap.get("bindCode");
        String fetchAmt = (String) paramMap.get("fetchAmt");
        baseMapper.updateAccount(bindCode, new BigDecimal("-" + fetchAmt), new BigDecimal(0));

        //交易流水
        TransFlowBO transFlowBO = new TransFlowBO(
                agentBillNo,
                bindCode,
                new BigDecimal(fetchAmt),
                TransTypeEnum.WITHDRAW,
                "提现啦");
        transFlowService.saveTransFlow(transFlowBO);
    }
}
