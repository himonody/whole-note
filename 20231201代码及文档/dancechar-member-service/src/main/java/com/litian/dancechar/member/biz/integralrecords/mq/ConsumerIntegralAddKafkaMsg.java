package com.litian.dancechar.member.biz.integralrecords.mq;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.litian.dancechar.framework.common.base.RespResult;
import com.litian.dancechar.member.biz.integralrecords.dto.IntegralRecordsSaveDTO;
import com.litian.dancechar.member.biz.integralrecords.service.IntegralRecordsService;
import com.litian.dancechar.member.common.constants.CommConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * 消费新增积分消息
 *
 * @author tojson
 * @date 2022/9/18 23:13
 */
@Slf4j
@Component
public class ConsumerIntegralAddKafkaMsg {
    @Resource
    private IntegralRecordsService integralRecordsService;
    @Resource
    private ProduceIntegralFinishKafkaMsg produceIntegralFinishKafkaMsg;

    @KafkaListener(groupId="dancechar-member-service", topics = {CommConstants.KafkaTopic.TOPIC_ADD_INTEGRAL})
    public void consumerCustomerInfo(String data, Acknowledgment ack) {
        log.info("消费新增积分消息！data:{}", data);
        IntegralRecordsSaveDTO integralRecordsSaveDTO = JSON.parseObject(data, IntegralRecordsSaveDTO.class);
        RespResult<String> respResult =  integralRecordsService.saveWithInsert(integralRecordsSaveDTO);
        if(respResult.isOk()){
            integralRecordsSaveDTO.setSerialNo(respResult.getData());
        }
        produceIntegralFinishKafkaMsg.sendMessage(JSONUtil.toJsonStr(integralRecordsSaveDTO));
        ack.acknowledge();
    }

    /**
     * 逻辑确认完成后，手工确认消息containerFactory = "batchFactory"
     */
    /**
    @KafkaListener(groupId="dancechar-member-service", topics = {CommConstants.KafkaTopic.TOPIC_ADD_INTEGRAL})
    public void consumerCustomerInfo(List<ConsumerRecord<?,?>> consumerRecords, Acknowledgment ack) {
        log.info("消费新增积分消息大小！size:{}", consumerRecords.size());
        if(CollUtil.isEmpty(consumerRecords)){
            return;
        }
        List<IntegralRecordsSaveDTO> recordNoList = Lists.newArrayList();
        for(ConsumerRecord<?,?> record : consumerRecords) {
            String value = (String)record.value();
            IntegralRecordsSaveDTO integralRecordsSaveDTO = JSON.parseObject(value, IntegralRecordsSaveDTO.class);
            recordNoList.add(integralRecordsSaveDTO);
        }
        if(CollUtil.isEmpty(recordNoList)){
            return;
        }
        for(IntegralRecordsSaveDTO integralRecordsSaveDTO : recordNoList){
            RespResult<String> respResult =  integralRecordsService.saveWithInsert(integralRecordsSaveDTO);
            if(respResult.isOk()){
                integralRecordsSaveDTO.setSerialNo(respResult.getData());
            }
            produceIntegralFinishKafkaMsg.sendMessage(JSONUtil.toJsonStr(integralRecordsSaveDTO));
        }
        ack.acknowledge();
    }
    */
}
