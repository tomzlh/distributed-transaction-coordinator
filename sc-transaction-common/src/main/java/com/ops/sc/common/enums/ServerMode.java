package com.ops.sc.common.enums;

import java.util.Arrays;
import java.util.Optional;


public enum ServerMode {

    REMOTE(0),

    LOCAL(1);

    private Integer value;

    ServerMode(Integer value) {
        this.value = value;
    }

    public static ServerMode getByValue(int value) {
        Optional<ServerMode> optionalFrameMode = Arrays.stream(ServerMode.values())
                .filter(frameMode -> frameMode.getValue().equals(value)).findAny();
        return optionalFrameMode.orElseThrow(IllegalArgumentException::new);
    }

    public boolean isLocalFrameMode() {
        return LOCAL == this;
    }

    public Integer getValue() {
        return value;
    }

}
