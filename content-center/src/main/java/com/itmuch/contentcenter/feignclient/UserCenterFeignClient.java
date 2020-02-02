package com.itmuch.contentcenter.feignclient;

import com.itmuch.contentcenter.domain.dto.messaging.UserAddBonusMsgDTO;
import com.itmuch.contentcenter.domain.dto.user.UserAddBonseDTO;
import com.itmuch.contentcenter.domain.dto.user.UserDTO;
import com.itmuch.contentcenter.feignclient.fallback.UserCenterFeginClientFallback;
import com.itmuch.contentcenter.feignclient.fallbackfactory.UserCenterFeginClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

//@FeignClient(name = "user-center", configuration = UserCenterConfiguration.class)
@FeignClient(name = "user-center",
//无法捕获异常 而且 fallback与 fallbackFactory只能存在一个
//        fallback = UserCenterFeginClientFallback.class,
        fallbackFactory = UserCenterFeginClientFallbackFactory.class
)
public interface UserCenterFeignClient {

    /**
     * http://user-center/users/{id}
     * @param id
     * @return
     */
    @GetMapping("/users/{id}")
    UserDTO findById(@PathVariable("id") Integer id);

    @PutMapping("/users/add-bonus")
    UserDTO addBonus(@RequestBody UserAddBonseDTO userAddBonseDTO);
}
