package com.litian.dancechar.examples.transaction.transactionmsg.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.litian.dancechar.examples.transaction.transactionmsg.dao.entity.SysTransactionMsgDO;
import com.litian.dancechar.examples.transaction.transactionmsg.dao.inf.SysTransactionMsgDao;
import com.litian.dancechar.examples.transaction.transactionmsg.dto.SysTransactionMsgReqDTO;
import com.litian.dancechar.examples.transaction.transactionmsg.dto.SysTransactionMsgRespDTO;
import com.litian.dancechar.examples.transaction.transactionmsg.enums.TransactionStatusEnum;
import com.litian.dancechar.framework.common.base.PageWrapperDTO;
import com.litian.dancechar.framework.common.base.RespResult;
import com.litian.dancechar.framework.common.util.PageResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 事务补偿消息服务
 *
 * @author tojson
 * @date 2022/7/9 6:30
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class TransactionMsgService extends ServiceImpl<SysTransactionMsgDao, SysTransactionMsgDO> {
    @Resource
    private SysTransactionMsgDao sysTransactionMsgDao;

    /**
     * 功能: 分页查询事务补偿消息列表
     */
    public RespResult<PageWrapperDTO<SysTransactionMsgRespDTO>> listPaged(SysTransactionMsgReqDTO req) {
        PageHelper.startPage(req.getPageNo(), req.getPageSize());
        return RespResult.success(PageResultUtil.setPageResult(sysTransactionMsgDao.findList(req), SysTransactionMsgRespDTO.class));
    }

    /**
     * 功能：根据Id-查询事务补偿消息
     */
    public SysTransactionMsgDO findById(String id) {
        return sysTransactionMsgDao.selectById(id);
    }

    /**
     * 功能：查询事务补偿消息列表
     */
    public List<SysTransactionMsgDO> findList(SysTransactionMsgReqDTO req) {
        return sysTransactionMsgDao.findList(req);
    }

    /**
     * 功能：新增或更新事务补偿消息
     */
    public void insertOrUpdate(SysTransactionMsgDO sysTransactionMsgDO) {
        if(StrUtil.hasBlank(sysTransactionMsgDO.getBusinessType(), sysTransactionMsgDO.getBusinessId())){
            return;
        }
        // 新增操作
        if(StrUtil.isEmpty(sysTransactionMsgDO.getId())){
            sysTransactionMsgDO.setMsgStatus(TransactionStatusEnum.IS_DOING.getCode());
            sysTransactionMsgDO.setRetryTimes(0);
            sysTransactionMsgDO.setMaxRetryTimes(5);
            sysTransactionMsgDao.insert(sysTransactionMsgDO);
            return;
        }
        // 修改操作
        int retryTimes = Convert.toInt(sysTransactionMsgDO.getRetryTimes(), 0)+1;
        int maxRetryTimes = Convert.toInt(sysTransactionMsgDO.getMaxRetryTimes(), 5);
        if(TransactionStatusEnum.FAIL.getCode().equals(sysTransactionMsgDO.getMsgStatus()) && (retryTimes > maxRetryTimes)){
            retryTimes = maxRetryTimes;
        }
        sysTransactionMsgDO.setRetryTimes(retryTimes);
        if(retryTimes == 6){
            sysTransactionMsgDO.setMsgStatus(TransactionStatusEnum.FAIL.getCode());
        }
        if(TransactionStatusEnum.SUCCESS.getCode().equals(sysTransactionMsgDO.getMsgStatus())){
            sysTransactionMsgDO.setDeleteFlag(1);
        }
        sysTransactionMsgDao.updateById(sysTransactionMsgDO);
    }

}