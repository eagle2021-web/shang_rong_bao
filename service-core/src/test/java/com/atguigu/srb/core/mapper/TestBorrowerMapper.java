package com.atguigu.srb.core.mapper;

import com.atguigu.srb.core.service.IntegralGradeService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class TestBorrowerMapper {
    @Resource
    private IntegralGradeService integralGradeService;
    @Test
    public void BorrowerServices(){
        System.out.println(integralGradeService.list());
    }
}
