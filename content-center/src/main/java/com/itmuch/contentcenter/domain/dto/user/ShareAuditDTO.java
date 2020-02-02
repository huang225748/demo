package com.itmuch.contentcenter.domain.dto.user;

import com.itmuch.contentcenter.domain.enums.AuditStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ShareAuditDTO {
    /**
     * 原因
     */
    private String reason;
    /**
     * 审核状态
     */
    private AuditStatusEnum auditStatusEnum;
}
