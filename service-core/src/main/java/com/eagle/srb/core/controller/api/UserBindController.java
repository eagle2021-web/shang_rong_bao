package com.eagle.srb.core.controller.api;

import com.alibaba.fastjson.JSON;
import com.eagle.common.result.R;
import com.eagle.srb.base.util.JwtUtils;
import com.eagle.srb.core.hfb.RequestHelper;
import com.eagle.srb.core.pojo.vo.UserBindVO;
import com.eagle.srb.core.service.UserBindService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author eagle2020
 * @date 2021/9/16
 */
@Api(tags = "会员账号绑定")
@RestController
@RequestMapping("/api/core/userBind")
@Slf4j
public class UserBindController {
    @Resource
    private UserBindService userBindService;

    @ApiOperation("账户绑定提交数据")
    @PostMapping("/auth/bind")
    public R bind(@RequestBody UserBindVO userBindVO, HttpServletRequest request){
        // 从header中获取token,并对token进行校验，确保用户已经登录， 并从token中提取userId
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        // 根据userId做账户绑定
        String formStr = userBindService.commitBindUser(userBindVO, userId);
        return R.ok().data("formStr", formStr);
    }

    /**
     * hfb得到表单信息处理后，会发来一个消息，在此接收和处理
     * @param request hfb的通知请求
     * @return  根据签名验证结果返回成功或失败字符串，防止伪造的签名
     */
    @SuppressWarnings("SpringMVCViewInspection")
    @ApiOperation("账户绑定异步回调")
    @PostMapping("/notify")
    public String notify(HttpServletRequest request){
        Map<String, Object> paramMap = RequestHelper.switchMap(request.getParameterMap());
        log.info("paramMap = " + JSON.toJSONString(paramMap));
        //校验签名
        if(!RequestHelper.isSignEquals(paramMap)){
            log.error("用户账号绑定异步回调时的签名验证反馈为错误 = " + JSON.toJSONString(paramMap));
            return "fail";
        }
        log.info("校验前面成功！");
        userBindService.notify(paramMap);
        return "success";
    }
}
