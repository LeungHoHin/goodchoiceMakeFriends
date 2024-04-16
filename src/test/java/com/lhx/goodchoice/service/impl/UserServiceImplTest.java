package com.lhx.goodchoice.service.impl;

import com.lhx.goodchoice.pojo.User;
import com.lhx.goodchoice.service.UserService;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
class UserServiceImplTest {


    @Autowired
    private UserService userService;

    @Test
    public void userRegister() {


        String userAccount = "";
        String userPassword = "12345678";
        String checkPassword = "12345678";
        long result;
        result = userService.UserRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);
        userAccount = "123";
        userPassword = "123456789";
        checkPassword = "123456789";
        result = userService.UserRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);
        userAccount = "1234";
        userPassword = "123456";
        checkPassword = "123456";
        result = userService.UserRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);
        userAccount = "test1234";
        userPassword = "123456789";
        checkPassword = "123456789";
        result = userService.UserRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);
        userAccount = "tes？t1  234";
        userPassword = "123456789";
        checkPassword = "123456789";
        result = userService.UserRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);
        userAccount = "test1234";
        userPassword = "123456789";
        checkPassword = "1234589";
        result = userService.UserRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);
    }

    @Test
    public void addAccount() {
        String userAccount = "test12345";
        String userPassword = "12345678";
        String checkPassword = "12345678";
        long result = userService.UserRegister(userAccount, userPassword, checkPassword);
        System.out.println("result = " + result);

    }

    @Test
    public void searchUsersByTags() {
        List<String> list = Arrays.asList("java","Go");
        List<User> userList = userService.searchUsersByTags(list);
        System.out.println("userList = " + userList);
        Assert.assertNotNull(userList);
    }
}
