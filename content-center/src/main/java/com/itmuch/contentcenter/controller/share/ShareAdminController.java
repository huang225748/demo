package com.itmuch.contentcenter.controller.share;

import com.itmuch.contentcenter.auth.CheckAuthorization;
import com.itmuch.contentcenter.domain.dto.user.ShareAuditDTO;
import com.itmuch.contentcenter.domain.entity.content.Share;
import com.itmuch.contentcenter.service.content.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/shares")
public class ShareAdminController {

    @Autowired
    private ShareService shareService;
    @PutMapping("/audit/{id}")
    @CheckAuthorization("admin")
    public Share auditById(@PathVariable  Integer id , @RequestBody  ShareAuditDTO auditDTO){
        //TODO  认证授权
        return this.shareService.auditById(id,auditDTO);
    }
}
