package com.rabbiter.em.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbiter.em.annotation.Authority;
import com.rabbiter.em.constants.Constants;
import com.rabbiter.em.common.Result;
import com.rabbiter.em.entity.AuthorityType;
import com.rabbiter.em.entity.Order;
import com.rabbiter.em.entity.User;
import com.rabbiter.em.exception.ServiceException;
import com.rabbiter.em.service.OrderService;
import com.rabbiter.em.utils.TokenUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.util.StrUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 * @author uzzyan
 */

@Authority(AuthorityType.requireLogin)
@RestController
@RequestMapping("/api/order")
public class OrderController {
    @Resource
    private OrderService orderService;

    private boolean isAdmin() {
        User current = TokenUtils.getCurrentUser();
        return current != null && "admin".equals(current.getRole());
    }

    private void checkOrderOwnership(String orderNo) {
        if (isAdmin()) return;
        User current = TokenUtils.getCurrentUser();
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("order_no", orderNo);
        Order order = orderService.getOne(wrapper);
        if (order == null || order.getUserId() != current.getId()) {
            throw new ServiceException(Constants.CODE_403, "无权访问该订单");
        }
    }

    @GetMapping("/userid/{userid}")
    public Result selectByUserId(@PathVariable int userid) {
        User current = TokenUtils.getCurrentUser();
        if (current.getId() != userid && !isAdmin()) {
            throw new ServiceException(Constants.CODE_403, "无权访问他人订单");
        }
        return Result.success(orderService.selectByUserId(userid));
    }

    @GetMapping("/orderNo/{orderNo}")
    public Result selectByOrderNo(@PathVariable String orderNo) {
        checkOrderOwnership(orderNo);
        return Result.success(orderService.selectByOrderNo(orderNo));
    }

    @Authority(AuthorityType.requireAuthority)
    @GetMapping
    public Result findAll() {
        List<Order> list = orderService.list();
        return Result.success(list);
    }

    @Authority(AuthorityType.requireAuthority)
    @GetMapping("/page")
    public Result findPage(@RequestParam int pageNum,
                           @RequestParam int pageSize,
                           String orderNo,String state){
        IPage<Order> orderPage = new Page<>(pageNum,pageSize);
        QueryWrapper<Order> orderQueryWrapper = new QueryWrapper<>();
        orderQueryWrapper.ne("state","待付款");
        if(StrUtil.isNotEmpty(state)){
            orderQueryWrapper.eq("state",state);
        }
        if(StrUtil.isNotEmpty(orderNo)){
            orderQueryWrapper.like("order_no",orderNo);
        }

        orderQueryWrapper.orderByDesc("create_time");
        return Result.success(orderService.page(orderPage,orderQueryWrapper));
    }

    @Authority(AuthorityType.requireAuthority)
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) String orderNo,
                                         @RequestParam(required = false) String state) {
        List<Map<String, Object>> rows = orderService.export(orderNo, state);

        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("订单编号,用户名,商品名称,商品规格,订单金额,订单状态,创建时间\n");
        for (Map<String, Object> r : rows) {
            sb.append(csv(r.get("orderNo"))).append(',')
              .append(csv(r.get("username"))).append(',')
              .append(csv(r.get("goodName"))).append(',')
              .append(csv(r.get("standard"))).append(',')
              .append(csv(r.get("totalPrice"))).append(',')
              .append(csv(r.get("state"))).append(',')
              .append(csv(r.get("createTime"))).append('\n');
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        String fileName = "orders_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    private String csv(Object v) {
        String s = v == null ? "" : String.valueOf(v);
        boolean needQuote = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        if (needQuote) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }

    /**
     * 创建订单接口 (Create Order)
     * @param order 订单实体
     * @return Result 订单编号
     */
    @PostMapping
    public Result save(@RequestBody Order order) {
        String orderNo = orderService.saveOrder(order);
        return Result.success(orderNo);

    }

    /**
     * 支付订单接口 (Pay Order)
     * @param orderNo 订单编号
     * @return Result 操作结果
     */
    @PostMapping("/paid/{orderNo}")
    public Result payOrder(@PathVariable String orderNo){
        checkOrderOwnership(orderNo);
        orderService.payOrder(orderNo);
        return Result.success();
    }

    /**
     * 订单发货接口 (Delivery Order)
     * 需要管理员权限
     * @param orderNo 订单编号
     * @return Result 操作结果
     */
    @Authority(AuthorityType.requireAuthority)
    @PostMapping("/delivery/{orderNo}")
    public Result delivery(@PathVariable String orderNo){
        orderService.delivery(orderNo);
        return Result.success();
    }

    @PostMapping("/received/{orderNo}")
    public Result receiveOrder(@PathVariable String orderNo){
        checkOrderOwnership(orderNo);
        if(orderService.receiveOrder(orderNo)){
            return Result.success();
        }
        else {
            return Result.error(Constants.CODE_500,"确认收货失败");
        }
    }

    @Authority(AuthorityType.requireAuthority)
    @PutMapping
    public Result update(@RequestBody Order order) {
        orderService.updateById(order);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        if (!isAdmin()) {
            Order order = orderService.getById(id);
            if (order == null || order.getUserId() != TokenUtils.getCurrentUser().getId()) {
                throw new ServiceException(Constants.CODE_403, "无权删除该订单");
            }
        }
        orderService.removeById(id);
        return Result.success();
    }
}
