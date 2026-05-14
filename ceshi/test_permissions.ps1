# Cosplay商城权限测试脚本
# 作者: uzzyan
# 日期: 2026-05-11

$ErrorActionPreference = "Stop"
$baseUrl = "http://localhost:9191"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Cosplay商城权限测试脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 测试计数器
$passed = 0
$failed = 0

function Test-Result {
    param(
        [string]$TestCase,
        [object]$Response,
        [string]$ExpectedCode,
        [string]$ExpectedMsg
    )
    
    try {
        $content = $Response.Content | ConvertFrom-Json
        $statusCode = $Response.StatusCode
        
        if ($content.code -eq $ExpectedCode) {
            Write-Host "✅ PASS: $TestCase" -ForegroundColor Green
            Write-Host "   返回: $($content.code) - $($content.msg)" -ForegroundColor Gray
            $script:passed++
        } else {
            Write-Host "❌ FAIL: $TestCase" -ForegroundColor Red
            Write-Host "   期望: $ExpectedCode - $ExpectedMsg" -ForegroundColor Yellow
            Write-Host "   实际: $($content.code) - $($content.msg)" -ForegroundColor Yellow
            $script:failed++
        }
    } catch {
        Write-Host "❌ ERROR: $TestCase" -ForegroundColor Red
        Write-Host "   错误: $_" -ForegroundColor Yellow
        $script:failed++
    }
    Write-Host ""
}

# ========================================
# TC-020: 未登录状态访问管理接口
# ========================================
Write-Host "--- TC-020: 未登录状态访问管理接口 ---" -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/user/page?pageNum=1&pageSize=10" -UseBasicParsing -ErrorAction Stop
    Test-Result "TC-020" $response "401" "token失效,请重新登陆"
} catch {
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-Host "✅ PASS: TC-020 - 返回 401 状态码" -ForegroundColor Green
        $passed++
    } else {
        Write-Host "❌ FAIL: TC-020 - 期望401,实际: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
        $failed++
    }
}
Write-Host ""

# ========================================
# 登录获取普通用户token
# ========================================
Write-Host "--- 准备: 普通用户登录 ---" -ForegroundColor Cyan
try {
    $loginBody = @{
        username = "testuser"
        password = (echo -n "123456" | md5sum).Split(' ')[0]
    } | ConvertTo-Json
    
    $loginResponse = Invoke-WebRequest -Uri "$baseUrl/login" -Method POST -ContentType "application/json" -Body $loginBody -UseBasicParsing
    $loginData = $loginResponse.Content | ConvertFrom-Json
    
    if ($loginData.code -eq '200') {
        $userToken = $loginData.data.token
        $userId = $loginData.data.id
        Write-Host "✅ 普通用户登录成功, userId=$userId" -ForegroundColor Green
    } else {
        Write-Host "❌ 登录失败,请先注册用户: testuser / 123456" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host " 登录失败: $_" -ForegroundColor Red
    Write-Host "提示: 请先注册用户 testuser,密码 123456" -ForegroundColor Yellow
    exit 1
}
Write-Host ""

# ========================================
# TC-021: 普通用户访问管理接口
# ========================================
Write-Host "--- TC-021: 普通用户访问/user/page管理接口 ---" -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/user/page?pageNum=1&pageSize=10" -Headers @{token=$userToken} -UseBasicParsing
    Test-Result "TC-021" $response "403" "无权限！"
} catch {
    if ($_.Exception.Response.StatusCode -eq 403) {
        Write-Host "✅ PASS: TC-021 - 返回 403 状态码" -ForegroundColor Green
        $passed++
    } else {
        Write-Host "❌ FAIL: TC-021 - 期望403,实际: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
        $failed++
    }
}
Write-Host ""

# ========================================
# TC-022: 访问他人购物车
# ========================================
Write-Host "--- TC-022: 普通用户尝试访问他人购物车 ---" -ForegroundColor Cyan
try {
    $otherUserId = if ($userId -eq 1) { 2 } else { 1 }
    $response = Invoke-WebRequest -Uri "$baseUrl/api/cart/userid/$otherUserId" -Headers @{token=$userToken} -UseBasicParsing
    Test-Result "TC-022" $response "403" "无权访问他人购物车"
} catch {
    if ($_.Exception.Response.StatusCode -eq 403) {
        Write-Host "✅ PASS: TC-022 - 返回 403 状态码" -ForegroundColor Green
        $passed++
    } else {
        Write-Host "❌ FAIL: TC-022 - 期望403,实际: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
        $failed++
    }
}
Write-Host ""

# ========================================
# TC-023: 访问他人订单
# ========================================
Write-Host "--- TC-023: 普通用户尝试访问他人订单 ---" -ForegroundColor Cyan
try {
    $otherUserId = if ($userId -eq 1) { 2 } else { 1 }
    $response = Invoke-WebRequest -Uri "$baseUrl/api/order/userid/$otherUserId" -Headers @{token=$userToken} -UseBasicParsing
    Test-Result "TC-023" $response "403" "无权访问他人订单"
} catch {
    if ($_.Exception.Response.StatusCode -eq 403) {
        Write-Host "✅ PASS: TC-023 - 返回 403 状态码" -ForegroundColor Green
        $passed++
    } else {
        Write-Host "❌ FAIL: TC-023 - 期望403,实际: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
        $failed++
    }
}
Write-Host ""

# ========================================
# 测试总结
# ========================================
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   测试完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ 通过: $passed" -ForegroundColor Green
Write-Host "❌ 失败: $failed" -ForegroundColor Red
Write-Host "📊 总计: $($passed + $failed)" -ForegroundColor Cyan
Write-Host ""

if ($failed -eq 0) {
    Write-Host "🎉 所有测试通过！" -ForegroundColor Green
} else {
    Write-Host "️  有测试失败,请检查" -ForegroundColor Yellow
}
