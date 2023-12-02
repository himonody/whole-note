package com.litian.dancechar.examples.transaction.actlottery.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 抽奖活动请求对象
 *
 * @author tojson
 * @date 2022/7/9 06:18
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ActDrawLotteryReqDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 手机号
     */
    @NotBlank(message = "mobile不能为空")
    private String mobile;

    /**
     * 活动编码
     */
    @NotBlank(message = "actNo不能为空")
    private String actNo;

    /**
     * 发放记录的流水号
     */
    private String recordNo;

    /**
     * 物品类型
     */
    private String itemType;

    /**
     * 物品流水号
     */
    private Integer itemNo;

    /**
     * 是否采用mq发送
     */
    private Boolean sendMQ = false;
}