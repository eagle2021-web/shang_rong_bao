package com.eagle.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eagle.common.exception.Assert;
import com.eagle.common.result.ResponseEnum;
import com.eagle.srb.core.enums.BorrowInfoStatusEnum;
import com.eagle.srb.core.enums.BorrowerStatusEnum;
import com.eagle.srb.core.enums.UserBindEnum;
import com.eagle.srb.core.mapper.BorrowInfoMapper;
import com.eagle.srb.core.mapper.BorrowerMapper;
import com.eagle.srb.core.mapper.IntegralGradeMapper;
import com.eagle.srb.core.mapper.UserInfoMapper;
import com.eagle.srb.core.pojo.entity.BorrowInfo;
import com.eagle.srb.core.pojo.entity.Borrower;
import com.eagle.srb.core.pojo.entity.IntegralGrade;
import com.eagle.srb.core.pojo.entity.UserInfo;
import com.eagle.srb.core.pojo.vo.BorrowInfoApprovalVO;
import com.eagle.srb.core.pojo.vo.BorrowerDetailVO;
import com.eagle.srb.core.service.BorrowInfoService;
import com.eagle.srb.core.service.BorrowerService;
import com.eagle.srb.core.service.DictService;
import com.eagle.srb.core.service.LendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Resource
    private DictService dictService;

    @Resource
    private BorrowerMapper borrowerMapper;

    @Resource
    private BorrowerService borrowerService;

    @Resource
    private BorrowInfoService borrowInfoService;

    @Resource
    private LendService lendService;
    /**
     * 获取借款人的借款额度
     * @param userId 用户id
     * @return 借款人的借款额度
     */
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

    /**
     * 获取借款人借款申请的审核状态
     * @param userId 用户id
     * @return 借款人借款申请的审核状态
     */
    @Override
    public Integer getStatusByUserId(Long userId) {
        QueryWrapper<BorrowInfo> borrowInfoQueryWrapper = new QueryWrapper<>();
        borrowInfoQueryWrapper.select("status").eq("user_id", userId);
        List<Object> objects = baseMapper.selectObjs(borrowInfoQueryWrapper);
        if(objects.size() == 0){
            return BorrowInfoStatusEnum.NO_AUTH.getStatus();
        }

        return (Integer)objects.get(0);
    }

    /**
     * 判断表单信息是否合法，合法则存储到数据库表borrower_info
     * 111111
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

    @Override
    public List<BorrowInfo> selectList() {
        List<BorrowInfo> borrowInfos = baseMapper.selectBorrowInfoList();
        borrowInfos.forEach(this::supplementBorrowInfo);
        return borrowInfos;
    }

    void supplementBorrowInfo(BorrowInfo borrowInfo){
        String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod"
                , borrowInfo.getReturnMethod());
        String moneyUse = dictService.getNameByParentDictCodeAndValue("moneyUse",
                borrowInfo.getMoneyUse());
        String msgByStatus = BorrowInfoStatusEnum.getMsgByStatus(borrowInfo.getStatus());
        borrowInfo.getParam().put("returnMethod", returnMethod);
        borrowInfo.getParam().put("moneyUse", moneyUse);
        borrowInfo.getParam().put("status", msgByStatus);
    }
    /**
     * 获取借款详情
     * @param id borrow_info id
     * @return 借款详情
     */
    @Override
    public Map<String, Object> getBorrowInfoDetail(Long id) {
        //查询借款对象：BorrowInfo
        BorrowInfo borrowInfo = baseMapper.selectById(id);
        this.supplementBorrowInfo(borrowInfo);

        //查询借款人对象：Borrower(BorrowerDetailVO)
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<>();
        borrowerQueryWrapper.eq("user_id", borrowInfo.getUserId());
        Borrower borrower = borrowerMapper.selectOne(borrowerQueryWrapper);
        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(borrower.getId());

        //组装集合结果
        Map<String, Object> result = new HashMap<>();
        result.put("borrowInfo", borrowInfo);
        result.put("borrower", borrowerDetailVO);
        return result;
    }

    /**
     * 审批借款
     * @param borrowInfoApprovalVO 借款信息对象
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approval(BorrowInfoApprovalVO borrowInfoApprovalVO) {

        //修改借款审核的状态 borrow_info
        Long borrowInfoId = borrowInfoApprovalVO.getId();
        BorrowInfo borrowInfo = baseMapper.selectById(borrowInfoId);
        borrowInfo.setStatus(borrowInfoApprovalVO.getStatus());
        baseMapper.updateById(borrowInfo);
        //如果审核通过，则产生新的标的记录 lend
        if(borrowInfoApprovalVO.getStatus().intValue() == BorrowInfoStatusEnum.CHECK_OK.getStatus().intValue()){
            //创建新标的
//            lendService.createLend(borrowInfoApprovalVO, borrowInfo);
            log.info("创建新标的");
            lendService.createLend(borrowInfoApprovalVO, borrowInfo);
        }
    }
}
