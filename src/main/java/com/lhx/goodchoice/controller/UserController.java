package com.lhx.goodchoice.controller;


import com.lhx.goodchoice.common.BaseResponse;
import com.lhx.goodchoice.common.ErrorCode;
import com.lhx.goodchoice.common.Result;
import com.lhx.goodchoice.exception.BusinessException;
import com.lhx.goodchoice.pojo.User;
import com.lhx.goodchoice.pojo.request.UserLoginRequest;
import com.lhx.goodchoice.pojo.request.UserRegisterRequest;
import com.lhx.goodchoice.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;


/**
 * 用户接口
 *
 * @author Liang_Haoxuan
 */

@RequestMapping("/user")
@RestController
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册接口
     *
     * @param userRegisterRequest 用户注册请求体
     * @return 新用户id
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long successRegister = userService.UserRegister(userAccount, userPassword, checkPassword);
        return Result.ok(successRegister);
    }

    /**
     * 用户登录接口
     *
     * @param userLoginRequest 用户登录请求体
     * @param request          httprequest
     * @return 用户脱敏信息
     */

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.UserLogin(userAccount, userPassword, request);
        return Result.ok(loginUser);
    }


    /**
     * 用户注销接口
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);

        }
        Integer logout = userService.userLogout(request);
        return Result.ok(logout);
    }


    /**
     * 管理员查询接口
     *
     * @param userAccount
     * @param request
     * @return
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String userAccount, HttpServletRequest request) {
        if (StringUtils.isBlank(userAccount)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        List<User> users = userService.searchUsers(userAccount, request);
        return Result.ok(users);
    }

    /**
     * 管理员删除接口
     *
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody Map<String,Long> id, HttpServletRequest request) {
        if (id.get("userId") < 0L) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Boolean deleted = userService.deleteUser(id.get("userId"),request);
        return Result.ok(deleted);
    }


    /**
     * 获取当前用户
     *
     * @param request
     * @return
     */
    @GetMapping("current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);
        return Result.ok(currentUser);
    }




}
