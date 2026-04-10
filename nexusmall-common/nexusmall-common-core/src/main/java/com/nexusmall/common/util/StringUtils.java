package com.nexusmall.common.util;

import cn.hutool.core.util.StrUtil;

/**
 * 字符串工具类
 * <p>
 * 扩展 Hutool StrUtil，提供业务相关的字符串处理方法
 * </p>
 *
 * @author shudl
 * @since 2026-04-09
 */
public class StringUtils {

    private StringUtils() {
        // 防止实例化
    }

    /**
     * 判断字符串是否为空（null 或 ""）
     *
     * @param str 待判断的字符串
     * @return true=为空
     */
    public static boolean isEmpty(String str) {
        return StrUtil.isEmpty(str);
    }

    /**
     * 判断字符串是否不为空
     *
     * @param str 待判断的字符串
     * @return true=不为空
     */
    public static boolean isNotEmpty(String str) {
        return StrUtil.isNotEmpty(str);
    }

    /**
     * 判断字符串是否为空白（null、"" 或只包含空白字符）
     *
     * @param str 待判断的字符串
     * @return true=为空白
     */
    public static boolean isBlank(String str) {
        return StrUtil.isBlank(str);
    }

    /**
     * 判断字符串是否不为空白
     *
     * @param str 待判断的字符串
     * @return true=不为空白
     */
    public static boolean isNotBlank(String str) {
        return StrUtil.isNotBlank(str);
    }

    /**
     * 去除字符串首尾空白，如果为 null 则返回 null
     *
     * @param str 待处理的字符串
     * @return 处理后的字符串
     */
    public static String trim(String str) {
        return StrUtil.trim(str);
    }

    /**
     * 去除字符串首尾空白，如果为 null 则返回空字符串
     *
     * @param str 待处理的字符串
     * @return 处理后的字符串
     */
    public static String trimToEmpty(String str) {
        return StrUtil.trimToEmpty(str);
    }

    /**
     * 如果字符串为 null 或空白，返回默认值
     *
     * @param str          待判断的字符串
     * @param defaultStr   默认值
     * @return 原字符串或默认值
     */
    public static String defaultIfBlank(String str, String defaultStr) {
        return isBlank(str) ? defaultStr : str;
    }

    /**
     * 生成随机字符串（字母+数字）
     *
     * @param length 长度
     * @return 随机字符串
     */
    public static String randomString(int length) {
        return cn.hutool.core.util.RandomUtil.randomString(length);
    }

    /**
     * 隐藏字符串中间部分（用于脱敏显示）
     *
     * @param str       原始字符串
     * @param startKeep 前面保留的字符数
     * @param endKeep   后面保留的字符数
     * @return 脱敏后的字符串
     */
    public static String hideMiddle(String str, int startKeep, int endKeep) {
        if (isBlank(str)) {
            return str;
        }
        
        int length = str.length();
        if (length <= startKeep + endKeep) {
            return str;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(str.substring(0, startKeep));
        for (int i = 0; i < length - startKeep - endKeep; i++) {
            sb.append('*');
        }
        sb.append(str.substring(length - endKeep));
        
        return sb.toString();
    }
}
