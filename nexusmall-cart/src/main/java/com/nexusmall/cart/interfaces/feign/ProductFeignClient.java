package com.nexusmall.cart.interfaces.feign;

import com.nexusmall.common.vo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Product服务 Feign客户端
 * <p>
 * 用于购物车调用Product服务获取商品信息、库存状态等
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@FeignClient(name = "nexusmall-product", path = "/api/v1")
public interface ProductFeignClient {

    /**
     * 批量查询SKU信息
     *
     * @param skuIds SKU ID列表
     * @return SKU信息Map (key: skuId, value: SKU详情)
     */
    @GetMapping("/skus/batch")
    Result<Map<Long, SkuInfo>> batchQuerySkus(@RequestParam("skuIds") List<Long> skuIds);

    /**
     * 批量查询库存状态
     *
     * @param skuIds SKU ID列表
     * @return 库存状态Map (key: skuId, value: 库存数量)
     */
    @GetMapping("/stocks/batch")
    Result<Map<Long, Integer>> batchQueryStocks(@RequestParam("skuIds") List<Long> skuIds);

    /**
     * SKU信息内部类
     */
    class SkuInfo {
        private Long skuId;
        private Long spuId;
        private String skuName;
        private String spuName;
        private String mainImage;
        private String subtitle;
        private java.math.BigDecimal price;
        private java.math.BigDecimal promotionPrice;
        private String attrs;
        private Integer stockQuantity;
        private Integer stockStatus; // 0-无货 1-有货 2-预售
        private Integer maxPurchaseLimit;
        private List<String> promotionTags;
        private String promotionId;

        // Getters and Setters
        public Long getSkuId() { return skuId; }
        public void setSkuId(Long skuId) { this.skuId = skuId; }
        
        public Long getSpuId() { return spuId; }
        public void setSpuId(Long spuId) { this.spuId = spuId; }
        
        public String getSkuName() { return skuName; }
        public void setSkuName(String skuName) { this.skuName = skuName; }
        
        public String getSpuName() { return spuName; }
        public void setSpuName(String spuName) { this.spuName = spuName; }
        
        public String getMainImage() { return mainImage; }
        public void setMainImage(String mainImage) { this.mainImage = mainImage; }
        
        public String getSubtitle() { return subtitle; }
        public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
        
        public java.math.BigDecimal getPrice() { return price; }
        public void setPrice(java.math.BigDecimal price) { this.price = price; }
        
        public java.math.BigDecimal getPromotionPrice() { return promotionPrice; }
        public void setPromotionPrice(java.math.BigDecimal promotionPrice) { this.promotionPrice = promotionPrice; }
        
        public String getAttrs() { return attrs; }
        public void setAttrs(String attrs) { this.attrs = attrs; }
        
        public Integer getStockQuantity() { return stockQuantity; }
        public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
        
        public Integer getStockStatus() { return stockStatus; }
        public void setStockStatus(Integer stockStatus) { this.stockStatus = stockStatus; }
        
        public Integer getMaxPurchaseLimit() { return maxPurchaseLimit; }
        public void setMaxPurchaseLimit(Integer maxPurchaseLimit) { this.maxPurchaseLimit = maxPurchaseLimit; }
        
        public List<String> getPromotionTags() { return promotionTags; }
        public void setPromotionTags(List<String> promotionTags) { this.promotionTags = promotionTags; }
        
        public String getPromotionId() { return promotionId; }
        public void setPromotionId(String promotionId) { this.promotionId = promotionId; }
    }
}
