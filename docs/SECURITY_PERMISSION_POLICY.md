## v0.4.8 설정 권한 메모

- 사업자 설정과 약관/정책 버전 관리 관리자 API는 `SETTINGS_MANAGE` 권한을 요구한다.
- `SUPER_ADMIN`은 기존 `PermissionChecker` 정책에 따라 모든 설정 작업을 수행할 수 있다.
- 공개 조회 API인 `GET /api/settings/**`는 비로그인 사용자가 이용약관, 개인정보처리방침, 배송/반품 정책, 공개 사업자 정보를 볼 수 있도록 인증을 요구하지 않는다.
- 사업자 설정 변경 감사 로그는 고객센터 전화번호와 이메일을 `phone`, `email` 키로 기록해 기존 민감정보 마스킹 로직을 적용한다.
- 약관/정책 본문 전체는 감사 로그에 남기지 않고 타입, 버전, 제목, 본문 길이만 기록한다.
# 愿由ъ옄 API Permission ?뺤콉

湲곗? 踰꾩쟾: `v0.4.7-admin-audit-log-expansion`

## 紐⑹쟻

v0.4.6遺??愿由ъ옄 API??湲곗〈 role 湲곕컲 ?묎렐 ?쒖뼱瑜??좎??섎㈃?? ?ㅼ젣 湲곕뒫 ?ㅽ뻾? permission code 湲곗??쇰줈 ??踰???寃利앺븳?? role? 愿由ъ옄 ?곸뿭???ㅼ뼱?????덈뒗 1李?湲곗??닿퀬, permission code???쎄린, ?곌린, ?곹깭 蹂寃? ?섎텋, 沅뚰븳 愿由?媛숈? ?몃? ?묒뾽 ?덉슜 湲곗??대떎.

## 湲곕낯 ?뺤콉

- `USER`? 鍮꾨줈洹몄씤 ?ъ슜?먮뒗 `/api/admin/**`???묎렐?????녿떎.
- `MANAGER`, `ADMIN`, `SUPER_ADMIN`? role 湲곕컲 1李??묎렐 ??곸씠??
- `SUPER_ADMIN`? 紐⑤뱺 ?쒖꽦 permission??蹂댁쑀??寃껋쑝濡?媛꾩＜?쒕떎.
- `ADMIN`, `MANAGER`??沅뚰븳 洹몃９怨?role 湲곕낯 洹몃９?쇰줈 怨꾩궛??effective permission code瑜?湲곗??쇰줈 API ?ㅽ뻾 沅뚰븳??寃利앺븳??
- 沅뚰븳???놁쑝硫?403怨??쒗빐???묒뾽???섑뻾??沅뚰븳???놁뒿?덈떎. 愿由ъ옄?먭쾶 沅뚰븳???붿껌?섏꽭????硫붿떆吏瑜?諛섑솚?쒕떎.

## 援ы쁽 援ъ“

- `PermissionCodes`: permission code ?곸닔 紐⑥쓬.
- `PermissionChecker`: ?꾩옱 ?ъ슜?먯? ?꾩슂??permission code瑜?諛쏆븘 沅뚰븳??寃利앺븳??
- `PermissionMatrixService`: ?ъ슜??effective permission??怨꾩궛?쒕떎.
- Controller method ?쒖옉遺?먯꽌 `permissionChecker.require(currentUser, PermissionCodes.X)`瑜??몄텧?쒕떎.

## 二쇱슂 沅뚰븳 留ㅽ븨

| ?곸뿭 | permission code |
| --- | --- |
| ??쒕낫???댁쁺 遺꾩꽍 | `DASHBOARD_READ` |
| ?곹뭹 議고쉶 | `PRODUCT_READ` |
| ?곹뭹 ?앹꽦/?섏젙/??젣/?곸꽭 釉붾줉/?댁쁺 硫붾え | `PRODUCT_WRITE` |
| ?곹뭹 ?곹깭 蹂寃?| `PRODUCT_STATUS_CHANGE` |
| ?곹뭹 ???蹂寃?| `PRODUCT_BULK_UPDATE` |
| 移댄뀒怨좊━ 愿由?| `CATEGORY_MANAGE` |
| 諛곕꼫 愿由?| `BANNER_MANAGE` |
| 二쇰Ц/諛곗넚/諛섑뭹 議고쉶 | `ORDER_READ` |
| 二쇰Ц/諛곗넚/諛섑뭹 ?곹깭 蹂寃?| `ORDER_STATUS_CHANGE` |
| 寃곗젣 痍⑥냼/?섎텋 | `PAYMENT_REFUND` |
| ?ш퀬 議고쉶 | `INVENTORY_READ` |
| ?ш퀬 議곗젙/?낃퀬/?좊떦 | `INVENTORY_WRITE` |
| SKU 생성/수정/활성 변경 | `SKU_MANAGE` |
| 바코드 발급/재발급 | `BARCODE_MANAGE` |
| 생산 주문 생성/수정/시작/완료/취소 | `PRODUCTION_MANAGE` |
| 李쎄퀬/?ш퀬 ?대룞 愿由?| `WAREHOUSE_MANAGE` |
| ?뚭퀎/留ㅼ텧 議고쉶 | `ACCOUNTING_READ` |
| ?뚭퀎 留덇컧 ?꾨낫 | `ACCOUNTING_CLOSE` |
| 荑좏룿 愿由?| `COUPON_MANAGE` |
| 由щ럭 ?댁쁺 | `REVIEW_MODERATE` |
| 臾몄쓽 ?듬?/醫낅즺 | `INQUIRY_REPLY` |
| 吏곸썝/HR 愿由?| `STAFF_MANAGE` |
| 沅뚰븳 洹몃９/沅뚰븳 留ㅽ듃由?뒪 愿由?| `ROLE_MANAGE` |
| 媛먯궗 濡쒓렇 議고쉶 | `AUDIT_LOG_READ` |

## ?꾩냽 ?댁뒋

- v0.4.7遺???몄쬆??愿由ъ옄??permission code 遺議깆쑝濡??명븳 403? `PERMISSION_DENIED` audit log濡?湲곕줉?쒕떎.
- Controller 吏곸젒 ?몄텧 諛⑹떇?먯꽌 annotation/AOP 湲곕컲 ?좎뼵??沅뚰븳 寃利앹쑝濡?怨좊룄?뷀븷 ???덈떎.
- ?꾩껜 愿由ъ옄 API???먮룞?붾맂 沅뚰븳蹂?200/403 ?뚯뒪??而ㅻ쾭由ъ????꾩냽 ?덉젙??踰붿쐞濡??좎??쒕떎.
