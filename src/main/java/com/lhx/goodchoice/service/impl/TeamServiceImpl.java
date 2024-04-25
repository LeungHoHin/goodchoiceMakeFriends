package com.lhx.goodchoice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhx.goodchoice.common.ErrorCode;
import com.lhx.goodchoice.exception.BusinessException;
import com.lhx.goodchoice.pojo.Team;
import com.lhx.goodchoice.pojo.User;
import com.lhx.goodchoice.pojo.UserTeam;
import com.lhx.goodchoice.pojo.dto.TeamQuery;
import com.lhx.goodchoice.pojo.enums.TeamStatusEnum;
import com.lhx.goodchoice.pojo.request.DeleteTeamRequest;
import com.lhx.goodchoice.pojo.request.JoinTeamRequest;
import com.lhx.goodchoice.pojo.request.QuitTeamRequest;
import com.lhx.goodchoice.pojo.request.TeamUpdateRequest;
import com.lhx.goodchoice.pojo.vo.UserTeamVO;
import com.lhx.goodchoice.pojo.vo.UserVO;
import com.lhx.goodchoice.service.TeamService;
import com.lhx.goodchoice.mapper.TeamMapper;
import com.lhx.goodchoice.service.UserService;
import com.lhx.goodchoice.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Neveremoer
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2024-04-24 18:15:44
 */
@Service
@Slf4j
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {


    @Resource
    private UserService userService;

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private RedissonClient redissonClient;

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
        if (loginUser.getUserCreatedAndJoinedTeams() >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前用户创建队伍数量超过最大值");
        }


        //插入队伍信息至队伍表中
        team.setLeaderId(loginUser.getUserId());
        boolean teamSaved = this.save(team);
        if (!teamSaved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }
        Long teamId = team.getTeamId();

        //用户所创建的队伍数量+1
        loginUser.setUserCreatedAndJoinedTeams(loginUser.getUserCreatedAndJoinedTeams() + 1);
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
        if (!userService.isAdmin(loginUser) && !team.getLeaderId().equals(loginUser.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限修改");
        }

        //3. 如果用户传入的值不变，就不update
        String oldDate = team.getTeamExpireTime() == null ? "" : new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(team.getTeamExpireTime());
        String newDate = teamUpdateRequest.getTeamExpireTime() == null ? "" : new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(teamUpdateRequest.getTeamExpireTime());
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
        if (updateTeam.getTeamMaxNum() < team.getTeamNum()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数有误");
        }
        return this.updateById(updateTeam);
    }

    @Override
    public List<UserTeamVO> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
        if (teamQuery != null) {
            //根据id查询
            Long teamId = teamQuery.getTeamId();
            if (teamId != null && teamId >= 0) {
                queryWrapper.eq(Team::getTeamId, teamId);
            }
            //根据id列表查询
            List<Long> teamIdList = teamQuery.getTeamIdList();
            if (CollectionUtils.isNotEmpty(teamIdList)) {
                queryWrapper.in(Team::getTeamId, teamIdList);
            }
            //根据搜索文本查询
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.like(Team::getTeamName, searchText).or().like(Team::getTeamDescription, searchText);
            }
            //根据队伍名称查询
            String name = teamQuery.getTeamName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like(Team::getTeamName, name);
            }
            //根据描述查询
            String description = teamQuery.getTeamDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like(Team::getTeamDescription, description);
            }
            //根据最大人数查询
            Integer maxNum = teamQuery.getTeamMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq(Team::getTeamMaxNum, maxNum);
            }
            //根据队长来查询
            Long userId = teamQuery.getUserId();
            if (userId != null && userId > 0) {
                queryWrapper.eq(Team::getLeaderId, userId);
            }
            //根据状态查询
            Integer statusValue = teamQuery.getTeamStatus();
            TeamStatusEnum status = TeamStatusEnum.getStatusByValue(statusValue);
            if (status == null) {
                status = TeamStatusEnum.PUBLIC;
            }
            if (!isAdmin && status.equals(TeamStatusEnum.PRIVATE)) {
                throw new BusinessException(ErrorCode.NO_AUTH, "你没有权限");
            }
            queryWrapper.eq(Team::getTeamStatus, status.getValue());
        }
        //不展示已经过期的队伍
//        queryWrapper.lt(Team::getTeamExpireTime, new Date()).or().isNull(Team::getTeamExpireTime);
        try {
            queryWrapper.and(qw -> qw.gt(Team::getTeamExpireTime, new Date()).or().isNull(Team::getTeamExpireTime));
        } catch (BusinessException e) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "没有查到队伍");
        }
        List<Team> teamList = this.list(queryWrapper);

        if (teamList == null) {
            return new ArrayList<>();
        }

        //关联查询创建人的用户信息
        ArrayList<UserTeamVO> userTeamVOList = new ArrayList<>();
        for (Team team : teamList) {
            Long userId = team.getLeaderId();
            if (userId == null) {
                continue;
            }
            UserTeamVO userTeamVO = new UserTeamVO();
            BeanUtils.copyProperties(team, userTeamVO);
            User user = userService.getById(userId);
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                userTeamVO.setUserVO(userVO);
            }
            userTeamVOList.add(userTeamVO);
        }
        return userTeamVOList;
    }

    @Override
    public Page<Team> listPageTeams(TeamQuery teamQuery) {
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery, team);
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>(team);
        return this.page(new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize()), queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean joinTeam(JoinTeamRequest joinTeamRequest, User loginUser) {
        if (joinTeamRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求加入队伍参数为空");
        }
        Long teamId = joinTeamRequest.getTeamId();
        if (teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍ID为空");
        }
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Team::getTeamId, teamId);
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        Date teamExpireTime = team.getTeamExpireTime();
        if (teamExpireTime != null && teamExpireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        Integer value = team.getTeamStatus();
        TeamStatusEnum status = TeamStatusEnum.getStatusByValue(value);
        if (TeamStatusEnum.PRIVATE.equals(status)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "禁止加入私有队伍");
        }
        String password = joinTeamRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(status)) {
            if (!password.equals(team.getTeamPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }


        RLock lock = redissonClient.getLock("goodchoice:join_team");
        //用户最多加入五个队伍
        try {
            while (true) {
                if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    if (loginUser.getUserCreatedAndJoinedTeams() >= 5) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建和加入 5 个队伍");
                    }

                    LambdaQueryWrapper<UserTeam> hasJoinedQueryWrapper = new LambdaQueryWrapper<>();
                    hasJoinedQueryWrapper.eq(UserTeam::getTeamId, teamId);
                    hasJoinedQueryWrapper.eq(UserTeam::getUserId, loginUser.getUserId());
                    long hasJoined = userTeamService.count(hasJoinedQueryWrapper);
                    if (hasJoined > 0) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已加入过该队伍");
                    }

                    if (team.getTeamNum() >= team.getTeamMaxNum()) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数已满");
                    }
                    //队伍人数+1
                    team.setTeamNum(team.getTeamNum() + 1);
                    this.updateById(team);

                    //新增用户队伍关联信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(loginUser.getUserId());
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
            return false;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(QuitTeamRequest quitTeamRequest, User loginUser) {
        if (quitTeamRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "退出队伍参数为空");
        }
        Long teamId = quitTeamRequest.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍id有误");
        }
        Long userId = loginUser.getUserId();
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "退出队伍不存在");
        }
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setUserId(userId);
        queryUserTeam.setTeamId(teamId);
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>(queryUserTeam);
        long count = userTeamService.count(queryWrapper);
        if (count == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入队伍");
        }
        //队伍操作
        //如果退出后队伍只剩一人，则解散队伍
        if (team.getTeamNum() - 1 == 1) {
            this.removeById(teamId);
        } else {//如果队伍中还有其他成员
            if (Objects.equals(team.getLeaderId(), userId)) {
                //如果是队长退出队伍，把权限转交给第二名进入队伍的成员
                LambdaQueryWrapper<UserTeam> findCaptain = new LambdaQueryWrapper<>();
                findCaptain.eq(UserTeam::getTeamId, teamId);
                findCaptain.last("order by user_id asc limit 2");
                List<UserTeam> captainList = userTeamService.list(findCaptain);
                if (captainList.size() < 2 || CollectionUtils.isEmpty(captainList)) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "选择退出队伍后队伍成员仍然多于2人");
                }
                //更新当前队伍的队长
                UserTeam captain = captainList.get(1);
                Long captainId = captain.getUserId();
                Team updateTeam = new Team();
                BeanUtils.copyProperties(quitTeamRequest, updateTeam);
                updateTeam.setLeaderId(captainId);
                boolean updated = this.updateById(updateTeam);
                if (!updated) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍队长失败");
                }
            }
        }
        return userTeamService.remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(DeleteTeamRequest deleteTeamRequest, User loginUser) {
        Long teamId = deleteTeamRequest.getTeamId();
        Long userId = loginUser.getUserId();
        Team teamToDelete = this.getById(teamId);
        if (teamToDelete == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "要删除的队伍不存在");
        }
        if (!teamToDelete.getLeaderId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }
        LambdaQueryWrapper<UserTeam> findUserTeamToRemoveWrapper = new LambdaQueryWrapper<>();
        findUserTeamToRemoveWrapper.eq(UserTeam::getTeamId, teamId);
        boolean remove = userTeamService.remove(findUserTeamToRemoveWrapper);
        if (!remove) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍关联信息失败");
        }
        return this.removeById(teamId);
    }


}




