package com.litian.dancechar.examples.transaction.actlottery.feign.enums;

/**
 * 业务类型枚举
 *
 * @author tojson
 * @date 2021/6/21 21:25
 */
public enum OperateTypeEnum {
    /**
     * 加积分
     */
    ADD(1, "加积分"),
    /**
     * 添加积分
     */
    REDUCE(2, "减积分");

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 描述信息
     */
    private String message;

    OperateTypeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
