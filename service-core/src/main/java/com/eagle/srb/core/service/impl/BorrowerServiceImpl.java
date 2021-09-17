package com.eagle.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.eagle.srb.core.enums.BorrowerStatusEnum;
import com.eagle.srb.core.mapper.BorrowerAttachMapper;
import com.eagle.srb.core.mapper.UserInfoMapper;
import com.eagle.srb.core.pojo.entity.Borrower;
import com.eagle.srb.core.mapper.BorrowerMapper;
import com.eagle.srb.core.pojo.entity.BorrowerAttach;
import com.eagle.srb.core.pojo.entity.UserInfo;
import com.eagle.srb.core.pojo.vo.BorrowerVO;
import com.eagle.srb.core.service.BorrowerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

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
    private UserInfoMapper userInfoMapper;
    @Resource
    private BorrowerAttachMapper borrowerAttachMapper;
    /**
     * 保存用户提交的信息到borrower数据库
     * @param borrowerVO 传过来的借款人信息
     * @param userId 登录用户的id
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
            item.setBorrowerId(item.getId());
            borrowerAttachMapper.insert(item);
        });
        //更新userInfo中认证状态
        userInfo.setBorrowAuthStatus(BorrowerStatusEnum.AUTH_RUN.getStatus());
        userInfoMapper.updateById(userInfo);
        log.info("更新userInfo中认证状态");
    }

    @Override
    public Integer getStatusByUserId(Long userId) {
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<>();
        borrowerQueryWrapper.select("status").eq("user_id", userId);
        List<Object> objects = baseMapper.selectObjs(borrowerQueryWrapper);
        if(objects.isEmpty()){
            return BorrowerStatusEnum.NO_AUTH.getStatus();
        }
        return (Integer) objects.get(0);
    }
}
