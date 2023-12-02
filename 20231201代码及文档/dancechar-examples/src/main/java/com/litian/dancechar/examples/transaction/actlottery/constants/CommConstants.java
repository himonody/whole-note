package com.litian.dancechar.examples.transaction.actlottery.constants;

/**
 * 公共的常量类
 *
 * @author tojson
 * @date 2022/7/28 10:33
 */
public class CommConstants {
    /**
     * redis防穿透默认空值
     */
    public static final String REDIS_NULL_DEFAULT_VALUE = "empty";

    /**
     * redis防穿，默认时间为2分钟
     */
    public static final long REDIS_NULL_DEFAULT_time = 120000L;

    /**
     * kafka信息
     */
    public static class KafkaTopic{
        /**
         * 积分新增主题信息
         */
        public static final String TOPIC_ADD_INTEGRAL = "topic_add_integral";
        /**
         * 积分处理完成主题信息
         */
        public static final String TOPIC_INTEGRAL_FINISH = "topic_integral_finished";
    }
}
