package com.nexusmall.order.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order")
public class Order {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 订单编号
     */
    private String orderSn;
    
    /**
     * 用户 ID
     */
    private Long memberId;
    
    /**
     * 收货人姓名
     */
    private String receiverName;
    
    /**
     * 收货人电话
     */
    private String receiverPhone;
    
    /**
     * 收货人地址
     */
    private String receiverAddress;
    
    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 实付金额
     */
    private BigDecimal payAmount;
    
    /**
     * 运费金额
     */
    private BigDecimal freightAmount;
    
    /**
     * 促销优惠金额
     */
    private BigDecimal promotionAmount;
    
    /**
     * 订单状态：0-待支付，1-已支付，2-已发货，3-已完成，4-已取消
     */
    private Integer status;
    
    /**
     * 支付方式：1-微信，2-支付宝，3-银行卡
     */
    private Integer paymentType;
    
    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;
    
    /**
     * 发货时间
     */
    private LocalDateTime deliveryTime;
    
    /**
     * 确认收货时间
     */
    private LocalDateTime receiveTime;
    
    /**
     * 订单备注
     */
    private String remark;
    
    /**
     * 版本号（乐观锁）
     */
    private Integer version;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
