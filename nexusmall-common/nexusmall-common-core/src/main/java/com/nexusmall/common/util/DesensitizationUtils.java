package com.nexusmall.common.util;

/**
 * 数据脱敏工具类
 * <p>
 * 生产级实践：对敏感信息进行脱敏处理，防止数据泄露
 * - 手机号：138****5678
 * - 身份证：110101**********1234
 * - 邮箱：abc***@example.com
 * - 姓名：张*三
 * - 银行卡：6222 **** **** 1234
 * </p>
 *
 * @author shudl
 * @since 2026-04-09
 */
public class DesensitizationUtils {

    private DesensitizationUtils() {
        // 防止实例化
    }

    /**
     * 手机号脱敏
     * <p>
     * 保留前3位和后4位，中间用****替代
     * 例如：13812345678 -> 138****5678
     * </p>
     *
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    public static String desensitizePhone(String phone) {
        if (StringUtils.isBlank(phone) || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 身份证号脱敏
     * <p>
     * 保留前6位和后4位，中间用**********替代
     * 例如：110101199001011234 -> 110101**********1234
     * </p>
     *
     * @param idCard 身份证号
     * @return 脱敏后的身份证号
     */
    public static String desensitizeIdCard(String idCard) {
        if (StringUtils.isBlank(idCard) || idCard.length() < 10) {
            return idCard;
        }
        return idCard.substring(0, 6) + "**********" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 邮箱脱敏
     * <p>
     * 保留前3个字符和@后面的域名，中间用***替代
     * 例如：abcdef@example.com -> abc***@example.com
     * </p>
     *
     * @param email 邮箱
     * @return 脱敏后的邮箱
     */
    public static String desensitizeEmail(String email) {
        if (StringUtils.isBlank(email) || !email.contains("@")) {
            return email;
        }
        
        int atIndex = email.indexOf("@");
        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex);
        
        if (localPart.length() <= 3) {
            return "***" + domainPart;
        }
        
        return localPart.substring(0, 3) + "***" + domainPart;
    }

    /**
     * 姓名脱敏
     * <p>
     * 保留第一个字和最后一个字，中间用*替代
     * 例如：张三 -> 张*，张三丰 -> 张*丰
     * </p>
     *
     * @param name 姓名
     * @return 脱敏后的姓名
     */
    public static String desensitizeName(String name) {
        if (StringUtils.isBlank(name)) {
            return name;
        }
        
        if (name.length() == 1) {
            return "*";
        }
        
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(name.charAt(0));
        for (int i = 1; i < name.length() - 1; i++) {
            sb.append("*");
        }
        sb.append(name.charAt(name.length() - 1));
        
        return sb.toString();
    }

    /**
     * 银行卡号脱敏
     * <p>
     * 保留前4位和后4位，中间用 **** **** 替代
     * 例如：6222021234567890123 -> 6222 **** **** 0123
     * </p>
     *
     * @param bankCard 银行卡号
     * @return 脱敏后的银行卡号
     */
    public static String desensitizeBankCard(String bankCard) {
        if (StringUtils.isBlank(bankCard) || bankCard.length() < 8) {
            return bankCard;
        }
        
        String prefix = bankCard.substring(0, 4);
        String suffix = bankCard.substring(bankCard.length() - 4);
        
        return prefix + " **** **** " + suffix;
    }

    /**
     * 地址脱敏
     * <p>
     * 保留前6个字符，后面用***替代
     * 例如：北京市朝阳区xxx -> 北京市朝阳区***
     * </p>
     *
     * @param address 地址
     * @return 脱敏后的地址
     */
    public static String desensitizeAddress(String address) {
        if (StringUtils.isBlank(address) || address.length() <= 6) {
            return address;
        }
        
        return address.substring(0, 6) + "***";
    }
}
