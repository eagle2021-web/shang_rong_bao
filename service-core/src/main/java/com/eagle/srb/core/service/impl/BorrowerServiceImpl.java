package com.eagle.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eagle.srb.core.enums.BorrowerStatusEnum;
import com.eagle.srb.core.enums.IntegralEnum;
import com.eagle.srb.core.mapper.BorrowerAttachMapper;
import com.eagle.srb.core.mapper.BorrowerMapper;
import com.eagle.srb.core.mapper.UserInfoMapper;
import com.eagle.srb.core.mapper.UserIntegralMapper;
import com.eagle.srb.core.pojo.entity.Borrower;
import com.eagle.srb.core.pojo.entity.BorrowerAttach;
import com.eagle.srb.core.pojo.entity.UserInfo;
import com.eagle.srb.core.pojo.entity.UserIntegral;
import com.eagle.srb.core.pojo.vo.BorrowerApprovalVO;
import com.eagle.srb.core.pojo.vo.BorrowerAttachVO;
import com.eagle.srb.core.pojo.vo.BorrowerDetailVO;
import com.eagle.srb.core.pojo.vo.BorrowerVO;
import com.eagle.srb.core.service.BorrowerAttachService;
import com.eagle.srb.core.service.BorrowerService;
import com.eagle.srb.core.service.DictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 借款人 服务实现类
 * </p>
 *
 * @author eagle
 * @since 2021-09-02
 */
@SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
@Service
@Slf4j
public class BorrowerServiceImpl extends ServiceImpl<BorrowerMapper, Borrower> implements BorrowerService {
    @Resource
    private BorrowerAttachService borrowerAttachService;
    @Resource
    private DictService dictService;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private BorrowerAttachMapper borrowerAttachMapper;
    @Resource
    private UserIntegralMapper userIntegralMapper;

    /**
     *
     * 保存用户提交的信息到borrower数据库，状态改为认证中1，以便后台审核
     * @param borrowerVO 传过来的借款人信息
     * @param userId     登录用户的id
     */
    @Override
    public void saveBorrowerVOByUserId(BorrowerVO borrowerVO, Long userId) {
        //获取用户基本信息
        UserInfo userInfo = userInfoMapper.selectById(userId);
        Borrower borrower = new Borrower();
        BeanUtils.copyProperties(borrowerVO, borrower);
        borrower.setUserId(userId);
        borrower.setName(userInfo.getName());
        borrower.setIdCard(userInfo.getIdCard());
        borrower.setMobile(userInfo.getMobile());
        borrower.setStatus(BorrowerStatusEnum.AUTH_RUN.getStatus());
        //保存到数据库
        baseMapper.insert(borrower);

        //保存附件
        List<BorrowerAttach> list = borrowerVO.getBorrowerAttachList();
        list.forEach(item -> {
            item.setBorrowerId(borrower.getId());
            borrowerAttachMapper.insert(item);
        });
        //更新userInfo中认证状态
        userInfo.setBorrowAuthStatus(BorrowerStatusEnum.AUTH_RUN.getStatus());
        userInfoMapper.updateById(userInfo);
        log.info("更新userInfo中认证状态");
    }

    /**
     * 根据用户id返回用户认证状态
     *
     * @param userId 用户id
     * @return 返回状态0 1 2 3
     */
    @Override
    public Integer getStatusByUserId(Long userId) {
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<>();
        borrowerQueryWrapper.select("status").eq("user_id", userId);
        List<Object> objects = baseMapper.selectObjs(borrowerQueryWrapper);
        if (objects.isEmpty()) {
            return BorrowerStatusEnum.NO_AUTH.getStatus();
        }
        return (Integer) objects.get(0);
    }

    /**
     * 根据参数返回搜索的borrower信息
     *
     * @param pageParam 分页参数
     * @param keyword   搜索的关键词
     * @return 返回分页列表
     */
    @Override
    public IPage<Borrower> listPage(Page<Borrower> pageParam, String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return baseMapper.selectPage(pageParam, null);
        }

        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<>();
        borrowerQueryWrapper
                .like("name", keyword)
                .or().like("id_card", keyword)
                .or().like("mobile", keyword)
                .orderByDesc("id");

        return baseMapper.selectPage(pageParam, borrowerQueryWrapper);
    }

    /**
     * 根据借款人borrower id获取借款人相关信息，以便进行审核
     *
     * @param id 借款人borrower表的id,不是用户id
     * @return 用对象返回相关信息
     */
    @Override
    public BorrowerDetailVO getBorrowerDetailVOById(Long id) {
        //获取借款人信息
        Borrower borrower = baseMapper.selectById(id);

        //填充基本借款人信息
        BorrowerDetailVO borrowerDetailVO = new BorrowerDetailVO();
        BeanUtils.copyProperties(borrower, borrowerDetailVO);

        //婚否
        borrowerDetailVO.setMarry(borrower.getMarry() ? "是" : "否");
        //性别
        borrowerDetailVO.setSex(borrower.getSex() == 1 ? "男" : "女");

        //下拉列表
        borrowerDetailVO.setEducation(dictService.getNameByParentDictCodeAndValue("education", borrower.getEducation()));
        borrowerDetailVO.setIndustry(dictService.getNameByParentDictCodeAndValue("industry", borrower.getIndustry()));
        borrowerDetailVO.setIncome(dictService.getNameByParentDictCodeAndValue("income", borrower.getIncome()));
        borrowerDetailVO.setReturnSource(dictService.getNameByParentDictCodeAndValue("returnSource", borrower.getReturnSource()));
        borrowerDetailVO.setContactsRelation(dictService.getNameByParentDictCodeAndValue("relation", borrower.getContactsRelation()));

        //审批状态
        String status = BorrowerStatusEnum.getMsgByStatus(borrower.getStatus());
        borrowerDetailVO.setStatus(status);

        //附件列表
        List<BorrowerAttachVO> borrowerAttachVOList = borrowerAttachService.selectBorrowerAttachVOList(id);
        borrowerDetailVO.setBorrowerAttachVOList(borrowerAttachVOList);

        return borrowerDetailVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approval(BorrowerApprovalVO borrowerApprovalVO) {
        //获取借款额度申请id
        Long borrowerId = borrowerApprovalVO.getBorrowerId();

        //获取借款额度申请对象
        Borrower borrower = baseMapper.selectById(borrowerId);

        //设置审核状态
        borrower.setStatus(borrowerApprovalVO.getStatus());
        baseMapper.updateById(borrower);

        //获取用户id
        Long userId = borrower.getUserId();

        //获取用户对象
        UserInfo userInfo = userInfoMapper.selectById(userId);

        //用户的原始积分
        Integer integral = userInfo.getIntegral();
        log.info("计算基本信息积分");
        //计算基本信息积分
        UserIntegral userIntegral = new UserIntegral();
        userIntegral.setUserId(userId);
        userIntegral.setIntegral(borrowerApprovalVO.getInfoIntegral());
        userIntegral.setContent("借款人基本信息");
        userIntegralMapper.insert(userIntegral);
        int currentIntegral = integral + borrowerApprovalVO.getInfoIntegral();
        log.info("身份证积分");
        //身份证积分
        if (borrowerApprovalVO.getIdCardOk()) {
            log.info("000000000");
            userIntegral = new UserIntegral();
            userIntegral.setUserId(userId);
            userIntegral.setIntegral(IntegralEnum.BORROWER_IDCARD.getIntegral());
            userIntegral.setContent(IntegralEnum.BORROWER_IDCARD.getMsg());
            System.out.println(1);
            userIntegralMapper.insert(userIntegral);
            System.out.println(2);
            currentIntegral += IntegralEnum.BORROWER_IDCARD.getIntegral();
        }

        log.info("房产积分");
        //房产积分
        if (borrowerApprovalVO.getHouseOk()) {
            userIntegral = new UserIntegral();
            userIntegral.setUserId(userId);
            userIntegral.setIntegral(IntegralEnum.BORROWER_HOUSE.getIntegral());
            userIntegral.setContent(IntegralEnum.BORROWER_HOUSE.getMsg());
            userIntegralMapper.insert(userIntegral);
            currentIntegral += IntegralEnum.BORROWER_HOUSE.getIntegral();
        }
        log.info("车辆积分");
        //车辆积分
        if (borrowerApprovalVO.getCarOk()) {
            userIntegral = new UserIntegral();
            userIntegral.setUserId(userId);
            userIntegral.setIntegral(IntegralEnum.BORROWER_CAR.getIntegral());
            userIntegral.setContent(IntegralEnum.BORROWER_CAR.getMsg());
            userIntegralMapper.insert(userIntegral);
            currentIntegral += IntegralEnum.BORROWER_CAR.getIntegral();
        }
        log.info("设置用户总积分");
        //设置用户总积分
        userInfo.setIntegral(currentIntegral);
        log.info("修改审核状态");
        //修改审核状态
        userInfo.setBorrowAuthStatus(borrowerApprovalVO.getStatus());
        log.info("更新userInfo");
        //更新userInfo
        userInfoMapper.updateById(userInfo);
    }
}
