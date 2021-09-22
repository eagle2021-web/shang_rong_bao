package com.eagle.srb.core.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class LendNoUtils {

    public static String getNo() {

        LocalDateTime time = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String strDate = dtf.format(time);
//        strDate = UUID.randomUUID().toString();
//        StringBuilder result = new StringBuilder();
//        Random random = new Random();
//        for (int i = 0; i < 3; i++) {
//            result.append(random.nextInt(10));
//        }

        return strDate + UUID.randomUUID().toString().substring(0,10);
    }

    public static String getLendNo() {

        return "LEND" + getNo();
    }

    public static String getLendItemNo() {

        return "INVEST" + getNo();
    }

    public static String getLoanNo() {

        return "LOAN" + getNo();
    }

    public static String getReturnNo() {
        return "RETURN" + getNo();
    }


    public static Object getWithdrawNo() {
        return "WITHDRAW" + getNo();
    }


    public static String getReturnItemNo() {
        return "RETURNITEM" + getNo();
    }


    public static String getChargeNo() {

        return "CHARGE" + getNo();
    }

    /**
     * 获取交易编码
     */
    public static String getTransNo() {
        return "TRANS" + getNo();
    }

}