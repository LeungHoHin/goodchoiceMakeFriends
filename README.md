# 良好选 - 交友组队系统

前端源码地址：<https://github.com/LeungHoHin/goodchoiceoj-frontend>

基于 Vue 3 + Spring Boot 2 的移动端网站，实现了用户管理、按标签检索用户、推荐相似用户、组队等功能。

## 后端
1. 用户登录：使用**Redis**实现分布式 Session，解决集群间登录态同步问题；并使用 Hash 代替 String 来存储用户信息，节约了内存并便于单字段的修改。
2. 使用 Redis 缓存首页高频访问的用户信息列表，将接口响应时长从平均5秒缩短至平均0.6秒。且通过自定义 Redis 序列化器来解决数据乱码、空间浪费的问题。
3. 为解决首次访问系统的用户主页加载过慢的问题，使用 Spring Scheduler 定时任务来实现缓存预热，并通过分布式锁保证多机部署时定时任务不会重复执行。
4. 为解决同一用户重复加入队伍、入队人数超限的问题，使用**Redisson**分布式锁来实现操作互斥，保证了接口幂等性。
5. 使用**编辑距离算法**实现了根据标签匹配最相似用户的功能，并通过优先队列来减少 TOP N 运算过程中的内存占用。
6. 使用 Knife4j + Swagger 自动生成后端接口文档，避免了人工编写维护文档的麻烦。

## 前端
1. 前端使用 Vant UI 组件库，并封装了全局通用的 Layout 组件，使主页、搜索页、组队页布局一致、并减少重复代码。
2. 基于 Vue Router 全局路由守卫实现了根据不同页面来动态切换导航栏标题， 并通过在全局路由配置文件扩展 title 字段来减少无意义的 if else 代码。


## 功能说明

### 主页
进去之后，会被要求先登录

![image](https://github.com/LeungHoHin/goodchoiceMakeFriends/assets/114863160/840b3f2d-3efc-4c46-97c6-5f41cbb0ae84)

登陆后可以看见如下主页的页面

![image](https://github.com/LeungHoHin/goodchoiceMakeFriends/assets/114863160/65c22987-42af-492e-b9fe-b33539525753)

可以选择普通模式或者匹配模式查看主页列举出来的用户。如果选择的是匹配模式，那么系统会根据**编辑距离算法**自动筛选出和当前用户标签相匹配的用户。

![image](https://github.com/LeungHoHin/goodchoiceMakeFriends/assets/114863160/2eac9e20-2273-4912-b5aa-64b05cc36494)

点击查看详细可以看见该用户的详细信息，方便当前用户进行联系

![image](https://github.com/LeungHoHin/goodchoiceMakeFriends/assets/114863160/e8574bfe-23f2-4604-af43-6d40889a62a0)


### 队伍页面

队伍页面显示如下，这里会显示已经创建的队伍

![image](https://github.com/LeungHoHin/goodchoiceMakeFriends/assets/114863160/2ff04f70-51f3-43f2-8190-14c549557f09)

点击右下角的加号，可以创建队伍。

![image](https://github.com/LeungHoHin/goodchoiceMakeFriends/assets/114863160/5da0ce1e-258b-4f54-9148-202369ab173d)

依次输入队伍信息，然后选择队伍过期的时间

![image](https://github.com/LeungHoHin/goodchoiceMakeFriends/assets/114863160/3f2ebd86-4c23-4c0d-b735-24e6b22aba5f)

队伍有三种状态，分别是公开，加密以及私有。公开状态任何人都可以加入，加密状态则需要设置密码，那么用户需要输入密码才能加入队伍，私有队伍其他用户不能加入

![image](https://github.com/LeungHoHin/goodchoiceMakeFriends/assets/114863160/62829830-0995-4e0a-a654-5f3d52f31239)


### 个人页面

个人页面会显示个人的详细信息

![image](https://github.com/LeungHoHin/goodchoiceMakeFriends/assets/114863160/5e8344fb-a4c8-45a5-8968-14c82ab8dea6)

点击某个信息可以进行更改

![image](https://github.com/LeungHoHin/goodchoiceMakeFriends/assets/114863160/0d1a8d55-e9e9-4373-a8c3-dc2bee271e81)

同时可以点击我的队伍来查看用户已经加入的队伍以及用户创建的队伍

![image](https://github.com/LeungHoHin/goodchoiceMakeFriends/assets/114863160/997fbc5b-5336-4ec1-80e2-8b758b0dc8aa)

### 搜索页面

点击右上角的放大镜按钮可以进入搜索界面

可以选择标签来搜索有相关标签的用户

![image](https://github.com/LeungHoHin/goodchoiceMakeFriends/assets/114863160/87de4012-e88a-4cf6-ad95-e56989e10f99)
