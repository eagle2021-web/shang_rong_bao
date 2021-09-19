package com.eagle.srb.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eagle.srb.core.enums.LendStatusEnum;
import com.eagle.srb.core.mapper.LendMapper;
import com.eagle.srb.core.pojo.entity.BorrowInfo;
import com.eagle.srb.core.pojo.entity.Lend;
import com.eagle.srb.core.pojo.vo.BorrowInfoApprovalVO;
import com.eagle.srb.core.service.LendService;
import com.eagle.srb.core.util.LendNoUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * <p>
 * 标的准备表 服务实现类
 * </p>
 *
 * @author eagle
 * @since 2021-09-02
 */
@Service
@Slf4j
public class LendServiceImpl extends ServiceImpl<LendMapper, Lend> implements LendService {

    @Override
    public void createLend(BorrowInfoApprovalVO borrowInfoApprovalVO, BorrowInfo borrowInfo) {
        Lend lend = new Lend() {{
            setUserId(borrowInfo.getUserId());
            setBorrowInfoId(borrowInfo.getId());
            setLendNo(LendNoUtils.getLendNo());
            setTitle(borrowInfoApprovalVO.getTitle());
            setAmount(borrowInfo.getAmount());
            setPeriod(borrowInfo.getPeriod());
            setLendYearRate(borrowInfoApprovalVO.getLendYearRate().divide(new BigDecimal(100)));
            setServiceRate(borrowInfoApprovalVO.getServiceRate().divide(new BigDecimal(100)));
            setReturnMethod(borrowInfo.getReturnMethod());
            setLowestAmount(new BigDecimal(100)); //最低投资金额
            setInvestAmount(new BigDecimal(0)); //已经投资金额
            setInvestNum(0);//已投人数
            setPublishDate(LocalDateTime.now());
        }};
        //起息日期
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate lendStartDate = LocalDate.parse(borrowInfoApprovalVO.getLendStartDate(), dateTimeFormatter);
        lend.setLendStartDate(lendStartDate);
        //结束日期
        LocalDate localEndDate = lendStartDate.plusMonths(borrowInfo.getPeriod());
        lend.setLendEndDate(localEndDate);

        //标的描述
        lend.setLendInfo(borrowInfoApprovalVO.getLendInfo());
        //平台预期收益 = 年化 / 12 * 期数
        //8, BigDecimal.ROUND_DOWN 保留8位，向下取整
        BigDecimal monthRate = lend.getServiceRate().divide(new BigDecimal(12), 8, BigDecimal.ROUND_DOWN);
        //当前period下利率
        BigDecimal multiply = monthRate.multiply(new BigDecimal(lend.getPeriod()));
        BigDecimal expectedProfit = lend.getAmount().multiply(monthRate);
        lend.setExpectAmount(expectedProfit);

        //当前实际收益
        lend.setRealAmount(new BigDecimal(0));
        //当前lend状态
        lend.setStatus(LendStatusEnum.INVEST_RUN.getStatus());
        //审核时间
        lend.setCheckTime(LocalDateTime.now());
        lend.setCheckAdminId(1L);

        log.info("monthRate = " + monthRate);
        log.info("LendServiceImpl创建标的" + lend);
        baseMapper.insert(lend);
    }
}
