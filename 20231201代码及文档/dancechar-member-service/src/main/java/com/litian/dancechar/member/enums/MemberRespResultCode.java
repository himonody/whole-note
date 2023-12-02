package com.litian.dancechar.member.enums;

import com.litian.dancechar.framework.common.base.IRespResultCode;

/**
 * member模块返回码对象枚举
 *
 * @author tojson
 * @date 2021/6/21 21:25
 */
public enum MemberRespResultCode implements IRespResultCode {
    /**
     * 不能重复加积分
     */
    REPEAT_ADD_INTEGRAL(200000, "不能重复加积分", "不能重复加积分"),
    /**
     * 渠道不能为空
     */
    ERR_SOURCE_NOT_EMPTY(200001, "渠道不能为空", "渠道不能为空");

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 详细的错误消息(开发看)
     */
    private String detailMessage;

    MemberRespResultCode(Integer code, String message, String detailMessage) {
        this.code = code;
        this.message = message;
        this.detailMessage = detailMessage;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getDetailMessage() {
        return detailMessage;
    }
}
