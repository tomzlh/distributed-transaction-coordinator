package com.ops.sc.server.service;

import com.ops.sc.common.dto.admin.AlarmEventResultList;
import com.ops.sc.common.enums.AlarmEvent;


public interface AlarmService {
    /**
     * 异步发送告警信息
     *
     * @param tid
     * @param branchId
     * @param alarmEvent
     */
    void sendAlarm(Long tid, Long branchId, AlarmEvent alarmEvent);

    /**
     * 获取所有告警事项信息
     *
     * @return
     */
    AlarmEventResultList getAllAlarmEventInfos();

    /**
     * 获取alarmEvent对应的告警信息
     *
     * @param alarmEvent
     * @return
     */
    AlarmEventResultList getAlarmEventInfo(AlarmEvent alarmEvent);
}
