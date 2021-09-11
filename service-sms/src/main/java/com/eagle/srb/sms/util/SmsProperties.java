package com.eagle.srb.sms.util;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aliyun.sms")
public class SmsProperties implements InitializingBean {

    // region-id: cn-hangzhou
    private String regionId;
    // key-id: LTAI4G5Svnb2TWBMuKnNT6jY
    private String keyId;
    // key-secret: N7v6R4V3EJ1SGDZlsqtqo8QyVVMmtQ
    private String keySecret;
    // template-code: SMS_96695065
    private String templateCode;
    // sign-name: 谷粒
    private String signName;

    public static String REGION_Id;
    public static String KEY_ID;
    public static String KEY_SECRET;
    public static String TEMPLATE_CODE;
    public static String SIGN_NAME;


    @Override
    public void afterPropertiesSet() throws Exception {
        REGION_Id = regionId;
        KEY_ID = keyId;
        KEY_SECRET = keySecret;
        TEMPLATE_CODE = templateCode;
        SIGN_NAME = signName;
    }
}

