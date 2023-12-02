package com.litian.dancechar.examples.transaction.actlottery.job;

import com.litian.dancechar.examples.transaction.actlottery.service.ActLotteryRecordsService;
import com.litian.dancechar.framework.cache.redis.distributelock.core.DistributeLockHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 积分补偿job(基于spring)
 *
 * @author tojson
 * @date 2022/8/21 23:25
 */
@Slf4j
@Configuration
@EnableScheduling
public class IntegralCompensateJob {
    @Resource
    private ActLotteryRecordsService actLotteryRecordsService;
    @Resource
    private DistributeLockHelper distributeLockHelper;

    private static final String INTEGRAL_COMPENSATE_LOCK = "integralCompensateLock";

    @Scheduled(cron = "0 0/1 * * * ?")
    public void integralCompensate() {
        if(!distributeLockHelper.tryLock(INTEGRAL_COMPENSATE_LOCK, TimeUnit.MINUTES, 1)){
            return;
        }
        try{
             actLotteryRecordsService.compensateIntegral();
        } catch (Exception e){
            log.error("积分补偿系统异常！errMsg：{}", e.getMessage(), e);
        } finally {
            distributeLockHelper.unlock(INTEGRAL_COMPENSATE_LOCK);
        }
    }
}
