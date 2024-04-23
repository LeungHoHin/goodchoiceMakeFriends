package com.lhx.goodchoice.pojo;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 队伍
 * @TableName team
 */
@TableName(value ="team")
@Data
public class Team implements Serializable {
    /**
     * 队伍id
     */
    @TableId(type = IdType.AUTO)
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

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}