package com.eagle.srb.core.redist.Redis;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

/**
 * @author eagle2020
 * @date 2021/9/25
 */
public class Redis {
    @Resource
    private RedisTemplate redisTemplate;

    @Test
    public void testRedis(){
        redisTemplate.opsForValue().set("aa", "123456");
    }
}
