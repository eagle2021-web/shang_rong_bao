package com.eagle.srb.core.mapper;

import com.eagle.srb.core.service.IntegralGradeService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class MapperTests {

    @Resource
    private IntegralGradeService integralGradeService;
    @Test
    public void BorrowerServices(){
        integralGradeService.list().forEach(System.out::println);
    }
}
