package com.nexusmall.order.service.impl;

import com.nexusmall.common.constant.MQConstants;
import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.enums.UserBehaviorType;
import com.nexusmall.common.exception.OrderException;
import com.nexusmall.common.vo.Result;
import com.nexusmall.common.vo.UserBehaviorVO;
import com.nexusmall.order.dao.OrderItemMapper;
import com.nexusmall.order.dao.OrderMapper;
import com.nexusmall.order.entity.Order;
import com.nexusmall.order.entity.OrderItem;
import com.nexusmall.order.feign.ProductFeignService;
import com.nexusmall.order.service.OrderService;
import com.nexusmall.order.service.RocketMQProducer;
import com.nexusmall.order.vo.OrderCreateRequest;
import com.nexusmall.order.vo.OrderQueryRequest;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private RocketMQProducer rocketMQProducer;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public Order getById(Long id) {
        log.debug("根据 ID 查询订单，orderId: {}", id);
        Order order = orderMapper.selectById(id);
        if (order != null) {
            log.info("订单查询成功，orderId: {}, orderSn: {}", id, order.getOrderSn());
        } else {
            log.warn("订单不存在，orderId: {}", id);
        }
        return order;
    }

    @Override
    public Order getByOrderSn(String orderSn) {
        log.debug("根据订单号查询订单，orderSn: {}", orderSn);
        Order order = orderMapper.selectByOrderSn(orderSn);
        if (order != null) {
            log.info("订单查询成功，orderSn: {}, orderId: {}", orderSn, order.getId());
        } else {
            log.warn("订单不存在，orderSn: {}", orderSn);
        }
        return order;
    }

    @Override
    public List<Order> list() {
        log.debug("查询所有订单");
        List<Order> orders = orderMapper.list();
        log.info("查询到{}条订单", orders.size());
        return orders;
    }

    @Override
    public List<Order> listByMemberId(Long memberId) {
        log.debug("根据用户 ID 查询订单，memberId: {}", memberId);
        List<Order> orders = orderMapper.listByMemberId(memberId);
        log.info("用户 {} 查询到{}条订单", memberId, orders.size());
        return orders;
    }

    @Override
    public List<Order> listByStatus(Integer status) {
        log.debug("根据状态查询订单，status: {}", status);
        List<Order> orders = orderMapper.listByStatus(status);
        log.info("状态 {} 查询到{}条订单", status, orders.size());
        return orders;
    }

    @Override
    public List<Order> listByCondition(OrderQueryRequest request) {
        log.debug("条件查询订单，memberId: {}, status: {}, startTime: {}, endTime: {}", 
                request.getMemberId(), request.getStatus(), request.getStartTime(), request.getEndTime());
        List<Order> orders = orderMapper.listByCondition(
                request.getMemberId(), 
                request.getStatus(), 
                request.getStartTime(), 
                request.getEndTime());
        log.info("条件查询到{}条订单", orders.size());
        return orders;
    }

    @Override
    @GlobalTransactional(name = "nexusmall-create-order-tx", rollbackFor = Exception.class)
    public Order createOrder(OrderCreateRequest request) {
        log.info("开始创建订单，请求参数：{}", request);

        // 打印当前事务上下文中的 XID
        String xid = RootContext.getXID();
        log.info("====== Order 服务 createOrder 方法中的 XID: {} ======", xid);

        // 1. 生成订单号
        String orderSn = generateOrderSn();

        // 2. 扣减库存（调用 Product 服务）
        log.info("====== 准备调用 Product 服务 Feign 接口，当前 XID: {} ======", xid);
        Result<Boolean> stockResult = productFeignService.decreaseStock(request.getProductId(), request.getCount());
        if (!stockResult.isSuccess() || !stockResult.getData()) {
            log.error("库存扣减失败：{}", stockResult.getMessage());
            throw new OrderException(CommonResultCode.INSUFFICIENT_STOCK.getCode(), CommonResultCode.INSUFFICIENT_STOCK.getMessage());
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
            throw new OrderException(CommonResultCode.ORDER_CREATE_FAILED.getCode(), CommonResultCode.ORDER_CREATE_FAILED.getMessage());
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
            throw new OrderException(CommonResultCode.ORDER_CREATE_FAILED.getCode(), CommonResultCode.ORDER_CREATE_FAILED.getMessage());
        }

        log.info("订单创建成功，订单号：{}", orderSn);
        
        // 发送用户下单行为到 RocketMQ
        try {
            UserBehaviorVO behaviorVO = UserBehaviorVO.builder()
                .userId(request.getMemberId())
                .behaviorType(UserBehaviorType.PLACE_ORDER.getCode())
                .objectId(order.getId())
                .objectType("order")
                .occurTime(LocalDateTime.now())
                .build();
            
            Message<UserBehaviorVO> message = MessageBuilder.withPayload(behaviorVO).build();
            rocketMQTemplate.send(MQConstants.UserBehavior.TOPIC + ":" + MQConstants.UserBehavior.TAG, message);
            log.info("【发送下单行为】userId: {}, orderId: {}", request.getMemberId(), order.getId());
        } catch (Exception e) {
            log.error("【发送下单行为失败】userId: {}, orderId: {}", request.getMemberId(), order.getId(), e);
        }
        
        // 5. 发送延迟消息（30 分钟后检查支付状态）
        rocketMQProducer.sendOrderCancelDelayMessage(order.getId(), MQConstants.Order.DELAY_LEVEL_30MIN);
        
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelUnpaidOrder(Long orderId) {
        log.info("检查并取消未支付订单，orderId: {}", orderId);
        
        // 查询订单
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.warn("订单不存在，orderId: {}", orderId);
            return;
        }
        
        // 检查订单状态：0-待支付
        if (order.getStatus() == 0) {
            log.info("订单未支付，执行取消操作，orderId: {}", orderId);
            // 取消订单
            int result = orderMapper.cancelOrder(orderId);
            if (result > 0) {
                log.info("订单已取消，orderId: {}", orderId);
                
                // TODO: 这里可以调用 Product 服务恢复库存
                // productFeignService.increaseStock(...);
            } else {
                log.error("订单取消失败，orderId: {}", orderId);
            }
        } else {
            log.info("订单已支付，无需取消，orderId: {}, status: {}", orderId, order.getStatus());
        }
    }

    /**
     * 生成订单号
     */
    private String generateOrderSn() {
        return "ORD-" + LocalDateTime.now().toLocalDate().toString().replace("-", "") + "-" +
                String.format("%06d", (int) (Math.random() * 1000000));
    }
}
