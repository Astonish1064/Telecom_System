DROP TABLE IF EXISTS login_info;
DROP TABLE IF EXISTS admin_info;
DROP TABLE IF EXISTS user_info;
DROP TABLE IF EXISTS package_info;

-- 创建套餐信息表
CREATE TABLE package_info (
    id INT PRIMARY KEY,
    duration INTERVAL NOT NULL,
    cost DECIMAL(10,2) NOT NULL
);
-- 创建用户信息表
CREATE TABLE user_info (
    account SERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL,
    password VARCHAR(20) NOT NULL,
	phone VARCHAR(20) NOT NULL,
    balance DECIMAL(10,2) NOT NULL,
    package_id INT NOT NULL,
    package_start_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (package_id) REFERENCES package_info(id)
);

-- 创建管理信息表
CREATE TABLE admin_info (
    account INT PRIMARY KEY,
    name VARCHAR(20) NOT NULL,
    password VARCHAR(20) NOT NULL
);

-- 创建登录信息表
CREATE TABLE login_info (
    account_id INT NOT NULL,
    login_time TIMESTAMPTZ NOT NULL,
    logout_time TIMESTAMPTZ,
    PRIMARY KEY (account_id, login_time),
    FOREIGN KEY (account_id) REFERENCES user_info(account)
);

-- 设置用户账号从200001开始
ALTER SEQUENCE user_info_account_seq RESTART WITH 200001;

-- 创建触发器函数
CREATE OR REPLACE FUNCTION delete_related_login_info()
RETURNS TRIGGER AS $$
BEGIN
    -- 当删除 user_info 记录时，自动删除 login_info 中的相关记录
    DELETE FROM login_info WHERE account_id = OLD.account;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器
CREATE TRIGGER cascade_delete_login_info
    BEFORE DELETE ON user_info
    FOR EACH ROW
    EXECUTE FUNCTION delete_related_login_info();