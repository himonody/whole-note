package com.litian.dancechar.examples.transaction.actlottery.controller;

import com.litian.dancechar.examples.transaction.actlottery.dto.ActDrawLotteryReqDTO;
import com.litian.dancechar.examples.transaction.actlottery.service.ActLotteryRecordsMQService;
import com.litian.dancechar.examples.transaction.actlottery.service.ActLotteryRecordsService;
import com.litian.dancechar.framework.cache.redis.distributelock.annotation.Lock;
import com.litian.dancechar.framework.common.base.RespResult;
import com.litian.dancechar.framework.common.validator.ValidatorUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 活动抽奖业务处理
 *
 * @author tojson
 * @date 2022/7/9 11:26
 */
@Api(tags = "活动抽奖相关api")
@RestController
@Slf4j
@RequestMapping(value = "/act/lottery")
public class ActLotteryController {
    @Resource
    private ActLotteryRecordsService actLotteryRecordsService;
    @Resource
    private ActLotteryRecordsMQService actLotteryRecordsMQService;


    @ApiOperation(value = "抽奖", notes = "抽奖")
    @PostMapping("drawLottery")
    @Lock(value = "#reqDTO.mobile,#reqDTO.actNo", lockFailMsg = "请勿重复提交", expireTime = 3000)
    public RespResult<Boolean> drawLottery(@RequestBody ActDrawLotteryReqDTO reqDTO){
        ValidatorUtil.validate(reqDTO);
        return actLotteryRecordsService.drawLottery(reqDTO);
    }

    @ApiOperation(value = "抽奖(基于MQ)", notes = "抽奖(基于MQ)")
    @PostMapping("drawLotteryWithMQ")
    @Lock(value = "#reqDTO.mobile,#reqDTO.actNo", lockFailMsg = "请勿重复提交", expireTime = 3000)
    public RespResult<Boolean> drawLotteryWithMQ(@RequestBody ActDrawLotteryReqDTO reqDTO){
        ValidatorUtil.validate(reqDTO);
        return actLotteryRecordsMQService.drawLottery(reqDTO);
    }
}