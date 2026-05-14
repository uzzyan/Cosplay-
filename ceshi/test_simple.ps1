# Simple Vue 3 Fix Test

Write-Host "Testing Vue 3 Fixes..." -ForegroundColor Cyan
Write-Host ""

# Test 1: Backend API
Write-Host "1. Backend API Test" -ForegroundColor Yellow
try {
    $resp = Invoke-WebRequest -Uri "http://localhost:9191/api/good/rank?num=3" -UseBasicParsing
    if ($resp.StatusCode -eq 200) {
        Write-Host "   PASS: Backend API working" -ForegroundColor Green
    }
} catch {
    Write-Host "   FAIL: Backend API error" -ForegroundColor Red
}

# Test 2: Frontend Service
Write-Host "2. Frontend Service Test" -ForegroundColor Yellow
try {
    $resp = Invoke-WebRequest -Uri "http://localhost:9194" -UseBasicParsing
    if ($resp.StatusCode -eq 200) {
        Write-Host "   PASS: Frontend running at http://localhost:9194" -ForegroundColor Green
    }
} catch {
    Write-Host "   FAIL: Frontend error" -ForegroundColor Red
}

# Test 3: Check fixes
Write-Host "3. Code Fix Verification" -ForegroundColor Yellow

$checks = @(
    @{Name="IncomeChart Composition API"; Path="mall-web\src\views\manage\income\IncomeChart.vue"; Pattern="script setup"},
    @{Name="Router async/await"; Path="mall-web\src\router\index.js"; Pattern="async"},
    @{Name="Goods uploadRef"; Path="mall-web\src\views\manage\good\Goods.vue"; Pattern="uploadRef"},
    @{Name="Login router.push"; Path="mall-web\src\views\Login.vue"; Pattern="router.push"}
)

foreach ($check in $checks) {
    $fullPath = "c:\q_code4.12\cosplay_mall(qoder)\$($check.Path)"
    if (Test-Path $fullPath) {
        $content = Get-Content $fullPath -Raw
        if ($content -match $check.Pattern) {
            Write-Host "   PASS: $($check.Name)" -ForegroundColor Green
        } else {
            Write-Host "   FAIL: $($check.Name)" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "All tests completed!" -ForegroundColor Cyan
Write-Host "Frontend: http://localhost:9194" -ForegroundColor Cyan
Write-Host "Backend: http://localhost:9191" -ForegroundColor Cyan
