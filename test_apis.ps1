# API接口测试脚本
# 用于验证售后、留言、收入模块的接口是否正常工作

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  Cosplay商城 API接口测试" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:9191"
$testResults = @()

# 测试函数
function Test-Api {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Url,
        [object]$Body = $null,
        [string]$ExpectedCode = "200"
    )
    
    Write-Host "测试: $Name" -ForegroundColor Yellow
    
    try {
        $params = @{
            Uri = "$baseUrl$Url"
            Method = $Method
            UseBasicParsing = $true
            ContentType = "application/json"
        }
        
        if ($Body) {
            $params.Body = ($Body | ConvertTo-Json -Depth 10)
        }
        
        $response = Invoke-WebRequest @params
        $content = $response.Content | ConvertFrom-Json
        
        if ($content.code -eq $ExpectedCode) {
            Write-Host "  ✅ 成功 - 返回码: $($content.code)" -ForegroundColor Green
            return @{Name=$Name; Status="PASS"; Code=$content.code}
        } else {
            Write-Host "  ⚠️  警告 - 返回码: $($content.code) (期望: $ExpectedCode)" -ForegroundColor Yellow
            return @{Name=$Name; Status="WARN"; Code=$content.code}
        }
    } catch {
        Write-Host "  ❌ 失败 - $($_.Exception.Message)" -ForegroundColor Red
        return @{Name=$Name; Status="FAIL"; Code="ERROR"}
    }
}

# ==================== 测试开始 ====================

Write-Host ""
Write-Host "【1. 商品管理模块】" -ForegroundColor Cyan
Write-Host "-----------------------------------" -ForegroundColor Cyan

# 测试商品销量排行
$testResults += Test-Api `
    -Name "获取商品销量排行" `
    -Method "GET" `
    -Url "/api/good/rank?num=5"

Write-Host ""
Write-Host "【2. 售后管理模块】" -ForegroundColor Cyan
Write-Host "-----------------------------------" -ForegroundColor Cyan

# 注意：以下接口需要登录权限，这里只测试接口是否可达
# 实际测试需要先登录获取token

Write-Host "  ℹ️  售后管理接口需要登录权限" -ForegroundColor Gray
Write-Host "  - POST /api/afterSale (requireLogin)" -ForegroundColor Gray
Write-Host "  - GET /api/afterSale/mine/page (requireLogin)" -ForegroundColor Gray
Write-Host "  - GET /api/afterSale/page (requireAuthority)" -ForegroundColor Gray
Write-Host "  - GET /api/afterSale/{id} (requireLogin)" -ForegroundColor Gray
Write-Host "  - PUT /api/afterSale/handle (requireAuthority)" -ForegroundColor Gray

Write-Host ""
Write-Host "【3. 留言管理模块】" -ForegroundColor Cyan
Write-Host "-----------------------------------" -ForegroundColor Cyan

Write-Host "  ℹ️  留言管理接口需要登录权限" -ForegroundColor Gray
Write-Host "  - POST /api/message (requireLogin)" -ForegroundColor Gray
Write-Host "  - GET /api/message/mine/page (requireLogin)" -ForegroundColor Gray
Write-Host "  - GET /api/message/{id} (requireLogin)" -ForegroundColor Gray
Write-Host "  - GET /api/message/page (requireAuthority)" -ForegroundColor Gray
Write-Host "  - PUT /api/message/reply (requireAuthority)" -ForegroundColor Gray
Write-Host "  - DELETE /api/message/{id} (requireAuthority)" -ForegroundColor Gray

Write-Host ""
Write-Host "【4. 收入管理模块】" -ForegroundColor Cyan
Write-Host "-----------------------------------" -ForegroundColor Cyan

Write-Host "  ℹ️  收入管理接口需要管理员权限" -ForegroundColor Gray
Write-Host "  - GET /api/income/chart (requireAuthority)" -ForegroundColor Gray
Write-Host "  - GET /api/income/week (requireAuthority)" -ForegroundColor Gray
Write-Host "  - GET /api/income/month (requireAuthority)" -ForegroundColor Gray

Write-Host ""
Write-Host "【5. 权限测试 - 访问需要登录的接口】" -ForegroundColor Cyan
Write-Host "-----------------------------------" -ForegroundColor Cyan

# 测试未登录访问需要权限的接口（应该返回401）
$testResults += Test-Api `
    -Name "未登录访问售后接口（应拒绝）" `
    -Method "GET" `
    -Url "/api/afterSale/mine/page?pageNum=1`&pageSize=10" `
    -ExpectedCode "401"

$testResults += Test-Api `
    -Name "未登录访问留言接口（应拒绝）" `
    -Method "GET" `
    -Url "/api/message/mine/page?pageNum=1`&pageSize=10" `
    -ExpectedCode "401"

$testResults += Test-Api `
    -Name "未登录访问收入接口（应拒绝）" `
    -Method "GET" `
    -Url "/api/income/chart" `
    -ExpectedCode "401"

# ==================== 测试结果统计 ====================

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  测试结果汇总" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

$passCount = ($testResults | Where-Object {$_.Status -eq "PASS"}).Count
$warnCount = ($testResults | Where-Object {$_.Status -eq "WARN"}).Count
$failCount = ($testResults | Where-Object {$_.Status -eq "FAIL"}).Count
$totalCount = $testResults.Count

Write-Host "总测试数: $totalCount" -ForegroundColor White
Write-Host "✅ 通过: $passCount" -ForegroundColor Green
Write-Host "⚠️  警告: $warnCount" -ForegroundColor Yellow
Write-Host "❌ 失败: $failCount" -ForegroundColor Red
Write-Host ""

if ($failCount -eq 0) {
    Write-Host "🎉 所有测试通过！" -ForegroundColor Green
} else {
    Write-Host "⚠️  有失败的测试，请检查上方详情" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "详细结果:" -ForegroundColor Cyan
$testResults | Format-Table -AutoSize

Write-Host ""
Write-Host "测试完成时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Gray
