package com.lhx.goodchoice.service;


import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    public void userTest(){
        RList<Object> list = redissonClient.getList("test-list");
        list.add("liang_Haoxuan");
        System.out.println("list = " + list.get(0));


        RMap<String, String> map = redissonClient.getMap("test-map");
        map.put("lhx-key","lhx-value");
        System.out.println("map.get(\"lhx-key\") = " + map.get("lhx-key"));


    }


}
