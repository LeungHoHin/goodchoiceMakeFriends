package com.lhx.goodchoice.pojo.dto;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 队伍查询封装类
 *
 * @author 梁浩轩
 */
@Data
public class TeamQuery {

    private Long teamId;

    private List<Long> teamIdList;

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
     * 搜索关键词（同时对队伍名称和描述搜索）
     */
    private String searchText;


}
