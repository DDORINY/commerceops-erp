UPDATE permissions
SET name = '회계 거래 관리',
    description = '회계 거래 생성과 보정 후보 작업을 수행합니다.',
    updated_at = NOW(6)
WHERE code = 'ACCOUNTING_MANAGE';

UPDATE permissions
SET name = '반품 배송비 회계 처리',
    description = '반품 배송비 부담 주체 기준으로 회계 거래를 생성하고 관리합니다.',
    updated_at = NOW(6)
WHERE code = 'RETURN_FEE_MANAGE';

UPDATE permissions
SET name = '택배비 매입 관리',
    description = '배송 방법과 택배사 기준으로 택배비 비용 회계 거래를 생성하고 관리합니다.',
    updated_at = NOW(6)
WHERE code = 'SHIPPING_COST_MANAGE';

UPDATE permissions
SET name = '정산 배치 관리',
    description = '기간별 정산 배치를 생성하고 정산 항목을 관리합니다.',
    updated_at = NOW(6)
WHERE code = 'SETTLEMENT_MANAGE';

UPDATE admin_menu_permissions
SET menu_label = '결제 내역',
    required_permission_code = 'ACCOUNTING_READ',
    updated_at = NOW(6)
WHERE menu_key = 'payments';

UPDATE admin_menu_permissions
SET menu_label = '환불 내역',
    required_permission_code = 'PAYMENT_REFUND',
    updated_at = NOW(6)
WHERE menu_key = 'refund-history';

UPDATE admin_menu_permissions
SET menu_label = '정산 관리',
    required_permission_code = 'ACCOUNTING_CLOSE',
    updated_at = NOW(6)
WHERE menu_key = 'settlements';
