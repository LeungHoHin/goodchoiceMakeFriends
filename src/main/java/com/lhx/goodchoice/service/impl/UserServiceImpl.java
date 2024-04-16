package com.lhx.goodchoice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lhx.goodchoice.common.ErrorCode;
import com.lhx.goodchoice.exception.BusinessException;
import com.lhx.goodchoice.pojo.User;
import com.lhx.goodchoice.service.UserService;
import com.lhx.goodchoice.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.lhx.goodchoice.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author Liang_Haoxuan
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2024-04-08 14:54:08
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {


    /**
     * 盐值，用户混淆加密密码
     */
    private static final String SALT = "XiaoLiangFromGuangZhou";


    @Autowired
    private UserMapper userMapper;


    @Override
    public Long UserRegister(String userAccount, String userPassword, String checkPassword) {
        //判断有无非空
        if (StringUtils.isAnyEmpty(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //账户不小于4位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户小于4位");
        }

        //密码不小于8位
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码小于8位");

        }


        //账户中不包含特殊字符
        String validPattern = "[\\u00A0\\s\"`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户中包含不合法的特殊字符");
        }

        //账户不能重复
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户已被注册");
        }

        //密码和校验密码是否相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        //对密码进行加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);

        boolean saved = this.save(user);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
        }
        return user.getUserId();
    }

    @Override
    public User UserLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //判断有无空
        if (StringUtils.isAnyEmpty(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "有空数据");
        }

        //账户不小于4位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户小于4位");
        }

        //密码不小于8位
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码小于8位");
        }


        //账户中不包含特殊字符
        String validPattern = "[\\u00A0\\s\"`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户中包含不合法的特殊字符");
        }


        //对密码进行加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());


        //如果登录的用户不存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount).eq(User::getUserPassword, encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            log.info("user login failed");
            throw new BusinessException(ErrorCode.NULL_ERROR, "登录的账户不存在");
        }


        //用户数据脱敏
        User dataMaskedUser = dataMasking(user);
        //记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return dataMaskedUser;

    }

    @Override
    public List<User> searchUsers(String userAccount, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(User::getUserAccount, userAccount);
        List<User> users = userMapper.selectList(queryWrapper);
        users.replaceAll(this::dataMasking);
        return users;
    }

    @Override
    public Boolean deleteUser(Long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }
        boolean isDeleted = this.removeById(id);
        if (!isDeleted) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "所删除的用户id不存在");
        }
        return true;
    }


    @Override
    public User getCurrentUser(HttpServletRequest request) {
        User ordinaryUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (ordinaryUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户未登录");
        }
        Long userUserId = ordinaryUser.getUserId();
        User selectedUser = getById(userUserId);
        return dataMasking(selectedUser);
    }

    @Override
    public Integer userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }


    @Override
    public List<User> searchUsersByTags(List<String> tagsList) {
//        if (CollectionUtils.isEmpty(tagsList)) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
//        for (String tagName : tagsList) {
//            queryWrapper.like(User::getUserTags, tagName);
//        }
//
//        List<User> userList = userMapper.selectList(queryWrapper);
//        return userList.stream().map(this::dataMasking).collect(Collectors.toList());
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        return userList.stream().filter(user -> {
            String userTags = user.getUserTags();
            if (StringUtils.isBlank(userTags)) {
                return false;
            }
            Set<String> tagSet = gson.fromJson(userTags, new TypeToken<Set<String>>() {
            }.getType());
            for (String tagName : tagsList) {
                if (!tagSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::dataMasking).collect(Collectors.toList());
    }


    /**
     * 身份校验
     *
     * @param request HttpServletRequest request
     * @return 是否为管理员
     */
    public boolean isAdmin(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        return user != null && user.getUserRole() == 1;
    }


    /**
     * 数据脱敏
     *
     * @param user 脱敏前的数据
     * @return 脱敏后的数据
     */

    private User dataMasking(User user) {
        User dataMaskedUser = new User();
        dataMaskedUser.setUserId(user.getUserId());
        dataMaskedUser.setUserName(user.getUserName());
        dataMaskedUser.setUserAccount(user.getUserAccount());
        dataMaskedUser.setAvatarUrl(user.getAvatarUrl());
        dataMaskedUser.setUserGender(user.getUserGender());
        dataMaskedUser.setUserPhone(user.getUserPhone());
        dataMaskedUser.setUserEmail(user.getUserEmail());
        dataMaskedUser.setUserStatus(user.getUserStatus());
        dataMaskedUser.setCreateTime(user.getCreateTime());
        dataMaskedUser.setUserRole(user.getUserRole());
        dataMaskedUser.setUserTags(user.getUserTags());
        return dataMaskedUser;
    }


}




