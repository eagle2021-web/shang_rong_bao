package com.eagle.srb.sms.client.callback;

import com.eagle.common.result.R;
import com.eagle.srb.sms.client.CoreUserInfoClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author eagle2020
 * @date 
 */
@Service
@Slf4j
public class CoreUserInfoClientFallback implements CoreUserInfoClient {
    @Override
    public R checkMobile(String mobile) {
        log.error("远程调用失败");
        return null;
    }
}
