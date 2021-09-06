package com.ops.sc.tc.lb;

import com.ops.sc.common.enums.TransactionResponseCode;
import com.ops.sc.common.exception.ScTransactionException;
import com.ops.sc.tc.service.SponsorInitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service("serverSelector")
public class ServerSelector {

    @Autowired
    private SponsorInitService sponsorInitService;

    public String getServerAddress() {
        List<String> serverList = sponsorInitService.getServerList();
        if(serverList!=null&& !serverList.isEmpty()) {
            return serverList.get(ThreadLocalRandom.current().nextInt(serverList.size()));
        }
        String msg = "no servers available!";
        throw new ScTransactionException(TransactionResponseCode.NO_SERVER_AVAILABLE, msg);
    }
}
