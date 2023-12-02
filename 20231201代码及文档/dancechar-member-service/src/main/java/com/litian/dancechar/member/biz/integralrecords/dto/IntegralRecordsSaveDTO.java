package com.litian.dancechar.member.biz.integralrecords.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 会员积分保存对象
 *
 * @author tojson
 * @date 2022/9/6 11:18
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class IntegralRecordsSaveDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 手机号
     */
    @NotBlank(message = "mobile不能为空")
    private String mobile;

    /**
     * 业务类型
     */
    @NotBlank(message = "businessType不能为空")
    private String businessType;

    /**
     * 业务唯一id(主键或code)
     */
    @NotBlank(message = "businessId不能为空")
    private String businessId;

    /**
     * 积分流水号
     */
    private String serialNo;

    /**
     * 操作类型，0:加积分 1:扣减积分
     */
    private Integer operateType;

    /**
     * 操作数量
     */
    private Integer operateNum;
}