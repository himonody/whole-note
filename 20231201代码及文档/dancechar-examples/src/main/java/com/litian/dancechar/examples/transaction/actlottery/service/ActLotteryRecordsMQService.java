package com.litian.dancechar.examples.transaction.actlottery.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
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
import com.litian.dancechar.examples.transaction.actlottery.feign.dto.IntegralRecordsSaveDTO;
import com.litian.dancechar.examples.transaction.actlottery.feign.enums.OperateTypeEnum;
import com.litian.dancechar.examples.transaction.actlottery.mq.ProduceIntegralAddKafkaMsg;
import com.litian.dancechar.examples.transaction.transactionmsg.dao.entity.SysTransactionMsgDO;
import com.litian.dancechar.examples.transaction.transactionmsg.dto.SysTransactionMsgReqDTO;
import com.litian.dancechar.examples.transaction.transactionmsg.enums.TransactionStatusEnum;
import com.litian.dancechar.examples.transaction.transactionmsg.service.TransactionMsgService;
import com.litian.dancechar.framework.common.base.RespResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 活动抽奖信息服务
 *
 * @author tojson
 * @date 2022/7/9 6:30
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class ActLotteryRecordsMQService extends ServiceImpl<ActLotteryRecordsDao, ActLotteryRecordsDO> {
    @Resource
    private ActLotteryRecordsDao actLotteryRecordsDao;

    @Resource
    private TransactionMsgService transactionMsgService;

    @Resource
    private ProduceIntegralAddKafkaMsg produceIntegralAddKafkaMsg;

    /**
     * 功能：批量更新
     */
    public void updateActLotteryRecordsBatch(List<IntegralRecordsSaveDTO> recordList){
        Map<String, String> bussinessToSerialNoMap = recordList.stream().collect(Collectors.toMap(
                                                     IntegralRecordsSaveDTO::getBusinessId,
                                                     IntegralRecordsSaveDTO::getSerialNo));
        LambdaQueryWrapper<ActLotteryRecordsDO> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.in(ActLotteryRecordsDO::getRecordNo, bussinessToSerialNoMap.keySet());
        List<ActLotteryRecordsDO> acList = actLotteryRecordsDao.selectList(lambdaQueryWrapper);
        if(CollUtil.isNotEmpty(acList)){
            acList.forEach(ac->{
                ac.setItemNo(bussinessToSerialNoMap.get(ac.getRecordNo()));
                ac.setStatus(SendStatusEnum.SUCCESS.getCode());
            });
            this.saveOrUpdateBatch(acList);
        }
    }

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
        log.info("开始抽奖基于MQ!param:{}", JSONUtil.toJsonStr(actDrawLotteryReqDTO));
        // step1: 新增客户发放记录
        ActLotteryRecordsDO actLotteryRecordsDO = buildActLotteryRecordsDO(actDrawLotteryReqDTO);
        actLotteryRecordsDao.insert(actLotteryRecordsDO);
        IntegralRecordsSaveDTO integralInfoSaveDTO = buildIntegralRecordsSaveDTO(actLotteryRecordsDO);
        try{
            produceIntegralAddKafkaMsg.sendMessage(JSONUtil.toJsonStr(integralInfoSaveDTO));
        } catch (Exception e){
            log.error("积分服务加积分异常，基于MQ开启本地表补偿处理！errMsg:{}", e.getMessage(), e);
            // 网络抖动引起的异常，记录本地消息表，进行重试
            SysTransactionMsgDO sysTransactionMsgDO = new SysTransactionMsgDO();
            sysTransactionMsgDO.setBusinessType(BusinessTypeEnum.ACT_LOTTERY.getCode());
            sysTransactionMsgDO.setBusinessId(actLotteryRecordsDO.getRecordNo());
            sysTransactionMsgDO.setBusinessContent(JSONUtil.toJsonStr(integralInfoSaveDTO));
            sysTransactionMsgDO.setRemark("客户抽奖加积分异常");
            transactionMsgService.insertOrUpdate(sysTransactionMsgDO);
        }
        return RespResult.success(true);
    }

    /**
    @Transactional(rollbackFor = Exception.class)
    public RespResult<Boolean> drawLottery(ActDrawLotteryReqDTO actDrawLotteryReqDTO) {
        log.info("开始抽奖基于MQ!param:{}", JSONUtil.toJsonStr(actDrawLotteryReqDTO));
        // 新增客户发放记录
        ActLotteryRecordsDO actLotteryRecordsDO = buildActLotteryRecordsDO(actDrawLotteryReqDTO);
        actLotteryRecordsDao.insert(actLotteryRecordsDO);
        IntegralRecordsSaveDTO integralInfoSaveDTO = buildIntegralRecordsSaveDTO(actLotteryRecordsDO);
        // 本地消息表插入一条记录
        SysTransactionMsgDO sysTransactionMsgDO = new SysTransactionMsgDO();
        sysTransactionMsgDO.setBusinessType(BusinessTypeEnum.ACT_LOTTERY.getCode());
        sysTransactionMsgDO.setBusinessId(actLotteryRecordsDO.getRecordNo());
        sysTransactionMsgDO.setBusinessContent(JSONUtil.toJsonStr(integralInfoSaveDTO));
        transactionMsgService.insertOrUpdate(sysTransactionMsgDO);
        try{
            produceIntegralAddKafkaMsg.sendMessage(JSONUtil.toJsonStr(integralInfoSaveDTO));
            // 发送消息成功后，删除消息表数据
            sysTransactionMsgDO.setDeleteFlag(1);
            transactionMsgService.updateById(sysTransactionMsgDO);
            return RespResult.success(true);
        } catch (Exception e){
            log.error("积分服务加积分异常，基于MQ开启本地表补偿处理！errMsg:{}", e.getMessage(), e);
            throw new BusinessException("积分服务加积分异常");
        }
    }
    */

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
                produceIntegralAddKafkaMsg.sendMessage(sysTransactionMsg.getBusinessContent());
                // 更新补偿表为成功
                sysTransactionMsg.setMsgStatus(TransactionStatusEnum.SUCCESS.getCode());
                transactionMsgService.insertOrUpdate(sysTransactionMsg);
            } catch (Exception e){
                log.error("补偿积分消息异常！id:{},errMsg:{}",sysTransactionMsg.getId(), e.getMessage(),e);
            }
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