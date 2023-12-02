package com.litian.dancechar.examples.transaction.actlottery.mq;

import com.litian.dancechar.examples.transaction.actlottery.constants.CommConstants;
import com.litian.dancechar.framework.kafka.util.KafkaProducerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 生产添加积分消息
 *
 * @author tojson
 * @date 2022/9/18 23:13
 */
@Slf4j
@Component
public class ProduceIntegralAddKafkaMsg {
    @Resource
    private KafkaProducerUtil kafkaProducerUtil;

    /**
     * 发送新增积分
     */
    public void sendMessage(String data){
        kafkaProducerUtil.sendMessage(CommConstants.KafkaTopic.TOPIC_ADD_INTEGRAL, data);
    }
}
