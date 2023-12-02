package com.litian.dancechar.examples.transaction.actlottery.enums;

/**
 * 业务类型枚举
 *
 * @author tojson
 * @date 2021/6/21 21:25
 */
public enum BusinessTypeEnum {
    /**
     * 活动抽奖
     */
    ACT_LOTTERY("actLottery", "活动抽奖");

    /**
     * 状态码
     */
    private String code;

    /**
     * 描述信息
     */
    private String message;

    BusinessTypeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
