package com.ops.sc.core.service.tcc;

import lombok.Data;
import lombok.ToString;

import java.lang.reflect.Method;

@Data
@ToString
public class TccExecuteInfo {

    private Method confirmMethod;

    private Method cancelMethod;

    private Object targetBean;

    private String tccBeanName;

    private String tagId;
}
