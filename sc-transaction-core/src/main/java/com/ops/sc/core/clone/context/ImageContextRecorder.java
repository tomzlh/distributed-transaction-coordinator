package com.ops.sc.core.clone.context;


import com.ops.sc.core.clone.RollbackItem;

public class ImageContextRecorder {

    private static final ThreadLocal<ImageContext> THREAD_LOCAL = new ThreadLocal<>();

    private ImageContextRecorder() {
    }

    public static void init() {
        THREAD_LOCAL.set(new ImageContext());
    }

    public static ImageContext get() {
        return THREAD_LOCAL.get();
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }

    public static void setBfValue(Object exValue) {
        get().setExtValue(exValue);
    }

    public static void addRollbackItem(RollbackItem rollbackItem) {
        THREAD_LOCAL.get().getRollbackItemList().add(rollbackItem);
    }
}
