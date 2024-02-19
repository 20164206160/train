# 12306 模拟火车购票系统

## 前台页面
![image](https://github.com/20164206160/train/assets/26060329/e18c457d-1d31-4858-988a-266d0c57d861)
![image](https://github.com/20164206160/train/assets/26060329/4588c47f-8728-4398-9c4e-848b3a368dcd)

## 后台页面
![image](https://github.com/20164206160/train/assets/26060329/5b989606-28a2-4726-96fb-31680dbc21d1)

## 高并发技术：
![image](https://github.com/20164206160/train/assets/26060329/7a37ad07-6e57-4b29-b9ce-a5711b46ec70)

## 功能难点： *抢票和不能超卖*

## 模型设计和逻辑实现
+ 余票查询：记录站站余票
一列火车5个站，可以拆分成4+3+2+1=10条站站记录
+ 座位购买：记录座位销售详情
一列火车有5个站A~E，1号座位：0111，表示只剩A~B可买

## 核心功能
![image](https://github.com/20164206160/train/assets/26060329/e4f3a426-7f08-4f7c-8e0a-9485ee2d8556)

## 功能模块划分
+ 网关模块：路由转发、登录校验
+ 会员模块：会员、乘客、已购车票
+ 业务模块：所有车次数据、余票信息
+ 跑批模块：管理定时任务，界面启停
+ web模块：会员相关界面
+ admin模块：管理员相关界面
  
## 整体系统架构图
![image](https://github.com/20164206160/train/assets/26060329/743a0eb7-cf48-4a16-a6c3-ba23679c04ea)

## 数据库设计
+ ![image](https://github.com/20164206160/train/assets/26060329/d429d4cc-c17d-4f14-8dbc-9bd0f571373e)
+ ![image](https://github.com/20164206160/train/assets/26060329/0464c73c-450b-4ccd-82fc-9b934bc1977d)
+ ![image](https://github.com/20164206160/train/assets/26060329/5221286b-6cda-4617-93a0-60708077b097)
