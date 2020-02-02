package com.itmuch.contentcenter.controller.share;

import com.github.pagehelper.PageInfo;
import com.itmuch.contentcenter.auth.CheckLogin;
import com.itmuch.contentcenter.domain.content.ShareDTO;
import com.itmuch.contentcenter.domain.entity.content.Share;
import com.itmuch.contentcenter.service.content.ShareService;
import com.itmuch.contentcenter.util.JwtOperator;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

//独立的线程池
//thread-pool-1 coreSize =10
@RestController
@RequestMapping("/shares")
public class ShareController {

    @Autowired
    private ShareService shareService;
    @Autowired
    private JwtOperator jwrOperator;

    //5秒以内的错误率、错误次数。。。
    //达到阈值就跳闸
    @GetMapping("/{id}")
    @CheckLogin
    public ShareDTO findById(@PathVariable Integer id){
        return  shareService.findById(id);
    }

    @GetMapping("/q")
    public PageInfo<Share> q(
            @RequestParam(required = false) String title,
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestHeader(value = "X-Token", required = false) String token){
        //注意点：pageSize务必做到控制 10000
        if(pageSize >100){
            pageSize = 100;
        }
        Integer userId = null;
        if(StringUtils.isNotBlank(token)){
            Claims claimsFromToken = this.jwrOperator.getClaimsFromToken(token);
            userId = (Integer) claimsFromToken.get("id");
        }
        //解析token

        return this.shareService.q(title,pageNo,pageSize,userId);
    }

    @GetMapping("/exchange/{id}")
    @CheckLogin
    public Share exchangeById (@PathVariable Integer id, HttpServletRequest request){
        return this.shareService.exchangeById(id,request);
    }
}
