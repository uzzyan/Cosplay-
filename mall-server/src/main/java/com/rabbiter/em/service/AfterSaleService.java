package com.rabbiter.em.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.em.constants.Constants;
import com.rabbiter.em.entity.AfterSale;
import com.rabbiter.em.entity.Order;
import com.rabbiter.em.exception.ServiceException;
import com.rabbiter.em.mapper.AfterSaleMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AfterSaleService extends ServiceImpl<AfterSaleMapper, AfterSale> {

    @Resource
    private OrderService orderService;

    public void create(AfterSale afterSale, Long userId) {
        if (afterSale == null) {
            throw new ServiceException(Constants.CODE_500, "参数错误");
        }
        if (StrUtil.isBlank(afterSale.getOrderNo())) {
            throw new ServiceException("400", "订单编号不能为空");
        }
        if (StrUtil.isBlank(afterSale.getType())) {
            throw new ServiceException("400", "申请类型不能为空");
        }
        if (!"退款".equals(afterSale.getType()) && !"售后".equals(afterSale.getType())) {
            throw new ServiceException("400", "申请类型不正确");
        }
        if (StrUtil.isBlank(afterSale.getReason())) {
            throw new ServiceException("400", "申请原因不能为空");
        }

        LambdaQueryWrapper<Order> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(Order::getOrderNo, afterSale.getOrderNo());
        orderQuery.eq(Order::getUserId, userId);
        Order order = orderService.getOne(orderQuery);
        if (order == null) {
            throw new ServiceException(Constants.NO_RESULT, "未找到订单");
        }

        LambdaQueryWrapper<AfterSale> existsQuery = new LambdaQueryWrapper<>();
        existsQuery.eq(AfterSale::getOrderNo, afterSale.getOrderNo());
        existsQuery.eq(AfterSale::getUserId, userId);
        existsQuery.eq(AfterSale::getStatus, "待处理");
        AfterSale exists = this.getOne(existsQuery);
        if (exists != null) {
            throw new ServiceException("400", "该订单已有待处理申请");
        }

        afterSale.setId(null);
        afterSale.setUserId(userId);
        afterSale.setStatus("待处理");
        afterSale.setHandleTime(null);
        afterSale.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        this.save(afterSale);
    }

    public IPage<AfterSale> pageMine(int pageNum, int pageSize, Long userId) {
        Page<AfterSale> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AfterSale> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AfterSale::getUserId, userId);
        wrapper.orderByDesc(AfterSale::getCreateTime);
        return this.page(page, wrapper);
    }

    public IPage<AfterSale> pageAdmin(int pageNum, int pageSize, String status, String type, String orderNo) {
        Page<AfterSale> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AfterSale> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(AfterSale::getStatus, status);
        }
        if (StrUtil.isNotBlank(type)) {
            wrapper.eq(AfterSale::getType, type);
        }
        if (StrUtil.isNotBlank(orderNo)) {
            wrapper.like(AfterSale::getOrderNo, orderNo);
        }
        wrapper.orderByDesc(AfterSale::getCreateTime);
        return this.page(page, wrapper);
    }

    public void handle(Long id, String status) {
        if (id == null) {
            throw new ServiceException("400", "参数错误");
        }
        if (!"待处理".equals(status) && !"已同意".equals(status) && !"已拒绝".equals(status)) {
            throw new ServiceException("400", "状态不正确");
        }
        AfterSale db = this.getById(id);
        if (db == null) {
            throw new ServiceException(Constants.NO_RESULT, "未找到申请");
        }
        db.setStatus(status);
        db.setHandleTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        this.updateById(db);
    }
}
