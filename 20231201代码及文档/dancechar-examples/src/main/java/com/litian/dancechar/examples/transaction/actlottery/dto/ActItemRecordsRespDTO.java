package com.litian.dancechar.examples.transaction.actlottery.dto;

import com.litian.dancechar.framework.common.base.BaseRespDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 活动领取返回对象
 *
 * @author tojson
 * @date 2022/7/9 06:18
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ActItemRecordsRespDTO extends BaseRespDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private String id;

    /**
     * 客户Id
     */
    private String customerId;

    /**
     * 活动Id
     */
    private String actId;

    /**
     * 活动编码
     */
    private String actNo;

    /**
     * 任务Id
     */
    private String taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 物品流水号
     */
    private String itemSerialNo;

    /**
     * 操作物品类型
     */
    private String operateItemType;

    /**
     * 操作物品数量
     */
    private Integer operateItemNum;
}