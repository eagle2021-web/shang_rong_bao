package com.eagle.srb.core.controller.api;


import com.eagle.common.result.R;
import com.eagle.srb.base.util.JwtUtils;
import com.eagle.srb.core.pojo.entity.LendItemReturn;
import com.eagle.srb.core.service.LendItemReturnService;
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
 * 标的出借回款记录表 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2021-02-20
 */
@Api(tags = "回款计划")
@RestController
@RequestMapping("/api/core/lendItemReturn")
@Slf4j
public class LendItemReturnController {

    @Resource
    private LendItemReturnService lendItemReturnService;

    @ApiOperation("获取列表")
    @GetMapping("/list/{lendId}")
    public R list(
            @ApiParam(value = "标的id", required = true)
            @PathVariable Long lendId, HttpServletRequest request) {

        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        log.info("前台查询回款列表");
        List<LendItemReturn> list = lendItemReturnService.selectByLendId(lendId, userId);
        log.info("list = {}", list);
        return R.ok().data("list", list);
    }
}

