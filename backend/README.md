# CommerceOps ERP — Backend

Spring Boot 기반 CommerceOps ERP 백엔드 서버입니다.

---

## 기술 스택

| 영역 | 기술 |
|---|---|
| Framework | Spring Boot 3.5.0 |
| Language | Java 17 |
| Build | Gradle |
| DB | MySQL 8.x (Docker, port 3308) |
| ORM | Spring Data JPA + Hibernate 6 |
| Auth | Spring Security 6 + JWT (jjwt 0.12.6) |

---

## 사전 준비

### 1. Java 17 확인

```powershell
java -version
```

설치 경로: `C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot`

### 2. Docker MySQL 컨테이너 실행

```powershell
docker run -d `
  --name commerceops-mysql `
  -e MYSQL_ROOT_PASSWORD=root1234 `
  -e MYSQL_DATABASE=commerceops `
  -e MYSQL_USER=commerce `
  -e MYSQL_PASSWORD=commerce1234 `
  -p 3308:3306 `
  mysql:8.0
```

> 로컬 3306 포트 충돌로 인해 **3308** 포트를 사용합니다.

이미 생성된 경우 시작만:

```powershell
docker start commerceops-mysql
```

---

## 실행 방법

```powershell
cd "C:\CommerceOps ERP\backend"
.\gradlew.bat bootRun
```

---

## 구현된 API

### Auth

| Method | URL | Auth | 설명 |
|---|---|---|---|
| GET | `/api/health` | 없음 | 서버 상태 확인 |
| POST | `/api/auth/signup` | 없음 | 회원가입 |
| POST | `/api/auth/login` | 없음 | 로그인 (JWT 발급) |
| GET | `/api/auth/me` | Bearer token | 내 정보 조회 |

### 시큐리티 정책

- `/api/health`, `/api/auth/signup`, `/api/auth/login` → 인증 없이 허용
- `/api/admin/**` → ADMIN, SUPER_ADMIN 역할만 허용
- 그 외 → JWT 인증 필요 (토큰 없으면 401)

---

## PowerShell 검증 명령어

### 한글 인코딩 설정 (필수)

```powershell
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
```

### Health Check

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/health" -Method Get | ConvertTo-Json
```

### 회원가입

```powershell
$body = [System.Text.Encoding]::UTF8.GetBytes('{"email":"user@example.com","password":"password123","name":"홍길동","phone":"010-1234-5678"}')
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/signup" -Method Post -ContentType "application/json; charset=utf-8" -Body $body | ConvertTo-Json -Depth 5
```

### 로그인 + 토큰 저장

```powershell
$body = [System.Text.Encoding]::UTF8.GetBytes('{"email":"user@example.com","password":"password123"}')
$login = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json; charset=utf-8" -Body $body
$token = $login.data.accessToken
Write-Host "Token: $token"
```

### 내 정보 조회

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/me" -Method Get -Headers @{ "Authorization" = "Bearer $token" } | ConvertTo-Json -Depth 5
```

### 관리자 로그인

```powershell
$body = [System.Text.Encoding]::UTF8.GetBytes('{"email":"admin@commerceops.com","password":"admin1234!"}')
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json; charset=utf-8" -Body $body | ConvertTo-Json -Depth 5
```

---

## 초기 계정 (DataInitializer 자동 생성)

| 이메일 | 비밀번호 | 역할 |
|---|---|---|
| admin@commerceops.com | admin1234! | ADMIN |

---

## 프로젝트 구조

```
backend/src/main/java/com/commerceops/erp/
├── CommerceOpsApplication.java
├── domain/
│   ├── auth/
│   │   ├── controller/AuthController.java      # POST /signup, /login, GET /me
│   │   ├── service/AuthService.java
│   │   └── dto/                                # SignupRequest/Response, LoginRequest/Response, MeResponse
│   └── user/
│       ├── entity/User.java
│       ├── repository/UserRepository.java
│       └── enums/UserRole.java, UserStatus.java
└── global/
    ├── config/
    │   ├── CorsConfig.java                     # localhost:3000 CORS 허용
    │   ├── JpaConfig.java                      # @EnableJpaAuditing
    │   └── DataInitializer.java                # 관리자 계정 초기화
    ├── security/
    │   ├── SecurityConfig.java                 # JWT 필터 체인, 권한 정책
    │   ├── JwtTokenProvider.java               # JWT 생성/검증
    │   ├── JwtAuthenticationFilter.java        # Bearer 토큰 추출 필터
    │   ├── CustomUserDetails.java
    │   └── CustomUserDetailsService.java
    ├── exception/
    │   ├── BusinessException.java
    │   ├── ErrorCode.java
    │   └── GlobalExceptionHandler.java
    ├── response/
    │   ├── ApiResponse.java
    │   └── ErrorResponse.java
    └── health/HealthController.java
```

---

## DB 연결 정보 (local)

| 항목 | 값 |
|---|---|
| Host | localhost:**3308** |
| Database | commerceops |
| Username | commerce |
| Password | commerce1234 |
| DDL Auto | update |

---

## JWT 설정 (application-local.yml)

| 항목 | 값 |
|---|---|
| 알고리즘 | HS384 (키 크기 432bit) |
| 만료 시간 | 3600000ms (1시간) |

---

## 트러블슈팅

### JAVA_HOME 미설정

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

### 포트 충돌 (8080)

```powershell
$owningPid = (Get-NetTCPConnection -LocalPort 8080 -State Listen).OwningProcess
Stop-Process -Id $owningPid -Force
```
