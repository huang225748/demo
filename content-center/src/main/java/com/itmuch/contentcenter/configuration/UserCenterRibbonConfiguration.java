package com.itmuch.contentcenter.configuration;

import com.netflix.loadbalancer.IRule;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ribbonconfiguration.Ribbonconfiguration;

@Configuration
//@RibbonClient(name = "user-center", configuration = Ribbonconfiguration.class)
@RibbonClients(defaultConfiguration = Ribbonconfiguration.class )
public class UserCenterRibbonConfiguration {

    @Bean
    public IRule ribbonRule(){
        return new NacosSameClusterWeightedRule();

    }
}
