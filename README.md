# Dynamic Menu Permission System (基于 Spring Boot 的动态菜单权限系统)

基于 **Spring Boot 3 + Spring Security + JWT + MyBatis-Plus** 实现的
**后端 RBAC 权限管理系统**，支持：

-   ✔ 动态菜单（多角色合并）
-   ✔ 按钮级权限（perms 标识）
-   ✔ JWT 无状态认证
-   ✔ 角色授权（菜单树勾选）
-   ✔ 用户管理
-   ✔ 分层清晰的可扩展架构

适合作为后台系统、SaaS 管理平台等项目的通用权限模块。

---

## 技术栈

| 技术              | 说明               |
| ----------------- | ------------------ |
| Spring Boot 3.x   | 主框架             |
| Spring Security 6 | 身份认证与权限控制 |
| JWT               | Token 认证         |
| MyBatis-Plus      | 数据持久层         |
| MapStruct         | DO/VO 转换         |
| Lombok            | 简化实体类         |
| Logback           | 日志系统           |
| Maven             | 构建工具           |
| VSCode            | 推荐开发 IDE       |

---

## 功能清单

### 认证授权

-   用户登录（JWT）
-   Token 校验过滤器（JwtAuthenticationFilter）
-   无状态会话（SessionCreationPolicy.STATELESS）
-   自定义 401/403 处理器

### 用户管理

-   用户列表（附带角色、权限、菜单树）
-   查询用户详情
-   获取当前登录用户（/api/auth/me）

### 角色管理

-   角色增删改查
-   角色绑定菜单（多选树）
-   查询角色菜单

### 菜单管理（核心）

-   支持目录 / 菜单 / 按钮（menuType=1/2/3）
-   动态菜单树生成（按用户 → 多角色 → 菜单）
-   系统全量菜单树（后台管理用）
-   多角色合并去重
-   菜单排序（sortOrder）

---

## 项目结构

```text
src/
 ├── main/
 │   ├── java/net/wcloud/helloworld/dynamicmenu/
 │   │   ├── common/                 # 通用返回结构 Result
 │   │   ├── config/                 # 安全 + JWT + 日志配置
 │   │   ├── controller/             # 控制器
 │   │   ├── convert/                # MapStruct 转换器
 │   │   ├── dto/                    # 请求/响应 DTO
 │   │   ├── entity/                 # 实体类
 │   │   ├── mapper/                 # MyBatis-Plus Mapper + 注解 SQL
 │   │   ├── security/               # Security + JWT + UserDetails
 │   │   ├── service/                # 服务接口
 │   │   └── service/impl/           # 服务实现
 │   └── resources/
 │       ├── application.yaml        # 配置文件（UTF-8）
 │       ├── logback-spring.xml      # 日志配置（UTF-8）
 │       └── mapper XML（如果有）
 └── test/                           # 单元测试
```

---

## ER 图

```mermaid
erDiagram
DYNAMICMENU_SYS_USER {
BIGINT id PK "用户 ID"
VARCHAR username "用户名（唯一）"
VARCHAR password "BCrypt 加密密码"
VARCHAR nickname "昵称"
VARCHAR avatar "头像 URL"
TINYINT status "状态：0-禁用 1-启用"
DATETIME create_time "创建时间"
}

    DYNAMICMENU_SYS_ROLE {
        BIGINT id PK "角色ID"
        VARCHAR role_code "角色编码（ROLE_ 前缀）"
        VARCHAR role_name "角色名称"
        TINYINT status "状态：0-禁用 1-启用"
        DATETIME create_time "创建时间"
        DATETIME update_time "更新时间"
    }

    DYNAMICMENU_SYS_MENU {
        BIGINT id PK "菜单ID"
        BIGINT parent_id "父菜单ID（0/NULL 根）"
        VARCHAR menu_name "菜单名称"
        VARCHAR menu_icon "菜单图标"
        TINYINT menu_type "1目录 2菜单 3按钮"
        VARCHAR route_path "路由路径"
        VARCHAR component "前端组件路径"
        VARCHAR perms "权限标识（如 sys:user:list）"
        TINYINT visible "是否可见：1显示 0隐藏"
        TINYINT status "状态：1启用 0禁用"
        INT sort_order "排序字段"
        DATETIME create_time "创建时间"
        DATETIME update_time "更新时间"
    }

    DYNAMICMENU_SYS_USER_ROLE {
        BIGINT user_id FK "用户ID"
        BIGINT role_id FK "角色ID"
    }

    DYNAMICMENU_SYS_ROLE_MENU {
        BIGINT role_id FK "角色ID"
        BIGINT menu_id FK "菜单ID"
    }

    %% 关系：用户 - 角色（多对多，通过 user_role）
    DYNAMICMENU_SYS_USER ||--o{ DYNAMICMENU_SYS_USER_ROLE : "has roles"
    DYNAMICMENU_SYS_ROLE ||--o{ DYNAMICMENU_SYS_USER_ROLE : "assigned to users"

    %% 关系：角色 - 菜单（多对多，通过 role_menu）
    DYNAMICMENU_SYS_ROLE ||--o{ DYNAMICMENU_SYS_ROLE_MENU : "has menus"
    DYNAMICMENU_SYS_MENU ||--o{ DYNAMICMENU_SYS_ROLE_MENU : "assigned to roles"

    %% 关系：菜单自关联（父子菜单）
    DYNAMICMENU_SYS_MENU ||--o{ DYNAMICMENU_SYS_MENU : "children"
```

---

## 类图

```mermaid
classDiagram

%% ========== Entity ==========
class User {
    +Long id
    +String username
    +String password
    +String nickname
    +String avatar
    +Integer status
    +LocalDateTime createTime
}

class Role {
    +Long id
    +String roleCode
    +String roleName
    +Integer status
    +LocalDateTime createTime
    +LocalDateTime updateTime
}

class Menu {
    +Long id
    +Long parentId
    +String menuName
    +String menuIcon
    +Integer menuType
    +String routePath
    +String component
    +String perms
    +Integer visible
    +Integer status
    +Integer sortOrder
    +LocalDateTime createTime
    +LocalDateTime updateTime
}

%% ========== Mapper ==========
class UserMapper
class RoleMapper
class MenuMapper

%% ========== Service 接口 & 实现 ==========
class UserService {
    +getByUsername()
    +listRoleCodesByUserId()
    +listPermissionsByUserId()
    +getUserDetail()
}

class UserServiceImpl {
    -UserMapper userMapper
    -MenuService menuService
    +getByUsername()
    +listUsersWithDetail()
    +buildUserVO()
}

UserService <|.. UserServiceImpl
UserServiceImpl --> UserMapper

class MenuService {
    +listMenuTreeByUserId()
    +listAllMenuTree()
}

class MenuServiceImpl {
    -MenuMapper menuMapper
    -MenuConvert menuConvert
    +listMenuTreeByUserId()
    +listAllMenuTree()
    -buildMenuTree()
}

MenuService <|.. MenuServiceImpl
MenuServiceImpl --> MenuMapper

%% ========== Security ==========
class LoginUserDetailsService {
    -UserService userService
    +loadUserByUsername()
}

LoginUserDetailsService --> UserService

class LoginUserDetails {
    -User user
    -List~String~ roleCodes
    -List~String~ permissions
    +getAuthorities()
    +isEnabled()
}

LoginUserDetails --> User

class JwtTokenUtil {
    -Key key
    +generateToken()
    +validateToken()
    +getUsername()
}

class JwtAuthenticationFilter {
    -JwtTokenUtil jwtTokenUtil
    -LoginUserDetailsService userDetailsService
    +doFilterInternal()
}

JwtAuthenticationFilter --> JwtTokenUtil
JwtAuthenticationFilter --> LoginUserDetailsService

%% ========== Converter ==========
class UserConvert
class MenuConvert

UserServiceImpl --> UserConvert
MenuServiceImpl --> MenuConvert

%% ========== Controller ==========
class AuthController {
    -AuthenticationManager authenticationManager
    -UserService userService
    -MenuService menuService
    -JwtTokenUtil jwtTokenUtil
    +login()
    +me()
}

AuthController --> UserService
AuthController --> MenuService
AuthController --> JwtTokenUtil

class UserController {
    -UserService userService
    +listUsers()
}

UserController --> UserService

class RoleController {
    -RoleMapper roleMapper
    -MenuService menuService
    +list()
    +assignMenus()
}

RoleController --> RoleMapper
RoleController --> MenuService
```

---

## 模块图

```mermaid
flowchart TD

subgraph controller["Controller 层"]
    AuthController
    UserController
    RoleController
end

subgraph service["Service 层"]
    UserServiceImpl
    RoleServiceImpl
    MenuServiceImpl
end

subgraph service_api["Service 接口"]
    UserService
    RoleService
    MenuService
end

subgraph mapper["Mapper 层"]
    UserMapper
    RoleMapper
    MenuMapper
end

subgraph security["Security 模块"]
    SecurityConfig
    JwtAuthenticationFilter
    JwtTokenUtil
    LoginUserDetailsService
    LoginUserDetails
    RestAuthenticationEntryPoint
    RestAccessDeniedHandler
end

subgraph convert["Convert（MapStruct）"]
    UserConvert
    MenuConvert
end

subgraph entity["Entity（DO）"]
    User
    Role
    Menu
end

AuthController --> UserServiceImpl
AuthController --> MenuServiceImpl
UserController --> UserServiceImpl
RoleController --> RoleServiceImpl

UserServiceImpl --> UserMapper
RoleServiceImpl --> RoleMapper
MenuServiceImpl --> MenuMapper

UserServiceImpl --> UserConvert
MenuServiceImpl --> MenuConvert

SecurityConfig --> JwtAuthenticationFilter
JwtAuthenticationFilter --> JwtTokenUtil
JwtAuthenticationFilter --> LoginUserDetailsService
LoginUserDetailsService --> UserServiceImpl
LoginUserDetails --> User
```

---

## 流程图

### 权限体系流程图（用户 → 角色 → 菜单 → perms）

```mermaid
flowchart TD

    %% ========== 登录与权限加载 ==========
    subgraph loginFlow[登录与权限加载]
        A[用户提交登录请求 /api/auth/login] --> B[LoginUserDetailsService 根据用户名查询用户]
        B --> C[UserMapper 查询用户信息]
        B --> D[UserMapper 查询用户角色列表]
        B --> E[UserMapper 查询用户权限标识列表]

        C --> F[构建 LoginUserDetails 包含用户 角色 权限]
        D --> F
        E --> F

        F --> G[AuthenticationManager 校验密码]
        G --> H[认证成功 生成 JWT Token]
        H --> I[返回 LoginResponseDTO 包含 token 菜单 权限]
    end

    %% ========== 每次请求的权限校验过程 ==========
    subgraph authFlow[请求鉴权与权限校验]
        J[前端携带 Token 访问接口 Authorization: Bearer xxx] --> K[JwtAuthenticationFilter 解析并校验 Token]
        K --> L[JwtTokenUtil validateToken 校验签名和过期时间]
        L --> M[JwtTokenUtil getUsername 解析用户名]
        M --> N[LoginUserDetailsService 再次根据用户名加载用户]
        N --> O[构建 LoginUserDetails 放入 SecurityContext]

        O --> P[Controller 方法执行 带有 PreAuthorize 注解]
        P --> Q[Spring Security 从 SecurityContext 读取权限]
        Q --> R[判断是否拥有所需角色或权限]
        R -->|通过| S[执行接口逻辑 返回业务数据]
        R -->|未通过| T[返回 403 无权限 由 RestAccessDeniedHandler 处理]
    end

    %% ========== RBAC 权限模型结构 ==========
    subgraph rbacModel[RBAC 权限模型]
        U[User 用户表 dynamicmenu_sys_user]
        V[UserRole 用户角色表 dynamicmenu_sys_user_role]
        W[Role 角色表 dynamicmenu_sys_role]
        X[RoleMenu 角色菜单表 dynamicmenu_sys_role_menu]
        Y[Menu 菜单表 dynamicmenu_sys_menu]

        U --> V
        V --> W
        W --> X
        X --> Y

        Y --> Z[perms 权限标识 例如 sys:user:list]
    end

    %% 登录完成结果关联到 RBAC 模型（虚线）
    I -.-> U
    %% 接口调用结果也与 RBAC 模型相关（虚线）
    S -.-> U
```

### JWT 鉴权流程图

#### 登录发 Token 流程

```mermaid
sequenceDiagram
    autonumber
    participant FE as 前端客户端
    participant AC as AuthController
    participant AM as AuthenticationManager
    participant UDS as LoginUserDetailsService
    participant UM as UserMapper
    participant JTU as JwtTokenUtil

    FE ->> AC: POST /api/auth/login<br/>携带用户名和密码
    AC ->> AM: 调用 authenticate(username, password)
    AM ->> UDS: loadUserByUsername(username)
    UDS ->> UM: selectByUsername(username)
    UM -->> UDS: 返回 User
    UDS -->> AM: 构建 LoginUserDetails（含用户 角色 权限）

    AM -->> AC: 认证成功 返回 Authentication

    AC ->> JTU: generateToken(username, userId 等 claims)
    JTU -->> AC: 返回 JWT 字符串

    AC -->> FE: 返回 LoginResponseDTO（token 菜单 权限等）
    note over FE: 前端保存 token（如 LocalStorage / Pinia / Vuex）
```

#### 携带 Token 访问受保护接口的鉴权流程

```mermaid
sequenceDiagram
    autonumber
    participant FE as 前端客户端
    participant FIL as JwtAuthenticationFilter
    participant JTU as JwtTokenUtil
    participant UDS as LoginUserDetailsService
    participant UM as UserMapper
    participant SEC as SecurityContext
    participant CTRL as Controller

    FE ->> FIL: HTTP 请求（Header: Authorization: Bearer token）

    FIL ->> JTU: validateToken(token)
    JTU -->> FIL: 返回校验结果（签名合法 未过期）

    FIL ->> JTU: getUsername(token)
    JTU -->> FIL: 返回 username

    FIL ->> UDS: loadUserByUsername(username)
    UDS ->> UM: 查询用户 基本信息/角色/权限
    UM -->> UDS: 返回 User 及关联数据
    UDS -->> FIL: 返回 LoginUserDetails（包含 authorities）

    FIL ->> SEC: 将 Authentication(LoginUserDetails) 写入 SecurityContext
    note over SEC: 本次请求线程上下文中<br/>已带当前登录用户及权限

    FIL -->> CTRL: 放行过滤链 进入 Controller

    CTRL ->> SEC: 由 @PreAuthorize 等从 SecurityContext 读取权限
    SEC -->> CTRL: 返回当前用户的 authorities

    alt 拥有所需权限
        CTRL -->> FE: 返回业务数据（200）
    else 无权限
        CTRL -->> FE: 返回 403（由 RestAccessDeniedHandler 处理）
    end
```
