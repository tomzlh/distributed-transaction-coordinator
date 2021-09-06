package com.ops.sc.core.model;

import com.ops.sc.common.bean.ScRequestMessage;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;


@Data
public class RpcTransMessage {

    private Long id;
    private int messageType;
    private byte codec;
    private byte compressor;
    private Map<String, String> headMap = new HashMap<>();
    private ScRequestMessage scRequestMessage;

}
