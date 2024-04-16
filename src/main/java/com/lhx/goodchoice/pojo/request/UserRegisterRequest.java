package com.lhx.goodchoice.pojo.request;

import lombok.Data;

import java.io.Serializable;


/**
 * 用户注册请求
 *
 * @author Liang_Haoxuan
 */

@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -3211329508926253914L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;


}
