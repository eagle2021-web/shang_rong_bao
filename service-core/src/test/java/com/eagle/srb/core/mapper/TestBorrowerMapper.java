package com.eagle.srb.core.mapper;

import com.eagle.srb.core.service.IntegralGradeService;
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
