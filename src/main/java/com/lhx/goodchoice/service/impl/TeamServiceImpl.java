package com.lhx.goodchoice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhx.goodchoice.common.ErrorCode;
import com.lhx.goodchoice.exception.BusinessException;
import com.lhx.goodchoice.mapper.UserMapper;
import com.lhx.goodchoice.pojo.Team;
import com.lhx.goodchoice.pojo.User;
import com.lhx.goodchoice.pojo.UserTeam;
import com.lhx.goodchoice.pojo.enums.TeamStatusEnum;
import com.lhx.goodchoice.pojo.request.TeamUpdateRequest;
import com.lhx.goodchoice.service.TeamService;
import com.lhx.goodchoice.mapper.TeamMapper;
import com.lhx.goodchoice.service.UserService;
import com.lhx.goodchoice.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

/**
 * @author Neveremoer
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2024-04-24 18:15:44
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {
    @Resource
    private TeamMapper teamMapper;

    @Resource
    private UserService userService;

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        //校验
        //1. 请求参数是否为空
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍为空");
        }
        //2. 是否已经登录
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH, "用户未登录");
        }
        //信息校验
        //3. 队伍最大人数人数要求大于1人少于20人
        int maxNum = Optional.ofNullable(team.getTeamMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不符合要求");
        }

        //4. 队伍标题字数不得大于20字
        if (StringUtils.isBlank(team.getTeamName()) || team.getTeamName().length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名称不符合要求");
        }

        //5. 描述不多于500字
        if (team.getTeamDescription().length() > 500 && StringUtils.isNotBlank(team.getTeamDescription())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述字数不符合要求");
        }

        //6. 队伍状态是否为公开
        int status = Optional.ofNullable(team.getTeamStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getStatusByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不符合要求");
        }

        //7. 如果status是加密状态必须要有密码，且密码长度不长于32位
        String password = team.getTeamPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(password)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密状态下密码不能为空");
            }
        }

        //8. 当前时间是否超过超时时间
        Date teamExpireTime = team.getTeamExpireTime();
        if (teamExpireTime != null) {
            if (new Date().after(teamExpireTime)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前时间已经超过队伍的超时时间");
            }
        }

        //9. 一名用户最多创建五个队伍
        if (loginUser.getUserCreatedTeams() >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前用户创建队伍数量超过最大值");
        }


        //插入队伍信息至队伍表中
        team.setUserId(loginUser.getUserId());
        boolean teamSaved = this.save(team);
        if (!teamSaved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }
        Long teamId = team.getTeamId();

        //用户所创建的队伍数量+1
        loginUser.setUserCreatedTeams(loginUser.getUserCreatedTeams() + 1);
        userService.updateById(loginUser);


        //插入用户队伍关系表至关系表中
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(loginUser.getUserId());
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        boolean userTeamSaved = userTeamService.save(userTeam);
        if (!userTeamSaved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }
        return teamId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        Long id = teamUpdateRequest.getTeamId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        //1. 查询队伍是否存在
        Team team = this.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求队伍不存在");
        }
        //2. 只有管理员或者队伍的创建者可以修改
        if (!userService.isAdmin(loginUser) && !team.getUserId().equals(loginUser.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限修改");
        }

        //3. 如果用户传入的值不变，就不update
        String oldDate = team.getTeamExpireTime() == null ? "" : new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(team.getTeamExpireTime());
        String newDate = team.getTeamExpireTime() == null ? "" : new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(teamUpdateRequest.getTeamExpireTime());
        if (oldDate.equals(newDate)) {
            if (teamUpdateRequest.getTeamDescription().equals(team.getTeamDescription())) {
                if (teamUpdateRequest.getTeamName().equals(team.getTeamName())) {
                    if (teamUpdateRequest.getTeamMaxNum().equals(team.getTeamMaxNum())) {
                        if (teamUpdateRequest.getTeamStatus().equals(team.getTeamStatus())) {
                            if (teamUpdateRequest.getTeamPassword().equals(team.getTeamPassword())) {
                                throw new BusinessException(ErrorCode.PARAMS_ERROR, "没有有变动的数据");
                            }
                        }
                    }
                }
            }
        }

        //4. 如果队伍状态改为加密，则必须要有密码
        TeamStatusEnum teamStatus = TeamStatusEnum.getStatusByValue(teamUpdateRequest.getTeamStatus());
        if (teamStatus.equals(TeamStatusEnum.SECRET)) {
            if (teamUpdateRequest.getTeamPassword() == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间必须要设置密码");
            }
        }

        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, updateTeam);
        return this.updateById(updateTeam);
    }


}




