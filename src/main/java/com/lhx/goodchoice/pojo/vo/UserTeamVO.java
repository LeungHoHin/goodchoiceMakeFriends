package com.lhx.goodchoice.pojo.vo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.models.auth.In;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * 用户和队伍信息封装类
 *
 * @author 梁浩轩
 */
@Data
public class UserTeamVO implements Serializable {


    private static final long serialVersionUID = 6478699439595907689L;


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
     * 用户id
     */
    private Long userId;

    /**
     * 0-公开，1-私有，2-加密
     */
    private Integer teamStatus;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;



    /**
     * 创建人的信息
     */
    private UserVO userVO;

    /**
     * 已加入队伍的用户数量
     */
    private Integer hasJoinNum;

    /**
     * 用戶是否已经加入队伍
     */
    private boolean userHasJoinTeam = false;

}
