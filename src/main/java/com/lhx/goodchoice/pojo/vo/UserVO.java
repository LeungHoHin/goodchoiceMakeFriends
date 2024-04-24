package com.lhx.goodchoice.pojo.vo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = -1749895843627888888L;

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long userId;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer userGender;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 电话号码
     */
    private String userPhone;

    /**
     * 邮箱
     */
    private String userEmail;

    /**
     * 标签列表
     */
    private String userTags;

    /**
     * 用户状态
     */
    private Integer userStatus;


    /**
     * 用户角色 0-普通用户 1-管理员
     */
    private Integer userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

}
