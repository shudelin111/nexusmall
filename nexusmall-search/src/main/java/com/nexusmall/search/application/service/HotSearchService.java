package com.nexusmall.search.application.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 热门搜索词服�?
 * <p>
 * 业界标准实现�?
 * - 使用 Redis Sorted Set (ZSET) 存储热门关键�?
 * - Score = 搜索次数（实时递增�?
 * - Member = 关键�?
 * - 定时持久化到 MySQL（可选）
 * </p>
 */
@Service
public class HotSearchService {

    private final StringRedisTemplate redisTemplate;
    
    /**
     * Redis Key: 热门搜索�?ZSET
     */
    private static final String HOT_SEARCH_KEY = "search:hot:keywords";
    
    /**
     * 保留最�?N 小时的热门搜索数�?
     */
    private static final long TTL_HOURS = 24;

    public HotSearchService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        // 设置过期时间�?4小时自动清理�?
        redisTemplate.expire(HOT_SEARCH_KEY, TTL_HOURS, TimeUnit.HOURS);
    }

    /**
     * 记录搜索行为（每次搜索时调用�?
     *
     * @param keyword 搜索关键�?
     */
    public void recordSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        
        String trimmedKeyword = keyword.trim().toLowerCase();
        
        // ZINCRBY: 增加关键词的分数（搜索次数）
        redisTemplate.opsForZSet().incrementScore(HOT_SEARCH_KEY, trimmedKeyword, 1.0);
        
        // 重置过期时间（滑动窗口）
        redisTemplate.expire(HOT_SEARCH_KEY, TTL_HOURS, TimeUnit.HOURS);
    }

    /**
     * 获取热门搜索词列�?
     *
     * @param limit 返回数量限制
     * @return 热门关键词列表（按搜索次数降序）
     */
    public List<Map<String, Object>> getHotKeywords(int limit) {
        // ZREVRANGE: 按分数降序获取前 N 个元�?
        Set<String> keywords = redisTemplate.opsForZSet()
                .reverseRange(HOT_SEARCH_KEY, 0, limit - 1);
        
        if (keywords == null || keywords.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (String keyword : keywords) {
            Double score = redisTemplate.opsForZSet().score(HOT_SEARCH_KEY, keyword);
            
            Map<String, Object> item = new HashMap<>();
            item.put("keyword", keyword);
            item.put("count", score != null ? score.longValue() : 0L);
            result.add(item);
        }
        
        return result;
    }

    /**
     * 获取热门搜索�?Top N（简化版，只返回关键词）
     *
     * @param topN �?N �?
     * @return 关键词列�?
     */
    public List<String> getTopKeywords(int topN) {
        Set<String> keywords = redisTemplate.opsForZSet()
                .reverseRange(HOT_SEARCH_KEY, 0, topN - 1);
        
        return keywords != null ? new ArrayList<>(keywords) : Collections.emptyList();
    }

    /**
     * 清空热门搜索统计（管理后台用�?
     */
    public void clearHotKeywords() {
        redisTemplate.delete(HOT_SEARCH_KEY);
    }
}
