package com.ops.sc.server.event;

import com.ops.sc.common.enums.TransStatus;
import lombok.Data;

import java.util.Date;

@Data
public class GlobalTransEvent extends TransEvent {

      private Long tid;
      private TransStatus fromStatus;
      private TransStatus toStatus;
      private Integer retryCount;
      private Date modifyDate;

}
