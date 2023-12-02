package com.litian.dancechar.base.biz.staff.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.litian.dancechar.base.biz.staff.dao.entity.StaffDO;
import com.litian.dancechar.base.biz.staff.dao.inf.StaffDao;
import com.litian.dancechar.base.biz.staff.dto.StaffRespDTO;
import com.litian.dancechar.base.common.constants.RedisKeyConstants;
import com.litian.dancechar.framework.cache.redis.util.RedisHelper;
import com.litian.dancechar.framework.common.base.RespResult;
import com.litian.dancechar.framework.common.util.DCBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 员工服务
 *
 * @author tojson
 * @date 2022/7/9 6:30
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class StaffService extends ServiceImpl<StaffDao, StaffDO> {
    @Resource
    private StaffDao staffDao;
    @Resource
    private RedisHelper redisHelper;

    public StaffRespDTO getByNo(String no){
        StaffDO employeeDO = redisHelper.getBean(RedisKeyConstants.Employee.EMPLOYEE_INFO_KEY +  no,
                StaffDO.class);
        if(ObjectUtil.isNull(employeeDO)){
            return null;
        }
        return DCBeanUtil.copyNotNull(new StaffRespDTO(), employeeDO);
    }

    /**
     * 将员工表db中的数据写入到redis(预计有100w左右)
     */
    public RespResult<Boolean> refreshDBToRedis(){
        int pageSize = 500;
        List<StaffDO> staffDOList = staffDao.findList("0L", pageSize);
        if(CollUtil.isEmpty(staffDOList)){
            log.warn("员工信息表没有记录！");
            return RespResult.success(true);
        }
        while (staffDOList.size() >= pageSize){
            addDataToRedis(staffDOList);
            String maxId = staffDOList.get(staffDOList.size()-1).getId();
            long start = System.currentTimeMillis();
            staffDOList = staffDao.findList(maxId , pageSize);
            log.info("本次db查询耗时:{}ms", System.currentTimeMillis()-start);
        }
        if(CollUtil.isNotEmpty(staffDOList)){
            addDataToRedis(staffDOList);
        }
        return RespResult.success(true);
    }

    private void addDataToRedis(List<StaffDO> employeeList){
        List<List<StaffDO>>  employeePartitionList = Lists.partition(employeeList, 500);
        employeePartitionList.forEach(partition->{
            Map<String, Object> map = Maps.newHashMap();
            partition.forEach(employeeDO -> {
                map.put(RedisKeyConstants.Employee.EMPLOYEE_INFO_KEY + employeeDO.getNo(), employeeDO);
            });
            // 通过redis pipeline提升系统性能，sleep指定时间防止redis cpu飙高
            redisHelper.executeAsyncPipeLinedSetString(map, 24*3600L, TimeUnit.SECONDS);
            try{
                Thread.sleep(300L);
            }catch (Exception e){
                log.error(e.getMessage() ,e);
            }
        });
    }
}