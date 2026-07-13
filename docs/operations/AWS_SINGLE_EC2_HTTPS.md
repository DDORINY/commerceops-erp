# AWS 단일 EC2 HTTPS 운영 가이드

대상 도메인: `commerceops.ddoriny.com`

## 구조

```text
Internet -> EC2 Security Group (80, 443) -> nginx
  HTTP ACME challenge -> certbot-www volume
  HTTP other paths    -> HTTPS 301 (인증서 발급 후)
  HTTPS /             -> frontend:3000
  HTTPS /api, uploads -> backend:8080

systemd timer -> certbot one-shot container -> letsencrypt volume -> nginx reload
```

Nginx는 시작 시 인증서 존재 여부에 따라 HTTP 부트스트랩 설정 또는 HTTPS 설정을 선택한다. 따라서 최초 발급 전에도 HTTP 서비스가 정상 기동한다.

최초 HTTPS 반영 시 HSTS는 복구 가능한 짧은 값인 `max-age=300`만 사용한다. 이 단계에서는 `includeSubDomains`와 `preload`를 사용하지 않는다. 인증서, HTTP에서 HTTPS로의 301 응답, HTTPS 메인/API 200 응답, 인증서 자동 갱신 dry-run을 모두 검증한 뒤에만 별도 변경으로 `max-age=31536000` 승격을 검토한다. 모든 하위 도메인의 HTTPS 지원을 별도로 검증하기 전에는 `includeSubDomains`와 `preload`를 추가하지 않는다.

## Elastic IP 및 DNS 연결 전 체크리스트

- [ ] Elastic IP와 EC2가 같은 리전이며 연결 대상을 확인했다.
- [ ] EC2 Security Group은 인터넷에서 TCP 80/443을 허용한다.
- [ ] MySQL 3306, backend 8080, frontend 3000은 외부에 열지 않는다.
- [ ] SSH 22는 가능한 한 관리자 고정 IP/CIDR만 허용한다.
- [ ] OS 방화벽을 사용하면 80/tcp와 443/tcp를 허용한다.
- [ ] `commerceops.ddoriny.com` A 레코드를 Elastic IP로 설정할 권한이 있다.
- [ ] 기존 A/AAAA/CNAME 충돌을 확인했다. IPv6 미구성 시 잘못된 AAAA를 두지 않는다.
- [ ] DNS 전환 전 TTL과 전파 시간을 확인했다.
- [ ] 운영 CORS origin과 media public base URL을 최종 HTTPS 도메인으로 준비했다.
- [ ] 인증서 만료 알림용 운영 이메일과 서버 NTP 동기화를 확인했다.
- [ ] MySQL/uploads volume 백업 및 복구 절차를 확인했다.

## DNS 연결 후 최초 발급

먼저 DNS가 Elastic IP를 반환하고 외부에서 HTTP가 도달하는지 확인한다.

```bash
dig +short A commerceops.ddoriny.com
curl -I http://commerceops.ddoriny.com/
```

새 Nginx 구조를 반영한다. 인증서가 없으므로 HTTP 설정이 선택된다.

```bash
cd ~/commerceops-erp
sudo docker compose --env-file .env.prod -f docker-compose.prod.yml up -d --no-deps nginx
sudo docker compose --env-file .env.prod -f docker-compose.prod.yml ps
```

실제 운영 이메일로 바꿔 인증서를 발급한다.

```bash
sudo docker compose \
  --env-file .env.prod \
  -f docker-compose.prod.yml \
  --profile tls \
  run --rm --no-deps certbot \
  certonly --webroot --webroot-path /var/www/certbot \
  --email "$CERTBOT_EMAIL" --agree-tos --no-eff-email \
  -d commerceops.ddoriny.com
```

`CERTBOT_EMAIL`은 `.env.prod`에 저장하지 않는다. 발급 직전에 운영자가 터미널에서 `read`로 입력하고 빈 값이 아닌지 확인한다. 이메일 값은 채팅, 명령 기록, 배포 로그에 출력하지 않는다.

발급 성공 후 Nginx만 재생성한다. 인증서를 감지해 HTTPS 설정과 HTTP 301 리다이렉트가 활성화된다.

```bash
sudo docker compose --env-file .env.prod -f docker-compose.prod.yml up -d --no-deps --force-recreate nginx
sudo docker compose --env-file .env.prod -f docker-compose.prod.yml exec -T nginx nginx -t
```

HTTPS 메인 200 응답을 확인한 뒤 `.env.prod`의 `PUBLIC_BASE_URL`만 `https://commerceops.ddoriny.com`으로 변경하고 backend만 `--no-deps --force-recreate`로 재생성한다. MySQL과 frontend는 재생성하지 않는다. backend 정상 기동, API 200, 상품 이미지 또는 업로드 URL의 HTTPS 반환을 다시 확인한다.

## 자동 갱신

최초 발급과 HTTPS 검증 후 systemd timer를 설치한다.

```bash
sudo install -m 0644 deploy/systemd/commerceops-certbot-renew.service /etc/systemd/system/
sudo install -m 0644 deploy/systemd/commerceops-certbot-renew.timer /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now commerceops-certbot-renew.timer
systemctl list-timers commerceops-certbot-renew.timer
bash deploy/scripts/renew-certificates.sh --dry-run
```

`letsencrypt` volume은 인증서 원본이다. `docker compose down -v`를 실행하거나 이 volume을 삭제하지 않는다.

## 배포 후 검증

```bash
git status --short --branch
sudo docker compose --env-file .env.prod -f docker-compose.prod.yml ps
sudo docker inspect --format '{{.Name}} restartCount={{.RestartCount}} status={{.State.Status}}' \
  commerceops-mysql commerceops-backend commerceops-frontend commerceops-nginx

curl -sS -o /dev/null -w '%{http_code} %{redirect_url}\n' http://commerceops.ddoriny.com/
curl -sS -o /dev/null -w '%{http_code}\n' https://commerceops.ddoriny.com/
curl -sS -o /dev/null -w '%{http_code}\n' https://commerceops.ddoriny.com/api/products

openssl s_client -connect commerceops.ddoriny.com:443 \
  -servername commerceops.ddoriny.com </dev/null 2>/dev/null \
  | openssl x509 -noout -subject -issuer -dates

sudo docker compose --env-file .env.prod -f docker-compose.prod.yml exec -T nginx nginx -t
sudo docker compose --env-file .env.prod -f docker-compose.prod.yml logs --tail=100 nginx
sudo docker compose --env-file .env.prod -f docker-compose.prod.yml logs --tail=100 backend
systemctl is-enabled commerceops-certbot-renew.timer
systemctl is-active commerceops-certbot-renew.timer
```

정상 기준은 HTTP 301, HTTPS 루트/API 200, 인증서 도메인과 유효기간 정상, MySQL healthy, 의도하지 않은 restart count 증가 없음이다.

최초 반영 완료 후에도 HSTS는 `max-age=300`으로 유지한다. 위 검증과 `renew --dry-run` 성공을 확인한 다음 별도 검토 및 변경으로만 `max-age=31536000`으로 승격한다.
