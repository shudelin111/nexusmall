package com.nexusmall.common.enums;

/**
 * 用户行为类型枚举
 */
public enum UserBehaviorType {

    /**
     * 浏览商品
     */
    VIEW_PRODUCT("view_product", "浏览商品"),

    /**
     * 收藏商品
     */
    FAVORITE_PRODUCT("favorite_product", "收藏商品"),

    /**
     * 加入购物车
     */
    ADD_TO_CART("add_to_cart", "加入购物车"),

    /**
     * 搜索商品
     */
    SEARCH_PRODUCT("search_product", "搜索商品"),

    /**
     * 下单购买
     */
    PLACE_ORDER("place_order", "下单购买");

    private final String code;
    private final String desc;

    UserBehaviorType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
