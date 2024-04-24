package com.lhx.goodchoice.pojo.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
public class TeamUpdateRequest implements Serializable {
    private static final long serialVersionUID = 8884625780980729449L;

    /**
     * 队伍id
     */
    private Long teamId;

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
     * 0-公开，1-私有，2-加密
     */
    private Integer teamStatus;

    /**
     * 密码
     */
    private String teamPassword;

}
