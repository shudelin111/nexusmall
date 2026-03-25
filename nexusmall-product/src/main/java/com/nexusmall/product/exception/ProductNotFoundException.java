package com.nexusmall.product.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long skuId) {
        super("商品不存在：" + skuId);
    }
    
    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
