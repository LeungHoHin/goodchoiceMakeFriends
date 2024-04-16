package com.lhx.goodchoice.service;

import com.lhx.goodchoice.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author Neveremoer
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2024-04-16 16:33:31
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    Long UserRegister(String userAccount, String userPassword, String checkPassword);


    /**
     * 用户登录
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @param request      cookie
     * @return 用户脱敏信息
     */
    User UserLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 搜索用户
     *
     * @param userAccount
     * @param request
     * @return
     */
    List<User> searchUsers(String userAccount, HttpServletRequest request);

    /**
     * 删除用户
     *
     * @param id
     * @param request
     * @return
     */


    /**
     * 获取当前用户
     *
     * @param request
     * @return
     */
    User getCurrentUser(HttpServletRequest request);

    /**
     * 用户注销
     * @param request
     * @return
     */
    Integer userLogout(HttpServletRequest request);


    /**
     * 用户删除
     * @param userId
     * @param request
     * @return
     */
    Boolean deleteUser(Long userId,HttpServletRequest request);


    /**
     * 根据用户的标签搜索用户
     * @param tagsList
     * @return
     */
    List<User> searchUsersByTags(List<String> tagsList);
}
