# DB 마이그레이션 운영 기준

기준 버전: `v0.2.5`

CommerceOps ERP는 v0.2.5부터 Flyway로 운영 스키마 변경 이력을 관리한다.

## 현재 기준

- 도구: Flyway
- 위치: `backend/src/main/resources/db/migration`
- 초기 DDL: `V1__initial_schema.sql`
- 대상 DB: MySQL 8.0
- 테스트: `application-test.yml`에서 Flyway를 끄고 H2 `create-drop` 회귀 테스트를 유지한다.

## 파일 규칙

```text
V{version}__{description}.sql
```

예:

```text
V1__initial_schema.sql
V2__add_notification_tables.sql
V3__add_audit_log_indexes.sql
```

현재 적용 파일:

- `V1__initial_schema.sql`
- `V2__add_notifications.sql`

## 운영 원칙

- 이미 배포된 migration 파일은 수정하지 않는다.
- 스키마 변경은 새 migration 파일로만 추가한다.
- 기존 데이터 보정이 필요한 경우 DDL과 DML을 분리하거나, 같은 버전 작업 체크리스트에 검증 절차를 기록한다.
- 운영 DB 반영 전에는 백업, dry-run, rollback 방안을 문서화한다.
- `audit_logs`처럼 스냅샷 보존 성격이 강한 테이블은 운영 데이터 보존을 우선하고 FK를 최소화한다.

## 기존 개발 DB 주의

v0.2.5 이전 로컬/개발 DB는 Hibernate `ddl-auto=update`로 생성되었을 수 있다. `baseline-on-migrate=true`, `baseline-version=0`을 사용하되, 운영 DB에 적용하기 전에는 실제 컬럼/인덱스/FK 차이를 수동 점검해야 한다.
