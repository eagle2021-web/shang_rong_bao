package com.eagle.srb.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eagle.common.exception.BusinessException;
import com.eagle.srb.core.enums.LendStatusEnum;
import com.eagle.srb.core.enums.ReturnMethodEnum;
import com.eagle.srb.core.enums.TransTypeEnum;
import com.eagle.srb.core.hfb.HfbConst;
import com.eagle.srb.core.hfb.RequestHelper;
import com.eagle.srb.core.mapper.BorrowerMapper;
import com.eagle.srb.core.mapper.LendMapper;
import com.eagle.srb.core.mapper.UserAccountMapper;
import com.eagle.srb.core.mapper.UserInfoMapper;
import com.eagle.srb.core.pojo.bo.TransFlowBO;
import com.eagle.srb.core.pojo.entity.*;
import com.eagle.srb.core.pojo.vo.BorrowInfoApprovalVO;
import com.eagle.srb.core.pojo.vo.BorrowerDetailVO;
import com.eagle.srb.core.service.*;
import com.eagle.srb.core.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Resource
    private LendItemReturnService lendItemReturnService;

    @Resource
    private LendReturnService lendReturnService;

    @Resource
    private DictService dictService;

    @Resource
    private BorrowerMapper borrowerMapper;

    @Resource
    private BorrowerService borrowerService;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private UserAccountMapper userAccountMapper;

    @Resource
    private TransFlowService transFlowService;

    @Resource
    private LendItemService lendItemService;

    /**
     * 创建新的标的
     * @param borrowInfoApprovalVO 审批信息
     * @param borrowInfo           借款信息
     */
    @Override
    public void createLend(BorrowInfoApprovalVO borrowInfoApprovalVO, BorrowInfo borrowInfo) {
        Lend lend = new Lend() {{
            setUserId(borrowInfo.getUserId());//借款人用户id
            setBorrowInfoId(borrowInfo.getId());//
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

    /**
     * 查看标的列表
     * @return
     */
    @Override
    public List<Lend> selectList() {
        List<Lend> lends = baseMapper.selectList(null);
        lends.forEach(lend -> {
            String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", lend.getReturnMethod());
            String msgByStatus = LendStatusEnum.getMsgByStatus(lend.getStatus());
            lend.getParam().put("returnMethod", returnMethod);
            lend.getParam().put("status", msgByStatus);
        });
        return lends;
    }

    /**
     * 查看标的详情
     * @param id 标的id
     * @return 标的信息和借款人信息
     */
    @Override
    public Map<String, Object> getLendDetail(Long id) {
        //查询lend
        Lend lend = baseMapper.selectById(id);
        String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", lend.getReturnMethod());
        String status = LendStatusEnum.getMsgByStatus(lend.getStatus());
        lend.getParam().put("returnMethod", returnMethod);
        lend.getParam().put("status", status);


        //查询借款人对象：Borrower(BorrowerDetailVO)
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<>();
        borrowerQueryWrapper.eq("user_id", lend.getUserId());
        Borrower borrower = borrowerMapper.selectOne(borrowerQueryWrapper);
        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(borrower.getId());

        //组装集合结果
        Map<String, Object> result = new HashMap<>();
        result.put("lend", lend);
        result.put("borrower", borrowerDetailVO);
        return result;
    }

    /**
     * 计算当次投资收益
     * @param invest 投资额
     * @param yearRate 年利率
     * @param totalMonth 投资多少个月
     * @param returnMethod 回款方式
     * @return 投资收益
     */
    @Override
    public BigDecimal getInterestCount(BigDecimal invest, BigDecimal yearRate, Integer totalMonth, Integer returnMethod) {
        BigDecimal interestCount;
        if (returnMethod.intValue() == ReturnMethodEnum.ONE.getMethod()) {
            interestCount = Amount1Helper.getInterestCount(invest, yearRate, totalMonth);
        } else if (returnMethod.intValue() == ReturnMethodEnum.TWO.getMethod()) {
            interestCount = Amount2Helper.getInterestCount(invest, yearRate, totalMonth);
        } else if (returnMethod.intValue() == ReturnMethodEnum.THREE.getMethod()) {
            interestCount = Amount3Helper.getInterestCount(invest, yearRate, totalMonth);
        } else {
            interestCount = Amount4Helper.getInterestCount(invest, yearRate, totalMonth);
        }
        return interestCount;
    }

    /**
     * 放款
     * @param lendId 标的id
     */

    @Override
    public void makeLoan(Long lendId) {
//        获取标的信息
        Lend lend = baseMapper.selectById(lendId);

        //调用汇付宝放款接口
        Map<String, Object> map = new HashMap<>();
        map.put("agentId", HfbConst.AGENT_ID);
        map.put("agentProjectCode", lend.getLendNo());
        map.put("agentBillNo", LendNoUtils.getLoanNo());

        //平台服务费月化
        BigDecimal monthRate = lend.getServiceRate().divide(new BigDecimal(12), 8, BigDecimal.ROUND_DOWN);
        //平台服务费 = 已投金额 * 月年化 * 投资时长
        BigDecimal realAmount = lend.getInvestAmount().multiply(monthRate).multiply(new BigDecimal(lend.getPeriod()));

        map.put("mchFee", realAmount);//商户手续费
        map.put("timestamp", RequestHelper.getTimestamp());//时间错
        map.put("sign", RequestHelper.getSign(map));//签名

        //提交远程请求
        JSONObject result = RequestHelper.sendRequest(map, HfbConst.MAKE_LOAD_URL);//请求
        log.info("放款结果：" + result.toJSONString());

        //放款失败的处理
        if (!"0000".equals(result.getString("resultCode"))) {
            throw new BusinessException(result.getString("resultMsg"));
        }

        //放款成功
//     （1）标的状态和标的平台收益：更新标的相关信息
        lend.setRealAmount(realAmount); //平台服务费
        lend.setStatus(LendStatusEnum.PAY_RUN.getStatus());//还款中
        lend.setPaymentTime(LocalDateTime.now());//放款时间
        baseMapper.updateById(lend);//更新标的

//     （2）给借款账号转入金额
        //获取借款人bindCode
        Long userId = lend.getUserId();//用户id
        UserInfo userInfo = userInfoMapper.selectById(userId);//用户信息
        String bindCode = userInfo.getBindCode();//绑定协议号
        //转账
        BigDecimal voteAmt = new BigDecimal(result.getString("voteAmt"));//真实放款金额
        userAccountMapper.updateAccount(bindCode, voteAmt, new BigDecimal(0));//转账给借款人

//     （3）增加借款交易流水
        TransFlowBO transFlowBO = new TransFlowBO(
                result.getString("agentBillNo"),
                bindCode,
                voteAmt,
                TransTypeEnum.BORROW_BACK,
                "项目放款，项目编号：" + lend.getLendNo() + "，项目名称：" + lend.getTitle()
        );
        transFlowService.saveTransFlow(transFlowBO);//增加借款交易流水

//     （4）解冻并扣除投资人资金
        //获取标的下的投资列表
        List<LendItem> lendItemList = lendItemService.selectByLendId(lendId, 1);//当前标的投资列表
        //当前标的投资列表 元素迭代
        lendItemList.stream().forEach(item -> {
            Long investUserId = item.getInvestUserId();//投资人用户id
            UserInfo investUserInfo = userInfoMapper.selectById(investUserId);//投资人信息
            String investBindCode = investUserInfo.getBindCode();//投资人绑定协议号

            BigDecimal investAmount = item.getInvestAmount();//投资金额
            userAccountMapper.updateAccount(investBindCode, new BigDecimal(0), investAmount.negate());//冻结资金取出

//         （5）增加投资人交易流水
            TransFlowBO investTransFlowBO = new TransFlowBO(
                    LendNoUtils.getTransNo(),
                    investBindCode,
                    investAmount,
                    TransTypeEnum.INVEST_UNLOCK,
                    "项目放款，冻结资金转出，项目编号：" + lend.getLendNo() + "，项目名称：" + lend.getTitle()
            );
            transFlowService.saveTransFlow(investTransFlowBO);//增加投资人交易流水

        });

//     （6）生成借款人还款计划和出借人回款计划
        this.repaymentPlan(lend);
    }

    /**
     * 生成还款计划和回款计划
     *
     * @param lend 标的
     */
    private void repaymentPlan(Lend lend) {

        //创建还款计划列表
        List<LendReturn> lendReturnList = new ArrayList<>();

        //按还款时间生成还款计划
        int len = lend.getPeriod().intValue();//还款期数
        for (int i = 1; i <= len; i++) {
            //创建还款计划对象
            LendReturn lendReturn = new LendReturn();
            //填充基本属性
            //创建还款计划对象
            lendReturn.setReturnNo(LendNoUtils.getReturnNo());//还款流水表
            lendReturn.setLendId(lend.getId());//标的id
            lendReturn.setBorrowInfoId(lend.getBorrowInfoId());//借款申请记录的id
            lendReturn.setUserId(lend.getUserId());//标的用户id
            lendReturn.setAmount(lend.getAmount());//标的期望金额
            lendReturn.setBaseAmount(lend.getInvestAmount());//标的实际投资金额
            lendReturn.setLendYearRate(lend.getLendYearRate());//年利率
            lendReturn.setCurrentPeriod(i);//当前期数
            lendReturn.setReturnMethod(lend.getReturnMethod());//还款方式
            lendReturn.setFee(new BigDecimal("0"));//手续费
            lendReturn.setReturnDate(lend.getLendStartDate().plusMonths(i)); //第二个月开始还款
            lendReturn.setOverdue(false);//是否逾期
            //判断是否是最后一期还款
            //最后一期
            lendReturn.setLast(i == len);//是否最后一期
            //设置还款状态
            lendReturn.setStatus(0);//0未还款
            //将还款对象加入还款计划列表
            lendReturnList.add(lendReturn);
        }//总共period期

        //批量保存还款计划
        log.info("保存前=");
        lendReturnList.forEach(System.out::println);
        lendReturnService.saveBatch(lendReturnList);//存到lend_return表，总共period期
        //生成期数和还款记录的id对应的键值对集合
        System.out.println(1111);
        Map<Integer, Long> lendReturnMap = lendReturnList.stream().collect(
                Collectors.toMap(LendReturn::getCurrentPeriod, LendReturn::getId) //期数和returnId,hash表
        );
        log.info("保存后=");
        lendReturnList.forEach(System.out::println);
        //创建所有投资的所有回款记录列表 为每个投资者
        List<LendItemReturn> lendItemReturnAllList = new ArrayList<>();

        //获取当前标的下的所有的已支付的投资 选出相关投资记录
        List<LendItem> lendItemList = lendItemService.selectByLendId(lend.getId(), 1);

        for (LendItem lendItem : lendItemList) {
            //根据投资记录的id调用回款计划生成的方法，得到当前这笔投资的回款计划列表
            List<LendItemReturn> lendItemReturnList = this.returnInvest(lendItem.getId(), lendReturnMap, lend);
            // 将当前这笔投资的回款计划列表  放入  所有投资的所有回款记录列表
            lendItemReturnAllList.addAll(lendItemReturnList);
        }


/////////////////////////////////////////////////////////////////

        //遍历还款记录列表
        for (LendReturn lendReturn : lendReturnList) {
            //通过filter、map、reduce将相关期数的回款数据过滤出来
            //将当前期数的所有投资人的数据相加，就是当前期数的所有投资人的回款数据（本金、利息、总金额）
            //LendReturnId 是指借款人某期还款id ----  item 是某投资人某期回款记录
            BigDecimal sumPrincipal = lendItemReturnAllList.stream()
                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getPrincipal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal sumInterest = lendItemReturnAllList.stream()
                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getInterest)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal sumTotal = lendItemReturnAllList.stream()
                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            //将计算出的数据填充入还款计划记录：设置本金、利息、总金额
            lendReturn.setPrincipal(sumPrincipal);//本金
            lendReturn.setInterest(sumInterest);//利息
            lendReturn.setTotal(sumTotal);//总金额
        }
        //批量更新还款计划列表
        lendReturnService.updateBatchById(lendReturnList);
    }

    /**
     * 回款计划（针对某一笔投资的回款计划）
     *
     * @param lendItemId    投资人一次投资记录
     * @param lendReturnMap //期数和returnId,hash表
     * @param lend          一次借款记录
     * @return 一次投资的所有回款列表
     */
    public List<LendItemReturn> returnInvest(Long lendItemId, Map<Integer, Long> lendReturnMap, Lend lend) {

        //获取当前投资记录信息
        LendItem lendItem = lendItemService.getById(lendItemId);

        //调用工具类计算还款本金和利息，存储为集合
        // {key：value}
        // {期数：本金|利息}
        BigDecimal amount = lendItem.getInvestAmount();
        BigDecimal yearRate = lendItem.getLendYearRate();
        Integer totalMonth = lend.getPeriod();

        Map<Integer, BigDecimal> mapInterest;  //还款期数 -> 利息
        Map<Integer, BigDecimal> mapPrincipal; //还款期数 -> 本金

        //根据还款方式计算本金和利息
        if (lend.getReturnMethod().intValue() == ReturnMethodEnum.ONE.getMethod()) {
            //利息
            mapInterest = Amount1Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            //本金
            mapPrincipal = Amount1Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else if (lend.getReturnMethod().intValue() == ReturnMethodEnum.TWO.getMethod()) {
            mapInterest = Amount2Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount2Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else if (lend.getReturnMethod().intValue() == ReturnMethodEnum.THREE.getMethod()) {
            mapInterest = Amount3Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount3Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else {
            mapInterest = Amount4Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount4Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        }

        //创建回款计划列表
        List<LendItemReturn> lendItemReturnList = new ArrayList<>();

        for (Map.Entry<Integer, BigDecimal> entry : mapInterest.entrySet()) {
            Integer currentPeriod = entry.getKey();//当前期数
            // 根据当前期数，获取还款计划的id
            Long lendReturnId = lendReturnMap.get(currentPeriod);

            //创建回款计划记录
            LendItemReturn lendItemReturn = new LendItemReturn();
            //将还款记录关联到回款记录
            lendItemReturn.setLendReturnId(lendReturnId);
            //设置回款记录的基本属性
            lendItemReturn.setLendItemId(lendItemId);
            lendItemReturn.setInvestUserId(lendItem.getInvestUserId());
            lendItemReturn.setLendId(lendItem.getLendId());
            lendItemReturn.setInvestAmount(lendItem.getInvestAmount());
            lendItemReturn.setLendYearRate(lend.getLendYearRate());
            lendItemReturn.setCurrentPeriod(currentPeriod);
            lendItemReturn.setReturnMethod(lend.getReturnMethod());

            //计算回款本金、利息和总额（注意最后一个月的计算）
            if (currentPeriod.intValue() == lend.getPeriod().intValue()) {//最后一期
                //本金
                BigDecimal sumPrincipal = lendItemReturnList.stream()
                        .map(LendItemReturn::getPrincipal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal lastPrincipal = lendItem.getInvestAmount().subtract(sumPrincipal);
                lendItemReturn.setPrincipal(lastPrincipal);

                //利息
                BigDecimal sumInterest = lendItemReturnList.stream()
                        .map(LendItemReturn::getInterest)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal lastInterest = lendItem.getExpectAmount().subtract(sumInterest);
                lendItemReturn.setInterest(lastInterest);

            } else {//非最后一期
                //本金
                lendItemReturn.setPrincipal(mapPrincipal.get(currentPeriod));
                //利息
                lendItemReturn.setInterest(mapInterest.get(currentPeriod));
            }

            //回款总金额
            lendItemReturn.setTotal(lendItemReturn.getPrincipal().add(lendItemReturn.getInterest()));

            //设置回款状态和是否逾期等其他属性
            lendItemReturn.setFee(new BigDecimal("0"));
            lendItemReturn.setReturnDate(lend.getLendStartDate().plusMonths(currentPeriod));//还款日期
            lendItemReturn.setOverdue(false);//未逾期
            lendItemReturn.setStatus(0);//0未归还， 1归还

            //将回款记录放入回款列表
            lendItemReturnList.add(lendItemReturn);
        }

        //批量保存
        lendItemReturnService.saveBatch(lendItemReturnList);

        return lendItemReturnList;
    }

    /**
     * 返回某个用户的借款记录
     * @param userId 用户id
     * @return 借款记录也就是标的记录
     */
    @Override
    public List<Lend> selectBorrowRecordByUserId(Long userId) {
        QueryWrapper<Lend> lendWrapper = new QueryWrapper<>();
        lendWrapper.eq("user_id", userId).orderByDesc("id");
        return baseMapper.selectList(lendWrapper);
    }

    @Override
    public IPage<Lend> listPage(Page<Lend> pageParam, Long userId) {
        QueryWrapper<Lend> lendWrapper = new QueryWrapper<>();
        lendWrapper.eq("user_id", userId).orderByDesc("id");
        return baseMapper.selectPage(pageParam, lendWrapper);
    }
}
