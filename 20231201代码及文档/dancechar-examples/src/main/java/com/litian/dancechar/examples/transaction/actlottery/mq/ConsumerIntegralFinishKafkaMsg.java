package com.litian.dancechar.examples.transaction.actlottery.mq;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.litian.dancechar.examples.transaction.actlottery.constants.CommConstants;
import com.litian.dancechar.examples.transaction.actlottery.dao.entity.ActLotteryRecordsDO;
import com.litian.dancechar.examples.transaction.actlottery.enums.SendStatusEnum;
import com.litian.dancechar.examples.transaction.actlottery.feign.dto.IntegralRecordsSaveDTO;
import com.litian.dancechar.examples.transaction.actlottery.service.ActLotteryRecordsMQService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * 消费积分完成消息
 *
 * @author tojson
 * @date 2022/9/18 23:13
 */
@Slf4j
@Component
public class ConsumerIntegralFinishKafkaMsg {
    @Resource
    private ActLotteryRecordsMQService actLotteyRecordsMQService;

    @KafkaListener(groupId="dancechar-examples", topics = {CommConstants.KafkaTopic.TOPIC_INTEGRAL_FINISH})
    public void consumerCustomerInfo(String data, Acknowledgment ack) {
        log.info("消费积分完成消息！data:{}", data);
        IntegralRecordsSaveDTO integralRecordsSaveDTO = JSON.parseObject(data, IntegralRecordsSaveDTO.class);
        if(integralRecordsSaveDTO != null){
            ActLotteryRecordsDO actLotteryRecordsDO = actLotteyRecordsMQService.findByRecordNo(
                    integralRecordsSaveDTO.getBusinessId());
            if(ObjectUtil.isNotNull(actLotteryRecordsDO)){
                if(StrUtil.isEmpty(integralRecordsSaveDTO.getSerialNo())){
                    actLotteryRecordsDO.setStatus(SendStatusEnum.FAIL.getCode());
                }else{
                    actLotteryRecordsDO.setStatus(SendStatusEnum.SUCCESS.getCode());
                }
                actLotteryRecordsDO.setItemNo(integralRecordsSaveDTO.getSerialNo());
                actLotteyRecordsMQService.updateById(actLotteryRecordsDO);
            }
        }
        ack.acknowledge();
    }



    /**
     * 逻辑确认完成后，手工确认消息containerFactory = "batchFactory"
     */
    /**
    @KafkaListener(groupId="dancechar-examples", topics = {CommConstants.KafkaTopic.TOPIC_INTEGRAL_FINISH})
    public void consumerCustomerInfo(List<ConsumerRecord<?,?>> consumerRecords, Acknowledgment ack) {
        log.info("消费积分完成消息大小！size:{}", consumerRecords.size());
        if(CollUtil.isEmpty(consumerRecords)){
            return;
        }
        for(ConsumerRecord<?,?> record : consumerRecords) {
            String value = (String)record.value();
            IntegralRecordsSaveDTO integralRecordsSaveDTO = JSON.parseObject(value, IntegralRecordsSaveDTO.class);
            if(integralRecordsSaveDTO != null){
                ActLotteryRecordsDO actLotteryRecordsDO = actLotteyRecordsMQService.findByRecordNo(
                                                          integralRecordsSaveDTO.getBusinessId());
                if(ObjectUtil.isNotNull(actLotteryRecordsDO)){
                    if(StrUtil.isEmpty(integralRecordsSaveDTO.getSerialNo())){
                        actLotteryRecordsDO.setStatus(SendStatusEnum.FAIL.getCode());
                    }else{
                        actLotteryRecordsDO.setStatus(SendStatusEnum.SUCCESS.getCode());
                    }
                    actLotteryRecordsDO.setItemNo(integralRecordsSaveDTO.getSerialNo());
                    actLotteyRecordsMQService.updateById(actLotteryRecordsDO);
                }
            }
        }
        ack.acknowledge();
    }
    */
}
