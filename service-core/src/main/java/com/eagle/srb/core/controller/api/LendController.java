package com.eagle.srb.core.controller.api;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eagle.common.result.R;
import com.eagle.srb.base.util.JwtUtils;
import com.eagle.srb.core.enums.LendStatusEnum;
import com.eagle.srb.core.pojo.entity.Lend;
import com.eagle.srb.core.pojo.entity.LendReturn;
import com.eagle.srb.core.service.LendReturnService;
import com.eagle.srb.core.service.LendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的准备表 前端控制器
 * </p>
 *
 * @author eagle
 * @since 2021-09-20
 */
@Api(tags = "标的")
@RestController
@RequestMapping("/api/core/lend")
@Slf4j
public class LendController {

    @Resource
    private LendService lendService;

    @Resource
    private LendReturnService lendReturnService;

    @ApiOperation("获取标的列表")
    @GetMapping("/list")
    public R list() {
        List<Lend> lendList = lendService.selectList();
        return R.ok().data("lendList", lendList);
    }

    @ApiOperation("获取标的信息")
    @GetMapping("/show/{id}")
    public R show(
            @ApiParam(value = "标的id", required = true)
            @PathVariable Long id) {
        Map<String, Object> lendDetail = lendService.getLendDetail(id);
        return R.ok().data("lendDetail", lendDetail);
    }

    @ApiOperation("计算投资收益")
    @GetMapping("/getInterestCount/{invest}/{yearRate}/{totalmonth}/{returnMethod}")
    public R getInterestCount(
            @ApiParam(value = "投资金额", required = true)
            @PathVariable("invest") BigDecimal invest,

            @ApiParam(value = "年化收益", required = true)
            @PathVariable("yearRate") BigDecimal yearRate,

            @ApiParam(value = "期数", required = true)
            @PathVariable("totalmonth")Integer totalmonth,

            @ApiParam(value = "还款方式", required = true)
            @PathVariable("returnMethod")Integer returnMethod) {

        BigDecimal  interestCount = lendService.getInterestCount(invest, yearRate, totalmonth, returnMethod);
        return R.ok().data("interestCount", interestCount);
    }

    @ApiOperation("借款列表")
    @GetMapping("/borrow/list")
    public R selectBorrowRecordByUserId(HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        List<Lend> lends = lendService.selectBorrowRecordByUserId(userId);
        return R.ok().data("lends", lends);
    }

    @ApiOperation("借款列表-分页")
    @GetMapping("/borrow/list/{page}/{limit}")
    public R listPage(
            @ApiParam(value = "当前页码", required = true)
            @PathVariable Long page,
            @ApiParam(value = "每页记录数", required = true)
            @PathVariable Long limit,
            HttpServletRequest request
    ) {
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        Page<Lend> pageParam = new Page<>(page, limit);
        IPage<Lend> pageModel = lendService.listPage(pageParam, userId);
        return R.ok().data("pageModel", pageModel);
    }

    @ApiOperation("还款列表-分页")
    @GetMapping("/repayment/list/{page}/{limit}")
    public R repaymentlistPage(
            @ApiParam(value = "当前页码", required = true)
            @PathVariable Long page,
            @ApiParam(value = "每页记录数", required = true)
            @PathVariable Long limit,
            HttpServletRequest request
    ) {
        //token
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        //查lend表正在还款的记录,一个用户只能有一个正在还款的lend记录
        QueryWrapper<Lend> lendQueryWrapper = new QueryWrapper<>();
        lendQueryWrapper.eq("user_id", userId)
                .eq("status", LendStatusEnum.PAY_RUN.getStatus());
        Lend one = lendService.getOne(lendQueryWrapper);
        //查lendReturn表具体还款计划（分期）
        Long lendId = one != null ? one.getId() : 0;//lend id,没有则做一次无用查询
        Page<LendReturn> pageParam = new Page<>(page, limit);
        IPage<LendReturn> pageModel = lendReturnService.listPage(pageParam, lendId);
        log.info("pageModel = {}", pageModel.getRecords());
        return R.ok().data("pageModel", pageModel);
    }
}
