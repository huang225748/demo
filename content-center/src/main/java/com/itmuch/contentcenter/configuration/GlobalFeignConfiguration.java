package com.itmuch.contentcenter.configuration;

import feign.Logger;
import org.springframework.context.annotation.Bean;


/**
 * Feign的配置类
 * 这个类别加@Configuration注解了
 */
public class GlobalFeignConfiguration {

    @Bean
    public Logger.Level level(){
        //让feign打印所有请求细节
        return Logger.Level.FULL;
    }
}
