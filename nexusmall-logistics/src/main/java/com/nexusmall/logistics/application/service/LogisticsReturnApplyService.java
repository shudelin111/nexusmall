package com.nexusmall.logistics.application.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nexusmall.logistics.domain.entity.LogisticsReturnApply;

import java.util.List;

/**
 * 退货申请服务接口(基于 Redis)
 * <p>
 * 业界标准：
 * - 支持退货申请提交
 * - 支持退货审核
 * - 支持退货物流跟踪
 * - 支持退货完成处理
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
public interface LogisticsReturnApplyService extends IService<LogisticsReturnApply> {

    /**
     * 提交退货申请
     *
     * @param orderSn          订单编号
     * @param memberId         会员ID
     * @param returnReason     退货原因
     * @param returnDescription 退货说明
     * @param returnImages     退货凭证图片（JSON数组)
     * @return 退货申请
     */
    LogisticsReturnApply submitReturnApply(String orderSn, Long memberId, String returnReason,
                                            String returnDescription, String returnImages);

    /**
     * 审核退货申请（同同意)
     *
     * @param id 退货申请ID
     * @return 是否成功
     */
    boolean approveReturnApply(Long id);

    /**
     * 审核退货申请（拒绝)
     *
     * @param id     退货申请ID
     * @param reason 拒绝原因
     * @return 是否成功
     */
    boolean rejectReturnApply(Long id, String reason);

    /**
     * 填写退货物流信息
     *
     * @param id             退货申请ID
     * @param expressCompany 退货快递公司
     * @param expressNo      退货快递单号
     * @return 是否成功
     */
    boolean fillReturnLogistics(Long id, String expressCompany, String expressNo);

    /**
     * 确认收到退货
     *
     * @param id 退货申请ID
     * @return 是否成功
     */
    boolean confirmReturnReceive(Long id);

    /**
     * 根据订单编号查询退货申请列表
     *
     * @param orderSn 订单编号
     * @return 退货申请列表
     */
    List<LogisticsReturnApply> listByOrderSn(String orderSn);

    /**
     * 根据会员ID查询退货申请列表
     *
     * @param memberId 会员ID
     * @return 退货申请列表
     */
    List<LogisticsReturnApply> listByMemberId(Long memberId);
}
