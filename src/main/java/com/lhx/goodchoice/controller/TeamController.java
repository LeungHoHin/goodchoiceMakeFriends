package com.lhx.goodchoice.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lhx.goodchoice.common.BaseResponse;
import com.lhx.goodchoice.common.ErrorCode;
import com.lhx.goodchoice.common.Result;
import com.lhx.goodchoice.exception.BusinessException;
import com.lhx.goodchoice.pojo.Team;
import com.lhx.goodchoice.pojo.User;
import com.lhx.goodchoice.pojo.UserTeam;
import com.lhx.goodchoice.pojo.dto.TeamQuery;
import com.lhx.goodchoice.pojo.request.TeamAddRequest;
import com.lhx.goodchoice.pojo.request.TeamUpdateRequest;
import com.lhx.goodchoice.pojo.vo.UserTeamVO;
import com.lhx.goodchoice.service.TeamService;
import com.lhx.goodchoice.service.UserService;
import com.lhx.goodchoice.service.UserTeamService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据更新失败");
        }
        return Result.ok(true);
    }


    @GetMapping("/list")
    public BaseResponse<List<UserTeamVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "传入的teamQuery为空");
        }
        boolean isAdmin = userService.isAdmin(request);
        List<UserTeamVO> userTeamVOList = teamService.listTeams(teamQuery, isAdmin);
        List<Long> teamIdsList = userTeamVOList.stream().map(UserTeamVO::getTeamId).collect(Collectors.toList());
        //判断用户是否已经加入队伍
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        User loginUser = userService.getCurrentUser(request);
        queryWrapper.eq(UserTeam::getUserId, loginUser.getUserId());
        queryWrapper.in(UserTeam::getTeamId, teamIdsList);
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        Set<Long> hasJoinTeamIdList = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
        userTeamVOList.forEach(team -> {
            boolean joined = hasJoinTeamIdList.contains(team.getTeamId());
            team.setUserHasJoinTeam(joined);
        });
        //3. 查询已加入队伍的人数
        LambdaQueryWrapper<UserTeam> userTeamQueryWrapper = new LambdaQueryWrapper<>();
        userTeamQueryWrapper.in(UserTeam::getTeamId, teamIdsList);
        List<UserTeam> userTeams = userTeamService.list(userTeamQueryWrapper);
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeams.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        userTeamVOList.forEach(team ->
                team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getTeamId(), new ArrayList<>()).size()));
        return Result.ok(userTeamVOList);
    }
}
