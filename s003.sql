/*
 Navicat Premium Dump SQL

 Source Server         : mysql8.1
 Source Server Type    : MySQL
 Source Server Version : 80100 (8.1)
 Source Host           : localhost:3309
 Source Schema         : s003

 Target Server Type    : MySQL
 Target Server Version : 80100 (8.1)
 File Encoding         : 65001

 Date: 14/04/2026 09:30:00
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- MySQL 8.0 兼容：使用 utf8mb4 字符集和 utf8mb4_0900_ai_ci 排序规则
CREATE DATABASE IF NOT EXISTS `s003` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `s003`;
-- ----------------------------
-- Table structure for address
-- ----------------------------
DROP TABLE IF EXISTS `address`;
CREATE TABLE `address`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `link_user` varchar(255) NULL DEFAULT NULL COMMENT '联系人',
  `link_address` varchar(255) NULL DEFAULT NULL COMMENT '地址',
  `link_phone` varchar(255) NULL DEFAULT NULL COMMENT '电话',
  `user_id` bigint NULL DEFAULT NULL COMMENT '所属用户',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '地址表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of address
-- ----------------------------
INSERT INTO `address` VALUES (17, '美式加冰', '浙江杭州', '15854685265', 8);
INSERT INTO `address` VALUES (18, '美式加冰', '浙江宁波', '15685985658', 8);
INSERT INTO `address` VALUES (19, '美式加冰', '浙江绍兴', '16598565856', 8);

-- ----------------------------
-- Table structure for avatar
-- ----------------------------
DROP TABLE IF EXISTS `avatar`;
CREATE TABLE `avatar`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `type` varchar(255) NULL DEFAULT NULL,
  `size` bigint NULL DEFAULT NULL,
  `url` varchar(255) NULL DEFAULT NULL,
  `md5` varchar(255) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '头像表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of avatar
-- ----------------------------
INSERT INTO `avatar` VALUES (10, 'jpg', 31, '/avatar/f40e08ebb6414cb19701a7d2dc6453a8.jpg', '2d9caf14083e56cb198a0e072423ff7c');

-- ----------------------------
-- Table structure for carousel
-- ----------------------------
DROP TABLE IF EXISTS `carousel`;
CREATE TABLE `carousel`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `good_id` bigint NULL DEFAULT NULL COMMENT '对应的商品id',
  `show_order` int NULL DEFAULT NULL COMMENT '播放顺序',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '轮播图表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of carousel
-- ----------------------------
INSERT INTO `carousel` VALUES (9, 26, 1);

-- ----------------------------
-- Table structure for cart
-- ----------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `count` int NULL DEFAULT NULL COMMENT '数量',
  `create_time` datetime NULL DEFAULT NULL COMMENT '加入时间',
  `good_id` bigint NULL DEFAULT NULL COMMENT '商品id',
  `standard` varchar(255) NULL DEFAULT NULL,
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '购物车表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of cart
-- ----------------------------
INSERT INTO `cart` VALUES (29, 1, '2025-11-04 22:14:24', 23, '大号', 8);
INSERT INTO `cart` VALUES (31, 1, '2025-11-04 22:14:33', 24, '大码', 8);

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(255) NULL DEFAULT NULL COMMENT '类别名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 46 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '分类表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (38, '动漫玩具');
INSERT INTO `category` VALUES (39, '动漫手办');
INSERT INTO `category` VALUES (40, '动漫文具');
INSERT INTO `category` VALUES (41, '动漫服饰');
INSERT INTO `category` VALUES (42, '生活类');
INSERT INTO `category` VALUES (43, '娱乐类');
INSERT INTO `category` VALUES (44, '装饰类');
INSERT INTO `category` VALUES (45, '美妆类');

-- ----------------------------
-- Table structure for good
-- ----------------------------
DROP TABLE IF EXISTS `good`;
CREATE TABLE `good`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(255) NULL DEFAULT NULL COMMENT '商品名称',
  `description` varchar(1600) NULL DEFAULT NULL COMMENT '描述',
  `discount` double(10, 2) NOT NULL DEFAULT 1.00 COMMENT '折扣',
  `sales` bigint NOT NULL DEFAULT 0 COMMENT '销量',
  `sale_money` double(10, 2) NULL DEFAULT 0.00 COMMENT '销售额',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类id',
  `imgs` varchar(255) NULL DEFAULT NULL COMMENT '商品图片',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `recommend` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否推荐。0不推荐，1推荐',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除，0未删除，1删除',
  `status` tinyint(1) DEFAULT 1 COMMENT '商品状态：1=上架,0=下架',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of good
-- ----------------------------
INSERT INTO `good` VALUES (22, '怪盗基德装饰盒', '一个精美的盒子', 0.50, 1, 50.00, 38, '/file/e0e987f26fcd430a9bed7ace279c37a9.png', '2025-11-04 21:42:13', 1, 0, 1);
INSERT INTO `good` VALUES (23, '海贼王系列', '海贼王系列周边', 0.50, 2, 100.00, 38, '/file/d035c345c22046d69fda006cacaa74fd.png', '2025-11-04 21:43:21', 1, 0, 1);
INSERT INTO `good` VALUES (24, '精美衬衫', '一个精美衬衫', 0.50, 1, 50.00, 38, '/file/c79b676d61f34a9b8d6ea0319ebc680c.png', '2025-11-04 21:44:51', 1, 0, 1);
INSERT INTO `good` VALUES (25, '精美长裤', '一个精美长裤', 0.50, 0, 0.00, 38, '/file/5a03bc37e4154e988ffbcc3a4e3d44d5.png', '2025-11-04 21:45:17', 1, 0, 1);
INSERT INTO `good` VALUES (26, '轮播图', '轮播图', 1.00, 0, 0.00, 39, '/file/db8f8d9e60424e16a76e12315292b3ba.jpg', '2025-11-04 21:48:26', 0, 0, 1);

-- ----------------------------
-- Table structure for good_standard
-- ----------------------------
DROP TABLE IF EXISTS `good_standard`;
CREATE TABLE `good_standard`  (
  `good_id` bigint NULL DEFAULT NULL COMMENT '商品id',
  `value` varchar(255) NULL DEFAULT NULL COMMENT '规格',
  `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '价格',
  `store` bigint NULL DEFAULT NULL COMMENT '库存'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品规格表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of good_standard
-- ----------------------------
INSERT INTO `good_standard` VALUES (24, '大码', 100.00, 99);
INSERT INTO `good_standard` VALUES (26, '1', 1.00, 1);
INSERT INTO `good_standard` VALUES (23, '大号', 100.00, 100);
INSERT INTO `good_standard` VALUES (23, '中号', 100.00, 98);
INSERT INTO `good_standard` VALUES (23, '小号', 100.00, 100);
INSERT INTO `good_standard` VALUES (22, '大号', 100.00, 99);
INSERT INTO `good_standard` VALUES (22, '中号', 100.00, 100);
INSERT INTO `good_standard` VALUES (22, '小号', 100.00, 100);
INSERT INTO `good_standard` VALUES (25, '大码', 100.00, 100);

-- ----------------------------
-- Table structure for icon
-- ----------------------------
DROP TABLE IF EXISTS `icon`;
CREATE TABLE `icon`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `value` varchar(255) NULL DEFAULT NULL COMMENT '图标的识别码',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '图标表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of icon
-- ----------------------------
INSERT INTO `icon` VALUES (22, '&#xe9f3');
INSERT INTO `icon` VALUES (23, '&#xe910');

-- ----------------------------
-- Table structure for icon_category
-- ----------------------------
DROP TABLE IF EXISTS `icon_category`;
CREATE TABLE `icon_category`  (
  `category_id` bigint NOT NULL COMMENT '分类id',
  `icon_id` bigint NOT NULL COMMENT '图标id',
  PRIMARY KEY (`category_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品分类 - 图标关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of icon_category
-- ----------------------------
INSERT INTO `icon_category` VALUES (38, 22);
INSERT INTO `icon_category` VALUES (39, 22);
INSERT INTO `icon_category` VALUES (40, 22);
INSERT INTO `icon_category` VALUES (41, 22);
INSERT INTO `icon_category` VALUES (42, 23);
INSERT INTO `icon_category` VALUES (43, 23);
INSERT INTO `icon_category` VALUES (44, 23);
INSERT INTO `icon_category` VALUES (45, 23);

-- ----------------------------
-- Table structure for order_goods
-- ----------------------------
DROP TABLE IF EXISTS `order_goods`;
CREATE TABLE `order_goods`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint NULL DEFAULT NULL COMMENT '订单id',
  `good_id` bigint NULL DEFAULT NULL COMMENT '商品id',
  `count` int NULL DEFAULT NULL COMMENT '数量',
  `standard` varchar(1600) NULL DEFAULT NULL COMMENT '规格',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 48 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_goods
-- ----------------------------
INSERT INTO `order_goods` VALUES (44, 44, 23, 2, '中号');
INSERT INTO `order_goods` VALUES (45, 45, 22, 1, '大号');
INSERT INTO `order_goods` VALUES (46, 46, 24, 1, '大码');
INSERT INTO `order_goods` VALUES (47, 47, 22, 1, '大号');

-- ----------------------------
-- Table structure for standard
-- ----------------------------
DROP TABLE IF EXISTS `standard`;
CREATE TABLE `standard`  (
  `goodId` bigint NOT NULL COMMENT '商品id',
  `value` varchar(255) NULL DEFAULT NULL COMMENT '商品规格',
  `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '该规格的价格',
  `store` bigint NULL DEFAULT NULL COMMENT '该规格的库存',
  PRIMARY KEY (`goodId`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '规格表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of standard
-- ----------------------------

-- ----------------------------
-- Table structure for sys_file
-- ----------------------------
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(255) NULL DEFAULT NULL COMMENT '文件名称',
  `type` varchar(255) NULL DEFAULT NULL COMMENT '文件类型',
  `size` bigint NULL DEFAULT NULL COMMENT '大小',
  `url` varchar(255) NULL DEFAULT NULL COMMENT '文件路径',
  `is_delete` tinyint(1) NULL DEFAULT NULL COMMENT '是否删除',
  `enable` tinyint(1) NULL DEFAULT NULL COMMENT '是否启用',
  `md5` varchar(255) NULL DEFAULT NULL COMMENT 'md5值',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 65 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统文件表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_file
-- ----------------------------
INSERT INTO `sys_file` VALUES (60, '1.png', 'png', 1020, '/file/e0e987f26fcd430a9bed7ace279c37a9.png', 0, 0, '9fca10344f761a3af0ca5758eb95dff0');
INSERT INTO `sys_file` VALUES (61, '2.png', 'png', 1129, '/file/d035c345c22046d69fda006cacaa74fd.png', 0, 0, '8705363b046a135e72dd670e9bf52105');
INSERT INTO `sys_file` VALUES (62, '32aa18c4-7b08-4dba-98ad-61e5fccb0d05.png', 'png', 468, '/file/c79b676d61f34a9b8d6ea0319ebc680c.png', 0, 0, '640a6988941a032c0869ca27ac14cb5a');
INSERT INTO `sys_file` VALUES (63, 'c953fabf-613b-4f6f-b478-474b4f767e8d.png', 'png', 877, '/file/5a03bc37e4154e988ffbcc3a4e3d44d5.png', 0, 0, '3629bbb9a77d8e0f477f048576ee871c');
INSERT INTO `sys_file` VALUES (64, '8733789e367ef19411990c5a34a9db47.jpg', 'jpg', 125, '/file/db8f8d9e60424e16a76e12315292b3ba.jpg', 0, 0, '34f7c92d473277f6f9340f406523db59');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(255) NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(255) NULL DEFAULT NULL COMMENT '密码',
  `nickname` varchar(255) NULL DEFAULT NULL COMMENT '昵称',
  `email` varchar(255) NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(255) NULL DEFAULT NULL COMMENT '手机号码',
  `address` varchar(1600) NULL DEFAULT NULL COMMENT '地址',
  `avatar_url` varchar(255) NULL DEFAULT NULL COMMENT '头像链接',
  `role` varchar(255) NULL DEFAULT NULL COMMENT '角色',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员', '110@qq.com', '13888888888', '浙江宁波', '/avatar/f40e08ebb6414cb19701a7d2dc6453a8.jpg', 'admin');
INSERT INTO `sys_user` VALUES (8, 'user', 'e10adc3949ba59abbe56e057f20f883e', '美式加冰', '110@qq.com', '15865845852', '浙江杭州', '/avatar/f40e08ebb6414cb19701a7d2dc6453a8.jpg', 'user');
INSERT INTO `sys_user` VALUES (9, 'user2', 'e10adc3949ba59abbe56e057f20f883e', '美式加奶', '123@qq.com', '15688585878', '浙江宁波', NULL, 'user');

-- ----------------------------
-- Table structure for t_order
-- ----------------------------
DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` varchar(255) NULL DEFAULT NULL COMMENT '订单号',
  `total_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '总价',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户id',
  `link_user` varchar(255) NULL DEFAULT NULL COMMENT '联系人',
  `link_phone` varchar(255) NULL DEFAULT NULL COMMENT '联系电话',
  `link_address` varchar(255) NULL DEFAULT NULL COMMENT '地址',
  `state` varchar(255) NULL DEFAULT NULL COMMENT '订单状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 48 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_order
-- ----------------------------
INSERT INTO `t_order` VALUES (44, '20251104220751362934', 100.00, 8, '美式加冰', '15685985658', '浙江宁波', '已收货', '2025-11-04 22:07:51');
INSERT INTO `t_order` VALUES (45, '20251104220805198684', 50.00, 8, '美式加冰', '15854685265', '浙江杭州', '已发货', '2025-11-04 22:08:05');
INSERT INTO `t_order` VALUES (46, '20251104220842374465', 50.00, 8, '美式加冰', '16598565856', '浙江绍兴', '已支付', '2025-11-04 22:08:42');
INSERT INTO `t_order` VALUES (47, '20251104222111580125', 50.00, 8, '美式加冰', '15685985658', '浙江宁波', '待付款', '2025-11-04 22:21:11');

SET FOREIGN_KEY_CHECKS = 1;
