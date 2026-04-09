package com.nexusmall.payment.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexusmall.payment.domain.port.out.PayChannelAdapter;
import com.nexusmall.payment.adapter.outbound.persistence.PayOrderMapper;
import com.nexusmall.payment.adapter.outbound.persistence.PayRefundMapper;
import com.nexusmall.payment.domain.model.entity.PayOrder;
import com.nexusmall.payment.domain.model.entity.PayRefund;
import com.nexusmall.payment.domain.model.enums.PayStatusEnum;
import com.nexusmall.payment.domain.model.enums.RefundStatusEnum;
import com.nexusmall.common.enums.ResultCode;
import com.nexusmall.common.exception.PaymentException;
import com.nexusmall.common.util.DesensitizationUtils;
import com.nexusmall.payment.interfaces.dto.request.CreateRefundRequest;
import com.nexusmall.payment.interfaces.dto.response.RefundResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 退款服务类
 * <p>
 * 核心业务逻辑：申请退款、审核退款、执行退款等
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {

    private final PayRefundMapper payRefundMapper;
    private final PayOrderMapper payOrderMapper;
    private final List<PayChannelAdapter> channelAdapters;

    /**
     * 渠道适配器缓存
     */
    private Map<String, PayChannelAdapter> adapterMap;

    /**
     * 初始化渠道适配器映射
     */
    private void initAdapterMap() {
        if (adapterMap == null) {
            adapterMap = new HashMap<>();
            for (PayChannelAdapter adapter : channelAdapters) {
                adapterMap.put(adapter.getChannelCode(), adapter);
            }
        }
    }

    /**
     * 申请退款
     *
     * @param request 退款申请请求
     * @return 退款单号
     */
    @Transactional(rollbackFor = Exception.class)
    public String applyRefund(CreateRefundRequest request) {
        log.info("【申请退款】开始 - paymentNo={}, orderNo={}, amount={}, userId={}", 
                DesensitizationUtils.desensitizePaymentNo(request.getPaymentNo()),
                DesensitizationUtils.desensitizeOrderNo(request.getOrderNo()),
                request.getRefundAmount(),
                request.getUserId());

        // 1. 查询支付单
        LambdaQueryWrapper<PayOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(PayOrder::getPaymentNo, request.getPaymentNo());
        PayOrder payOrder = payOrderMapper.selectOne(orderWrapper);

        if (payOrder == null) {
            throw new PaymentException(ResultCode.PAYMENT_NOT_FOUND, "支付单不存在: " + request.getPaymentNo());
        }

        // 2. 验证支付单状态（只有支付成功的才能退款）
        if (!PayStatusEnum.SUCCESS.getCode().equals(payOrder.getStatus())) {
            throw new PaymentException(ResultCode.PAYMENT_FAILED, "只有支付成功的订单才能申请退款");
        }

        // 3. 验证退款金额
        if (request.getRefundAmount().compareTo(payOrder.getPayAmount()) > 0) {
            throw new PaymentException(ResultCode.INVALID_PAYMENT_AMOUNT, "退款金额不能超过支付金额");
        }

        // 4. 生成退款单号
        String refundNo = generateRefundNo();

        // 5. 创建退款申请
        PayRefund refund = new PayRefund();
        BeanUtils.copyProperties(request, refund);
        refund.setRefundNo(refundNo);
        refund.setStatus(RefundStatusEnum.WAITING_AUDIT.getCode());

        int rows = payRefundMapper.insert(refund);
        if (rows <= 0) {
            throw new PaymentException(ResultCode.REFUND_FAILED, "创建退款申请失败");
        }

        log.info("【申请退款】成功 - refundNo={}", 
                DesensitizationUtils.desensitizePaymentNo(refundNo));
        return refundNo;
    }

    /**
     * 审核退款
     *
     * @param refundNo 退款单号
     * @param auditorId 审核人ID
     * @param auditorName 审核人姓名
     * @param approved 是否通过
     * @param remark 审核备注
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditRefund(String refundNo, Long auditorId, String auditorName, boolean approved, String remark) {
        log.info("【审核退款】开始 - refundNo={}, auditorId={}, approved={}", 
                DesensitizationUtils.desensitizePaymentNo(refundNo),
                auditorId,
                approved);

        // 1. 查询退款单
        LambdaQueryWrapper<PayRefund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PayRefund::getRefundNo, refundNo);
        PayRefund refund = payRefundMapper.selectOne(wrapper);

        if (refund == null) {
            throw new PaymentException(ResultCode.REFUND_NOT_FOUND, "退款单不存在: " + refundNo);
        }

        // 2. 验证状态（只有待审核的才能审核）
        if (!RefundStatusEnum.WAITING_AUDIT.getCode().equals(refund.getStatus())) {
            throw new PaymentException(ResultCode.REFUND_FAILED, "退款单状态不正确");
        }

        // 3. 更新审核信息
        refund.setAuditorId(auditorId);
        refund.setAuditorName(auditorName);
        refund.setAuditTime(LocalDateTime.now());
        refund.setAuditRemark(remark);

        if (approved) {
            refund.setStatus(RefundStatusEnum.AUDIT_PASSED.getCode());
        } else {
            refund.setStatus(RefundStatusEnum.AUDIT_REJECTED.getCode());
        }

        payRefundMapper.updateById(refund);

        log.info("【审核退款】完成 - refundNo={}, result={}", 
                DesensitizationUtils.desensitizePaymentNo(refundNo),
                approved ? "通过" : "拒绝");
    }

    /**
     * 执行退款
     *
     * @param refundNo 退款单号
     */
    @Transactional(rollbackFor = Exception.class)
    public void executeRefund(String refundNo) {
        log.info("【执行退款】开始 - refundNo={}", 
                DesensitizationUtils.desensitizePaymentNo(refundNo));

        // 1. 查询退款单
        LambdaQueryWrapper<PayRefund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PayRefund::getRefundNo, refundNo);
        PayRefund refund = payRefundMapper.selectOne(wrapper);

        if (refund == null) {
            throw new PaymentException(ResultCode.REFUND_NOT_FOUND, "退款单不存在: " + refundNo);
        }

        // 2. 验证状态（只有审核通过的才能执行退款）
        if (!RefundStatusEnum.AUDIT_PASSED.getCode().equals(refund.getStatus())) {
            throw new PaymentException(ResultCode.REFUND_FAILED, "退款单未审核通过");
        }

        // 3. 查询支付单
        LambdaQueryWrapper<PayOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(PayOrder::getPaymentNo, refund.getPaymentNo());
        PayOrder payOrder = payOrderMapper.selectOne(orderWrapper);

        if (payOrder == null || payOrder.getTradeNo() == null) {
            throw new PaymentException(ResultCode.PAYMENT_NOT_FOUND, "支付单或交易号不存在");
        }

        // 4. 更新退款状态为退款中
        refund.setStatus(RefundStatusEnum.REFUNDING.getCode());
        payRefundMapper.updateById(refund);

        try {
            // 5. 调用第三方退款接口
            PayChannelAdapter adapter = getChannelAdapter(payOrder.getChannelCode());
            boolean success = adapter.refund(
                    refund.getRefundNo(),
                    payOrder.getTradeNo(),
                    refund.getRefundAmount().toString(),
                    refund.getReason()
            );

            if (success) {
                // 6. 更新退款状态为成功
                refund.setStatus(RefundStatusEnum.SUCCESS.getCode());
                refund.setRefundTime(LocalDateTime.now());
                payRefundMapper.updateById(refund);

                // 7. 更新支付单的退款金额和状态
                payOrder.setRefundAmount(payOrder.getRefundAmount().add(refund.getRefundAmount()));
                if (payOrder.getRefundAmount().compareTo(payOrder.getPayAmount()) >= 0) {
                    payOrder.setStatus(PayStatusEnum.REFUNDED.getCode());
                }
                payOrderMapper.updateById(payOrder);

                log.info("【执行退款】成功 - refundNo={}, amount={}", 
                        DesensitizationUtils.desensitizePaymentNo(refundNo),
                        refund.getRefundAmount());
            } else {
                // 8. 更新退款状态为失败
                refund.setStatus(RefundStatusEnum.FAILED.getCode());
                payRefundMapper.updateById(refund);

                log.error("【执行退款】失败 - refundNo={}", 
                        DesensitizationUtils.desensitizePaymentNo(refundNo));
                throw new PaymentException(ResultCode.REFUND_FAILED, "退款执行失败");
            }
        } catch (Exception e) {
            log.error("【执行退款】异常 - refundNo={}", 
                    DesensitizationUtils.desensitizePaymentNo(refundNo), e);
            refund.setStatus(RefundStatusEnum.FAILED.getCode());
            payRefundMapper.updateById(refund);
            throw new PaymentException(ResultCode.REFUND_FAILED, "退款执行异常：" + e.getMessage(), e);
        }
    }

    /**
     * 查询退款单
     *
     * @param refundNo 退款单号
     * @return 退款响应
     */
    public RefundResponse queryRefund(String refundNo) {
        log.info("【查询退款单】开始 - refundNo={}", 
                DesensitizationUtils.desensitizePaymentNo(refundNo));

        LambdaQueryWrapper<PayRefund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PayRefund::getRefundNo, refundNo);
        PayRefund refund = payRefundMapper.selectOne(wrapper);

        if (refund == null) {
            throw new PaymentException(ResultCode.REFUND_NOT_FOUND, "退款单不存在: " + refundNo);
        }

        RefundResponse response = new RefundResponse();
        BeanUtils.copyProperties(refund, response);
        response.setStatusDesc(RefundStatusEnum.getByCode(refund.getStatus()).getDesc());

        log.info("【查询退款单】成功 - refundNo={}, status={}", 
                DesensitizationUtils.desensitizePaymentNo(refundNo),
                response.getStatusDesc());
        
        return response;
    }

    /**
     * 生成退款单号
     *
     * @return 退款单号
     */
    private String generateRefundNo() {
        return "REF" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 获取渠道适配器
     *
     * @param channelCode 渠道编码
     * @return 渠道适配器
     */
    private PayChannelAdapter getChannelAdapter(String channelCode) {
        initAdapterMap();
        PayChannelAdapter adapter = adapterMap.get(channelCode);
        if (adapter == null) {
            throw new PaymentException(ResultCode.PAYMENT_CHANNEL_UNAVAILABLE, "不支持的支付渠道: " + channelCode);
        }
        return adapter;
    }
}
