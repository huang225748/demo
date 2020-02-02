package com.itmuch.usercenter.controller.user;

import com.itmuch.usercenter.domain.dto.messaging.UserAddBonusMsgDTO;
import com.itmuch.usercenter.domain.dto.user.UserAddBonseDTO;
import com.itmuch.usercenter.domain.entity.user.User;
import com.itmuch.usercenter.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class BonusController {

    @Autowired
    private UserService userService;
    @PutMapping("/add-bonus")
    public User addBonus(@RequestBody  UserAddBonseDTO userAddBonseDTO){
        userService.addBonus(
                UserAddBonusMsgDTO.builder()
                        .userId(userAddBonseDTO.getUserId())
                        .bonus(userAddBonseDTO.getBouns())
                        .description("兑换分享。。。")
                        .event("BUY")
                        .build()
        );
        return this.userService.findById(userAddBonseDTO.getUserId());
    }
}
