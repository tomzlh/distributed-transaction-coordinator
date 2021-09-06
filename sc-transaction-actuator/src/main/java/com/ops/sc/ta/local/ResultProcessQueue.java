package com.ops.sc.ta.local;


public class ResultProcessQueue {

    private Status status;

    private ResultProcessQueue(Status status) {
        this.status = status;
    }

    public static ResultProcessQueue done() {
        return new ResultProcessQueue(Status.DONE);
    }

    public static ResultProcessQueue toFailQueue() {
        return new ResultProcessQueue(Status.TO_FAIL_QUEUE);
    }

    public static ResultProcessQueue waitNextCheck() {
        return new ResultProcessQueue(Status.WAIT_NEXT_CHECK);
    }

    public boolean isToFailQueue() {
        return Status.TO_FAIL_QUEUE == status;
    }

    public boolean isDone() {
        return Status.DONE == status;
    }

    public boolean isWaitNextCheck() {
        return Status.WAIT_NEXT_CHECK == status;
    }

    @Override
    public String toString() {
        return "HandleResult{" + "status=" + status + '}';
    }

    private enum Status {

        // 本地二阶段成功或者全局事务timeout
        DONE,

        TO_FAIL_QUEUE,

        // 全局事务是trying状态，分支等待下次statCheck处理
        WAIT_NEXT_CHECK
    }
}
