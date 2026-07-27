INSERT INTO sys_user (id, username, password, real_name, phone, email, status, must_change_password, created_by, created_time, updated_by, updated_time) VALUES
(1, 'admin', '$2a$10$SjVONS8FnzdxfgUQ5busP.GtlpQtxzoM9VMxzrxFfzMILmNb71t3q', '系统管理员', '13800000000', 'admin@example.com', 1, 0, NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP);

INSERT INTO sys_role (id, role_code, role_name, description, parent_id, sort_order, status, created_time, updated_time) VALUES
(1, 'SYSTEM_ADMIN', '系统管理员', '拥有系统全部权限', NULL, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'EMPLOYEE', '普通员工', '基本操作权限', NULL, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO sys_permission (id, permission_code, permission_name, permission_type, parent_id, path, icon, sort_order, status, created_time, updated_time) VALUES
(1,  'system',           '系统管理',     'MENU',   NULL,  '/system',          'Setting',    1,  1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2,  'system:user',       '用户管理',     'MENU',   1,    '/system/user',     'User',       1,  1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3,  'system:user:add',   '用户新增',     'BUTTON', 2,    NULL,               NULL,         1,  1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4,  'system:user:edit',  '用户编辑',     'BUTTON', 2,    NULL,               NULL,         2,  1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5,  'system:user:delete','用户删除',     'BUTTON', 2,    NULL,               NULL,         3,  1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6,  'system:user:query', '用户查询',     'BUTTON', 2,    NULL,               NULL,         4,  1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7,  'system:user:export','用户导出',     'BUTTON', 2,    NULL,               NULL,         5,  1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8,  'system:user:import','用户导入',     'BUTTON', 2,    NULL,               NULL,         6,  1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9,  'system:role',       '角色管理',     'MENU',   1,    '/system/role',     'UserFilled', 2,  1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'system:role:add',   '角色新增',     'BUTTON', 9,    NULL,               NULL,         1,  1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 'system:role:edit',  '角色编辑',     'BUTTON', 9,    NULL,               NULL,         2,  1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 'system:role:delete','角色删除',     'BUTTON', 9,    NULL,               NULL,         3,  1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 'system:role:query', '角色查询',     'BUTTON', 9,    NULL,               NULL,         4,  1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 'system:permission', '权限管理',     'MENU',   1,    '/system/permission','Lock',       3,  1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 'system:permission:add',   '权限新增', 'BUTTON', 14, NULL,              NULL,         1,  1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, 'system:permission:edit',  '权限编辑', 'BUTTON', 14, NULL,              NULL,         2,  1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 'system:permission:delete','权限删除', 'BUTTON', 14, NULL,              NULL,         3,  1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 'system:permission:query', '权限查询', 'BUTTON', 14, NULL,              NULL,         4,  1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(19, 'biz',              '数据管理',     'MENU',   NULL, '/biz',             'DataAnalysis',2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20, 'biz:model',        '数据模型',     'MENU',   19,   '/biz/model',       'Grid',        1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21, 'biz:model:add',    '模型新增',     'BUTTON', 20,   NULL,               NULL,          1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(22, 'biz:model:edit',   '模型编辑',     'BUTTON', 20,   NULL,               NULL,          2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(23, 'biz:model:delete', '模型删除',     'BUTTON', 20,   NULL,               NULL,          3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(24, 'biz:model:query',  '模型查询',     'BUTTON', 20,   NULL,               NULL,          4, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(25, 'settings',         '系统设置',     'MENU',   NULL, '/settings',        'Tools',       3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(26, 'settings:ui',      '界面配置',     'MENU',   25,   '/settings/ui',     'Monitor',     1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(27, 'settings:security','安全设置',     'MENU',   25,   '/settings/security','Lock',       2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(28, 'settings:notification','通知设置', 'MENU',   25,   '/settings/notification','Bell',   3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(29, 'settings:log',     '日志管理',     'MENU',   25,   '/settings/log',    'Document',    4, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(30, 'system:user:reset-password', '重置密码', 'BUTTON', 2, NULL,            NULL,          7, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1);

INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(1, 1),  (1, 2),  (1, 3),  (1, 4),  (1, 5),  (1, 6),  (1, 7),  (1, 8),  (1, 30),
(1, 9),  (1, 10), (1, 11), (1, 12), (1, 13),
(1, 14), (1, 15), (1, 16), (1, 17), (1, 18),
(1, 19), (1, 20), (1, 21), (1, 22), (1, 23), (1, 24),
(1, 25), (1, 26), (1, 27), (1, 28), (1, 29),
(2, 1),  (2, 2),  (2, 6),
(2, 9),  (2, 13),
(2, 19), (2, 20), (2, 24);

INSERT INTO sys_config (id, config_key, config_value, config_type, description, created_time, updated_time) VALUES
(1, 'sys.theme',              'light',          'UI',          '系统主题',          CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'sys.layout',             'sidebar',        'UI',          '布局模式',          CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'sys.language',           'zh-CN',          'UI',          '系统语言',          CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'sys.password.minLength', '8',              'SECURITY',    '密码最小长度',      CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'sys.password.requireUpper', 'true',        'SECURITY',    '密码需包含大写字母', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'sys.password.requireLower', 'true',        'SECURITY',    '密码需包含小写字母', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'sys.password.requireDigit',  'true',       'SECURITY',    '密码需包含数字',    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'sys.password.requireSpecial', 'false',     'SECURITY',    '密码需包含特殊字符', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'sys.login.maxAttempts',  '5',              'SECURITY',    '最大登录尝试次数',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10,'sys.session.timeout',    '30',             'SECURITY',    '会话超时时间(分钟)',CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11,'sys.notification.email', 'true',           'NOTIFICATION','邮件通知开关',      CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12,'sys.notification.sms',   'false',          'NOTIFICATION','短信通知开关',      CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO biz_data_model (id, model_code, model_name, description, table_name, status, created_time, updated_time) VALUES
(1, 'CUSTOMER', '客户信息', '客户基础信息管理', 'biz_customer', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'PRODUCT', '产品信息', '产品基础信息管理', 'biz_product', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'ORDER', '订单信息', '订单基础信息管理', 'biz_order', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO biz_data_field (id, model_id, field_code, field_name, field_type, required, unique_flag, reference_model_id, sort_order, options, created_time, updated_time) VALUES
(1, 1, 'customer_name', '客户名称', 'TEXT', 1, 1, NULL, 1, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 'customer_type', '客户类型', 'SELECT', 1, 0, NULL, 2, '["企业客户","个人客户","政府机构"]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, 'contact_phone', '联系电话', 'TEXT', 1, 0, NULL, 3, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 1, 'address', '地址', 'TEXT', 0, 0, NULL, 4, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 2, 'product_code', '产品编码', 'TEXT', 1, 1, NULL, 1, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 2, 'product_name', '产品名称', 'TEXT', 1, 0, NULL, 2, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 2, 'price', '价格', 'NUMBER', 1, 0, NULL, 3, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 2, 'category', '分类', 'SELECT', 0, 0, NULL, 4, '["电子产品","服装","食品","其他"]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 3, 'order_no', '订单编号', 'TEXT', 1, 1, NULL, 1, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 3, 'customer_id', '客户', 'REFERENCE', 1, 0, 1, 2, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 3, 'product_id', '产品', 'REFERENCE', 1, 0, 2, 3, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 3, 'quantity', '数量', 'NUMBER', 1, 0, NULL, 4, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 3, 'order_date', '订单日期', 'DATE', 1, 0, NULL, 5, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO biz_data_record (id, model_id, data_json, created_by, created_time, updated_by, updated_time) VALUES
(1, 1, '{"customer_name":"张三公司","customer_type":"企业客户","contact_phone":"13800138001","address":"北京市朝阳区"}', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
(2, 1, '{"customer_name":"李四个人","customer_type":"个人客户","contact_phone":"13800138002","address":"上海市浦东新区"}', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
(3, 2, '{"product_code":"P001","product_name":"笔记本电脑","price":5999.00,"category":"电子产品"}', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
(4, 2, '{"product_code":"P002","product_name":"智能手机","price":3999.00,"category":"电子产品"}', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
(5, 3, '{"order_no":"ORD001","customer_id":1,"product_id":3,"quantity":10,"order_date":"2024-01-15"}', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
(6, 3, '{"order_no":"ORD002","customer_id":2,"product_id":4,"quantity":5,"order_date":"2024-01-16"}', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP);
