package com.itmuch.contentcenter.feignclient.fallbackfactory;

import com.itmuch.contentcenter.domain.dto.user.UserAddBonseDTO;
import com.itmuch.contentcenter.domain.dto.user.UserDTO;
import com.itmuch.contentcenter.feignclient.UserCenterFeignClient;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserCenterFeginClientFallbackFactory implements FallbackFactory<UserCenterFeignClient> {
    @Override
    public UserCenterFeignClient create(Throwable e) {
        return new UserCenterFeignClient() {
            @Override
            public UserDTO findById(Integer id) {
                log.warn("远程调用被限流/降级了",e);
                UserDTO userDTO = new UserDTO();
                userDTO.setWxNickname("流控或降级返回的用户");
                return userDTO;
            }

            @Override
            public UserDTO addBonus(UserAddBonseDTO userAddBonseDTO) {
                log.warn("远程调用被限流/降级了",e);
                return null;
            }
        };
    }
}
