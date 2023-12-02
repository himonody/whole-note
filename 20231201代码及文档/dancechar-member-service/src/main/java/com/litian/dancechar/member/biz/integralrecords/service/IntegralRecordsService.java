package com.litian.dancechar.member.biz.integralrecords.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.litian.dancechar.framework.common.base.RespResult;
import com.litian.dancechar.framework.common.util.DCBeanUtil;
import com.litian.dancechar.member.biz.integralrecords.dto.IntegralRecordsReqDTO;
import com.litian.dancechar.member.biz.integralrecords.dto.IntegralRecordsSaveDTO;
import com.litian.dancechar.member.biz.integralrecords.entity.IntegralRecordsDO;
import com.litian.dancechar.member.biz.integralrecords.inf.IntegralRecordsDao;
import com.litian.dancechar.member.enums.MemberRespResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 会员积分流水服务
 *
 * @author tojson
 * @date 2022/7/9 6:30
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class IntegralRecordsService extends ServiceImpl<IntegralRecordsDao, IntegralRecordsDO> {
    @Resource
    private IntegralRecordsDao integralRecordsDao;

    /**
     * 功能：根据条件-查询积分信息
     */
    public IntegralRecordsDO findByCondition(IntegralRecordsReqDTO reqDTO) {
        LambdaQueryWrapper<IntegralRecordsDO> lambda = new LambdaQueryWrapper<>();
        lambda.eq(IntegralRecordsDO::getBusinessType,reqDTO.getBusinessType());
        lambda.eq(IntegralRecordsDO::getBusinessId,reqDTO.getBusinessId());
        List<IntegralRecordsDO> integralRecordList = integralRecordsDao.selectList(lambda);
        return CollUtil.isNotEmpty(integralRecordList) ? integralRecordList.get(0) : null;
    }

    /**
     * 功能：新增积分
     */
    public RespResult<String> saveWithInsert(IntegralRecordsSaveDTO integralRecordsSaveDTO) {
        LambdaQueryWrapper<IntegralRecordsDO> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(IntegralRecordsDO::getMobile, integralRecordsSaveDTO.getMobile());
        lambdaQueryWrapper.eq(IntegralRecordsDO::getBusinessType, integralRecordsSaveDTO.getBusinessType());
        lambdaQueryWrapper.eq(IntegralRecordsDO::getBusinessId, integralRecordsSaveDTO.getBusinessId());
        List<IntegralRecordsDO> irList = integralRecordsDao.selectList(lambdaQueryWrapper);
        if(CollUtil.isNotEmpty(irList)){
            return RespResult.error(MemberRespResultCode.REPEAT_ADD_INTEGRAL, irList.get(0).getSerialNo());
        }
        IntegralRecordsDO integralRecordsDO = new IntegralRecordsDO();
        DCBeanUtil.copyNotNull(integralRecordsDO, integralRecordsSaveDTO);
        String recordNo = "I" + RandomUtil.randomNumbers(10);
        integralRecordsDO.setSerialNo(recordNo);
        save(integralRecordsDO);
        try{
            TimeUnit.SECONDS.sleep(4);
        }catch (Exception e){

        }
        return RespResult.success(integralRecordsDO.getSerialNo());
    }
}