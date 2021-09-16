package com.eagle.srb.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author eagle2020
 */

@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
@AllArgsConstructor
@Getter
public enum UserBindEnum {

    NO_BIND(0, "未绑定"),
    BIND_OK(1, "绑定成功"),
    BIND_FAIL(-1, "绑定失败"),
    ;

    private Integer status;
    private String msg;
}
