-- ============================================================
-- Travelingo 초기 데이터 (챕터 목록)
-- ============================================================
-- Spring Boot가 시작할 때 자동으로 실행됨
-- chapter 테이블에 기본 챕터 데이터를 삽입
-- 이미 존재하는 경우 중복 삽입 방지 (INSERT IGNORE)
--
-- ★ 중요: learning_content 테이블의 chapter_id가 이 테이블의 id를 참조함
-- ============================================================

-- 챕터 데이터 삽입 (10개 챕터)
-- 각 챕터: language(언어), category(카테고리), chapter_no(번호), title(제목), total_sessions(세션수)
-- ★ Chapter 1: 엑셀 데이터에 따라 "출국 및 기내" (10세션)
-- 나머지 챕터는 추후 엑셀 업로드 시 업데이트 예정
-- ★ ON DUPLICATE KEY UPDATE: 인코딩 오류로 한글이 깨진 데이터를 자동 복구
INSERT INTO chapter (id, language, category, chapter_no, title, persona_setting, total_sessions, created_at) VALUES
(1, 'english', '출국 및 기내',   1, '출국 및 기내',   '공항 직원',           10, NOW()),
(2, 'english', '공항에서',       2, '공항에서',       '공항 직원',           4, NOW()),
(3, 'english', '음식 & 식사',    3, '음식 & 식사',    '레스토랑 웨이터',      5, NOW()),
(4, 'english', '숙박시설',       4, '숙박시설',       '호텔 프론트 직원',     4, NOW()),
(5, 'english', '쇼핑하기',       5, '쇼핑하기',       '쇼핑몰 점원',         4, NOW()),
(6, 'english', '교통 & 길찾기',  6, '교통 & 길찾기',  '택시 기사',           4, NOW()),
(7, 'english', '긴급 상황',      7, '긴급 상황',      '경찰관',              3, NOW()),
(8, 'english', '문화 & 여가',    8, '문화 & 여가',    '관광 가이드',         4, NOW()),
(9, 'english', '일상 대화',      9, '일상 대화',      '현지 친구',           5, NOW()),
(10, 'english', '고급 표현',     10, '고급 표현',     '비즈니스 파트너',      4, NOW())
ON DUPLICATE KEY UPDATE
    category = VALUES(category),
    title = VALUES(title),
    persona_setting = VALUES(persona_setting),
    total_sessions = VALUES(total_sessions);
