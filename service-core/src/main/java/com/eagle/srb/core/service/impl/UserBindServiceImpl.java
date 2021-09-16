package com.eagle.srb.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eagle.srb.core.hfb.FormHelper;
import com.eagle.srb.core.hfb.HfbConst;
import com.eagle.srb.core.hfb.RequestHelper;
import com.eagle.srb.core.mapper.UserBindMapper;
import com.eagle.srb.core.pojo.entity.UserBind;
import com.eagle.srb.core.pojo.vo.UserBindVO;
import com.eagle.srb.core.service.UserBindService;
import org.springframework.stereotype.Service;

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
@Service
public class UserBindServiceImpl extends ServiceImpl<UserBindMapper, UserBind> implements UserBindService {

    @Override
    public String commitBindUser(UserBindVO userBindVO, Long userId) {
        Map<String, Object> paramMap= new HashMap<String, Object>(){{
            put("agentId", HfbConst.AGENT_ID);
            put("agentUserId", userId);
            put("idCard",userBindVO.getIdCard());
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
                (HfbConst.USER_BIND_URL,  paramMap);
    }
}
