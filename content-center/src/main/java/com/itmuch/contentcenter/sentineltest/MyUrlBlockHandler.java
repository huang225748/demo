package com.itmuch.contentcenter.sentineltest;

import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlBlockHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class MyUrlBlockHandler implements UrlBlockHandler {
    @Override
    public void blocked(HttpServletRequest request, HttpServletResponse response, BlockException e) throws IOException {
        ErrorMsg errorMsg = null;
        //限流异常
        if(e instanceof FlowException)
        {
            errorMsg = ErrorMsg.builder()
                    .status(100)
                    .msg("限流了")
                    .build();
        }//降级异常
        else if(e instanceof DegradeException)
        {
            errorMsg = ErrorMsg.builder()
                    .status(100)
                    .msg("降级了")
                    .build();
        }//参数异常
        else if(e instanceof ParamFlowException)
        {
            errorMsg = ErrorMsg.builder()
                    .status(100)
                    .msg("热点参数限流")
                    .build();
        }//系统异常
        else if(e instanceof SystemBlockException)
        {
            errorMsg = ErrorMsg.builder()
                    .status(100)
                    .msg("系统规则(负载/...不满足要求)")
                    .build();
        }//授权异常
        else if(e instanceof AuthorityException)
        {
            errorMsg = ErrorMsg.builder()
                    .status(100)
                    .msg("授权规则不通过")
                    .build();
        }
        // http状态码
        response.setStatus(500);
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Type","application/json;charset=utf-8");
        response.setContentType("application/json;charset=utf-8");
        // spring mvc自带的json操作工具，叫jackson
        new ObjectMapper()
                .writeValue(
                        response.getWriter(),
                        errorMsg
                );
    }

}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class ErrorMsg{
    private Integer status;
    private String msg;
}
