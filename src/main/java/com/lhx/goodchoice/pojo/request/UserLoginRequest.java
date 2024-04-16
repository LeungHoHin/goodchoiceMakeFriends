package com.lhx.goodchoice.pojo.request;

import lombok.Data;

import java.io.Serializable;


/**
 * 用户登录请求
 *
 * @author Liang_Haoxuan
 */
@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = 6497836643746852768L;

    private String userAccount;

    private String userPassword;

}
