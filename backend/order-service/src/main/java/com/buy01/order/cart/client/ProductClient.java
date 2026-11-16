package com.buy01.order.cart.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "product-service")
public interface ProductClient { 

    @GetMapping("/products/{id}")
    ProductResponse getProduct(@PathVariable("id") String id);

    @PostMapping("/internal/stock/reserve")
    void reserveStock(@RequestBody StockAdjustRequest request);

    @PostMapping("/internal/stock/release")
    void releaseStock(@RequestBody StockAdjustRequest request);
}
