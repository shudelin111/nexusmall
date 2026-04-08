package com.nexusmall.search.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "nexusmall-inventory")
public interface InventoryClient {
}
