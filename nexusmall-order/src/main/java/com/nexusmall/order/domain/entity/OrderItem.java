package com.nexusmall.order.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_item")
public class OrderItem {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 订单 ID
     */
    private Long orderId;
    
    /**
     * 订单编号
     */
    private String orderSn;
    
    /**
     * 商品 SKU ID
     */
    private Long skuId;
    
    /**
     * 商品名称
     */
    private String skuName;
    
    /**
     * 商品价格
     */
    private BigDecimal skuPrice;
    
    /**
     * 购买数量
     */
    private Integer quantity;
    
    /**
     * 小计金额
     */
    private BigDecimal subtotal;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
