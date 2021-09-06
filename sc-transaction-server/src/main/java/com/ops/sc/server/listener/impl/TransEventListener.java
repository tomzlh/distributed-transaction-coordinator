package com.ops.sc.server.listener.impl;

import com.ops.sc.server.event.BranchTransEvent;
import com.ops.sc.server.event.GlobalTransEvent;
import com.ops.sc.server.event.TransEvent;
import com.ops.sc.server.listener.EventListener;
import com.ops.sc.server.service.BranchTransService;
import com.ops.sc.server.service.GlobalTransService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TransEventListener implements EventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransEventListener.class);

    private GlobalTransService globalTransService;

    private BranchTransService branchTransService;

    public TransEventListener(GlobalTransService globalTransService,BranchTransService branchTransService){
        this.globalTransService=globalTransService;
        this.branchTransService=branchTransService;
    }

    @Override
    public void listen(TransEvent transEvent) {
        if(transEvent instanceof BranchTransEvent) {
            BranchTransEvent branchTransEvent=(BranchTransEvent)transEvent;
            if(branchTransEvent.getFromStatus()==null) {
                branchTransService.updateStatusById(branchTransEvent.getBid(), branchTransEvent.getToStatus().getValue(), branchTransEvent.getRetryCount(), branchTransEvent.getModifyDate());
            }
            else{
                List<Integer> fromStatusList=new ArrayList<>();
                fromStatusList.add(branchTransEvent.getFromStatus().getValue());
                branchTransService.updateStatusByBidAndStatus(branchTransEvent.getBid(),fromStatusList,branchTransEvent.getToStatus().getValue());
            }

        }
        else if(transEvent instanceof GlobalTransEvent){
            GlobalTransEvent globalTransEvent=(GlobalTransEvent)transEvent;
            if(globalTransEvent.getFromStatus()==null) {
                globalTransService.updateStatusAndEndTimeById(globalTransEvent.getTid(), globalTransEvent.getToStatus().getValue());
            }
            else{
                globalTransService.updateStatusByTidAndStatus(globalTransEvent.getTid(), globalTransEvent.getFromStatus().getValue(),globalTransEvent.getToStatus().getValue());
            }
        }
        else{
            LOGGER.warn("not supported trans event:{}",transEvent);
        }
    }
}
