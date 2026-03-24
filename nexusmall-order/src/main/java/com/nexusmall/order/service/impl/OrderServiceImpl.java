package com.nexusmall.order.service.impl;

import com.nexusmall.common.vo.Result;
import com.nexusmall.order.dao.OrderItemMapper;
import com.nexusmall.order.dao.OrderMapper;
import com.nexusmall.order.entity.Order;
import com.nexusmall.order.entity.OrderItem;
import com.nexusmall.order.feign.ProductFeignService;
import com.nexusmall.order.service.OrderService;
import com.nexusmall.order.vo.OrderCreateRequest;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 订单服务实现类
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public Order getById(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    public Order getByOrderSn(String orderSn) {
        return orderMapper.selectByOrderSn(orderSn);
    }

    @Override
    public List<Order> list() {
        return orderMapper.list();
    }

    @Override
    public List<Order> listByMemberId(Long memberId) {
        return orderMapper.listByMemberId(memberId);
    }

    @Override
    public List<Order> listByStatus(Integer status) {
        return orderMapper.listByStatus(status);
    }

    @Override
    public List<Order> listByCondition(Long memberId, Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        return orderMapper.listByCondition(memberId, status, startTime, endTime);
    }

    @Override
    @GlobalTransactional(name = "create-order-tx", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(OrderCreateRequest request) {
        log.info("开始创建订单，请求参数：{}", request);

        // 1. 生成订单号
        String orderSn = generateOrderSn();

        // 2. 扣减库存（调用 Product 服务）
        Result<Boolean> stockResult = productFeignService.decreaseStock(
                request.getProductId(),
                request.getCount()
        );
        if (!stockResult.isSuccess() || !stockResult.getData()) {
            log.error("库存扣减失败：{}", stockResult.getMessage());
            throw new RuntimeException("库存不足");
        }

        // 3. 创建订单主表
        Order order = new Order();
        order.setOrderSn(orderSn);
        order.setMemberId(request.getMemberId());
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setReceiverAddress(request.getReceiverAddress());
        order.setTotalAmount(request.getTotalAmount());
        order.setPayAmount(request.getPayAmount());
        order.setFreightAmount(request.getFreightAmount());
        order.setPromotionAmount(request.getPromotionAmount());
        order.setStatus(0); // 待支付
        order.setPaymentType(request.getPaymentType());
        order.setRemark(request.getRemark());
        order.setVersion(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        int insertResult = orderMapper.insert(order);
        if (insertResult <= 0) {
            log.error("创建订单失败，插入主表数据为 0");
            throw new RuntimeException("创建订单失败");
        }

        // 4. 创建订单项
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(order.getId());
        orderItem.setOrderSn(orderSn);
        orderItem.setSkuId(request.getProductId());
        orderItem.setSkuName(request.getSkuName());
        orderItem.setSkuPrice(request.getPrice());
        orderItem.setQuantity(request.getCount());
        orderItem.setSubtotal(request.getPayAmount());
        orderItem.setCreateTime(LocalDateTime.now());

        int itemInsertResult = orderItemMapper.insert(orderItem);
        if (itemInsertResult <= 0) {
            log.error("创建订单失败，插入订单项数据为 0");
            throw new RuntimeException("创建订单失败");
        }

        // TODO: 测试分布式事务回滚 - 取消下面这行的注释来测试回滚功能
        // throw new RuntimeException("测试回滚 - 订单创建后会回滚");

        log.info("订单创建成功，订单号：{}", orderSn);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(Order order) {
        log.info("更新订单，orderId: {}", order.getId());
        order.setUpdateTime(LocalDateTime.now());
        return orderMapper.updateById(order) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        log.info("删除订单，orderId: {}", id);
        // 先删除订单项
        orderItemMapper.deleteByOrderId(id);
        // 再删除订单主表
        return orderMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelete(List<Long> ids) {
        log.info("批量删除订单，ids: {}", ids);
        // 批量删除订单项
        for (Long id : ids) {
            orderItemMapper.deleteByOrderId(id);
        }
        // 批量删除订单主表
        return orderMapper.batchDelete(ids) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(Long id, Integer paymentType) {
        log.info("支付订单，orderId: {}, paymentType: {}", id, paymentType);
        int result = orderMapper.payOrder(id, paymentType, LocalDateTime.now());
        if (result > 0) {
            log.info("订单支付成功，orderId: {}", id);
            return true;
        } else {
            log.error("订单支付失败，orderId: {}", id);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deliveryOrder(Long id) {
        log.info("发货，orderId: {}", id);
        int result = orderMapper.deliveryOrder(id, LocalDateTime.now());
        if (result > 0) {
            log.info("订单发货成功，orderId: {}", id);
            return true;
        } else {
            log.error("订单发货失败，orderId: {}", id);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean receiveOrder(Long id) {
        log.info("确认收货，orderId: {}", id);
        int result = orderMapper.receiveOrder(id, LocalDateTime.now());
        if (result > 0) {
            log.info("订单确认收货成功，orderId: {}", id);
            return true;
        } else {
            log.error("订单确认收货失败，orderId: {}", id);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long id) {
        log.info("取消订单，orderId: {}", id);
        int result = orderMapper.cancelOrder(id);
        if (result > 0) {
            log.info("订单取消成功，orderId: {}", id);
            return true;
        } else {
            log.error("订单取消失败，orderId: {}", id);
            return false;
        }
    }

    /**
     * 生成订单号
     */
    private String generateOrderSn() {
        return "ORD-" + LocalDateTime.now().toLocalDate().toString().replace("-", "")
                + "-" + String.format("%06d", (int) (Math.random() * 1000000));
    }
}
