package com.nexusmall.common.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * HTTP缓存控制拦截器
 * <p>
 * 生产级实践：
 * 1. 通过CommonWebAutoConfiguration中的WebMvcConfigurer注册
 * 2. 为静态资源添加强缓存（7天）
 * 3. 为API响应添加协商缓存或禁止缓存
 * </p>
 *
 * @author shudl
 * @since 2026-04-09
 */
@Slf4j
public class CacheControlInterceptor implements HandlerInterceptor {

    /**
     * 静态资源缓存时间：7天
     */
    private static final long STATIC_RESOURCE_CACHE_SECONDS = TimeUnit.DAYS.toSeconds(7);

    /**
     * API响应缓存时间：不缓存（默认）
     */
    private static final long API_CACHE_SECONDS = 0;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();

        // 静态资源：强缓存
        if (isStaticResource(uri)) {
            CacheControl cacheControl = CacheControl.maxAge(STATIC_RESOURCE_CACHE_SECONDS, TimeUnit.SECONDS)
                    .cachePublic()
                    .mustRevalidate();
            response.setHeader("Cache-Control", cacheControl.getHeaderValue());
            
            // 添加ETag支持（基于文件最后修改时间）
            response.setHeader("ETag", generateETag(uri));
            
            log.debug("【缓存控制】静态资源: {}, Cache-Control: max-age={}", uri, STATIC_RESOURCE_CACHE_SECONDS);
        } 
        // API接口：不缓存或协商缓存
        else if (isApiRequest(uri)) {
            // GET请求可以添加短缓存，其他方法不缓存
            if ("GET".equalsIgnoreCase(request.getMethod())) {
                CacheControl cacheControl = CacheControl.noCache()
                        .mustRevalidate();
                response.setHeader("Cache-Control", cacheControl.getHeaderValue());
            } else {
                // POST/PUT/DELETE等方法：禁止缓存
                response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
                response.setHeader("Pragma", "no-cache");
                response.setDateHeader("Expires", 0);
            }
            
            log.debug("【缓存控制】API请求: {}, Method: {}", uri, request.getMethod());
        }

        return true;
    }

    /**
     * 判断是否为静态资源
     */
    private boolean isStaticResource(String uri) {
        return uri.startsWith("/static/")
                || uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/images/")
                || uri.startsWith("/fonts/")
                || uri.endsWith(".css")
                || uri.endsWith(".js")
                || uri.endsWith(".png")
                || uri.endsWith(".jpg")
                || uri.endsWith(".jpeg")
                || uri.endsWith(".gif")
                || uri.endsWith(".svg")
                || uri.endsWith(".ico")
                || uri.endsWith(".woff")
                || uri.endsWith(".woff2")
                || uri.endsWith(".ttf");
    }

    /**
     * 判断是否为API请求
     */
    private boolean isApiRequest(String uri) {
        return uri.startsWith("/api/")
                || uri.contains("/v1/")
                || uri.contains("/v2/");
    }

    /**
     * 生成简单的ETag（基于URI）
     * 生产环境应基于文件内容或版本号生成
     */
    private String generateETag(String uri) {
        // 简化实现：实际应使用文件hash或版本号
        return "\"" + Integer.toHexString(uri.hashCode()) + "\"";
    }
}
