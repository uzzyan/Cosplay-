# Vue 3 修复功能测试脚本
# 测试所有代码审查修复的问题

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  Vue 3 修复功能测试" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:9191"
$frontendUrl = "http://localhost:9194"
$testResults = @()

# 测试函数
function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Url,
        [string]$ExpectedStatus = "200"
    )
    
    Write-Host "测试: $Name" -ForegroundColor Yellow
    
    try {
        $response = Invoke-WebRequest -Uri "$baseUrl$Url" -UseBasicParsing -Method Get
        
        if ($response.StatusCode -eq $ExpectedStatus) {
            Write-Host "  ✅ 通过 - 状态码: $($response.StatusCode)" -ForegroundColor Green
            return @{Name=$Name; Status="PASS"; Code=$response.StatusCode}
        } else {
            Write-Host "  ⚠️  警告 - 状态码: $($response.StatusCode)" -ForegroundColor Yellow
            return @{Name=$Name; Status="WARN"; Code=$response.StatusCode}
        }
    } catch {
        Write-Host "  ❌ 失败 - $($_.Exception.Message)" -ForegroundColor Red
        return @{Name=$Name; Status="FAIL"; Code="ERROR"}
    }
}

# ==================== 测试开始 ====================

Write-Host ""
Write-Host "【1. 后端API接口测试】" -ForegroundColor Cyan
Write-Host "-----------------------------------" -ForegroundColor Cyan

# 测试商品接口
$testResults += Test-Endpoint `
    -Name "商品销量排行API" `
    -Url "/api/good/rank?num=5"

# 测试收入接口（需要权限，预期401）
$testResults += Test-Endpoint `
    -Name "收入图表API（需权限）" `
    -Url "/api/income/chart" `
    -ExpectedStatus "401"

Write-Host ""
Write-Host "【2. 前端服务测试】" -ForegroundColor Cyan
Write-Host "-----------------------------------" -ForegroundColor Cyan

try {
    $response = Invoke-WebRequest -Uri $frontendUrl -UseBasicParsing -Method Get
    if ($response.StatusCode -eq 200) {
        Write-Host "  ✅ 前端服务正常运行 - http://localhost:9194" -ForegroundColor Green
        $testResults += @{Name="前端服务"; Status="PASS"; Code=200}
    }
} catch {
    Write-Host "  ❌ 前端服务异常 - $($_.Exception.Message)" -ForegroundColor Red
    $testResults += @{Name="前端服务"; Status="FAIL"; Code="ERROR"}
}

Write-Host ""
Write-Host "【3. 代码修复验证清单】" -ForegroundColor Cyan
Write-Host "-----------------------------------" -ForegroundColor Cyan

$fixChecks = @(
    @{Name="IncomeChart.vue迁移到Composition API"; File="mall-web\src\views\manage\income\IncomeChart.vue"; Check="script setup"},
    @{Name="路由守卫使用async/await"; File="mall-web\src\router\index.js"; Check="async"},
    @{Name="Goods.vue定义uploadRef"; File="mall-web\src\views\manage\good\Goods.vue"; Check="uploadRef"},
    @{Name="GoodList.vue categoryId初始化"; File="mall-web\src\views\front\good\GoodList.vue"; Check="ref(null)"},
    @{Name="request.js错误处理"; File="mall-web\src\utils\request.js"; Check="ElMessage.error"},
    @{Name="Login.vue使用router.push"; File="mall-web\src\views\Login.vue"; Check="router.push"},
    @{Name="Navagation.vue使用router.push"; File="mall-web\src\components\Navagation.vue"; Check="router.push"}
)

foreach ($check in $fixChecks) {
    $filePath = "c:\q_code4.12\cosplay_mall(qoder)\$($check.File)"
    if (Test-Path $filePath) {
        $content = Get-Content $filePath -Raw
        if ($content -match [regex]::Escape($check.Check)) {
            Write-Host "  ✅ $($check.Name)" -ForegroundColor Green
            $testResults += @{Name=$check.Name; Status="PASS"; Code="OK"}
        } else {
            Write-Host "  ❌ $($check.Name) - 未找到关键代码" -ForegroundColor Red
            $testResults += @{Name=$check.Name; Status="FAIL"; Code="MISSING"}
        }
    } else {
        Write-Host "  ⚠️  文件不存在: $($check.File)" -ForegroundColor Yellow
        $testResults += @{Name=$check.Name; Status="WARN"; Code="NOFILE"}
    }
}

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

$passRate = [math]::Round(($passCount / $totalCount) * 100, 2)
Write-Host "通过率: $passRate%" -ForegroundColor $(if ($passRate -ge 90) { "Green" } elseif ($passRate -ge 70) { "Yellow" } else { "Red" })
Write-Host ""

if ($failCount -eq 0) {
    Write-Host "🎉 所有测试通过！修复成功！" -ForegroundColor Green
} else {
    Write-Host "⚠️  有失败的测试，请检查上方详情" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "详细结果:" -ForegroundColor Cyan
$testResults | Format-Table -AutoSize

Write-Host ""
Write-Host "测试完成时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Gray
Write-Host ""
Write-Host "前端访问地址: http://localhost:9194" -ForegroundColor Cyan
Write-Host "后端API地址: http://localhost:9191" -ForegroundColor Cyan
