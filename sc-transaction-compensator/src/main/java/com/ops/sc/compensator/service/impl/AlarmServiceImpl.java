package com.ops.sc.compensator.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonSyntaxException;
import com.ops.sc.common.constant.ServerConstants;
import com.ops.sc.common.dto.admin.AlarmEventInfoDTO;
import com.ops.sc.common.dto.admin.AlarmEventResultList;
import com.ops.sc.common.enums.AlarmEvent;
import com.ops.sc.common.hold.RequestProxy;
import com.ops.sc.common.hold.ResponseStatus;
import com.ops.sc.common.model.TransGroup;
import com.ops.sc.common.utils.JsonUtil;
import com.ops.sc.common.utils.UUIDGenerator;
import com.ops.sc.compensator.dao.TransInfoDao;
import com.ops.sc.compensator.service.AlarmService;
import com.ops.sc.core.bean.AlarmMessageBean;
import com.ops.sc.core.bean.AlarmEventBean;
import com.ops.sc.core.service.ResourceInfoService;
import com.ops.sc.mybatis.mapper.TransGroupMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class AlarmServiceImpl implements AlarmService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmServiceImpl.class);
    private static final Locale DEFAULT_ALARM_LOCALE = Locale.CHINESE;
    private static final Long GROUP_CACHE_EXPIRE_MS = 60 * 60 * 1000L; // 1 hour
    private static final Integer GROUP_CACHE_MAX_SIZE = 1000;
    private static final String GROUP_CACHE_PREFIX = "group-";
    private static final String ALARM_TENANT_ID_HEADER = ServerConstants.HttpConst.HEADER_TENANTID;
    private static final String ALARM_RESPONSE_CODE = "Code";
    private static final Integer ALARM_RESPONSE_SUCCESS_CODE = 200;

    private static Cache<String, TransGroup> groupInfoCache = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMillis(GROUP_CACHE_EXPIRE_MS)).maximumSize(GROUP_CACHE_MAX_SIZE)
            .removalListener((key, graph, cause) -> LOGGER.info("Remove group cache: {}.", key)).build();

    @Value("${alarm.server:#{null}}")
    private String alarmServer;

    @Value("${skip.deploy:true}")
    private Boolean skipDeploy;


    @Resource
    private ResourceInfoService resourceInfoService;

    @Resource
    private TransGroupMapper transGroupMapper;

    @Resource
    private TransInfoDao transInfoDao;

    @Override
    @Async("commonTask")
    public void sendAlarm(Long tid, Long bid, AlarmEvent alarmEvent) {
        if (!skipDeploy) {
            logAlarmInfo(tid, bid, alarmEvent);
            return;
        }
        // 发送告警信息
        String groupId = transInfoDao.findByTid(tid).getGroupId();
        TransGroup transGroup = groupInfoCache.getIfPresent(GROUP_CACHE_PREFIX + groupId);
        if (transGroup == null) {
            transGroup = transGroupMapper.getTransGroupByGroupId(groupId);
            groupInfoCache.put(GROUP_CACHE_PREFIX + groupId, transGroup);
        }
        sendAlarmRequest(tid, bid, transGroup, alarmEvent);
    }


    private void logAlarmInfo(Long tid, Long bid, AlarmEvent alarmEvent) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String dateTime = LocalDateTime.now(ZoneOffset.of("+8")).format(formatter);
        Map<String, String> message = new HashMap<>();

        String alarmContent = resourceInfoService.getMessage(alarmEvent.getAlarmContentResourceKey(), Locale.ENGLISH,
                String.valueOf(tid), String.valueOf(bid));
        message.put("tid", String.valueOf(tid));
        message.put("branchId", String.valueOf(bid));
        message.put("content", alarmContent);
        LOGGER.info("time={}, module={}, topic={}, id={}, message={}", dateTime, "sc", "alarm",
                UUIDGenerator.generateUUID(), JsonUtil.toString(message));
    }

    private boolean sendAlarmRequest(Long tid, Long bid, TransGroup transGroup, AlarmEvent alarmEvent) {
        String tenantId = transGroup.getTenantId();
        String groupName = transGroup.getGroupName();

        Map<String, String> headers = new HashMap<>();
        headers.put(ALARM_TENANT_ID_HEADER, tenantId);

        String alarmContent = resourceInfoService.getMessage(alarmEvent.getAlarmContentResourceKey(),
                DEFAULT_ALARM_LOCALE, String.valueOf(tid), String.valueOf(bid));
        AlarmMessageBean annotationsDTO = new AlarmMessageBean(alarmContent);

        AlarmEventBean alarmEventBean = new AlarmEventBean(groupName, annotationsDTO, alarmEvent.getEventName());

        ResponseStatus result;
        try {
            result = RequestProxy.post(alarmServer, headers, JsonUtil.toString(alarmEventBean));
        } catch (IOException e) {
            LOGGER.error("send to alarm service get an io exception: ", e);
            return false;
        }

        if (result.isSuccess()) {
            try {
                Map<String, Object> resultMap = JsonUtil.toMap(result.getBody());
                Integer code = (int) Double.parseDouble((resultMap.get(ALARM_RESPONSE_CODE).toString()));
                if (!code.equals(ALARM_RESPONSE_SUCCESS_CODE)) {
                    LOGGER.error(
                            "tid : {}, bid : {}, alarmEvent: {} send alarm fail, code != success,  responseBody = {}",
                            tid, bid, alarmEvent, result.getBody());
                    return false;
                }
                LOGGER.info("tid : {}, bid : {}, alarmType : {} send alarm success", tid, bid, alarmEvent);
                return true;
            } catch (JsonSyntaxException e) {
                LOGGER.error("send alarm fail: result json error, body : {}", result.getBody());
                return false;
            }
        } else {
            LOGGER.error(
                    "tid : {}, bid : {}, alarmType : {} send alarm fail, response code : {}, response body : {}",
                    tid, bid, alarmEvent, result.getStatusCode(), result.getBody());
            return false;
        }

    }

    @Override
    public AlarmEventResultList getAllAlarmEventInfos() {
        List<AlarmEventInfoDTO> alarmEventInfoDTOList = Arrays.stream(AlarmEvent.values())
                .map(this::getAlarmEventInfoDTO).collect(Collectors.toList());
        return new AlarmEventResultList(alarmEventInfoDTOList);
    }

    @Override
    public AlarmEventResultList getAlarmEventInfo(AlarmEvent alarmEvent) {
        return new AlarmEventResultList(Collections.singletonList(getAlarmEventInfoDTO(alarmEvent)));
    }

    private AlarmEventInfoDTO getAlarmEventInfoDTO(AlarmEvent alarmEvent) {
        String description = resourceInfoService.getMessage(alarmEvent.getAlarmDescriptionResourceKey(),
                DEFAULT_ALARM_LOCALE);
        return new AlarmEventInfoDTO(alarmEvent.getEventName(), description);
    }

}
