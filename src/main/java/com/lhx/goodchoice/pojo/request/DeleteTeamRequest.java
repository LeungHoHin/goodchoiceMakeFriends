package com.lhx.goodchoice.pojo.request;


import lombok.Data;

import java.io.Serializable;

/**
 * 删除队伍请求参数
 *
 * @author 梁浩轩
 */

@Data
public class DeleteTeamRequest implements Serializable {

    private static final long serialVersionUID = 4855980095632906539L;

    /**
     * 队伍id
     */
    private Long teamId;
}
