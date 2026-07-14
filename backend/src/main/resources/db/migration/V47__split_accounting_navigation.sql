UPDATE admin_menu_permissions
SET menu_path = CASE menu_key
        WHEN 'payments' THEN '/admin/accounting/payments'
        WHEN 'refund-history' THEN '/admin/accounting/refunds'
        WHEN 'settlements' THEN '/admin/accounting/settlements'
    END,
    updated_at = NOW(6)
WHERE menu_key IN ('payments', 'refund-history', 'settlements');
