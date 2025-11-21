USE helloworld;

-- =========================
-- 1. 初始化角色
-- =========================
INSERT INTO
    `dynamicmenu_sys_role` (`role_code`, `role_name`, `description`)
VALUES
    ('ROLE_ADMIN', '管理员', '系统管理员，拥有所有权限'),
    ('ROLE_EDITOR', '编辑者', '内容编辑人员，可以管理部分内容'),
    ('ROLE_VIEWER', '查看者', '只读用户，只能浏览数据');

-- =========================
-- 2. 初始化菜单数据（示例：仪表盘 + 系统管理 + 内容管理 + 统计）
--    说明：ID 按自增，插入顺序决定 parent_id 对应关系
-- =========================
INSERT INTO
    `dynamicmenu_sys_menu` (
        `parent_id`,
        `menu_name`,
        `menu_icon`,
        `menu_type`,
        `route_path`,
        `component`,
        `perms`,
        `visible`,
        `sort_order`,
        `status`
    )
VALUES
    -- 1. 仪表盘（菜单）
    (
        0,
        '仪表板',
        'DataBoard',
        2,
        '/dashboard',
        'dashboard/index',
        'sys:dashboard:view',
        1,
        1,
        1
    ),
    -- 2. 系统管理（目录）
    (
        0,
        '系统管理',
        'Setting',
        1,
        '/system',
        NULL,
        NULL,
        1,
        100,
        1
    ),
    -- 3. 用户管理（菜单）
    (
        2,
        '用户管理',
        'User',
        2,
        '/system/user',
        'system/user/index',
        'sys:user:list',
        1,
        1,
        1
    ),
    -- 4. 用户新增按钮
    (
        3,
        '新增用户',
        NULL,
        3,
        NULL,
        NULL,
        'sys:user:add',
        1,
        10,
        1
    ),
    -- 5. 用户编辑按钮
    (
        3,
        '编辑用户',
        NULL,
        3,
        NULL,
        NULL,
        'sys:user:update',
        1,
        11,
        1
    ),
    -- 6. 用户删除按钮
    (
        3,
        '删除用户',
        NULL,
        3,
        NULL,
        NULL,
        'sys:user:delete',
        1,
        12,
        1
    ),
    -- 7. 角色管理（菜单）
    (
        2,
        '角色管理',
        'Lock',
        2,
        '/system/role',
        'system/role/index',
        'sys:role:list',
        1,
        2,
        1
    ),
    -- 8. 角色菜单分配按钮
    (
        7,
        '分配菜单',
        NULL,
        3,
        NULL,
        NULL,
        'sys:role:assignMenu',
        1,
        10,
        1
    ),
    -- 9. 菜单管理（菜单）
    (
        2,
        '菜单管理',
        'Menu',
        2,
        '/system/menu',
        'system/menu/index',
        'sys:menu:list',
        1,
        3,
        1
    ),
    -- 10. 菜单新增按钮
    (
        9,
        '新增菜单',
        NULL,
        3,
        NULL,
        NULL,
        'sys:menu:add',
        1,
        10,
        1
    ),
    -- 11. 菜单编辑按钮
    (
        9,
        '编辑菜单',
        NULL,
        3,
        NULL,
        NULL,
        'sys:menu:update',
        1,
        11,
        1
    ),
    -- 12. 菜单删除按钮
    (
        9,
        '删除菜单',
        NULL,
        3,
        NULL,
        NULL,
        'sys:menu:delete',
        1,
        12,
        1
    ),
    -- 13. 内容管理（目录）
    (
        0,
        '内容管理',
        'Document',
        1,
        '/content',
        NULL,
        NULL,
        1,
        2,
        1
    ),
    -- 14. 文章管理
    (
        13,
        '文章管理',
        'Document',
        2,
        '/content/article',
        'content/article/index',
        'content:article:list',
        1,
        1,
        1
    ),
    -- 15. 分类管理
    (
        13,
        '分类管理',
        'Collection',
        2,
        '/content/category',
        'content/category/index',
        'content:category:list',
        1,
        2,
        1
    ),
    -- 16. 数据统计（目录）
    (
        0,
        '数据统计',
        'DataAnalysis',
        1,
        '/statistics',
        NULL,
        NULL,
        1,
        3,
        1
    ),
    -- 17. 访问统计
    (
        16,
        '访问统计',
        'TrendCharts',
        2,
        '/statistics/visit',
        'statistics/visit/index',
        'statistics:visit:view',
        1,
        1,
        1
    );

-- =========================
-- 3. 初始化管理员用户
-- =========================
-- 说明：
--   1）password 建议使用 BCrypt 加密后的字符串
--   2）password 字段内容，请替换成你通过 PasswordGenerator 生成的实际密文
--   3）登录时使用明文 '123456'，Spring Security 会用 BCrypt 校验
INSERT INTO
    `dynamicmenu_sys_user` (`username`, `password`, `nickname`, `status`)
VALUES
    (
        'admin',
        '$2a$10$I4zDCNKUUcosYFCu9VSUBe3idBKL13UZztprLUaUGLSRZjVjkZABK',
        '系统管理员',
        1
    );

-- =========================
-- 4. 设置权限关系
-- =========================
-- 4.1 管理员（ROLE_ADMIN）拥有所有菜单权限
-- 假设上面的角色插入后，ROLE_ADMIN 的 id = 1
-- 假设上面的菜单插入后，id 从 1 ~ 17 连续递增
INSERT INTO
    `dynamicmenu_sys_role_menu` (`role_id`, `menu_id`)
VALUES
    (1, 1),
    (1, 2),
    (1, 3),
    (1, 4),
    (1, 5),
    (1, 6),
    (1, 7),
    (1, 8),
    (1, 9),
    (1, 10),
    (1, 11),
    (1, 12),
    (1, 13),
    (1, 14),
    (1, 15),
    (1, 16),
    (1, 17);

-- 4.2 编辑者（ROLE_EDITOR）拥有部分内容管理 / 系统查看权限
INSERT INTO
    `dynamicmenu_sys_role_menu` (`role_id`, `menu_id`)
VALUES
    (2, 1),
    -- 仪表板
    (2, 3),
    -- 用户管理（只看列表，不给新增/删除按钮也可以）
    (2, 7),
    -- 角色管理（只看）
    (2, 9),
    -- 菜单管理（只看）
    (2, 13),
    -- 内容管理
    (2, 14),
    -- 文章管理
    (2, 15);

-- 分类管理
-- 4.3 查看者（ROLE_VIEWER）只有仪表板和部分只读权限
INSERT INTO
    `dynamicmenu_sys_role_menu` (`role_id`, `menu_id`)
VALUES
    (3, 1),
    -- 仪表板
    (3, 14),
    -- 文章管理
    (3, 17);

-- 访问统计
-- 4.4 设置用户角色：管理员用户 admin -> ROLE_ADMIN
-- 假设 admin 用户 ID = 1，ROLE_ADMIN 角色 ID = 1
INSERT INTO
    `dynamicmenu_sys_user_role` (`user_id`, `role_id`)
VALUES
    (1, 1);