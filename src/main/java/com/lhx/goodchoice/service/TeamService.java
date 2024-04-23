package com.lhx.goodchoice.service;

import com.lhx.goodchoice.pojo.Team;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author 梁浩轩
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-04-23 20:22:20
*/
public interface TeamService extends IService<Team> {


    long addTeam(Team team, HttpServletRequest request);
}
