package com.lhx.goodchoice.service;
import java.util.Date;


import com.lhx.goodchoice.pojo.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

/**
 * redis测试
 *
 * @author 梁浩轩
 */
@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void test(){
        ValueOperations valueOperations = redisTemplate.opsForValue();

        valueOperations.set("LHXString","goodChoice");
        valueOperations.set("LHXInt",123);
        valueOperations.set("LHXDouble",3.1415926);
        User user = new User();
        user.setUserId(2L);
        user.setUserName("lhx");
        user.setUserAccount("123456");
        valueOperations.set("LHXUser",user);

        Object lhxString = valueOperations.get("LHXString");
        Assertions.assertTrue("goodChoice".equals((String) lhxString));
        Object lhxInt = valueOperations.get("LHXInt");
        Assertions.assertTrue(123 == ((Integer) lhxInt));
        Object lhxDouble = valueOperations.get("LHXDouble");
        Assertions.assertTrue(3.1415926 == ((Double) lhxDouble));
    }

}
