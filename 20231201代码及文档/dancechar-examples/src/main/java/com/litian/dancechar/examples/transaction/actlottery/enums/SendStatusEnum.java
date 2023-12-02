package com.litian.dancechar.examples.transaction.actlottery.enums;

/**
 * 发放状态枚举
 *
 * @author tojson
 * @date 2021/6/21 21:25
 */
public enum SendStatusEnum {
    /**
     * 1-发放中 2-成功 3、失败
     */
    DOING(1, "发放中"),
    /**
     * 成功
     */
    SUCCESS(2, "成功"),
    /**
     * 失败
     */
    FAIL(3, "失败");

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 描述信息
     */
    private String message;

    SendStatusEnum(Integer code, String message) {
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
