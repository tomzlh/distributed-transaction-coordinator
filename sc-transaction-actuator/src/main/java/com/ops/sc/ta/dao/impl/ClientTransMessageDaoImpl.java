package com.ops.sc.ta.dao.impl;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.SQLException;


import com.ops.sc.common.enums.ClientErrorCode;
import com.ops.sc.common.exception.ScClientException;
import com.ops.sc.common.store.ScTransRecord;
import com.ops.sc.common.enums.BootMode;
import com.ops.sc.common.exception.ScMessageException;
import com.ops.sc.common.model.CommonTransMessage;
import com.ops.sc.ta.dao.ClientTransMessageDao;
import com.ops.sc.ta.dao.LogDao;
import com.ops.sc.ta.util.ScTransMessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class ClientTransMessageDaoImpl implements ClientTransMessageDao {

    @Resource(name = "scLogDao")
    private LogDao scLogDao;

    public void save(CommonTransMessage commonTransMessage) {
        try {
            scLogDao.insert(ScTransMessageBuilder.transferToLog(commonTransMessage));
        } catch (SQLException e) {
            throw new ScMessageException(ClientErrorCode.LOCAL_DATABASE_FAILED, e);
        }
        catch (ScClientException e) {
            throw new ScMessageException(ClientErrorCode.LOCAL_DATABASE_FAILED, e);
        }
    }

    public void updateStatusByTidBranchId(CommonTransMessage commonTransMessage) {
        try {
            scLogDao.updateStatus(commonTransMessage.getTid(), commonTransMessage.getBid(),
                    commonTransMessage.getStatus());
        } catch (SQLException e) {
            throw new ScMessageException(ClientErrorCode.LOCAL_DATABASE_FAILED, e);
        }
    }

    @Override
    public void delete(Long tid, Long bid) {
        try {
            scLogDao.delete(tid, bid);
        } catch (SQLException e) {
            throw new ScMessageException(ClientErrorCode.LOCAL_DATABASE_FAILED, e);
        }
    }

    public CommonTransMessage findScMessage(Long tid, Long bid) {
        try {
            ScTransRecord globalTransLog = scLogDao.findByTidAndBranchId(tid, bid);
            return globalTransLog == null ? null : ScTransMessageBuilder.transferFromLog(globalTransLog);
        } catch (SQLException e) {
            throw new ScMessageException(ClientErrorCode.LOCAL_DATABASE_FAILED, e);
        }
    }

}
