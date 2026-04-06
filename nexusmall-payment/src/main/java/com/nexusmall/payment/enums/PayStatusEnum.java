package com.nexusmall.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付状态枚举
 * <p>
 * 状态机流转：
 * 0(待支付) → 1(支付中) → 2(支付成功) / 3(支付失败)
 * 0(待支付) → 4(已关闭) [超时]
 * 2(支付成功) → 5(已退款)
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Getter
@AllArgsConstructor
public enum PayStatusEnum {

    /**
     * 待支付
     */
    WAITING(0, "待支付"),

    /**
     * 支付中
     */
    PAYING(1, "支付中"),

    /**
     * 支付成功
     */
    SUCCESS(2, "支付成功"),

    /**
     * 支付失败
     */
    FAILED(3, "支付失败"),

    /**
     * 已关闭（超时未支付）
     */
    CLOSED(4, "已关闭"),

    /**
     * 已退款
     */
    REFUNDED(5, "已退款");

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
    public static PayStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PayStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否为终态（不可再变更状态）
     *
     * @param code 状态码
     * @return true=终态，false=非终态
     */
    public static boolean isFinalStatus(Integer code) {
        return SUCCESS.getCode().equals(code)
                || FAILED.getCode().equals(code)
                || CLOSED.getCode().equals(code)
                || REFUNDED.getCode().equals(code);
    }
}
