-- ----------------------------
-- Table structure for sensor_extend (传感器扩展信息表)
-- ----------------------------
DROP TABLE IF EXISTS `sensor_extend`;
CREATE TABLE `sensor_extend`  (
  `sensor_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '主键（IMEI号，Tag0x65）',
  `material_code` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '物料编码（固定JTY-GD-TCN2010）',
  `installation_location` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '安装位置',
  `area_id` bigint(0) NULL DEFAULT NULL COMMENT '所属区域ID',
  `area_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所属区域名称',
  `iccid` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'SIM卡ID（Tag0x70，BCD[10]）',
  `module_version` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '通信模组版本（Tag0x67）',
  `module_vendor` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '模组厂商（Tag0x68，如利尔达=03）',
  `terminal_type` tinyint(0) NOT NULL COMMENT '终端类型（固定0x98，Tag0x03）',
  `normal_work_interval` int(0) NULL DEFAULT NULL COMMENT '正常上报间隔（秒，Tag0x06）',
  `emergency_alarm_interval` int(0) NULL DEFAULT NULL COMMENT '紧急报警间隔（秒，Tag0x08）',
  `smoke_threshold` int(0) NULL DEFAULT NULL COMMENT '烟雾阈值（%，Tag0x14）',
  `temp_upper_limit` tinyint(0) NULL DEFAULT NULL COMMENT '温度上限（℃，Tag0x0B）',
  `temp_lower_limit` tinyint(0) NULL DEFAULT NULL COMMENT '温度下限（℃，Tag0x21）',
  `battery_life` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '3年' COMMENT '电池寿命（固定3年）',
  `protection_level` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT 'IP30' COMMENT '防护等级（固定IP30）',
  `del_flag` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`sensor_id`) USING BTREE,
  INDEX `idx_sensor_extend_area_id`(`area_id`) USING BTREE COMMENT '按区域筛选',
  INDEX `idx_sensor_extend_type`(`sensor_id`, `terminal_type`) USING BTREE COMMENT '定位设备类型'
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '传感器扩展信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sensor_contact (传感器联系人表)
-- ----------------------------
DROP TABLE IF EXISTS `sensor_contact`;
CREATE TABLE `sensor_contact`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sensor_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '传感器IMEI号',
  `user_id` bigint(0) NOT NULL COMMENT '联系人用户ID',
  `phone` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '联系电话',
  `notify_type` tinyint(0) NOT NULL COMMENT '通知类型（1-语音 2-短信）',
  `priority` tinyint(0) NOT NULL COMMENT '通知优先级（1-最高）',
  `del_flag` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sensor_contact_sensor_id`(`sensor_id`) USING BTREE COMMENT '关联传感器',
  INDEX `idx_sensor_contact_user_id`(`user_id`) USING BTREE COMMENT '关联用户',
  INDEX `idx_sensor_contact_priority`(`sensor_id`, `priority`) USING BTREE COMMENT '查询高优先级联系人'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '传感器联系人表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for notification_quota (通知额度表)
-- ----------------------------
DROP TABLE IF EXISTS `notification_quota`;
CREATE TABLE `notification_quota`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sensor_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '传感器IMEI号',
  `free_voice_count` int(0) NOT NULL DEFAULT 0 COMMENT '剩余免费语音条数',
  `free_sms_count` int(0) NOT NULL DEFAULT 0 COMMENT '剩余免费短信条数',
  `balance` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_notification_quota_sensor_id`(`sensor_id`) USING BTREE COMMENT '唯一关联传感器',
  INDEX `idx_notification_quota_voice`(`free_voice_count`) USING BTREE COMMENT '语音额度不足筛选',
  INDEX `idx_notification_quota_sms`(`free_sms_count`) USING BTREE COMMENT '短信额度不足筛选'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '通知额度表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sensor_alarm_log (传感器报警日志表)
-- ----------------------------
DROP TABLE IF EXISTS `sensor_alarm_log`;
CREATE TABLE `sensor_alarm_log`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sensor_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '传感器IMEI',
  `alarm_type` tinyint(0) NOT NULL COMMENT '报警类型（1=火灾，2=温度，3=低电，4=防拆）',
  `trigger_tag` tinyint(0) NOT NULL COMMENT '触发报警的协议Tag（0x01/0x02）',
  `trigger_value` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '触发值（如“烟雾浓度:120%”）',
  `alarm_time` datetime(0) NOT NULL COMMENT '报警时间',
  `handle_status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '处理状态（0=未处理，1=已处理）',
  `handle_time` datetime(0) NULL DEFAULT NULL COMMENT '处理时间（如消音时间）',
  `handle_user` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '处理人（用户名）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sensor_alarm_log_sensor_id`(`sensor_id`) USING BTREE COMMENT '关联传感器',
  INDEX `idx_sensor_alarm_log_time_type`(`alarm_time`, `alarm_type`) USING BTREE COMMENT '按时间+类型筛选'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '传感器报警日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_notification_template (通知模板表)
-- ----------------------------
DROP TABLE IF EXISTS `sys_notification_template`;
CREATE TABLE `sys_notification_template`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `product_id` bigint(0) NOT NULL COMMENT '关联产品ID（实现模板复用）',
  `sign_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '短信签名（阿里云）',
  `template_code` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '短信模板CODE（阿里云）',
  `template_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '语音模板ID（腾讯云）',
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sys_notification_template_product_id`(`product_id`) USING BTREE COMMENT '关联产品'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '通知模板表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for iot_import_log (导入日志表)
-- ----------------------------
DROP TABLE IF EXISTS `iot_import_log`;
CREATE TABLE `iot_import_log`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `import_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '导入人',
  `import_time` datetime(0) NOT NULL COMMENT '导入时间',
  `success_count` int(0) NOT NULL DEFAULT 0 COMMENT '成功数量',
  `fail_count` int(0) NOT NULL DEFAULT 0 COMMENT '失败数量',
  `fail_reason` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '失败原因（如“IMEI=xxx已存在”）',
  `file_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '导入文件名',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_iot_import_log_time`(`import_time`) USING BTREE COMMENT '按导入时间筛选',
  INDEX `idx_iot_import_log_user`(`import_user`) USING BTREE COMMENT '按导入人筛选'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '导入日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role_area (角色区域关联表)
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_area`;
CREATE TABLE `sys_role_area`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id` bigint(0) NOT NULL COMMENT '角色ID',
  `area_id` bigint(0) NOT NULL COMMENT '区域ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_role_area`(`role_id`, `area_id`) USING BTREE COMMENT '角色与区域权限关联'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '角色区域关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for iot_threshold_log (阈值调整日志表)
-- ----------------------------
DROP TABLE IF EXISTS `iot_threshold_log`;
CREATE TABLE `iot_threshold_log`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sensor_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '传感器IMEI',
  `threshold_type` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '阈值类型（如“battery”）',
  `old_value` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '旧值',
  `new_value` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '新值',
  `operate_user` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '操作人',
  `operate_time` datetime(0) NOT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_iot_threshold_log_sensor_id`(`sensor_id`) USING BTREE COMMENT '关联传感器'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '阈值调整日志表' ROW_FORMAT = Dynamic;