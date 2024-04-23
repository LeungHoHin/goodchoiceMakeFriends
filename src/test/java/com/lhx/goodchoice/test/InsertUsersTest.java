package com.lhx.goodchoice.test;

import com.lhx.goodchoice.pojo.User;
import com.lhx.goodchoice.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class InsertUsersTest {

    @Resource
    private UserService userService;

    private ExecutorService executorService = new ThreadPoolExecutor(40, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));


    @Test
    public void doConcurrencyInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int batchSize = 5000;
        int j = 0;
        ArrayList<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            ArrayList<User> userList = new ArrayList<>();
            while (true) {
                j++;
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
                if (j % batchSize == 0) {
                    break;
                }
            }
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("threadName" + Thread.currentThread().getName());
                userService.saveBatch(userList, batchSize);
            }, executorService);
            futures.add(future);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println("stopWatch.getTotalTimeMillis() = " + stopWatch.getTotalTimeMillis());
    }

    @Test
    //用时17秒
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
