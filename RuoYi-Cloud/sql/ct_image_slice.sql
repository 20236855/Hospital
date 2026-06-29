-- =============================================
-- CT影像切片存储表
-- 用途：存储不同检查类型的CT切片图像文件路径
--       images存磁盘，表中仅存URL/路径
-- 注意：检验医生负责影像，诊断报告归门诊医生
-- =============================================

DROP TABLE IF EXISTS `ct_image_slice`;

CREATE TABLE `ct_image_slice` (
  `slice_id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '切片ID',
  `apply_id`           bigint       NOT NULL                COMMENT '申请单ID',
  `check_type`         varchar(30)  NOT NULL                COMMENT '检查类型：BRAIN_CT/LUNG_CT',
  `patient_id`         bigint       DEFAULT NULL            COMMENT '患者ID',
  `doctor_id`          bigint       DEFAULT NULL            COMMENT '检验医生ID',
  `slice_index`        int          NOT NULL DEFAULT 0      COMMENT '切片层号',
  `slice_z`            int          DEFAULT NULL            COMMENT 'Z层坐标',
  `image_path`         varchar(500) DEFAULT NULL            COMMENT '标注切片文件URL(/profile/ct_slices/...)',
  `raw_image_path`     varchar(500) DEFAULT NULL            COMMENT '原始切片文件URL',
  `hu_min`             double       DEFAULT NULL            COMMENT 'HU最小值',
  `hu_max`             double       DEFAULT NULL            COMMENT 'HU最大值',
  `hu_mean`            double       DEFAULT NULL            COMMENT 'HU平均值',
  `slice_thickness`    double       DEFAULT NULL            COMMENT '层厚mm',
  `pixel_spacing_x`    double       DEFAULT NULL            COMMENT '像素间距X',
  `pixel_spacing_y`    double       DEFAULT NULL            COMMENT '像素间距Y',
  `segmentation_data`  mediumtext   DEFAULT NULL            COMMENT '分割掩码JSON',
  `detection_result`   mediumtext   DEFAULT NULL            COMMENT '检测结节JSON',
  `file_name`          varchar(255) DEFAULT NULL            COMMENT '原始文件名',
  `remark`             varchar(512) DEFAULT NULL            COMMENT '备注',
  `create_time`        datetime     DEFAULT CURRENT_TIMESTAMP,
  `update_time`        datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`slice_id`) USING BTREE,
  INDEX `idx_apply_id`   (`apply_id`)   USING BTREE,
  INDEX `idx_check_type` (`check_type`) USING BTREE,
  INDEX `idx_patient_id` (`patient_id`) USING BTREE,
  INDEX `idx_doctor_id`  (`doctor_id`)  USING BTREE,
  CONSTRAINT `ct_slice_fk_apply` FOREIGN KEY (`apply_id`) REFERENCES `exam_apply` (`apply_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='CT影像切片存储（文件路径）' ROW_FORMAT=Dynamic;
