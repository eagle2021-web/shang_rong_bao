package com.eagle.srb.core.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "借款人审批")
public class BorrowerApprovalVO {

    @ApiModelProperty(value = "id")
    private Long borrowerId;

    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "身份证信息是否正确")
    private Boolean idCardOk;

    @ApiModelProperty(value = "房产信息是否正确")
    private Boolean houseOk;

    @ApiModelProperty(value = "车辆信息是否正确")
    private Boolean carOk;

    @ApiModelProperty(value = "基本信息积分")
    private Integer infoIntegral;
}