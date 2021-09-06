package com.ops.sc.tc.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TransQueryRequest {

     private String businessId;

     private Long timeout;

}
