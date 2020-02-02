package ribbonconfiguration;

import com.itmuch.contentcenter.configuration.NacosWeightedRule;
import com.netflix.loadbalancer.IRule;
 import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Ribbonconfiguration {
    @Bean
    public IRule ribbonRule(){
        return new NacosWeightedRule();
    }


}
