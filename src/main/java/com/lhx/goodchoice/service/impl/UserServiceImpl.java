package com.lhx.goodchoice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lhx.goodchoice.Utils.AlgorithmUtils;
import com.lhx.goodchoice.common.ErrorCode;
import com.lhx.goodchoice.exception.BusinessException;
import com.lhx.goodchoice.pojo.User;
import com.lhx.goodchoice.service.UserService;
import com.lhx.goodchoice.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.lhx.goodchoice.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author 梁浩轩
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

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


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
        User user = this.getOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "登录的账户不存在");
        }


        //用户数据脱敏
        User dataMaskedUser = userDataMasking(user);
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
        users.replaceAll(this::userDataMasking);
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
        return userDataMasking(selectedUser);
    }

    @Override
    public Integer userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }


//    @Override
//    public List<User> searchUsersByTags(List<String> tagNameList) {
//        if (CollectionUtils.isEmpty(tagNameList)) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        // 1. 先查询所有用户
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        List<User> userList = userMapper.selectList(queryWrapper);
//        Gson gson = new Gson();
//        // 2. 在内存中判断是否包含要求的标签
//        return userList.stream().filter(user -> {
//            String tagsStr = user.getUserTags();
//            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
//            }.getType());
//            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
//            for (String tagName : tagNameList) {
//                if (!tempTagNameSet.contains(tagName)) {
//                    return false;
//                }
//            }
//            return true;
//        }).map(this::dataMasking).collect(Collectors.toList());
//    }


    @Override
    public List<User> searchUsersByTags(List<String> tagsList) {
        if (CollectionUtils.isEmpty(tagsList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        for (String tagName : tagsList) {
            queryWrapper.like(User::getUserTags, tagName);
        }

        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::userDataMasking).collect(Collectors.toList());
    }

    @Override
    public int updateUser(User user, HttpServletRequest request) {
        long userId = user.getUserId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户id不存在");
        }
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (!isAdmin(loginUser) && loginUser.getUserId() != userId) {
            throw new BusinessException(ErrorCode.NO_AUTH, "没有修改权限");
        }
        User originalUser = userMapper.selectById(userId);
        if (originalUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "所修改用户不存在");
        }
        return userMapper.updateById(user);
    }

    @Override
    public Page<User> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        String redisKey = String.format("goodchoice:user:recommend:%s", currentUser.getUserId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        //如果又缓存的话直接读缓存
        Page<User> usersPage = (Page<User>) valueOperations.get(redisKey);
        if (usersPage != null) {
            return usersPage;
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        usersPage = page(new Page<>(pageNum, pageSize), queryWrapper);
        try {
            valueOperations.set(redisKey, usersPage, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("redis set ket error", e);
        }
        return usersPage;
    }


    @Override
    public boolean isAdmin(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        return user != null && user.getUserRole() == 1;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && user.getUserRole() == 1;
    }


    @Override
    public User userDataMasking(User user) {
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
        dataMaskedUser.setUserProfile(user.getUserProfile());
        dataMaskedUser.setUserCreatedAndJoinedTeams(user.getUserCreatedAndJoinedTeams());
        return dataMaskedUser;
    }

    @Override
    public List<User> matchUsers(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        // 用户列表的下标 => 相似度
        List<org.apache.commons.math3.util.Pair<User, Long>> list = new ArrayList<>();
        // 依次计算所有用户和当前用户的相似度

        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getUserTags();
            // 无标签或者为当前用户自己
            if (StringUtils.isBlank(userTags) || Objects.equals(user.getUserId(), loginUser.getUserId())) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 计算分数
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            list.add(new org.apache.commons.math3.util.Pair<>(user, distance));
        }
        // 按编辑距离由小到大排序
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        // 原本顺序的 userId 列表
        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getUserId()).collect(Collectors.toList());

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        // 1, 3, 2
        // User1、User2、User3
        // 1 => User1, 2 => User2, 3 => User3
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(this::userDataMasking)
                .collect(Collectors.groupingBy(User::getUserId));
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }


}




