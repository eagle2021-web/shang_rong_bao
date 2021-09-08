package com.eagle.srb.core.controller.admin;


import com.eagle.common.exception.Assert;
import com.eagle.common.result.R;
import com.eagle.common.result.ResponseEnum;
import com.eagle.srb.core.pojo.entity.IntegralGrade;
import com.eagle.srb.core.service.IntegralGradeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author eagle2020
 */
@Api(tags = "积分等级管理")
@CrossOrigin
@RestController
@RequestMapping("/admin/core/integralGrade")
@Slf4j
public class AdminIntegralGradeController {
    @Resource
    private IntegralGradeService integralGradeService;

    @ApiOperation(value = "积分等级列表", notes = "逻辑删除数据记录")
    @GetMapping("/list")
    public R ListAll() {
        // http://localhost:8110/admin/core/integralGrade/list
        log.info("info");
        log.warn("warn");
        log.error("error");
        List<IntegralGrade> list = integralGradeService.list();
        return R.ok().data("list", list).message("获取列表成功");
    }

    @ApiOperation(value = "根据id删除数据记录")
    @DeleteMapping("/remove/{id}")
    public R removeById(
            @ApiParam(value = "数据id", example = "155", required = true) @PathVariable Long id) {
        // http://localhost:8110/admin/core/integralGrade/remove/1
        boolean b = integralGradeService.removeById(id);
        return b ?
                R.ok().message("删除成功")
                : R.error().message("删除失败");
    }

    @ApiOperation(value = "新增积分等级")
    @PostMapping("/save")
    public R save(@ApiParam(value = "积分等级对象", required = true) @RequestBody IntegralGrade integralGrade) {
        Assert.notNull(integralGrade.getBorrowAmount(), ResponseEnum.BORROW_AMOUNT_NULL_ERROR);
 //        if(integralGrade.getBorrowAmount() == null){
//            throw new BusinessException(ResponseEnum.BORROW_AMOUNT_NULL_ERROR);
//        }
        boolean save = integralGradeService.save(integralGrade);
        return save ? R.ok().message("保存成功") : R.error().message("保存失败");
    }

    @ApiOperation("根据id获取积分等级")
    @GetMapping("/get/{id}")
    public R getById(@ApiParam(value = "数据id", required = true, example = "1") @PathVariable Long id) {

        IntegralGrade byId = integralGradeService.getById(id);
        return byId != null ? R.ok().data("record", byId) : R.error().message("数据获取失败");
    }

    @ApiOperation("根据id更新积分等级")
    @PutMapping("/update")
    public R update(@ApiParam(value = "积分等级对象", required = true) @RequestBody IntegralGrade integralGrade) {
        boolean save = integralGradeService.updateById(integralGrade);
        return save ? R.ok().message("更新成功") : R.error().message("更新失败");
    }
}
