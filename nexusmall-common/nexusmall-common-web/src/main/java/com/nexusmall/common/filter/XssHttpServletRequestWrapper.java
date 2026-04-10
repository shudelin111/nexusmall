package com.nexusmall.common.filter;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * XSS防护请求包装器
 * <p>
 * 生产级实践：对所有请求参数进行XSS清理，防止脚本注入攻击
 * </p>
 *
 * @author shudl
 * @since 2026-04-09
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    /**
     * XSS白名单（允许的安全HTML标签）
     * 如需允许富文本，可改为 Safelist.relaxed()
     */
    private static final Safelist XSS_SAFE_LIST = Safelist.none();

    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return cleanXSS(value);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }

        String[] cleanedValues = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            cleanedValues[i] = cleanXSS(values[i]);
        }
        return cleanedValues;
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return cleanXSS(value);
    }

    @Override
    public String getQueryString() {
        String value = super.getQueryString();
        return cleanXSS(value);
    }

    /**
     * 清理XSS攻击代码
     *
     * @param value 原始值
     * @return 清理后的值
     */
    private String cleanXSS(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        // 使用Jsoup清理HTML标签和脚本
        return Jsoup.clean(value, XSS_SAFE_LIST);
    }
}
