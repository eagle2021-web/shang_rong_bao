package com.eagle.srb.core.controller.admin;

import com.eagle.common.result.R;
import com.eagle.srb.core.pojo.entity.BorrowInfo;
import com.eagle.srb.core.pojo.vo.BorrowInfoApprovalVO;
import com.eagle.srb.core.service.BorrowInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Api(tags = "借款管理")
@RestController
@RequestMapping("/admin/core/borrowInfo")
@Slf4j
public class AdminBorrowInfoController {

    @Resource
    private BorrowInfoService borrowInfoService;

    /**
     * 借款人借款申请列表，非借款人资质审核列表
     * @return 列表
     */
    @ApiOperation("借款信息列表")
    @GetMapping("/list")
    public R list() {
        List<BorrowInfo> borrowInfoList = borrowInfoService.selectList();
        return R.ok().data("list", borrowInfoList);
    }

    /**
     * 查看借款信息详情
     * @param id borrow_info id
     * @return 借款信息详情
     */
    @ApiOperation("借款信息详情")
    @GetMapping("/show/{id}")
    public R show(
            @ApiParam(value = "借款信息id", required = true)
            @PathVariable Long id){

        Map<String, Object> borrowInfoDetail = borrowInfoService.getBorrowInfoDetail(id);
        return R.ok().data("borrowInfoDetail", borrowInfoDetail);
    }

    @ApiOperation("审批借款信息")
    @PostMapping("/approval")
    public R approval(@RequestBody BorrowInfoApprovalVO borrowInfoApprovalVO) {

        borrowInfoService.approval(borrowInfoApprovalVO);
        return R.ok().message("审批完成");
    }
}