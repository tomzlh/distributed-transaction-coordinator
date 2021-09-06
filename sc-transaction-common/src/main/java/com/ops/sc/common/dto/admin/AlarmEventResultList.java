package com.ops.sc.common.dto.admin;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import com.ops.sc.common.bean.ResponseResult;
import com.ops.sc.common.enums.TransactionResponseCode;


public class AlarmEventResultList extends ResponseResult {
    @JSONField(name = "AlarmEventInfos")
    private List<AlarmEventInfoDTO> alarmEventInfoDTOList;

    public AlarmEventResultList(List<AlarmEventInfoDTO> alarmEventInfoDTOList) {
        super(TransactionResponseCode.SUCCESS);
        this.alarmEventInfoDTOList = alarmEventInfoDTOList;
    }

    public List<AlarmEventInfoDTO> getAlarmEventInfoDTOList() {
        return alarmEventInfoDTOList;
    }

}
