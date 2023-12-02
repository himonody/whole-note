package com.litian.dancechar.member.biz.integralrecords.inf;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.litian.dancechar.member.biz.integralrecords.dto.IntegralRecordsReqDTO;
import com.litian.dancechar.member.biz.integralrecords.dto.IntegralRecordsInfoRespDTO;
import com.litian.dancechar.member.biz.integralrecords.entity.IntegralRecordsDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * 会员积分记录Dao
 *
 * @author tojson
 * @date 2022/7/9 6:30
 */
@Mapper
public interface IntegralRecordsDao extends BaseMapper<IntegralRecordsDO> {

    List<IntegralRecordsInfoRespDTO> findList(IntegralRecordsReqDTO req);
}