package com.ops.sc.server.listener;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.ops.sc.server.event.BranchTransEvent;
import com.ops.sc.server.event.GlobalTransEvent;
import com.ops.sc.server.event.TransEvent;

public interface EventListener {

    @Subscribe
    @AllowConcurrentEvents
    void listen(TransEvent branchTransEvent);

}
