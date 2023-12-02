package com.litian.dancechar.member.biz.integralrecords.controller;

import cn.hutool.core.util.StrUtil;
import com.litian.dancechar.framework.cache.redis.distributelock.annotation.Lock;
import com.litian.dancechar.framework.common.base.RespResult;
import com.litian.dancechar.framework.common.util.DCBeanUtil;
import com.litian.dancechar.framework.common.validator.ValidatorUtil;
import com.litian.dancechar.member.biz.integralrecords.dto.IntegralRecordsInfoRespDTO;
import com.litian.dancechar.member.biz.integralrecords.dto.IntegralRecordsReqDTO;
import com.litian.dancechar.member.biz.integralrecords.dto.IntegralRecordsSaveDTO;
import com.litian.dancechar.member.biz.integralrecords.service.IntegralRecordsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 会员积分流水业务处理
 *
 * @author tojson
 * @date 2022/7/9 06:26
 */
@Api(tags = "会员积分相关api")
@RestController
@Slf4j
@RequestMapping(value = "/member/integral/")
public class IntegraRecordsInfoController {
    @Resource
    private IntegralRecordsService integralInfoService;

    @ApiOperation(value = "根据业务Id-查询积分信息", notes = "根据业务Id-查询积分信息")
    @PostMapping("findByBusinessId")
    public RespResult<IntegralRecordsInfoRespDTO> findByBusinessId(@RequestBody IntegralRecordsReqDTO req) {
        if(StrUtil.hasBlank(req.getBusinessType(), req.getBusinessId())){
            return RespResult.error("businessType或businessId不能为空！");
        }
        return RespResult.success(DCBeanUtil.copyNotNull(new IntegralRecordsInfoRespDTO(), integralInfoService
                .findByCondition(req)));
    }

    @ApiOperation(value = "新增积分", notes = "新增积分")
    @PostMapping("add")
    @Lock(value = "#integralRecordsSaveDTO.mobile,#integralRecordsSaveDTO.businessType,#integralRecordsSaveDTO.businessId",
            lockFailMsg = "请勿重复提交", expireTime = 3000)
    public RespResult<String> add(@RequestBody IntegralRecordsSaveDTO integralRecordsSaveDTO) {
        log.info("新增保存积分....");
        ValidatorUtil.validate(integralRecordsSaveDTO);
        return integralInfoService.saveWithInsert(integralRecordsSaveDTO);
    }
}