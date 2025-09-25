package com.collega.payment_gateway_cip.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Request;

@Configuration
public class FeignConfig {

    @Bean
    public Request.Options feignOptions() {
        // 5s connection timeout, 10s read timeout
        return new Request.Options(5000, 10000);
    }
}
