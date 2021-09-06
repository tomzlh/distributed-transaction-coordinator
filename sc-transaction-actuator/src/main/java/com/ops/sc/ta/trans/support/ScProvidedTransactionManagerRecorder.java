package com.ops.sc.ta.trans.support;

import org.springframework.core.NamedThreadLocal;


public class ScProvidedTransactionManagerRecorder {

    private static final ThreadLocal<Boolean> PROVIDED_TM_CONTEXT = new NamedThreadLocal<>("Local transaction flag");

    /**
     * 判断是否使用SC提供的事务管理器
     *
     * @return
     */
    public static boolean isInProvidedTM() {
        return PROVIDED_TM_CONTEXT.get() != null && PROVIDED_TM_CONTEXT.get();
    }

    /**
     * 启用SC提供的事务管理器
     *
     * @param flag
     */
    public static void setInProvidedTM(Boolean flag) {
        PROVIDED_TM_CONTEXT.set(flag);
    }
}
