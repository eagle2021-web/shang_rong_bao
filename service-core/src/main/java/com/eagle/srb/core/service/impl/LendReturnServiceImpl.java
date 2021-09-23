package com.eagle.srb.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eagle.common.exception.Assert;
import com.eagle.common.result.ResponseEnum;
import com.eagle.srb.core.enums.LendStatusEnum;
import com.eagle.srb.core.enums.TransTypeEnum;
import com.eagle.srb.core.hfb.FormHelper;
import com.eagle.srb.core.hfb.HfbConst;
import com.eagle.srb.core.hfb.RequestHelper;
import com.eagle.srb.core.mapper.*;
import com.eagle.srb.core.pojo.bo.TransFlowBO;
import com.eagle.srb.core.pojo.entity.Lend;
import com.eagle.srb.core.pojo.entity.LendItem;
import com.eagle.srb.core.pojo.entity.LendItemReturn;
import com.eagle.srb.core.pojo.entity.LendReturn;
import com.eagle.srb.core.service.*;
import com.eagle.srb.core.util.LendNoUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 还款记录表 服务实现类
 * </p>
 *
 * @author eagle
 * @since 2021-09-20
 */
@SuppressWarnings("unused")
@Service
@Slf4j
public class LendReturnServiceImpl extends ServiceImpl<LendReturnMapper, LendReturn> implements LendReturnService {

    @Resource
    private LendMapper lendMapper;

    @Resource
    private UserBindService userBindService;

    @Resource
    private UserAccountService userAccountService;

    @Resource
    private LendItemReturnService lendItemReturnService;

    @Resource
    private TransFlowService transFlowService;

    @Resource
    private UserAccountMapper userAccountMapper;

    @Resource
    private LendItemReturnMapper lendItemReturnMapper;

    @Resource
    private LendItemMapper lendItemMapper;

    @Override
    public List<LendReturn> selectByLendId(Long lendId) {
        QueryWrapper<LendReturn> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("lend_id", lendId);
        return baseMapper.selectList(queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String commitReturn(Long lendReturnId, Long userId) {

        //还款记录
        LendReturn lendReturn = baseMapper.selectById(lendReturnId);//某一期id

        //获取用户余额
        BigDecimal amount = userAccountService.getAccount(userId);//用户id
        Assert.isTrue(amount.doubleValue() >= lendReturn.getTotal().doubleValue(),
                ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR);//断言钱足够

        //标的记录
        Lend lend = lendMapper.selectById(lendReturn.getLendId());//标的id
        //获取还款人的绑定协议号
        String bindCode = userBindService.getBindCodeByUserId(userId);//用户id

        //组装参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        //商户商品名称
        paramMap.put("agentGoodsName", lend.getTitle());
        //某期还款批次号
        paramMap.put("agentBatchNo", lendReturn.getReturnNo());
        //还款人绑定协议号
        paramMap.put("fromBindCode", bindCode);
        //还款总额
        paramMap.put("totalAmt", lendReturn.getTotal());
        paramMap.put("note", "");
        //还款明细
        List<Map<String, Object>> lendItemReturnDetailList = lendItemReturnService.addReturnDetail(lendReturnId);
        paramMap.put("data", JSONObject.toJSONString(lendItemReturnDetailList));

        paramMap.put("voteFeeAmt", new BigDecimal(0));
        paramMap.put("notifyUrl", HfbConst.BORROW_RETURN_NOTIFY_URL);
        paramMap.put("returnUrl", HfbConst.BORROW_RETURN_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);
        //构建自动提交表单
        return FormHelper.buildForm(HfbConst.BORROW_RETURN_URL, paramMap);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void notify(Map<String, Object> paramMap) {

        log.info("hfb还款成功，处理通知");

        //还款编号
        String agentBatchNo = (String)paramMap.get("agentBatchNo");//某一期还款的流水号

        boolean result = transFlowService.isSaveTransFlow(agentBatchNo);
        if(result){
            log.warn("幂等性返回");
            return;
        }

        //获取还款数据
        QueryWrapper<LendReturn> lendReturnQueryWrapper = new QueryWrapper<>();
        lendReturnQueryWrapper.eq("return_no", agentBatchNo);
        LendReturn lendReturn = baseMapper.selectOne(lendReturnQueryWrapper);

        //更新还款状态
        String voteFeeAmt = (String)paramMap.get("voteFeeAmt");
        lendReturn.setStatus(1);
        lendReturn.setFee(new BigDecimal(voteFeeAmt));
        lendReturn.setRealReturnTime(LocalDateTime.now());
        baseMapper.updateById(lendReturn);

        //更新标的信息
        Lend lend = lendMapper.selectById(lendReturn.getLendId());
        //如果是最后一次还款，那么就更新标的状态
        if(lendReturn.getLast()){
            lend.setStatus(LendStatusEnum.PAY_OK.getStatus());//PAY_OK(3, "已结清"),
            lendMapper.updateById(lend);//更新
        }

        //还款账号转出金额
        BigDecimal totalAmt = new BigDecimal((String)paramMap.get("totalAmt"));//本金+利息
        String bindCode = userBindService.getBindCodeByUserId(lendReturn.getUserId());//根据还款人id得到绑定协议号
        userAccountMapper.updateAccount(bindCode, totalAmt.negate(), new BigDecimal(0));//还款人扣款

        //还款流水
        TransFlowBO transFlowBO = new TransFlowBO(
                agentBatchNo,//某期还款批次号
                bindCode,//还款人绑定协议号
                totalAmt,//还款本金+利息
                TransTypeEnum.RETURN_DOWN,//流水类型
                "借款人还款扣减，项目编号：" + lend.getLendNo() + "，项目名称：" + lend.getTitle());
        transFlowService.saveTransFlow(transFlowBO);//保存流水

        //回款明细的获取。
        List<LendItemReturn> lendItemReturnList = lendItemReturnService
                .selectLendItemReturnList(lendReturn.getId());//当前还款对应所有投资人的回款列表
        lendItemReturnList.forEach(item -> {//遍历所有投资人对应某期回款列表
            //更新回款状态
            item.setStatus(1);//已回款 1
            item.setRealReturnTime(LocalDateTime.now());//回款时间戳
            lendItemReturnMapper.updateById(item);//更新

            //更新出借信息
            LendItem lendItem = lendItemMapper.selectById(item.getLendItemId());//一次投资，并非回款id
            lendItem.setRealAmount(lendItem.getRealAmount().add(item.getInterest())); //+利息，动态的实际收益
            lendItemMapper.updateById(lendItem);//更新投资获利状态

            // 投资账号转入金额
            String investBindCode = userBindService.getBindCodeByUserId(item.getInvestUserId());
            userAccountMapper.updateAccount(investBindCode, item.getTotal(), new BigDecimal(0));//投资账户转入金额

            //回款流水
            TransFlowBO investTransFlowBO = new TransFlowBO(
                    LendNoUtils.getReturnItemNo(),
                    investBindCode,
                    item.getTotal(),
                    TransTypeEnum.INVEST_BACK,
                    "还款到账，项目编号：" + lend.getLendNo() + "，项目名称：" + lend.getTitle());
            transFlowService.saveTransFlow(investTransFlowBO);
        });
    }
}
