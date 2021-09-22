package com.eagle.srb.core.controller.api;


import com.eagle.common.result.R;
import com.eagle.srb.core.pojo.entity.LendReturn;
import com.eagle.srb.core.service.LendReturnService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 还款记录表 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2021-02-20
 */
@Api(tags = "还款计划")
@RestController
@RequestMapping("/api/core/lendReturn")
@Slf4j
public class LendReturnController {

    @Resource
    private LendReturnService lendReturnService;

    @ApiOperation("获取列表")
    @GetMapping("/list/{lendId}")
    public R list(
            @ApiParam(value = "标的id", required = true)
            @PathVariable Long lendId) {
        List<LendReturn> list = lendReturnService.selectByLendId(lendId);
        return R.ok().data("list", list);
    }

//    @ApiOperation("用户还款")
//    @PostMapping("/auth/commitReturn/{lendReturnId}")
//    public R commitReturn(
//            @ApiParam(value = "还款计划id", required = true)
//            @PathVariable Long lendReturnId, HttpServletRequest request) {
//
//        String token = request.getHeader("token");
//        Long userId = JwtUtils.getUserId(token);
//        String formStr = lendReturnService.commitReturn(lendReturnId, userId);
//        return R.ok().data("formStr", formStr);
//    }
//
//    @ApiOperation("还款异步回调")
//    @PostMapping("/notifyUrl")
//    public String notifyUrl(HttpServletRequest request) {
//
//        Map<String, Object> paramMap = RequestHelper.switchMap(request.getParameterMap());
//        log.info("还款异步回调：" + JSON.toJSONString(paramMap));
//
//        //校验签名
//        if(RequestHelper.isSignEquals(paramMap)) {
//            if("0001".equals(paramMap.get("resultCode"))) {
//                lendReturnService.notify(paramMap);
//            } else {
//                log.info("还款异步回调失败：" + JSON.toJSONString(paramMap));
//                return "fail";
//            }
//        } else {
//            log.info("还款异步回调签名错误：" + JSON.toJSONString(paramMap));
//            return "fail";
//        }
//        return "success";
//    }
}

