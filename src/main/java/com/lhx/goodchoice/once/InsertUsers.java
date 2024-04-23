package com.lhx.goodchoice.once;

import com.lhx.goodchoice.mapper.UserMapper;
import com.lhx.goodchoice.pojo.User;
import com.lhx.goodchoice.service.UserService;
import org.junit.Test;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 *导入假数据用户任务
 *
 * @author 梁浩轩
 */

@Component
public class InsertUsers {

    @Resource
    private UserService userService;



    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUserName("假用户");
            user.setUserAccount("fakeuser" + i);
            user.setAvatarUrl("https://p.qqan.com/up/2020-9/16010876394007474.jpg");
            user.setUserGender(0);
            user.setUserPassword("12345678");
            user.setUserPhone("1371906018");
            user.setUserEmail("121222@qq.com");
            user.setUserTags("[\"男\",\"C++\",\"大三\",\"java\",\"C\",\"iOS\"]");
            user.setUserStatus(0);
            user.setUserProfile("这是假数据第" + i + "号账户");
            userList.add(user);
        }
        userService.saveBatch(userList, 10000);
        stopWatch.stop();
        System.out.println("stopWatch.getTotalTimeMillis() = " + stopWatch.getTotalTimeMillis());
    }

}
