package com.ops.sc.common.enums;


public enum TransRecordType {

    // 一阶段
    OPEN_GLOBAL_TRANS("OpenGlobalTrans", "开启事务"),

    COMMIT_GLOBAL_TRANS("CommitGlobalTrans", "事务提交"),

    ROLLBACK_GLOBAL_TRANS("RollbackGlobalTrans", "事务回滚"),

    MESSAGE_PREPARE("MessagePrepare", "事务消息一阶段上报"),

    NATIVE_PREPARE("NativePrepare", "原生事务消息一阶段投递"),

    // 二阶段
    BRANCH_PREPARE("BranchPrepare", "分支事务预提交"),
    BRANCH_COMMIT("BranchCommit", "分支事务提交"),

    BRANCH_ROLLBACK("BranchRollback", "分支事务回滚"),

    MESSAGE_SEND("MessageSend", "事务消息二阶段投递"),

    MESSAGE_ROLLBACK("MessageRollback", "原生事务消息一阶段投递"),

    NATIVE_COMMIT("NativeCommit", "原生事务消息一阶段投递"),

    NATIVE_ROLLBACK("NativeRollback", "原生事务消息一阶段投递"),

    MESSAGE_COMMIT("MessageCommit", "服务端投递事务消息");

    private String typeName;

    private String desc;

    TransRecordType(String typeName, String desc) {
        this.typeName = typeName;
        this.desc = desc;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getDesc() {
        return desc;
    }
}
