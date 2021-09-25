package com.eagle.srb.core.controller.api;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eagle.common.result.R;
import com.eagle.srb.base.util.JwtUtils;
import com.eagle.srb.core.pojo.entity.TransFlow;
import com.eagle.srb.core.service.TransFlowService;
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
import java.util.List;

/**
 * <p>
 * 交易流水表 前端控制器
 * </p>
 *
 * @author eagle
 * @since 2021-09-23
 */
@Api(tags = "资金记录")
@RestController
@RequestMapping("/api/core/transFlow")
@Slf4j
public class TransFlowController {

    @Resource
    private TransFlowService transFlowService;

    @ApiOperation("获取列表")
    @GetMapping("/list")
    public R list(HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        List<TransFlow> list = transFlowService.selectByUserId(userId);
        return R.ok().data("list", list);
    }

    @ApiOperation("获取资金列表-分页")
    @GetMapping("/fund/list/{page}/{limit}")
    public R listPage(
            @ApiParam(value = "当前页码", required = true)
            @PathVariable Long page,
            @ApiParam(value = "每页记录数", required = true)
            @PathVariable Long limit,
            HttpServletRequest request
    ) {
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        Page<TransFlow> pageParam = new Page<>(page, limit);
        IPage<TransFlow> pageModel = transFlowService.listPage(pageParam, userId);
        return R.ok().data("pageModel", pageModel);
    }
}
