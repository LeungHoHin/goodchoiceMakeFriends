package com.lhx.goodchoice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lhx.goodchoice.pojo.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lhx.goodchoice.pojo.User;
import com.lhx.goodchoice.pojo.dto.TeamQuery;
import com.lhx.goodchoice.pojo.request.DeleteTeamRequest;
import com.lhx.goodchoice.pojo.request.JoinTeamRequest;
import com.lhx.goodchoice.pojo.request.QuitTeamRequest;
import com.lhx.goodchoice.pojo.request.TeamUpdateRequest;
import com.lhx.goodchoice.pojo.vo.UserTeamVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Neveremoer
 * @description 针对表【team(队伍)】的数据库操作Service
 * @createDate 2024-04-24 18:15:44
 */
public interface TeamService extends IService<Team> {

    /**
     * 用户新增（创建）队伍
     *
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);


    /**
     * 用户修改队伍信息
     *
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);


    List<UserTeamVO> listTeams(TeamQuery teamQuery, boolean isAdmin);

    Page<Team> listPageTeams(TeamQuery teamQuery);

    boolean joinTeam(JoinTeamRequest joinTeamRequest, User loginUser);


    boolean quitTeam(QuitTeamRequest quitTeamRequest, User loginUser);

    boolean deleteTeam(DeleteTeamRequest deleteTeamRequest, User loginUser);

}
