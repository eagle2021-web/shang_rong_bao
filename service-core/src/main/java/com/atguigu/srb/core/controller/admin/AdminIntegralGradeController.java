package com.atguigu.srb.core.controller.admin;

import com.atguigu.srb.core.pojo.entity.IntegralGrade;
import com.atguigu.srb.core.service.IntegralGradeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/admin/core/integralGrade")
public class AdminIntegralGradeController {
    @Resource
    private IntegralGradeService integralGradeService;

    @GetMapping("/list")
    public List<IntegralGrade> ListAll(){
        // http://localhost:8110/admin/core/integralGrade/list
        return integralGradeService.list();
    }
    @DeleteMapping("/remove/{id}")
    public boolean removeById(@PathVariable Long id){
        // http://localhost:8110/admin/core/integralGrade/remove/1
        return integralGradeService.removeById(id);
    }
}