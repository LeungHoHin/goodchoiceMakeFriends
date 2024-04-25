package com.lhx.goodchoice.pojo.request;


import lombok.Data;

import java.io.Serializable;


/**
 * 用户加入队伍的请求体
 *
 * @author 梁浩轩
 */
@Data
public class JoinTeamRequest implements Serializable {

    private static final long serialVersionUID = 1525522563456279557L;


    /**
     * 队伍id
     */
    private Long teamId;


    /**
     * 加入队伍的密码
     */
    private String password;
}

