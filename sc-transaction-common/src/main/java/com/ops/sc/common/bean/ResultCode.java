
package com.ops.sc.common.bean;


public enum ResultCode {

    /**
     * Failed result code.
     */
    // Failed
    Failed,

    /**
     * Success result code.
     */
    // Success
    Success;

    /**
     * Get result code.
     *
     * @param ordinal the ordinal
     * @return the result code
     */
    public static ResultCode get(byte ordinal) {
        return get((int)ordinal);
    }

    /**
     * Get result code.
     *
     * @param ordinal the ordinal
     * @return the result code
     */
    public static ResultCode get(int ordinal) {
        for (ResultCode resultCode : ResultCode.values()) {
            if (resultCode.ordinal() == ordinal) {
                return resultCode;
            }
        }
        throw new IllegalArgumentException("Unknown ResultCode[" + ordinal + "]");
    }
}
