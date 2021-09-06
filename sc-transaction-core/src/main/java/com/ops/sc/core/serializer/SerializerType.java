
package com.ops.sc.core.serializer;


public enum SerializerType {




    /**
     * The protobuf.
     * <p>
     * Math.pow(2, 1)
     */
    PROTOBUF((byte)0x2),

    /**
     * The kryo.
     * <p>
     * Math.pow(2, 2)
     */
    KRYO((byte)0x4),

    /**
     * The fst.
     * <p>
     * Math.pow(2, 3)
     */
    FST((byte)0x8),

    /**
     * The hessian.
     * <p>
     * Math.pow(2, 4)
     */
    HESSIAN((byte)0x16),
    ;

    private final byte code;

    SerializerType(final byte code) {
        this.code = code;
    }


    public static SerializerType getByCode(byte code) {
        for (SerializerType b : SerializerType.values()) {
            if (code == b.code) {
                return b;
            }
        }
        throw new IllegalArgumentException("unknown codec:" + code);
    }


    public static SerializerType getByName(String name) {
        for (SerializerType b : SerializerType.values()) {
            if (b.name().equalsIgnoreCase(name)) {
                return b;
            }
        }
        throw new IllegalArgumentException("unknown codec:" + name);
    }


    public byte getCode() {
        return code;
    }
}
