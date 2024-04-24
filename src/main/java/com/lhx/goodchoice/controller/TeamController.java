package com.lhx.goodchoice.controller;


import com.lhx.goodchoice.common.BaseResponse;
import com.lhx.goodchoice.common.ErrorCode;
import com.lhx.goodchoice.common.Result;
import com.lhx.goodchoice.exception.BusinessException;
import com.lhx.goodchoice.mapper.TeamMapper;
import com.lhx.goodchoice.mapper.UserMapper;
import com.lhx.goodchoice.pojo.Team;
import com.lhx.goodchoice.pojo.User;
import com.lhx.goodchoice.pojo.request.TeamAddRequest;
import com.lhx.goodchoice.pojo.request.TeamUpdateRequest;
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
        User loginUser = userService.getCurrentUser(request);
        long teamId = teamService.addTeam(team, loginUser);
        return Result.ok(teamId);
    }


    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍数据修改请求参数为空");
        }
        User loginUser = userService.getCurrentUser(request);
        boolean updated = teamService.updateTeam(teamUpdateRequest, loginUser);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"数据更新失败");
        }
        return Result.ok(true);
    }


}
