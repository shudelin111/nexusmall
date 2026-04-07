# =====================================================
# Promotion 模块第一阶段功能测试
# 测试用户优惠券领取记录相关接口
# =====================================================

$baseUrl = "http://localhost:16000/api/v1"
$headers = @{
    "X-User-ID" = "1001"
    "Content-Type" = "application/json"
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Promotion 模块第一阶段功能测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 测试1: 查询可领取的优惠券列表
Write-Host "[测试1] 查询可领取的优惠券列表" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/coupons/" -Method Get -Headers $headers
    Write-Host "✓ 成功获取可领取优惠券列表" -ForegroundColor Green
    Write-Host "  优惠券数量: $($response.data.Count)" -ForegroundColor Gray
    if ($response.data.Count -gt 0) {
        $response.data | ForEach-Object {
            Write-Host "  - $($_.name) (ID: $($_.id))" -ForegroundColor Gray
        }
    }
} catch {
    Write-Host "✗ 失败: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 测试2: 领取优惠券（假设有ID为1的优惠券）
Write-Host "[测试2] 领取优惠券" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/coupons/1/receive" -Method Post -Headers $headers
    Write-Host "✓ 领取优惠券成功" -ForegroundColor Green
} catch {
    Write-Host "⚠ 领取失败（可能已达到上限或库存不足）: $($_.Exception.Message)" -ForegroundColor Yellow
}
Write-Host ""

# 测试3: 查询用户优惠券列表（全部）
Write-Host "[测试3] 查询用户优惠券列表（全部）" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/user-coupons/" -Method Get -Headers $headers
    Write-Host "✓ 成功获取用户优惠券列表" -ForegroundColor Green
    Write-Host "  优惠券数量: $($response.data.Count)" -ForegroundColor Gray
    if ($response.data.Count -gt 0) {
        $response.data | ForEach-Object {
            Write-Host "  - $($_.couponName) (状态: $($_.useStatusDesc), 可用: $($_.available))" -ForegroundColor Gray
        }
    }
} catch {
    Write-Host "✗ 失败: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 测试4: 查询用户未使用的优惠券
Write-Host "[测试4] 查询用户未使用的优惠券" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/user-coupons/?useStatus=0" -Method Get -Headers $headers
    Write-Host "✓ 成功获取未使用优惠券列表" -ForegroundColor Green
    Write-Host "  未使用优惠券数量: $($response.data.Count)" -ForegroundColor Gray
} catch {
    Write-Host "✗ 失败: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 测试5: 锁定优惠券（假设有记录ID为1的优惠券）
Write-Host "[测试5] 锁定优惠券" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/user-coupons/1/lock" -Method Post -Headers $headers
    Write-Host "✓ 锁定优惠券成功" -ForegroundColor Green
} catch {
    Write-Host "⚠ 锁定失败（可能记录不存在或状态不正确）: $($_.Exception.Message)" -ForegroundColor Yellow
}
Write-Host ""

# 测试6: 释放优惠券
Write-Host "[测试6] 释放优惠券" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/user-coupons/1/release" -Method Post -Headers $headers
    Write-Host "✓ 释放优惠券成功" -ForegroundColor Green
} catch {
    Write-Host "⚠ 释放失败: $($_.Exception.Message)" -ForegroundColor Yellow
}
Write-Host ""

# 测试7: 核销优惠券（模拟订单支付）
Write-Host "[测试7] 核销优惠券（模拟订单ID: 99999）" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/user-coupons/1/use?orderId=99999" -Method Post -Headers $headers
    Write-Host "✓ 核销优惠券成功" -ForegroundColor Green
} catch {
    Write-Host "⚠ 核销失败: $($_.Exception.Message)" -ForegroundColor Yellow
}
Write-Host ""

# 测试8: 退款回退优惠券
Write-Host "[测试8] 退款回退优惠券" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/user-coupons/1/refund" -Method Post -Headers $headers
    Write-Host "✓ 退款回退优惠券成功" -ForegroundColor Green
} catch {
    Write-Host "⚠ 回退失败: $($_.Exception.Message)" -ForegroundColor Yellow
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "测试完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
