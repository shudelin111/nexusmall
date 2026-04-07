package com.nexusmall.logistics.application.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nexusmall.logistics.domain.entity.LogisticsOrder;

/**
 * 物流订单服务接口
 * <p>
 * 业界标准：
 * - 支持物流订单CRUD
 * - 支持物流状态流转
 * - 支持轨迹同步
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
public interface LogisticsOrderService extends IService<LogisticsOrder> {

    /**
     * 根据订单编号查询物流订单
     *
     * @param orderSn 订单编号
     * @return 物流订单
     */
    LogisticsOrder getByOrderSn(String orderSn);

    /**
     * 根据快递单号查询物流订单
     *
     * @param expressNo 快递单号
     * @return 物流订单
     */
    LogisticsOrder getByExpressNo(String expressNo);

    /**
     * 创建物流订单（发货）
     * <p>
     * 业界标准：
     * - 生成快递单号
     * - 初始化物流状态为"已发货"
     * - 记录发货时间
     * </p>
     *
     * @param orderSn       订单编号
     * @param memberId      会员ID
     * @param warehouseId   仓库ID
     * @param expressCompany 快递公司
     * @param receiverName  收货人姓名
     * @param receiverPhone 收货人电话
     * @param receiverAddress 收货地址
     * @param freightAmount 运费
     * @return 物流订单
     */
    LogisticsOrder createLogisticsOrder(String orderSn, Long memberId, Long warehouseId,
                                         String expressCompany, String receiverName,
                                         String receiverPhone, String receiverAddress,
                                         java.math.BigDecimal freightAmount);

    /**
     * 更新物流状态
     *
     * @param id     物流订单ID
     * @param status 新状态
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 确认签收
     *
     * @param id 物流订单ID
     * @return 是否成功
     */
    boolean confirmReceive(Long id);
}
