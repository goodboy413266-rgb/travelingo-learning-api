-- ============================================
-- Travelingo Database Schema v1.2
-- 캠퍼스 MySQL 서버용 (project-db-campus.smhrd.com:3307)
-- DB: campus_24KDT_LI8_p2_3
-- ============================================
-- ⚠️ 중요: Entity에서 id를 Long(=BIGINT)으로 쓰므로
--    모든 PK/FK를 BIGINT로 맞춰야 합니다!
-- ============================================

-- ── 1. 사용자 ──────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    email         VARCHAR(100) NOT NULL,
    password      VARCHAR(255) NOT NULL COMMENT 'bcrypt 암호화',
    nickname      VARCHAR(50)  NOT NULL,
    role          ENUM('STUDENT','ADMIN') NOT NULL DEFAULT 'STUDENT',
    learning_lang VARCHAR(20)  NOT NULL DEFAULT 'english',
    interface_lang VARCHAR(10) NOT NULL DEFAULT 'ko',
    user_level    INT          NOT NULL DEFAULT 1 COMMENT '1~3단계',
    weekly_goal   INT          NOT NULL DEFAULT 5 COMMENT '주간목표시간(3/5/7)',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_email (email)
) ENGINE=InnoDB COMMENT='서비스 회원';

-- ── 2. 여행 설정 ───────────────────────────
CREATE TABLE IF NOT EXISTS trip (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    destination VARCHAR(100) NOT NULL COMMENT '여행 목적지',
    d_day       DATE         NOT NULL COMMENT '출발 예정일',
    language    VARCHAR(20)  NOT NULL DEFAULT 'english',
    user_level  INT          NOT NULL DEFAULT 1,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE COMMENT '현재 활성 여행',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='여행 설정 (복수 등록 가능)';

-- ── 3. 챕터 ────────────────────────────────
CREATE TABLE IF NOT EXISTS chapter (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    language         VARCHAR(20)  NOT NULL DEFAULT 'english',
    category         VARCHAR(50)  NOT NULL COMMENT '공항/호텔/식당 등',
    chapter_no       INT          NOT NULL COMMENT 'Ch1~Ch10',
    title            VARCHAR(100) NOT NULL,
    persona_setting  TEXT         COMMENT 'AI 페르소나 프롬프트',
    total_sessions   INT          NOT NULL DEFAULT 10,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='여행 시나리오 챕터';

-- ── 4. 학습 콘텐츠 ──────────────────────────
CREATE TABLE IF NOT EXISTS learning_content (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    chapter_id   BIGINT       NOT NULL,
    session_no   INT          NOT NULL COMMENT '1~10',
    session_name VARCHAR(50)  NOT NULL COMMENT '예약확인/체크인 등',
    type         ENUM('WORD','EXPRESSION','QNA') NOT NULL,
    priority     INT          NOT NULL DEFAULT 1 COMMENT '1=높음 3=낮음',
    content_en   VARCHAR(255) NOT NULL COMMENT '영어 원문',
    content_ko   VARCHAR(255) NOT NULL COMMENT '한국어 번역',
    example_en   TEXT,
    example_ko   TEXT,
    qna_q_en     TEXT,
    qna_a_en     TEXT,
    qna_q_ko     TEXT,
    qna_a_ko     TEXT,
    audio_path   VARCHAR(255) COMMENT 'TTS 캐시 파일 경로',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (chapter_id) REFERENCES chapter(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='단어/표현/문답 학습 데이터';

-- ── 5. 문화 팁 ──────────────────────────────
CREATE TABLE IF NOT EXISTS culture_tip (
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    chapter_id BIGINT   NOT NULL,
    session_no INT      NOT NULL,
    tip_ko     TEXT     NOT NULL,
    tip_en     TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (chapter_id) REFERENCES chapter(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='세션별 현지 문화 팁';

-- ── 6. 학습 진도 ────────────────────────────
CREATE TABLE IF NOT EXISTS user_progress (
    id              BIGINT   NOT NULL AUTO_INCREMENT,
    user_id         BIGINT   NOT NULL,
    chapter_id      BIGINT   NOT NULL,
    session_no      INT      NOT NULL,
    step_word       BOOLEAN  NOT NULL DEFAULT FALSE COMMENT 'Step A 완료',
    step_expr       BOOLEAN  NOT NULL DEFAULT FALSE COMMENT 'Step B 완료',
    step_ai         BOOLEAN  NOT NULL DEFAULT FALSE COMMENT 'Step C 완료',
    progress_pct    INT      NOT NULL DEFAULT 0 COMMENT '0~100%',
    last_studied_at DATETIME,
    study_duration  INT      NOT NULL DEFAULT 0 COMMENT '누적학습초',
    PRIMARY KEY (id),
    UNIQUE KEY uk_progress (user_id, chapter_id, session_no),
    FOREIGN KEY (user_id)    REFERENCES users(id)   ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapter(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='사용자 학습 진도';

-- ── 7. 단어 정오답 ──────────────────────────
CREATE TABLE IF NOT EXISTS word_result (
    id            BIGINT   NOT NULL AUTO_INCREMENT,
    user_id       BIGINT   NOT NULL,
    content_id    BIGINT   NOT NULL,
    is_correct    BOOLEAN  NOT NULL,
    attempt_count INT      NOT NULL DEFAULT 1,
    studied_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id)   REFERENCES users(id)            ON DELETE CASCADE,
    FOREIGN KEY (content_id) REFERENCES learning_content(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='플래시카드 정오답 이력';

-- ── 8. AI 대화 세션 ─────────────────────────
CREATE TABLE IF NOT EXISTS conv_session (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    user_id       BIGINT       NOT NULL,
    chapter_id    BIGINT       NOT NULL,
    session_no    INT          NOT NULL,
    persona       VARCHAR(500) NOT NULL COMMENT '적용 페르소나',
    message_count INT          NOT NULL DEFAULT 0 COMMENT '최대10',
    started_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at      DATETIME,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id)    REFERENCES users(id)   ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapter(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='AI 대화 세션';

-- ── 9. AI 대화 메시지 ──────────────────────
CREATE TABLE IF NOT EXISTS conv_message (
    id              BIGINT   NOT NULL AUTO_INCREMENT,
    conv_session_id BIGINT   NOT NULL,
    role            ENUM('USER','ASSISTANT') NOT NULL,
    content         TEXT     NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (conv_session_id) REFERENCES conv_session(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='AI 대화 메시지 (최대 10회)';

-- ============================================
-- 실행 완료 확인: SHOW TABLES;
-- ============================================
