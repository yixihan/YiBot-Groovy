# yibot ddl
drop database if exists `yibot`;
create database if not exists `yibot`;

use `yibot`;

# 随机复读表
drop table if exists `repeater_random`;
create table if not exists `repeater_random`
(
    `id`          bigint(18) unsigned auto_increment comment 'id',
    `text`        varchar(255) not null comment '触发词',
    `weight`       double       not null comment '触发权重',
    `group_id`    bigint(18)   not null comment '群号',
    `create_date` date         not null comment '创建时间',
    `create_by`   bigint(18) comment '创建人',
    `del_flag`    tinyint(1)   not null default 0 comment '逻辑删除',

    primary key (`id`),
    index weight_idx (`weight`),
    index group_id_idx (`group_id`),
    index text_idx (`text`)
) comment '随机复读表';

drop table if exists `repeater_exclude_group`;
create table if not exists `repeater_exclude_group`
(
    `id`          bigint(18) unsigned auto_increment comment 'id',
    `group_id`    bigint(18) not null comment '群号',
    `create_date` date       not null comment '创建时间',
    `create_by`   bigint(18) comment '创建人',
    `del_flag`    tinyint(1) not null default 0 comment '逻辑删除',

    primary key (`id`),
    index group_id_idx (`group_id`)
) comment '复读排除群表';

drop table if exists `off_work_time`;
create table if not exists `off_work_time`
(
    `id`            bigint(18) unsigned auto_increment comment 'id',
    `off_work_time` varchar(10) not null comment '下班时间',
    `create_date`   date        not null comment '创建时间',
    `create_by`     bigint(18) comment '创建人',
    `del_flag`      tinyint(1)  not null default 0 comment '逻辑删除',

    primary key (`id`),
    index off_work_time_idx (`off_work_time`)
) comment '下班时间表';

insert into `off_work_time`(off_work_time, create_date, create_by)
values
    ('17:00', now(), '3113788997'),
    ('17:30', now(), '3113788997'),
    ('18:00', now(), '3113788997'),
    ('18:30', now(), '3113788997'),
    ('19:00', now(), '3113788997'),
    ('20:30', now(), '3113788997'),
    ('23:00', now(), '3113788997');