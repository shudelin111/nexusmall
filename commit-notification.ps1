# Git 提交脚本 - 通知模块事件驱动架构
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "开始提交通知模块代码" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 切换到项目根目录
Set-Location "D:\IdeaProjects\nexusmall"

# 1. 创建并切换到新分支
Write-Host "`n[1/5] 创建并切换到 dev-0407-notification 分支..." -ForegroundColor Yellow
git checkout -b dev-0407-notification 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "分支已存在，切换到该分支..." -ForegroundColor Yellow
    git checkout dev-0407-notification
}

# 2. 添加通知模块的所有文件
Write-Host "`n[2/5] 添加通知模块文件..." -ForegroundColor Yellow
git add nexusmall-notification/src/main/java/com/nexusmall/notification/

# 3. 查看状态
Write-Host "`n[3/5] 检查 Git 状态..." -ForegroundColor Yellow
git status

# 4. 提交代码
Write-Host "`n[4/5] 提交代码..." -ForegroundColor Yellow
git commit -m "feat(notification): 实现通知模块事件驱动架构

- 新增领域事件层（Domain Events）
  * UserRegisteredEvent - 用户注册事件
  * CouponIssuedEvent - 优惠券发放事件
  * FlashSaleStartedEvent - 秒杀活动开始事件
  * OrderStatusChangedEvent - 订单状态变更事件

- 新增基础设施层事件监听器（Event Listeners）
  * UserRegisteredEventListener - 用户注册监听器
  * CouponIssuedEventListener - 优惠券发放监听器
  * FlashSaleStartedEventListener - 秒杀活动监听器
  * OrderStatusChangedEventListener - 订单状态变更监听器
  * 支持幂等性校验和事务管理
  * 符合 RocketMQ 消费者最佳实践

- 新增应用服务层（Application Services）
  * NotificationMessageService - 通知消息服务接口
  * NotificationMessageServiceImpl - 服务实现
  * 提供站内消息的查询和管理功能

- 新增接口层控制器（Controllers）
  * NotificationMessageController - RESTful API
  * 支持未读消息查询、标记已读、批量操作等

- 技术特性
  * 遵循 DDD + CQRS + Event Sourcing 架构模式
  * 实现 Saga Pattern 分布式事务最终一致性
  * 完整的幂等性处理框架
  * 详细的日志记录和异常处理
  * 编译验证通过

Author: shudl
Date: 2026-04-07"

# 5. 显示提交结果
Write-Host "`n[5/5] 提交完成！" -ForegroundColor Green
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "提交摘要：" -ForegroundColor Cyan
git log --oneline -1
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`n✅ 代码已成功提交到 dev-0407-notification 分支" -ForegroundColor Green
