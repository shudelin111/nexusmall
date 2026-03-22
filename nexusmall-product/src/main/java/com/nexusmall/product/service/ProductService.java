package com.nexusmall.product.service;

import com.nexusmall.product.entity.Product;

import java.util.List;

public interface ProductService {

    List<Product> listProducts();

    Product getBySkuId(Long skuId);
}
