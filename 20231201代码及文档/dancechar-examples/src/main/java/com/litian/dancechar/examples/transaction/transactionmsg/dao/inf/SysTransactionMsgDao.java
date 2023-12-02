package com.litian.dancechar.examples.transaction.transactionmsg.dao.inf;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.litian.dancechar.examples.transaction.transactionmsg.dao.entity.SysTransactionMsgDO;
import com.litian.dancechar.examples.transaction.transactionmsg.dto.SysTransactionMsgReqDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * 本地事务消息Dao
 *
 * @author tojson
 * @date 2022/7/9 6:30
 */
@Mapper
public interface SysTransactionMsgDao extends BaseMapper<SysTransactionMsgDO> {

    List<SysTransactionMsgDO> findList(SysTransactionMsgReqDTO req);
}