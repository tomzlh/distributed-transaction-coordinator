package com.ops.sc.common.enums;

public enum Participant {

    /**
     * tm
     */
    TS(1),
    /**
     * rm
     */
    TA(2),
    /**
     * server
     */
    SERVER(3);

    Participant(int value) {
        this.value = value;
    }

    /**
     * Gets value.
     *
     * @return value value
     */
    public int getValue() {
        return value;
    }

    /**
     * value
     */
    private int value;
}
