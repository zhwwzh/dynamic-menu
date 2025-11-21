-- 如果存在旧库，先删除
DROP DATABASE IF EXISTS helloworld;

-- 创建数据库
CREATE DATABASE helloworld DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE helloworld;

-- =========================
-- 1. 用户表：存储系统用户信息
-- =========================
CREATE TABLE `dynamicmenu_sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码（BCrypt 加密）',
    `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
    `avatar` VARCHAR(200) DEFAULT NULL COMMENT '头像',
    `status` TINYINT DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB COMMENT = '用户表';

-- =========================
-- 2. 角色表：定义系统中的角色
-- =========================
CREATE TABLE `dynamicmenu_sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码（如 ROLE_ADMIN）',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
    `status` TINYINT DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE = InnoDB COMMENT = '角色表';

-- =========================
-- 3. 菜单表：系统的所有菜单 / 按钮
-- =========================
CREATE TABLE `dynamicmenu_sys_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `parent_id` BIGINT DEFAULT '0' COMMENT '父菜单ID，0 表示根菜单',
    `menu_name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
    `menu_icon` VARCHAR(50) DEFAULT NULL COMMENT '菜单图标',
    `menu_type` TINYINT NOT NULL COMMENT '菜单类型：1-目录，2-菜单，3-按钮',
    `route_path` VARCHAR(200) DEFAULT NULL COMMENT '路由路径（前端路由）',
    `component` VARCHAR(200) DEFAULT NULL COMMENT '前端组件路径',
    `perms` VARCHAR(100) DEFAULT NULL COMMENT '权限标识（如 sys:user:list）',
    `visible` TINYINT DEFAULT '1' COMMENT '显示状态：0-隐藏，1-显示',
    `sort_order` INT DEFAULT '0' COMMENT '排序号（越小越靠前）',
    `status` TINYINT DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT = '菜单表';

-- =========================
-- 4. 角色菜单关联表：角色拥有哪些菜单权限
-- =========================
CREATE TABLE `dynamicmenu_sys_role_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_menu_id` (`menu_id`)
) ENGINE = InnoDB COMMENT = '角色菜单关联表';

-- =========================
-- 5. 用户角色关联表：用户属于哪些角色
-- =========================
CREATE TABLE `dynamicmenu_sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE = InnoDB COMMENT = '用户角色关联表';