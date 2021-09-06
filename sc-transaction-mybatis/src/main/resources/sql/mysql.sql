CREATE TABLE `tb_trans_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `tid` bigint(20) NOT NULL COMMENT 'transaction ID',
  `trans_group_id` varchar(36) NOT NULL COMMENT 'transaction group ID',
  `business_id` varchar(100) NOT NULL DEFAULT '' COMMENT 'business Id',
  `status` tinyint(4) NOT NULL COMMENT 'status',
  `caller_ip` varchar(32) NOT NULL COMMENT 'client ip',
  `timeout` int(10) NOT NULL COMMENT 'transaction timeout',
  `timeout_type` tinyint(4) NOT NULL COMMENT 'timeout type',
  `app_name` varchar(32) NOT NULL COMMENT 'app name',
  `trans_name` varchar(32) NOT NULL DEFAULT '' COMMENT 'transaction name',
  `trans_mode` varchar(16) NOT NULL COMMENT 'transaction mode',
  `callback_strategy` tinyint(4) NOT NULL COMMENT 'call back strategy',
  `retry_count` tinyint(4) NOT NULL COMMENT 'retry count',
  `end_time` datetime DEFAULT NULL COMMENT 'transaction end time',
  `create_time` datetime NOT NULL COMMENT 'transaction create time',
  `modify_time` datetime NOT NULL COMMENT 'transaction modify time',
  `data_source` varchar(100) DEFAULT NULL COMMENT 'dataSource',
  `call_in_parallel` tinyint(4) NOT NULL COMMENT 'Parallel call',
  `roll_back_info` varchar(1000) DEFAULT NULL COMMENT 'roll back info',
  PRIMARY KEY (`id`),
  UNIQUE KEY `index_tid` (`tid`),
  UNIQUE KEY `index_biz_id` (`business_id`),
  KEY `index_status` (`status`),
  KEY `index_group_id` (`trans_group_id`),
  KEY `index_end_time` (`end_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='global transaction record';


CREATE TABLE `tb_branch_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tid` bigint(20) NOT NULL COMMENT 'global transaction id',
  `branch_id` bigint(20) NOT NULL COMMENT 'branch transaction ID',
  `business_id` varchar(100) NOT NULL DEFAULT '' COMMENT 'business Id',
  `parent_name` varchar(36) DEFAULT NULL COMMENT 'parent transaction ID',
  `status` tinyint(4) NOT NULL COMMENT 'status',
  `caller_ip` varchar(32) DEFAULT NULL COMMENT 'client IP',
  `branch_trans_name` varchar(32) NOT NULL COMMENT 'unique trans name',
  `branch_name` varchar(32) NOT NULL DEFAULT '' COMMENT 'branch name',
  `transaction_name` varchar(32) NOT NULL DEFAULT '' COMMENT 'transaction name',
  `branch_param` varchar(2048) COMMENT 'branch param',
  `external_param` varchar(2048)  COMMENT 'external param',
  `return_param` varchar(2048)  COMMENT 'return param',
  `timeout` int(10)  COMMENT 'timeout',
  `timeout_type` tinyint(4)  COMMENT 'timeout strategy',
  `retry` tinyint(4) NOT NULL COMMENT 'retry',
  `trans_mode` varchar(16) NOT NULL COMMENT 'trans_mode',
  `has_parent` tinyint(1) NOT NULL COMMENT 'has_parent',
  `retry_count` tinyint(4) NOT NULL COMMENT 'retry count',
  `data_source` varchar(100)  COMMENT 'dataSource',
  `url` varchar(1000)  COMMENT 'url',
  `rollback_param` varchar(2048) COMMENT 'rollback param',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `modify_time` datetime NOT NULL COMMENT 'modify time',
  `end_time` datetime DEFAULT NULL COMMENT 'end time',
  `order_no` tinyint(4)  COMMENT 'order number',
  PRIMARY KEY (`id`),
  UNIQUE KEY `index_branch_id` (`branch_id`),
  KEY `index_branch_biz_id` (`business_id`),
  KEY `index_branch_tid` (`tid`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='branch transaction record';

CREATE TABLE `tb_trans_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `trans_group_name` varchar(100) NOT NULL COMMENT 'transaction group name',
  `trans_group_id` varchar(36) NOT NULL COMMENT 'trans group id',
  `tenant_id` varchar(100) NOT NULL COMMENT 'tenant id',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `modify_time` datetime NOT NULL COMMENT 'modify time',
  `is_invalid` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1 invalid，0 valid',
  PRIMARY KEY (`id`),
  UNIQUE KEY `index_group` (`trans_group_id`),
  UNIQUE KEY `index_trans_name` (`transaction_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='transaction group';


CREATE TABLE `tb_trans_model` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `trans_code` varchar(100) NOT NULL COMMENT 'transaction code',
  `trans_group_id` varchar(36) NOT NULL COMMENT 'transaction group id',
  `trans_mode` varchar(100) NOT NULL COMMENT 'transaction mode',
  `call_mode` varchar(100) NOT NULL COMMENT 'call mode',
  `model_name` varchar(100) NOT NULL COMMENT 'model name',
  `timeout` int(10)  COMMENT 'timeout',
  `timeout_type` tinyint(4)  COMMENT 'timeout strategy',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime COMMENT 'update time',
  `remark` varchar(1000) COMMENT 'remark',
  `is_invalid` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1 invalid，0 valid',
  PRIMARY KEY (`id`),
  UNIQUE KEY `index_trans_model_code` (`trans_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='transaction model';

CREATE TABLE `tb_trans_model_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `model_id` bigint(20) NOT NULL COMMENT 'transaction model id',
  `model_branch_name` varchar(36) NOT NULL COMMENT 'transaction branch name',
  `branch_name` varchar(36) NOT NULL COMMENT 'transaction branch name',
  `retry_required` tinyint(4)  COMMENT 'retry required',
  `retry_count` tinyint(4)  COMMENT 'retry count',
  `timeout` int(10)  COMMENT 'timeout',
  `timeout_type` tinyint(4)  COMMENT 'timeout strategy',
  `parent_names` varchar(100) COMMENT 'parent ids',
  `has_parent` tinyint(4)  COMMENT 'has parent',
  `request_params` varchar(500) COMMENT 'request params',
  `external_params` varchar(500) COMMENT 'external params',
  `response_params` varchar(500) COMMENT 'response params',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime COMMENT 'update time',
  `is_invalid` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1 invalid，0 valid',
  PRIMARY KEY (`id`),
  UNIQUE KEY `index_branch_detail_name` (`model_branch_name`),
  KEY `index_trans_model_detail_id` (`model_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='transaction model detail';


CREATE TABLE `tb_error_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `tid` varchar(36) NOT NULL COMMENT 'global id',
  `branch_id` varchar(36) NOT NULL DEFAULT '' COMMENT 'branch id',
  `error_type` tinyint(4) NOT NULL COMMENT 'error type',
  `error_detail` varchar(255) NOT NULL COMMENT 'error detail info',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `modify_time` datetime NOT NULL COMMENT 'modify time',
  PRIMARY KEY (`id`),
  KEY `index_error_tid` (`tid`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='transaction error table';