package com.litian.dancechar.member.biz.integralrecords.mq;

import com.litian.dancechar.framework.kafka.util.KafkaProducerUtil;
import com.litian.dancechar.member.common.constants.CommConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 生产积分新增完成消息
 *
 * @author tojson
 * @date 2022/9/18 23:13
 */
@Slf4j
@Component
public class ProduceIntegralFinishKafkaMsg {
    @Resource
    private KafkaProducerUtil kafkaProducerUtil;

    /**
     * 发送积分新增完成
     */
    public void sendMessage(String data){
        kafkaProducerUtil.sendMessage(CommConstants.KafkaTopic.TOPIC_INTEGRAL_FINISH, data);
    }
}
