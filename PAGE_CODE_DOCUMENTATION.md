# 页面关键代码文档

## 1. 登录页面 (Login.vue)
实现用户登录逻辑，包含表单验证和Token存储。

```javascript
// 登录请求
login() {
  this.$refs['userForm'].validate((valid) => {
    if (valid) {
      this.request.post("/login", this.user).then(res => {
        if (res.code === '200') {
          localStorage.setItem("user", JSON.stringify(res.data)); // 存储用户信息
          this.$router.push("/");
          this.$message.success("登录成功");
        } else {
          this.$message.error(res.msg);
        }
      })
    }
  });
}
```

## 2. 商品详情页 (GoodView.vue)
展示商品信息、规格选择、购买与加入购物车入口。

```javascript
// 购买逻辑
goToOrder() {
  this.$router.push({
    name: "preOrder",
    query: {
      good: JSON.stringify(this.good),
      realPrice: this.realPrice,
      num: this.count,
      standard: this.checkedStandard,
    },
  });
},
```

## 3. 确认订单页 (PreOrder.vue)
处理订单提交并确认收货地址。

```javascript
// 提交订单
submitOrder() {
    let totalPrice = this.sumPrice;
    API.post("/api/order", {
        totalPrice: totalPrice,
        linkUser: address.linkUser,
        linkPhone: address.linkPhone,
        linkAddress: address.linkAddress,
        state: "待付款",
        goods: JSON.stringify(this.goods),
        cartId: this.cartId
    }).then((res) => {
        // 跳转支付...
    });
}
```

## 4. 地址管理页 (AddressManage.vue)
地址的增删改查及校验。

```javascript
// 手机号校验与保存
saveAddress() {
  if (!/^\d{11}$/.test(this.address.linkPhone)) {
    this.$message.warning("联系电话必须为11位数字");
    return;
  }
  // ...
  API.post("/api/address", this.address).then((res) => {
    // ...
  });
}
```

## 5. 后台商品管理 (Goods.vue)
管理员对商品的CRUD操作。

```javascript
// 保存商品
save() {
    this.request.post("/api/good", this.form).then(res => {
        if (res.code === '200') {
            this.$message.success("保存成功");
            this.load();
            this.dialogFormVisible = false;
        } else {
            this.$message.error("保存失败");
        }
    })
}
```
