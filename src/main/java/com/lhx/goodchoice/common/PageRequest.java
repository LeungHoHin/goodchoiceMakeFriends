package com.lhx.goodchoice.common;


import lombok.Data;

import java.io.Serializable;


/**
 *通用分页请求参数
 *
 * @author 梁浩轩
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = -3894557407460592067L;

    protected int pageSize = 10;

    protected int pageNum = 1;
}
