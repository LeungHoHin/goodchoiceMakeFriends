package com.lhx.goodchoice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhx.goodchoice.common.ErrorCode;
import com.lhx.goodchoice.exception.BusinessException;
import com.lhx.goodchoice.pojo.Team;
import com.lhx.goodchoice.pojo.User;
import com.lhx.goodchoice.pojo.UserTeam;
import com.lhx.goodchoice.pojo.enums.TeamStatusEnum;
import com.lhx.goodchoice.service.TeamService;
import com.lhx.goodchoice.mapper.TeamMapper;
import com.lhx.goodchoice.service.UserService;
import com.lhx.goodchoice.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Optional;

/**
 * @author 梁浩轩
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2024-04-23 20:22:20
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, HttpServletRequest request) {
        //验证用户是否登录
        User currentUser = userService.getCurrentUser(request);
        //校验
        //1. 请求参数是否为空
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍为空");
        }
        //2. 是否已经登录
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH, "用户未登录");
        }

        //信息校验
        //3. 队伍人数要求大于1人少于20人
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
        if (new Date().after(teamExpireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前时间已经超过队伍的超时时间");
        }

        //9. 一名用户最多创建五个队伍
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
        Long userId = currentUser.getUserId();
        queryWrapper.eq(Team::getUserId, userId);
        long teamCount = this.count(queryWrapper);
        if (teamCount > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前用户创建队伍数量超过最大值");
        }


        //插入队伍信息至队伍表中
        team.setUserId(userId);
        boolean teamSaved = this.save(team);
        if (!teamSaved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }
        Long teamId = team.getTeamId();


        //插入用户队伍关系表至关系表中
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        boolean userTeamSaved = userTeamService.save(userTeam);
        if (!userTeamSaved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }

        return teamId;
    }
}




