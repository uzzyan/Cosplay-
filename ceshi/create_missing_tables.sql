-- ============================================
-- Supplement missing database tables
-- Execution Date: 2026-04-27
-- ============================================

USE `s003`;

-- ----------------------------
-- Table structure for message (Online Message)
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `user_id` bigint NULL DEFAULT NULL COMMENT 'User ID',
  `title` varchar(255) NULL DEFAULT NULL COMMENT 'Message Title',
  `content` varchar(1600) NULL DEFAULT NULL COMMENT 'Message Content',
  `contact` varchar(255) NULL DEFAULT NULL COMMENT 'Contact Info',
  `custom_intent` tinyint(1) NULL DEFAULT NULL COMMENT 'Custom Intent',
  `create_time` varchar(50) NULL DEFAULT NULL COMMENT 'Create Time',
  `reply` varchar(1600) NULL DEFAULT NULL COMMENT 'Reply Content',
  `reply_time` varchar(50) NULL DEFAULT NULL COMMENT 'Reply Time',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Online Message Table' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for after_sale (After Sale Application)
-- ----------------------------
DROP TABLE IF EXISTS `after_sale`;
CREATE TABLE `after_sale`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `order_no` varchar(255) NULL DEFAULT NULL COMMENT 'Order No',
  `user_id` bigint NULL DEFAULT NULL COMMENT 'User ID',
  `type` varchar(50) NULL DEFAULT NULL COMMENT 'After Sale Type',
  `reason` varchar(1600) NULL DEFAULT NULL COMMENT 'After Sale Reason',
  `create_time` varchar(50) NULL DEFAULT NULL COMMENT 'Apply Time',
  `status` varchar(50) NULL DEFAULT NULL COMMENT 'Status',
  `handle_time` varchar(50) NULL DEFAULT NULL COMMENT 'Handle Time',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'After Sale Table' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for comment (Product Comment)
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `content` varchar(1600) NULL DEFAULT NULL COMMENT 'Comment Content',
  `user_id` bigint NULL DEFAULT NULL COMMENT 'User ID',
  `good_id` bigint NULL DEFAULT NULL COMMENT 'Product ID',
  `create_time` varchar(50) NULL DEFAULT NULL COMMENT 'Comment Time',
  `reply` varchar(1600) NULL DEFAULT NULL COMMENT 'Merchant Reply',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Product Comment Table' ROW_FORMAT = DYNAMIC;

-- ============================================
-- Tables created successfully
-- ============================================
