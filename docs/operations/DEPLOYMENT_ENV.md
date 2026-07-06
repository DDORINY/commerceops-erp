# 배포와 환경 분리 문서

기준 버전: `v0.2.6`

v0.2.6은 실제 배포 자동화를 완성하지 않고, CI 자동 검증과 환경 변수 분리 기준을 먼저 고정한다.

## CI

GitHub Actions workflow:

- `.github/workflows/ci.yml`
- 트리거: `main`, `v*` 브랜치 push, `main` 대상 pull request
- Frontend: Node 20, `npm ci`, `npm run lint`, `npm run build`
- Backend: Java 17, Gradle cache, `./gradlew test`
- Next 빌드는 CI 전용 `NEXT_DIST_DIR=.next-build-check-ci`를 사용한다.

## 환경 파일 기준

커밋 가능한 예시:

- `.env.example`
- `frontend/.env.example`
- `backend/src/main/resources/application-prod.yml`

커밋 금지:

- `.env`
- `.env.local`
- `frontend/.env.local`
- `backend/src/main/resources/application-local.yml`
- 실제 DB 비밀번호, JWT secret, API key

## Backend 환경 변수

| 이름 | 용도 |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | 실행 프로파일. 로컬은 `local`, 운영은 `prod` |
| `DB_URL` | 운영 DB JDBC URL |
| `DB_USERNAME` | 운영 DB 사용자 |
| `DB_PASSWORD` | 운영 DB 비밀번호 |
| `JWT_SECRET` | JWT 서명 secret |
| `JWT_ACCESS_EXPIRATION_MS` | access token 만료 시간 |
| `JWT_REFRESH_EXPIRATION_MS` | refresh token 만료 시간 |
| `COMMERCEOPS_CORS_ALLOWED_ORIGINS` | 허용 origin 목록. 쉼표로 여러 개 지정 |
| `COMMERCEOPS_MEDIA_UPLOAD_DIR` | 업로드 파일 저장 경로 |
| `COMMERCEOPS_MEDIA_PUBLIC_BASE_URL` | 업로드 파일 공개 base URL |

## Frontend 환경 변수

| 이름 | 용도 |
| --- | --- |
| `NEXT_PUBLIC_API_BASE_URL` | 프론트가 호출할 API base URL |

## 운영 반영 전 확인

- 운영 DB에는 Flyway migration 적용 전 백업을 수행한다.
- `SPRING_PROFILES_ACTIVE=prod`에서는 Hibernate `ddl-auto=validate`를 사용한다.
- CORS origin에는 실제 프론트 도메인만 지정한다.
- JWT secret은 저장소에 커밋하지 않고 배포 플랫폼 secret으로 주입한다.
- 파일 업로드 경로는 애플리케이션 재배포로 삭제되지 않는 영속 스토리지를 사용한다.
