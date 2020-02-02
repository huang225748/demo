package com.itmuch.contentcenter.service.content;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itmuch.contentcenter.dao.content.MidUserShareMapper;
import com.itmuch.contentcenter.dao.content.ShareMapper;
import com.itmuch.contentcenter.dao.messaging.RocketmqTransactionLogMapper;
import com.itmuch.contentcenter.domain.content.MidUserShare;
import com.itmuch.contentcenter.domain.content.ShareDTO;
import com.itmuch.contentcenter.domain.dto.messaging.UserAddBonusMsgDTO;
import com.itmuch.contentcenter.domain.dto.user.ShareAuditDTO;
import com.itmuch.contentcenter.domain.dto.user.UserAddBonseDTO;
import com.itmuch.contentcenter.domain.dto.user.UserDTO;
import com.itmuch.contentcenter.domain.entity.content.Share;
import com.itmuch.contentcenter.domain.entity.messaging.RocketmqTransactionLog;
import com.itmuch.contentcenter.domain.enums.AuditStatusEnum;
import com.itmuch.contentcenter.feignclient.UserCenterFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ShareService {

    @Autowired
    private ShareMapper shareMapper;


    @Autowired
    private DiscoveryClient discoveryClient;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private RocketmqTransactionLogMapper rocketmqTransactionLogMapper;
    @Autowired
    private Source source;

    @Autowired
    private UserCenterFeignClient userCenterFeignClient;

    @Autowired
    private MidUserShareMapper midUserShareMapper;

    public ShareDTO findById(Integer id){
        //获取分享详情
        Share  share =  this.shareMapper.selectByPrimaryKey(id);
        //发布人id
        Integer userId = share.getUserId();
        /**
         * 强调
         * 了解 stream -> jdk8
         * lambda表达式
         * functional --> 函数式编程
         */
        //用户中心所有实例的信息
        List<ServiceInstance> instances = discoveryClient.getInstances("user-center");
 /*        String targetURL = instances.stream()
                //数据变换
                .map(instance-> instance.getUri().toString()+"/users/{id}")
                .findFirst()
                .orElseThrow(()-> new IllegalArgumentException("当前没有实例！"))
                ;*/
        //所有用户中心实例的请求地址
       /* List<String> targetURLs = instances.stream()
                //数据变换
                .map(instance-> instance.getUri().toString()+"/users/{id}")
                .collect(Collectors.toList());
                ;
        int i = ThreadLocalRandom.current().nextInt(targetURLs.size());
         log.info("请求的目标路径：{}",targetURLs);*/
        //怎么调用用户微服务/users/{userId}
        //用HTTP Get方法去请求,并返回一个对象
        UserDTO userDTO = this.userCenterFeignClient.findById(userId);
        ShareDTO shareDTO = new ShareDTO();
        BeanUtils.copyProperties(share,shareDTO);
        shareDTO.setWxNickname(userDTO.getWxNickname());
        return shareDTO;
    }

    public Share auditById(Integer id, ShareAuditDTO auditDTO) {
        //1.查询share是否存在，不存在或者当前的audit_status != NOT_YET,那么久抛异常
        Share share = shareMapper.selectByPrimaryKey(id);
        if(null == share){
            throw  new IllegalArgumentException("参数非法！该分享不存在！");
        }
        if(!Objects.equals("NOT_YET",share.getAuditStatus())){
            throw  new IllegalArgumentException("参数非法！该分享已审核通过或审核不通过！");
        }
        if(AuditStatusEnum.PASS.equals(auditDTO.getAuditStatusEnum())){
            //发送半消息。。。
            String transactionId = UUID.randomUUID().toString();

            this.source.output().send(
                    MessageBuilder.withPayload(
                            UserAddBonusMsgDTO.builder()
                                    .userId(share.getUserId())
                                    .bonus(50)
                                    .build()
                    )
                            //也有秒用...
                            .setHeader(RocketMQHeaders.TRANSACTION_ID, transactionId)
                            .setHeader("share_id",id)
                            .setHeader("dto", JSON.toJSONString(auditDTO))
                            .build()
            );

           /* this.rocketMQTemplate.sendMessageInTransaction(
              "tx-add-bonus-group",
                    "add-bonus",
                    MessageBuilder.withPayload(
                            UserAddBonusMsgDTO.builder()
                                    .userId(share.getUserId())
                                    .bonus(50)
                                    .build()
                    )
                            //也有秒用...
                    .setHeader(RocketMQHeaders.TRANSACTION_ID, transactionId)
                     .setHeader("share_id",id)
                    .build(),
                    //arg有大用处
                    auditDTO
            );*/
        }else {
            this.auditById(id,auditDTO);
        }

        return  share;
    }

    public void auditByIdInDB(Integer id, ShareAuditDTO auditDTO){
        Share share = Share.builder()
                .id(id)
                .auditStatus(auditDTO.getAuditStatusEnum().toString())
                .reason(auditDTO.getReason())
                .build();
        this.shareMapper.updateByPrimaryKeySelective(share);
        //4 把share写入缓存
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditByIdWithRocketMqLog(Integer id,ShareAuditDTO auditDTO,String transactionId){
        this.auditByIdInDB(id,auditDTO);
        this.rocketmqTransactionLogMapper.insertSelective(
                RocketmqTransactionLog.builder()
                .transactionId(transactionId)
                .log("审核分享...")
                .build()
        );
    }

    public PageInfo<Share> q(String title, Integer pageNo, Integer pageSize, Integer userId) {
        //表示我要分页了。。。
        //它会切入下面这条不分页的SQL,自动拼接分页的SQL
        PageHelper.startPage(pageNo,pageSize);
        List<Share> shares = this.shareMapper.selectByParam(title);
        List<Share> sharesDealed = new ArrayList<>();
        //1.如果用户未登录,那么downloadUrl全部设为null
        //2.如果用户登录了,那么查询一下mid_user_share,如果没有数据,那么这条数据share的downloadUrl全部设为null
        if(userId == null){
            sharesDealed =  shares.stream().peek(share -> {
                share.setDownloadUrl(null);
            }).collect(Collectors.toList());
        }else{
            sharesDealed = shares.stream().peek(share -> {
                MidUserShare midUserShare = this.midUserShareMapper.selectOne(
                        MidUserShare.builder()
                        .userId(userId)
                        .shareId(share.getId())
                        .build()
                );
                if(midUserShare == null){
                    share.setDownloadUrl(null);
                }
            }).collect(Collectors.toList());
        }

        return new PageInfo<Share>(sharesDealed);
    }

    public Share exchangeById(Integer id, HttpServletRequest request) {
        Object userId = request.getAttribute("id");
        Integer integerUserId = (Integer) userId;

        // 1. 根据id查询share，校验是否存在
        Share share = this.shareMapper.selectByPrimaryKey(id);
        if (share == null) {
            throw new IllegalArgumentException("该分享不存在！");
        }
        Integer price = share.getPrice();

        // 2. 如果当前用户已经兑换过该分享，则直接返回
        MidUserShare midUserShare = this.midUserShareMapper.selectOne(
                MidUserShare.builder()
                        .shareId(id)
                        .userId(integerUserId)
                        .build()
        );
        if (midUserShare != null) {
            return share;
        }

        // 3. 根据当前登录的用户id，查询积分是否够
        UserDTO userDTO = this.userCenterFeignClient.findById(integerUserId);
        if (price > userDTO.getBonus()) {
            throw new IllegalArgumentException("用户积分不够用！");
        }

        // 4. 扣减积分 & 往mid_user_share里插入一条数据
        this.userCenterFeignClient.addBonus(
                UserAddBonseDTO.builder()
                        .userId(integerUserId)
                        .bonus(0 - price)
                        .build()
        );
        this.midUserShareMapper.insert(
                MidUserShare.builder()
                        .userId(integerUserId)
                        .shareId(id)
                        .build()
        );
        return share;
    }
}
