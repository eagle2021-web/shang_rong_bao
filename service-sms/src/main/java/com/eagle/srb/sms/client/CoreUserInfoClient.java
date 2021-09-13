package com.eagle.srb.sms.client;

import com.eagle.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author eagle2020
 */
@FeignClient(value = "service-core")
public interface CoreUserInfoClient {
    /**
     * 检查手机是否已经被注册
     * @param mobile 手机号
     * @return R
     */
    @GetMapping("/api/core/userInfo/checkMobile/{mobile}")
    R checkMobile(@PathVariable String mobile);
}
