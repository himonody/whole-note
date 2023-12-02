package com.litian.dancechar.examples.transaction.actlottery.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.litian.dancechar.framework.common.mybatis.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 活动抽奖信息DO
 *
 * @author tojson
 * @date 2022/9/5 06:18
 */
@Data
@TableName("act_lottery_records")
@EqualsAndHashCode(callSuper = false)
public class ActLotteryRecordsDO  extends BaseDO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private String id;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 活动编码
     */
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
    private String itemNo;

    /**
     * 发放状态
     */
    private Integer status;
}