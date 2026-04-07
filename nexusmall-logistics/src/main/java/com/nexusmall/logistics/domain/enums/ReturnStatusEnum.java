package com.nexusmall.logistics.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 退货状态枚举
 * <p>
 * 业界标准：
 * - 0=申请中（用户提交申请，等待审核）
 * - 1=已同意（商家同意退货，用户提供退货物流）
 * - 2=已拒绝（商家拒绝退货申请）
 * - 3=已完成（商家收到退货，退款完成）
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Getter
@AllArgsConstructor
public enum ReturnStatusEnum {

    /**
     * 申请中
     */
    APPLYING(0, "申请中"),

    /**
     * 已同意
     */
    APPROVED(1, "已同意"),

    /**
     * 已拒绝
     */
    REJECTED(2, "已拒绝"),

    /**
     * 已完成
     */
    COMPLETED(3, "已完成");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 枚举值
     */
    public static ReturnStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ReturnStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
