-- ============================================
-- Update product standards to common size formats
-- Execution Date: 2026-04-27
-- ============================================

USE `s003`;

-- Update good_id 22 (怪盗基德装饰盒) - 改为标准尺码
UPDATE `good_standard` SET `value` = 'L' WHERE `good_id` = 22 AND `value` = '大号';
UPDATE `good_standard` SET `value` = 'M' WHERE `good_id` = 22 AND `value` = '中号';
UPDATE `good_standard` SET `value` = 'S' WHERE `good_id` = 22 AND `value` = '小号';

-- Update good_id 23 (海贼王系列) - 改为标准尺码
UPDATE `good_standard` SET `value` = 'L' WHERE `good_id` = 23 AND `value` = '大号';
UPDATE `good_standard` SET `value` = 'M' WHERE `good_id` = 23 AND `value` = '中号';
UPDATE `good_standard` SET `value` = 'S' WHERE `good_id` = 23 AND `value` = '小号';

-- Update good_id 24 (精美衬衫) - 改为标准尺码
UPDATE `good_standard` SET `value` = 'XL' WHERE `good_id` = 24 AND `value` = '大码';

-- Update good_id 25 (精美长裤) - 改为标准尺码
UPDATE `good_standard` SET `value` = 'XL' WHERE `good_id` = 25 AND `value` = '大码';

-- ============================================
-- Update completed
-- ============================================
