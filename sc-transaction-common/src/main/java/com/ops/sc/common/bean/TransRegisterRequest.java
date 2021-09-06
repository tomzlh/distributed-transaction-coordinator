package com.ops.sc.common.bean;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class TransRegisterRequest extends ScResponseMessage implements Serializable {


    protected String applicationName;

    /**
     * The Transaction service group.
     */
    protected String transactionServiceGroup;

    /**
     * The Extra data.
     */
    protected String extraData;

    //only xa need
    protected String dataSource;




    @Override
    public String toString() {
        return "RegisterRequest{" +
                "dataSource='" + dataSource + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", transactionServiceGroup='" + transactionServiceGroup + '\'' +
                '}';
    }
}
