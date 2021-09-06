package com.ops.sc.common.enums;

import com.ops.sc.common.constant.ServerConstants;

import java.util.Arrays;
import java.util.Optional;


public enum AlarmEvent {

    TCC_GLOBAL_TRY_TIMEOUT(0, "tccGlobalTryTimeout"),

    TCC_GLOBAL_ERROR(1, "tccGlobalReportError"),

    TCC_BRANCH_TRY_TIMEOUT(2, "tccBranchTryTimeout"),

    TCC_BRANCH_CONFIRM_FAILED(3, "tccBranchConfirmFailed"),

    TCC_BRANCH_CANCEL_FAILED(4, "tccBranchCancelFailed"),

    TCC_BRANCH_REPORT_ERROR(5, "tccBranchReportError"),

    MSG_CONFIRM_FAILED(6, "msgConfirmFailed"),

    MSG_CANCEL_FAILED(7, "msgCancelFailed");

    private Integer value;

    private String eventName; // 报警内容在resource中的key

    AlarmEvent(Integer value, String eventName) {
        this.value = value;
        this.eventName = eventName;
    }

    public static AlarmEvent getAlarmEventByEventName(String name) {
        Optional<AlarmEvent> alarmEvent = Arrays.stream(AlarmEvent.values())
                .filter(event -> event.getEventName().equals(name)).findAny();
        if (!alarmEvent.isPresent()) {
            throw new IllegalArgumentException();
        }
        return alarmEvent.get();
    }

    public Integer getValue() {
        return value;
    }

    public String getEventName() {
        return eventName;
    }

    public String getAlarmContentResourceKey() {
        // 告警内容
        return ServerConstants.AlarmEventConst.ALARM_PREFIX + eventName + ServerConstants.AlarmEventConst.ALARM_CONTENT_SUFFIX;
    }

    public String getAlarmDescriptionResourceKey() {
        // 告警项描述
        return ServerConstants.AlarmEventConst.ALARM_PREFIX + eventName + ServerConstants.AlarmEventConst.ALARM_DESCRIPTION_SUFFIX;
    }
}
