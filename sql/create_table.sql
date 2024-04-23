-- 用户表
create table user
(
    user_name     varchar(256)                       null comment '用户昵称',
    user_id       bigint auto_increment comment 'id'
        primary key,
    user_account  varchar(256)                       null comment '账号',
    avatar_url    varchar(1024)                      null comment '用户头像',
    user_gender   tinyint                            null comment '性别',
    user_password varchar(512)                       not null comment '密码',
    user_phone    varchar(128)                       null comment '电话号码',
    user_email    varchar(512)                       null comment '邮箱',
    user_tags     varchar(1024)                      null comment '标签列表',
    user_profile  varchar(512)                       null comment '用户个人简介',
    user_status   int      default 0                 not null comment '用户状态',
    user_role     int      default 0                 not null comment '用户角色 0-普通用户 1-管理员',
    create_time   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP null comment '修改时间',
    delete_time   tinyint  default 0                 not null comment '是否删除',
    is_deleted    tinyint  default 0                 not null comment '是否删除'
)
    comment '用户';


-- 队伍表
create table team
(
    team_id          bigint auto_increment comment '队伍id'
        primary key,
    team_name        varchar(256)                       not null comment '队伍名称',
    team_description varchar(1024)                      null comment '队伍描述',
    team_max_num     int      default 1                 not null comment '队伍最大人数',
    team_expire_time datetime                           null comment '队伍过期时间',
    user_id          bigint comment '用户id',
    team_status      int      default 0                 not null comment '0-公开，1-私有，2-加密',
    team_password    varchar(512)                       null comment '密码',
    create_time      datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time      datetime default CURRENT_TIMESTAMP null comment '修改时间',
    is_deleted       tinyint  default 0                 not null comment '是否删除'
) comment '队伍';


-- 用户队伍关系
create table user_team
(
    user_team_id bigint auto_increment comment '用户队伍关系id' primary key,
    user_id      bigint comment '用户id',
    team_id      bigint comment '队伍id',
    join_time    datetime                           not null comment '加入时间',
    create_time  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP null comment '修改时间',
    is_deleted   tinyint  default 0                 not null comment '是否删除'
) comment '用户队伍关系';
