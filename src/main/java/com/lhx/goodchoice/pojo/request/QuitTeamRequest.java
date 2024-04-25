package com.lhx.goodchoice.pojo.request;

import lombok.Data;

import java.io.Serializable;


/**
 * 用户退出队伍请求体
 */
@Data
public class QuitTeamRequest implements Serializable {

    private static final long serialVersionUID = 4245242562096337688L;


    private Long teamId;

}
