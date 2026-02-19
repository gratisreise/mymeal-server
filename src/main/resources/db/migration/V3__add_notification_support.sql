-- 식후 반응 추적 기능을 위한 스키마 변경
-- Phase 1: notification_sent 컬럼 추가
ALTER TABLE meals ADD COLUMN IF NOT EXISTS notification_sent BOOLEAN NOT NULL DEFAULT false;

-- Phase 2: pgvector 확장 설치 (벡터 유사도 검색용)
CREATE EXTENSION IF NOT EXISTS vector;

-- Phase 3: meal_logs 테이블 생성 (Meal + Reaction 통합 로그)
CREATE TABLE IF NOT EXISTS meal_logs (
    id BIGSERIAL PRIMARY KEY,
    meal_id BIGINT NOT NULL UNIQUE REFERENCES meals(id) ON DELETE CASCADE,
    member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    meal_summary TEXT NOT NULL,
    reaction_summary TEXT NOT NULL,
    combined_summary TEXT NOT NULL,
    embedding vector(1536),
    embedding_created_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Phase 4: 인덱스 생성
-- HNSW 인덱스: 벡터 유사도 검색용 (cosine distance)
CREATE INDEX IF NOT EXISTS idx_meal_logs_embedding_hnsw
    ON meal_logs USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);

-- B-Tree 인덱스: member_id 검색용
CREATE INDEX IF NOT EXISTS idx_meal_logs_member_id
    ON meal_logs(member_id);

-- B-Tree 인덱스: meal_id 검색용
CREATE INDEX IF NOT EXISTS idx_meal_logs_meal_id
    ON meal_logs(meal_id);

-- 코멘트 추가
COMMENT ON COLUMN meals.notification_sent IS '식후 반응 알림 발송 여부';
COMMENT ON COLUMN meal_logs.meal_id IS '연결된 식사 ID';
COMMENT ON COLUMN meal_logs.member_id IS '회원 ID';
COMMENT ON COLUMN meal_logs.meal_summary IS '식사 정보 요약 (음식명, 영양성분 등)';
COMMENT ON COLUMN meal_logs.reaction_summary IS '식후 반응 요약';
COMMENT ON COLUMN meal_logs.combined_summary IS 'meal_summary + reaction_summary 합친 텍스트 (임베딩 입력용)';
COMMENT ON COLUMN meal_logs.embedding IS 'OpenAI text-embedding-3-small 벡터 (1536차원)';
COMMENT ON COLUMN meal_logs.embedding_created_at IS '임베딩 생성 시점';
