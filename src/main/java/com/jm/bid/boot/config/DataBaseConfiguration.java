package com.jm.bid.boot.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Created by xiangyang on 16/8/14.
 */
@Configuration
@MapperScan(basePackages = {"com.jm.bid.**.mapper"})
public class DataBaseConfiguration {


    @Bean
    public PageInterceptor pageInterceptor() {
        PageInterceptor pageInterceptor = new PageInterceptor();
        return pageInterceptor;
    }


}
