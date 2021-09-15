package com.eagle.srb.sms.controller.api;

import com.eagle.common.exception.Assert;
import com.eagle.common.result.R;
import com.eagle.common.result.ResponseEnum;
import com.eagle.common.util.RandomUtils;
import com.eagle.common.util.RegexValidateUtils;
import com.eagle.srb.sms.client.CoreUserInfoClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/sms")
@Api(tags = "短信管理")
@Slf4j
public class ApiSmsController {



    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private CoreUserInfoClient coreUserInfoClient;

    @ApiOperation("获取验证码")
    @GetMapping("/send/{mobile}")
    public R send(
            @ApiParam(value = "手机号", required = true)
            @PathVariable String mobile){

        //校验手机号吗不能为空
        Assert.notEmpty(mobile, ResponseEnum.MOBILE_NULL_ERROR);
        //是否是合法的手机号码
        Assert.isTrue(RegexValidateUtils.checkCellphone(mobile), ResponseEnum.MOBILE_ERROR);

        //判断手机号是否已经注册
        R r = coreUserInfoClient.checkMobile(mobile);
        log.info("result = " + r.getData());
        log.info("isExist = {}", r.getData().get("isExist"));
        boolean result = (boolean)r.getData().get("isExist");
        log.info("result = {}", result == true);
         Assert.isTrue(result == false, ResponseEnum.MOBILE_EXIST_ERROR);

        String code = RandomUtils.getFourBitRandom();
        HashMap<String, Object> map = new HashMap<>();
        map.put("code", code);
        log.info(code);
//        smsService.send(mobile, SmsProperties.TEMPLATE_CODE, map);

        //将验证码存入redis
        redisTemplate.opsForValue().set("srb:sms:code:" + mobile, code, 5, TimeUnit.MINUTES);

        return R.ok().message("短信发送成功").data("code",code);
    }
}
