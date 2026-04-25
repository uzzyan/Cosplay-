# 管理员模块测试脚本

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   管理员后台管理模块全面测试" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# 1. Admin登录
Write-Host "【步骤1】Admin登录..." -ForegroundColor Yellow
$adminLogin = Invoke-RestMethod -Uri "http://localhost:9191/login" -Method POST -ContentType "application/json" -Body '{"username":"admin","password":"e99a18c428cb38d5f260853678922e03"}'
if ($adminLogin.code -eq "200") {
    Write-Host "✅ 登录成功" -ForegroundColor Green
    Write-Host "   用户名: $($adminLogin.data.username)" -ForegroundColor Green
    Write-Host "   昵称: $($adminLogin.data.nickname)" -ForegroundColor Green  
    Write-Host "   角色: $($adminLogin.data.role)" -ForegroundColor Green
    $token = $adminLogin.data.token
    $h = @{"token"=$token; "Content-Type"="application/json"}
} else {
    Write-Host "❌ 登录失败: $($adminLogin.msg)" -ForegroundColor Red
    exit
}

# 2. 测试用户管理
Write-Host "`n【步骤2】测试用户管理API..." -ForegroundColor Yellow
try {
    $users = Invoke-RestMethod -Uri "http://localhost:9191/user/page?pageNum=1`&pageSize=10" -Headers $h
    Write-Host "✅ 用户管理API正常" -ForegroundColor Green
    Write-Host "   用户总数: $($users.data.total)" -ForegroundColor Green
} catch {
    Write-Host "❌ 用户管理API失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 3. 测试商品管理
Write-Host "`n【步骤3】测试商品管理API..." -ForegroundColor Yellow
try {
    $goods = Invoke-RestMethod -Uri "http://localhost:9191/api/good/page?pageNum=1`&pageSize=10" -Headers $h
    Write-Host "✅ 商品管理API正常" -ForegroundColor Green
    Write-Host "   商品总数: $($goods.data.total)" -ForegroundColor Green
} catch {
    Write-Host "❌ 商品管理API失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 4. 测试分类管理
Write-Host "`n【步骤4】测试分类管理API..." -ForegroundColor Yellow
try {
    $categories = Invoke-RestMethod -Uri "http://localhost:9191/api/category" -Headers $h
    Write-Host "✅ 分类管理API正常" -ForegroundColor Green
    Write-Host "   分类数量: $($categories.data.Count)" -ForegroundColor Green
} catch {
    Write-Host "❌ 分类管理API失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 5. 测试轮播图管理
Write-Host "`n【步骤5】测试轮播图管理API..." -ForegroundColor Yellow
try {
    $carousels = Invoke-RestMethod -Uri "http://localhost:9191/api/carousel" -Headers $h
    Write-Host "✅ 轮播图管理API正常" -ForegroundColor Green
    Write-Host "   轮播图数量: $($carousels.data.Count)" -ForegroundColor Green
} catch {
    Write-Host "❌ 轮播图管理API失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 6. 测试订单管理
Write-Host "`n【步骤6】测试订单管理API..." -ForegroundColor Yellow
try {
    $orders = Invoke-RestMethod -Uri "http://localhost:9191/api/order/page?pageNum=1`&pageSize=10" -Headers $h
    Write-Host "✅ 订单管理API正常" -ForegroundColor Green
    Write-Host "   订单总数: $($orders.data.total)" -ForegroundColor Green
} catch {
    Write-Host "❌ 订单管理API失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 7. 测试留言管理
Write-Host "`n【步骤7】测试留言管理API..." -ForegroundColor Yellow
try {
    $messages = Invoke-RestMethod -Uri "http://localhost:9191/api/message" -Headers $h
    Write-Host "✅ 留言管理API正常" -ForegroundColor Green
    Write-Host "   留言数量: $($messages.data.Count)" -ForegroundColor Green
} catch {
    Write-Host "❌ 留言管理API失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 8. 测试售后管理
Write-Host "`n【步骤8】测试售后管理API..." -ForegroundColor Yellow
try {
    $afterSales = Invoke-RestMethod -Uri "http://localhost:9191/api/afterSale" -Headers $h
    Write-Host "✅ 售后管理API正常" -ForegroundColor Green
    Write-Host "   售后申请数量: $($afterSales.data.Count)" -ForegroundColor Green
} catch {
    Write-Host "❌ 售后管理API失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   测试完成！" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "请在浏览器中打开以下URL进行可视化测试：" -ForegroundColor Yellow
Write-Host "  后台管理首页: http://localhost:9192/manage" -ForegroundColor White
Write-Host "  用户管理: http://localhost:9192/manage/user" -ForegroundColor White
Write-Host "  商品管理: http://localhost:9192/manage/good" -ForegroundColor White
Write-Host "  订单管理: http://localhost:9192/manage/order" -ForegroundColor White
Write-Host "`n"
