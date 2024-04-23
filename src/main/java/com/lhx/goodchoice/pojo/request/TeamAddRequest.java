package com.lhx.goodchoice.pojo.request;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 添加队伍请求类
 * 为了减少暴露出去的数据
 *
 * @author 梁浩轩
 */

@Data
public class TeamAddRequest implements Serializable {
    private static final long serialVersionUID = 5529352196653227526L;

    /**
     * 队伍名称
     */
    private String teamName;

    /**
     * 队伍描述
     */
    private String teamDescription;

    /**
     * 队伍最大人数
     */
    private Integer teamMaxNum;

    /**
     * 队伍过期时间
     */
    private Date teamExpireTime;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0-公开，1-私有，2-加密
     */
    private Integer teamStatus;

    /**
     * 密码
     */
    private String teamPassword;


}
