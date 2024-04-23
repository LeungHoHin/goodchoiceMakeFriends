package com.lhx.goodchoice.controller;


import com.lhx.goodchoice.common.BaseResponse;
import com.lhx.goodchoice.common.ErrorCode;
import com.lhx.goodchoice.common.Result;
import com.lhx.goodchoice.exception.BusinessException;
import com.lhx.goodchoice.mapper.TeamMapper;
import com.lhx.goodchoice.mapper.UserMapper;
import com.lhx.goodchoice.pojo.Team;
import com.lhx.goodchoice.pojo.request.TeamAddRequest;
import com.lhx.goodchoice.service.TeamService;
import com.lhx.goodchoice.service.UserService;
import com.lhx.goodchoice.service.UserTeamService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RequestMapping("/team")
@RestController
@CrossOrigin(origins = "http://localhost:5173/")
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private UserTeamService userTeamService;


    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "传入的新增队伍请求为空");
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        long teamId = teamService.addTeam(team, request);
        return Result.ok(teamId);
    }

}
