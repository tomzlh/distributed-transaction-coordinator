package com.ops.sc.common.bean;

import com.ops.sc.common.enums.ParamSource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

@Data
@ToString
@EqualsAndHashCode(of={"id"})
public class ModelDetail {

    private Long id;

    private Long modelId;

    private String modelBranchName;

    private String branchName;

    private int retryRequired;

    private int retryCount;

    private long timeout;

    private int timeoutType;

    private List<String> parentNames;

    private int hasParent;

    private String url;

    private String commitMethod;

    private String rollbackMethod;

    private List<String> requestParamNames;

    private List<String> externalParamNames;

    private List<String> resParamsList;

}
