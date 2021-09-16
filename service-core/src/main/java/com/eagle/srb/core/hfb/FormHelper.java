package com.eagle.srb.core.hfb;

import java.util.Map;

/**
 * @author eagle2020
 */
public class FormHelper {

    /**
     * 构建自动提交form表单
     * @param url 表单提交的url
     * @param paramMap 表单提交项
     * @return html字符串
     */
    public static String buildForm(String url, Map<String, Object> paramMap) {

        StringBuilder inputStr = new StringBuilder();
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            inputStr.append("<input type='hidden' name='").append(key).append("' value='").append(value).append("'/>");
        }

        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\" xmlns:th=\"http://www.thymeleaf.org\">\n" +
                "<head>\n" +
                "</head>\n" +
                "<body>\n" +
                "<form name=\"form\" action=\""+url+"\" method=\"post\">\n"+

                inputStr +

                "</form>\n" +
                "<script>\n" +
                "\tdocument.form.submit();\n" +
                "</script>\n" +
                "</body>\n" +
                "</html>";
    }

}
