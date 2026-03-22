package com.nexusmall.order.service.impl;

import com.nexusmall.order.entity.Order;
import com.nexusmall.order.entity.OrderItem;
import com.nexusmall.order.exception.OrderNotFoundException;
import com.nexusmall.order.service.OrderService;
import com.nexusmall.order.vo.CreateOrderItemRequest;
import com.nexusmall.order.vo.CreateOrderRequest;
import com.nexusmall.order.vo.UpdateOrderStatusRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final DateTimeFormatter ORDER_SN_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final AtomicLong sequenceGenerator = new AtomicLong(1);
    private final Map<String, Order> orderStore = new ConcurrentHashMap<String, Order>();

    @Override
    public Order createOrder(CreateOrderRequest request) {
        List<OrderItem> items = buildOrderItems(request.getItems());
        BigDecimal totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        LocalDateTime now = LocalDateTime.now();
        Order order = Order.builder()
                .id(idGenerator.getAndIncrement())
                .orderSn(generateOrderSn(now))
                .memberId(request.getMemberId())
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .receiverAddress(request.getReceiverAddress())
                .totalAmount(totalAmount)
                .status("CREATED")
                .createTime(now)
                .updateTime(now)
                .items(items)
                .build();
        orderStore.put(order.getOrderSn(), order);
        return order;
    }

    @Override
    public Order getByOrderSn(String orderSn) {
        Order order = orderStore.get(orderSn);
        if (order == null) {
            throw new OrderNotFoundException(orderSn);
        }
        return order;
    }

    @Override
    public List<Order> listOrders(Long memberId) {
        return orderStore.values().stream()
                .filter(order -> memberId == null || memberId.equals(order.getMemberId()))
                .sorted(Comparator.comparing(Order::getCreateTime).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Order updateStatus(String orderSn, UpdateOrderStatusRequest request) {
        Order order = getByOrderSn(orderSn);
        order.setStatus(request.getStatus());
        order.setUpdateTime(LocalDateTime.now());
        return order;
    }

    private List<OrderItem> buildOrderItems(List<CreateOrderItemRequest> itemRequests) {
        List<OrderItem> items = new ArrayList<OrderItem>();
        for (CreateOrderItemRequest itemRequest : itemRequests) {
            BigDecimal subtotal = itemRequest.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            items.add(OrderItem.builder()
                    .skuId(itemRequest.getSkuId())
                    .skuName(itemRequest.getSkuName())
                    .quantity(itemRequest.getQuantity())
                    .price(itemRequest.getPrice())
                    .subtotal(subtotal)
                    .build());
        }
        return items;
    }

    private String generateOrderSn(LocalDateTime now) {
        return "ORD" + ORDER_SN_FORMATTER.format(now) + String.format("%04d", sequenceGenerator.getAndIncrement());
    }
}
