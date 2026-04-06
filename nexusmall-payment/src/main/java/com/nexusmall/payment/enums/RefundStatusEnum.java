package com.nexusmall.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 退款状态枚举
 * <p>
 * 状态机流转：
 * 0(待审核) → 1(审核通过) / 2(审核拒绝)
 * 1(审核通过) → 3(退款中) → 4(退款成功) / 5(退款失败)
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Getter
@AllArgsConstructor
public enum RefundStatusEnum {

    /**
     * 待审核
     */
    WAITING_AUDIT(0, "待审核"),

    /**
     * 审核通过
     */
    AUDIT_PASSED(1, "审核通过"),

    /**
     * 审核拒绝
     */
    AUDIT_REJECTED(2, "审核拒绝"),

    /**
     * 退款中
     */
    REFUNDING(3, "退款中"),

    /**
     * 退款成功
     */
    SUCCESS(4, "退款成功"),

    /**
     * 退款失败
     */
    FAILED(5, "退款失败");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态描述
     */
    private final String desc;

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 枚举值
     */
    public static RefundStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RefundStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否为终态
     *
     * @param code 状态码
     * @return true=终态，false=非终态
     */
    public static boolean isFinalStatus(Integer code) {
        return AUDIT_REJECTED.getCode().equals(code)
                || SUCCESS.getCode().equals(code)
                || FAILED.getCode().equals(code);
    }
}
