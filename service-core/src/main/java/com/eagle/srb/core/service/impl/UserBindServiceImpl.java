package com.eagle.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eagle.common.exception.Assert;
import com.eagle.common.result.ResponseEnum;
import com.eagle.srb.core.enums.UserBindEnum;
import com.eagle.srb.core.hfb.FormHelper;
import com.eagle.srb.core.hfb.HfbConst;
import com.eagle.srb.core.hfb.RequestHelper;
import com.eagle.srb.core.mapper.UserBindMapper;
import com.eagle.srb.core.mapper.UserInfoMapper;
import com.eagle.srb.core.pojo.entity.UserBind;
import com.eagle.srb.core.pojo.entity.UserInfo;
import com.eagle.srb.core.pojo.vo.UserBindVO;
import com.eagle.srb.core.service.UserBindService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户绑定表 服务实现类
 * </p>
 *
 * @author eagle
 * @since 2021-09-16
 */
@Slf4j
@Service
public class UserBindServiceImpl extends ServiceImpl<UserBindMapper, UserBind> implements UserBindService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    public String commitBindUser(UserBindVO userBindVO, Long userId) {
        //不用的user_id，相同的身份证，如果存在，则不允许
        //汇付宝相关数据库存储的数据要求银行卡唯一
        //这里也需要在尚荣宝后台相关处理，是的数据库对应数据银行卡唯一
        QueryWrapper<UserBind> userBindQueryWrapper = new QueryWrapper<UserBind>(){{
            eq("id_card", userBindVO.getIdCard());
            ne("user_id", userId);
        }};
        UserBind userBind = baseMapper.selectOne(userBindQueryWrapper);
        Assert.isNull(userBind, ResponseEnum.USER_BIND_IDCARD_EXIST_ERROR);
        //=================================
        //用户是否曾经填写过绑定表单
        userBindQueryWrapper = new QueryWrapper<UserBind>(){{
            eq("user_id", userId);
        }};
        userBind = baseMapper.selectOne(userBindQueryWrapper);
        if(userBind == null){
            //创建用户绑定记录
            userBind = new UserBind();
            //复制响应属性值
            BeanUtils.copyProperties(userBindVO, userBind);
            userBind.setUserId(userId);
            userBind.setStatus(UserBindEnum.NO_BIND.getStatus());
            baseMapper.insert(userBind);
        }else{
            BeanUtils.copyProperties(userBindVO, userBind);
            baseMapper.updateById(userBind);
        }

        Map<String, Object> paramMap = new HashMap<String, Object>(16) {{
            put("agentId", HfbConst.AGENT_ID);
            put("agentUserId", userId);
            put("idCard", userBindVO.getIdCard());
            put("personalName", userBindVO.getName());
            put("bankType", userBindVO.getBankType());
            put("bankNo", userBindVO.getBankNo());
            put("mobile", userBindVO.getMobile());
            put("returnUrl", HfbConst.USER_BIND_RETURN_URL);
            put("notifyUrl", HfbConst.USER_BIND_NOTIFY_URL);
            put("timestamp", RequestHelper.getTimestamp());
        }};
        paramMap.put("sign", RequestHelper.getSign(paramMap));
        //生成动态表单字符串
        return FormHelper.buildForm
                (HfbConst.USER_BIND_URL, paramMap);
    }

    @Override
    public void notify(Map<String, Object> paramMap) {

        String bindCode = (String) paramMap.get("bindCode");
        String agentUserId = (String) paramMap.get("agentUserId");
        QueryWrapper<UserBind> userBindQueryWrapper = new QueryWrapper<UserBind>(){{
            eq("user_id", agentUserId);
        }};
        UserBind userBind = baseMapper.selectOne(userBindQueryWrapper);
        userBind.setBindCode(bindCode);
        userBind.setStatus(UserBindEnum.BIND_OK.getStatus());
        baseMapper.updateById(userBind);
        log.info("成功更新userBind表的绑定code和status");
        //更新userInfo表
        UserInfo userInfo = userInfoMapper.selectById(agentUserId);
        userInfo.setBindCode(bindCode);
        userInfo.setName(userBind.getName());
        userInfo.setIdCard(userBind.getIdCard());
        userInfo.setBindStatus(UserBindEnum.BIND_OK.getStatus());
        userInfoMapper.updateById(userInfo);
        log.info("成功更新userInfo表的绑定bindCode name 和 idCard");
    }

    @Override
    public String getBindCodeByUserId(Long userId) {

        QueryWrapper<UserBind> userBindQueryWrapper = new QueryWrapper<>();
        userBindQueryWrapper.eq("user_id", userId);
        UserBind userBind = baseMapper.selectOne(userBindQueryWrapper);
        return userBind.getBindCode();
    }
}
