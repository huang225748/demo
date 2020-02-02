package com.itmuch.contentcenter.configuration;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.alibaba.nacos.NacosDiscoveryAutoConfiguration;
import org.springframework.cloud.alibaba.nacos.NacosDiscoveryProperties;
import org.springframework.cloud.alibaba.nacos.ribbon.NacosServer;

/**
 * 扩展Ribbon 支持Nacos权重
 */
@Slf4j
public class NacosWeightedRule extends AbstractLoadBalancerRule {

    @Autowired
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {
        //读取配置文件,并初始化NacosWeightedRule
    }

    @Override
    public Server choose(Object o) {
        try {
            BaseLoadBalancer loadBalancer = (BaseLoadBalancer) this.getLoadBalancer();
//        log.info("lb = {}",loadBalancer);

            //想要请求的微服务的名称
            String name = loadBalancer.getName();
            //拿到服务发现的相关API...
            NamingService namingService = nacosDiscoveryProperties.namingServiceInstance();
            //nacos clients自动通过基于权重的负载均衡算法,给我们选择一个实例。
            Instance instance = namingService.selectOneHealthyInstance(name);
            log.info("contentcenter:port = {}, instance = {}",instance.getPort(),instance);
            return new NacosServer(instance);
        } catch (NacosException e) {
           return  null;
        }

    }
}
