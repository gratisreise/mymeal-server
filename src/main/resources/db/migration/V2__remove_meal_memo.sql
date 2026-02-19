-- meals 테이블에서 memo 컬럼 삭제
-- Reaction.memo와 중복되어 제거
-- 식후 반응 메모는 Reaction 테이블에서 관리
ALTER TABLE meals DROP COLUMN IF EXISTS memo;
