package com.eagle.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eagle.common.exception.Assert;
import com.eagle.common.result.ResponseEnum;
import com.eagle.srb.core.enums.BorrowInfoStatusEnum;
import com.eagle.srb.core.enums.BorrowerStatusEnum;
import com.eagle.srb.core.enums.UserBindEnum;
import com.eagle.srb.core.mapper.BorrowInfoMapper;
import com.eagle.srb.core.mapper.IntegralGradeMapper;
import com.eagle.srb.core.mapper.UserInfoMapper;
import com.eagle.srb.core.pojo.entity.BorrowInfo;
import com.eagle.srb.core.pojo.entity.IntegralGrade;
import com.eagle.srb.core.pojo.entity.UserInfo;
import com.eagle.srb.core.service.BorrowInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 借款信息表 服务实现类
 * </p>
 *
 * @author eagle
 * @since 2021-09-02
 */
@Service
@Slf4j
public class BorrowInfoServiceImpl extends ServiceImpl<BorrowInfoMapper, BorrowInfo> implements BorrowInfoService {
    @Resource
    private IntegralGradeMapper integralGradeMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    public BigDecimal getBorrowAmount(Long userId) {
        //获取用户积分
        UserInfo userInfo = userInfoMapper.selectById(userId);
        Assert.notNull(userInfo, ResponseEnum.LOGIN_MOBILE_ERROR);
        Integer integral = userInfo.getIntegral();

        //根据积分查询额度
        QueryWrapper<IntegralGrade> integralGradeQueryWrapper = new QueryWrapper<>();
        integralGradeQueryWrapper
                .le("integral_start", integral)
                .ge("integral_end", integral);
        IntegralGrade integralGrade = integralGradeMapper.selectOne(integralGradeQueryWrapper);
        if (integralGrade == null) {
            return new BigDecimal("0");
        }

        return integralGrade.getBorrowAmount();
    }

    @Override
    public Integer getStatusByUserId(Long userId) {
        QueryWrapper<BorrowInfo> borrowInfoQueryWrapper = new QueryWrapper<>();
        borrowInfoQueryWrapper.select("status").eq("user_id", userId);
        List<Object> objects = baseMapper.selectObjs(borrowInfoQueryWrapper);
        if(objects.size() == 0){
            return BorrowInfoStatusEnum.NO_AUTH.getStatus();
        }

        Integer status = (Integer)objects.get(0);
        return status;
    }

    /**
     * 判断表单信息是否合法，合法则存储到数据库表borrower_info
     * @param borrowInfo 表单信息
     * @param userId 用户id
     */
    @Override
    public void saveBorrowInfo(BorrowInfo borrowInfo, Long userId) {
        //获取userInfo信息
        UserInfo userInfo = userInfoMapper.selectById(userId);

        //判断用户绑定状态
        Assert.isTrue(
                userInfo.getBindStatus().intValue() == UserBindEnum.BIND_OK.getStatus().intValue(),
                ResponseEnum.USER_NO_BIND_ERROR);

        //判断借款人额度申请状态
        Assert.isTrue(
                userInfo.getBorrowAuthStatus().intValue() == BorrowerStatusEnum.AUTH_OK.getStatus().intValue(),
                ResponseEnum.USER_NO_AMOUNT_ERROR);

        //判断借款人额度是否充足
        BigDecimal borrowAmount = this.getBorrowAmount(userId);
        Assert.isTrue(
                borrowInfo.getAmount().doubleValue() <= borrowAmount.doubleValue(),
                ResponseEnum.USER_AMOUNT_LESS_ERROR);


        //存储borrowInfo数据
        borrowInfo.setUserId(userId);
        //百分比转小数
        log.info("年利率百分点 = " + borrowInfo.getBorrowYearRate());
        borrowInfo.setBorrowYearRate(borrowInfo.getBorrowYearRate().divide(new BigDecimal(100)));
        //设置借款申请的审核状态
        borrowInfo.setStatus(BorrowInfoStatusEnum.CHECK_RUN.getStatus());
        baseMapper.insert(borrowInfo);
    }
}
