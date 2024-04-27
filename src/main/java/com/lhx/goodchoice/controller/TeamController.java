package com.lhx.goodchoice.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lhx.goodchoice.common.BaseResponse;
import com.lhx.goodchoice.common.ErrorCode;
import com.lhx.goodchoice.common.Result;
import com.lhx.goodchoice.exception.BusinessException;
import com.lhx.goodchoice.pojo.Team;
import com.lhx.goodchoice.pojo.User;
import com.lhx.goodchoice.pojo.UserTeam;
import com.lhx.goodchoice.pojo.dto.TeamQuery;
import com.lhx.goodchoice.pojo.request.*;
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

    @GetMapping("/get")
    public BaseResponse<Team> getCurrentTeam(Long teamId) {
        if (teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "传入的队伍ID为空");
        }
        Team team = teamService.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍为空");
        }
        return Result.ok(team);
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


    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listPage(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "传入的teamQuery为空");
        }
        Page<Team> teamPage = teamService.listPageTeams(teamQuery);
        return Result.ok(teamPage);
    }


    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody JoinTeamRequest joinTeamRequest, HttpServletRequest request) {
        if (joinTeamRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "加入队伍请求参数为空");
        }
        User loginUser = userService.getCurrentUser(request);
        boolean isJoined = teamService.joinTeam(joinTeamRequest, loginUser);
        if (!isJoined) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "加入队伍请失败");
        }
        return Result.ok(true);
    }


    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody QuitTeamRequest quitTeamRequest, HttpServletRequest request) {
        if (quitTeamRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "退出队伍请求参数为空");
        }
        User loginUser = userService.getCurrentUser(request);
        boolean quited = teamService.quitTeam(quitTeamRequest, loginUser);
        if (!quited) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "退出队伍请失败");
        }
        return Result.ok(true);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteTeamRequest deleteTeamRequest, HttpServletRequest request) {
        if (deleteTeamRequest == null || deleteTeamRequest.getTeamId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "删除队伍请求参数为空");
        }
        User loginUser = userService.getCurrentUser(request);
        boolean deleted = teamService.deleteTeam(deleteTeamRequest, loginUser);
        if (!deleted) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍请失败");
        }
        return Result.ok(true);
    }


    @GetMapping("/my/createdTeams")
    public BaseResponse<List<UserTeamVO>> listMyCreatedTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "传入的teamQuery为空");
        }
        User loginUser = userService.getCurrentUser(request);
        teamQuery.setUserId(loginUser.getUserId());
        List<UserTeamVO> userTeamVOList = teamService.listTeams(teamQuery, true);
        return Result.ok(userTeamVOList);

    }


    @GetMapping("/my/joinedTeams")
    public BaseResponse<List<UserTeamVO>> listMyJoinedTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "传入的teamQuery为空");
        }
        User loginUser = userService.getCurrentUser(request);
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getUserId, loginUser.getUserId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        Map<Long, List<UserTeam>> listMap = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        List<Long> idList = new ArrayList<>(listMap.keySet());
        teamQuery.setTeamIdList(idList);
        List<UserTeamVO> userTeamVOList = teamService.listTeams(teamQuery, true);
        return Result.ok(userTeamVOList);
    }


}
