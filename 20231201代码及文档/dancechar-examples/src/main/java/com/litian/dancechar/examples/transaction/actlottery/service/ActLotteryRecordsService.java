package com.litian.dancechar.examples.transaction.actlottery.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.litian.dancechar.examples.transaction.actlottery.dao.entity.ActLotteryRecordsDO;
import com.litian.dancechar.examples.transaction.actlottery.dao.inf.ActLotteryRecordsDao;
import com.litian.dancechar.examples.transaction.actlottery.dto.ActDrawLotteryReqDTO;
import com.litian.dancechar.examples.transaction.actlottery.enums.BusinessTypeEnum;
import com.litian.dancechar.examples.transaction.actlottery.enums.ItemTypeEnum;
import com.litian.dancechar.examples.transaction.actlottery.enums.SendStatusEnum;
import com.litian.dancechar.examples.transaction.actlottery.feign.client.IntegralClient;
import com.litian.dancechar.examples.transaction.actlottery.feign.dto.IntegralLogInfoReqDTO;
import com.litian.dancechar.examples.transaction.actlottery.feign.dto.IntegralLogInfoRespDTO;
import com.litian.dancechar.examples.transaction.actlottery.feign.dto.IntegralRecordsSaveDTO;
import com.litian.dancechar.examples.transaction.actlottery.feign.enums.OperateTypeEnum;
import com.litian.dancechar.examples.transaction.transactionmsg.dao.entity.SysTransactionMsgDO;
import com.litian.dancechar.examples.transaction.transactionmsg.dto.SysTransactionMsgReqDTO;
import com.litian.dancechar.examples.transaction.transactionmsg.enums.TransactionStatusEnum;
import com.litian.dancechar.examples.transaction.transactionmsg.service.TransactionMsgService;
import com.litian.dancechar.framework.common.base.RespResult;
import com.litian.dancechar.framework.common.exception.BusinessException;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 活动抽奖信息服务
 *
 * @author tojson
 * @date 2022/7/9 6:30
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class ActLotteryRecordsService extends ServiceImpl<ActLotteryRecordsDao, ActLotteryRecordsDO> {
    @Resource
    private ActLotteryRecordsDao actLotteryRecordsDao;
    @Resource
    private IntegralClient integralClient;
    @Resource
    private TransactionMsgService transactionMsgService;

    /**
     * 功能：根据发放记录的流水号-查询抽奖记录
     */
    public ActLotteryRecordsDO findByRecordNo(String recordNo){
        LambdaQueryWrapper<ActLotteryRecordsDO> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(ActLotteryRecordsDO::getRecordNo, recordNo);
        List<ActLotteryRecordsDO> acList = actLotteryRecordsDao.selectList(lambdaQueryWrapper);
        if(CollUtil.isNotEmpty(acList)){
            return acList.get(0);
        }
        return null;
    }

    /**
     * 功能：抽奖
     */
    @Transactional(rollbackFor = Exception.class)
    public RespResult<Boolean> drawLottery(ActDrawLotteryReqDTO actDrawLotteryReqDTO) {
        actLotteryRecordsDao.selectById(1);
        log.info("开始抽奖!param:{}", JSONUtil.toJsonStr(actDrawLotteryReqDTO));
        // step1: 新增客户发放记录
        ActLotteryRecordsDO actLotteryRecordsDO = buildActLotteryRecordsDO(actDrawLotteryReqDTO);
        actLotteryRecordsDao.insert(actLotteryRecordsDO);
        IntegralRecordsSaveDTO integralInfoSaveDTO = buildIntegralRecordsSaveDTO(actLotteryRecordsDO);
        try{
            // 通过http调用积分服务，给客户加积分
            RespResult<String> integralR = integralClient.add(integralInfoSaveDTO);
            if(integralR.isNotOk()){
                // 抛出运行时异常，回滚事务
                log.error("扣减积分失败,回滚事务！");
                throw new BusinessException("扣减积分失败");
            }
            // 更新客户发放记录(积分流水号)
            if(StrUtil.isNotEmpty(integralR.getData())){
                actLotteryRecordsDO.setStatus(SendStatusEnum.SUCCESS.getCode());
                actLotteryRecordsDO.setItemNo(integralR.getData());
                actLotteryRecordsDao.updateById(actLotteryRecordsDO);
            }
        } catch (RetryableException e){
            log.error("积分服务加积分异常，开启本地表补偿处理！errMsg:{}", e.getMessage(), e);
            // 网络抖动引起的异常，记录本地消息表，进行重试
            SysTransactionMsgDO sysTransactionMsgDO = new SysTransactionMsgDO();
            sysTransactionMsgDO.setBusinessType(BusinessTypeEnum.ACT_LOTTERY.getCode());
            sysTransactionMsgDO.setBusinessId(actLotteryRecordsDO.getRecordNo());
            sysTransactionMsgDO.setBusinessContent(JSONUtil.toJsonStr(integralInfoSaveDTO));
            sysTransactionMsgDO.setRemark("客户抽奖加积分异常");
            transactionMsgService.insertOrUpdate(sysTransactionMsgDO);
        } catch (Exception e){
            // 除非超时异常，其他异常都抛异常回滚
            log.error("扣减积分失败！", e);
            throw new BusinessException("扣减积分失败");
        }
        return RespResult.success(true);
    }

    /**
     * 功能：补偿积分
     */
    @Transactional(rollbackFor = Exception.class)
    public void compensateIntegral(){
        SysTransactionMsgReqDTO sysTransactionMsgReqDTO = new SysTransactionMsgReqDTO();
        sysTransactionMsgReqDTO.setBusinessType(BusinessTypeEnum.ACT_LOTTERY.getCode());
        List<SysTransactionMsgDO>  transactionMsgList = transactionMsgService.findList(sysTransactionMsgReqDTO);
        if(CollUtil.isEmpty(transactionMsgList)){
            return;
        }
        for(SysTransactionMsgDO sysTransactionMsg : transactionMsgList){
            try {
                // 如果业务表没有值，清理补偿消息表记录
                ActLotteryRecordsDO actLotteryRecordsDO = this.findByRecordNo(sysTransactionMsg.getBusinessId());
                if(ObjectUtil.isNull(actLotteryRecordsDO)){
                    sysTransactionMsg.setDeleteFlag(1);
                    transactionMsgService.updateById(sysTransactionMsg);
                    continue;
                }
                // 查询下游是否已经加过积分
                IntegralLogInfoReqDTO integralLogInfoReqDTO = new IntegralLogInfoReqDTO();
                integralLogInfoReqDTO.setBusinessType(sysTransactionMsg.getBusinessType());
                integralLogInfoReqDTO.setBusinessId(sysTransactionMsg.getBusinessId());
                RespResult<IntegralLogInfoRespDTO>  isR = integralClient.findByBusinessId(integralLogInfoReqDTO);
                if(isR.isNotOk()){
                    continue;
                }
                if(ObjectUtil.isNotNull(isR.getData())){
                    // 说明之前调用新增积分是成功的，这个时候只需要更新客户发送记录和消息补偿表
                    actLotteryRecordsDO.setItemNo(isR.getData().getSerialNo());
                    actLotteryRecordsDO.setStatus(SendStatusEnum.SUCCESS.getCode());
                    this.updateById(actLotteryRecordsDO);
                    // 更新补偿表为成功
                    sysTransactionMsg.setMsgStatus(TransactionStatusEnum.SUCCESS.getCode());
                    transactionMsgService.insertOrUpdate(sysTransactionMsg);
                } else{
                    // 补偿积分记录
                    try{
                        RespResult<String> integralR = integralClient.add(JSONUtil.toBean(sysTransactionMsg.getBusinessContent(), IntegralRecordsSaveDTO.class));
                        addIntegral(integralR, actLotteryRecordsDO, sysTransactionMsg);
                    } catch (RetryableException e){
                        // 补偿积分超时，更新消息表
                        transactionMsgService.insertOrUpdate(sysTransactionMsg);
                    }
                }
            } catch (Exception e){
                log.error("补偿积分消息异常！id:{},errMsg:{}",sysTransactionMsg.getId(), e.getMessage(),e);
            }
        }
    }

    private void addIntegral(RespResult<String> integralR,ActLotteryRecordsDO actLotteryRecordsDO,
                             SysTransactionMsgDO sysTransactionMsg){
        if(integralR.isOk()){
            // 更新客户发放记录成功
            actLotteryRecordsDO.setItemNo(integralR.getData());
            actLotteryRecordsDO.setStatus(SendStatusEnum.SUCCESS.getCode());
            this.updateById(actLotteryRecordsDO);
            // 更新补偿表为成功
            sysTransactionMsg.setMsgStatus(TransactionStatusEnum.SUCCESS.getCode());
            transactionMsgService.insertOrUpdate(sysTransactionMsg);
        } else if(integralR.getCode() == 200000){
            // 说明之前调用新增积分是成功的，这个时候只需要更新客户发送记录和消息补偿表
            actLotteryRecordsDO.setItemNo(integralR.getData());
            actLotteryRecordsDO.setStatus(SendStatusEnum.SUCCESS.getCode());
            this.updateById(actLotteryRecordsDO);
            // 更新补偿表为成功
            sysTransactionMsg.setMsgStatus(TransactionStatusEnum.SUCCESS.getCode());
            transactionMsgService.insertOrUpdate(sysTransactionMsg);
        } else {
            // 更新客户发放记录失败
            actLotteryRecordsDO.setItemNo(integralR.getData());
            actLotteryRecordsDO.setStatus(SendStatusEnum.FAIL.getCode());
            this.updateById(actLotteryRecordsDO);
            sysTransactionMsg.setMsgStatus(TransactionStatusEnum.FAIL.getCode());
            sysTransactionMsg.setRemark(sysTransactionMsg.getRemark()+integralR.getMessage());
            transactionMsgService.insertOrUpdate(sysTransactionMsg);
        }
    }

    private IntegralRecordsSaveDTO buildIntegralRecordsSaveDTO(ActLotteryRecordsDO actLotteryRecordsDO){
        IntegralRecordsSaveDTO integralRecordsSaveDTO = new IntegralRecordsSaveDTO();
        integralRecordsSaveDTO.setMobile(actLotteryRecordsDO.getMobile());
        integralRecordsSaveDTO.setBusinessType(BusinessTypeEnum.ACT_LOTTERY.getCode());
        integralRecordsSaveDTO.setBusinessId(actLotteryRecordsDO.getRecordNo());
        integralRecordsSaveDTO.setOperateType(OperateTypeEnum.ADD.getCode());
        integralRecordsSaveDTO.setOperateNum(5);
        return integralRecordsSaveDTO;
    }

    private ActLotteryRecordsDO buildActLotteryRecordsDO(ActDrawLotteryReqDTO actDrawLotteryReqDTO){
        ActLotteryRecordsDO actLotteryRecordsDO = new ActLotteryRecordsDO();
        actLotteryRecordsDO.setMobile(actDrawLotteryReqDTO.getMobile());
        actLotteryRecordsDO.setActNo(actDrawLotteryReqDTO.getActNo());
        // 生成流水号(这里根据业务的规则生成)
        String recordNo = "AR" + RandomUtil.randomNumbers(10);
        actLotteryRecordsDO.setRecordNo(recordNo);
        actLotteryRecordsDO.setItemType(ItemTypeEnum.INTEGRAL.getCode());
        actLotteryRecordsDO.setStatus(SendStatusEnum.DOING.getCode());
        return actLotteryRecordsDO;
    }
}