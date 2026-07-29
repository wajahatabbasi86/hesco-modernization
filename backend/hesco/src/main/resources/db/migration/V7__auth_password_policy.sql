-- =====================================================================
-- HESCO Network Survey & Asset Management System
-- Schema: Auth / password policy support (auth_policy_prompt.md §1-5)
-- Depends on: V1 (app_user, role)
-- =====================================================================
-- The auth-module patch that introduced LoginHistory, PasswordHistory,
-- PasswordResetToken, PasswordChangeAudit, and two new AppUser columns
-- shipped without this migration — added here so hibernate.ddl-auto:
-- validate doesn't fail app startup.

ALTER TABLE app_user
    ADD COLUMN password_changed_at   TIMESTAMPTZ,
    ADD COLUMN must_change_password  BOOLEAN NOT NULL DEFAULT false;

COMMENT ON COLUMN app_user.password_changed_at IS
    'NULL means legacy account, never tracked - treated as NOT expired '
    '(no baseline to measure 30-day expiry against) until the first '
    'deliberate change or legacy-password rehash sets it.';

-- ---------------------------------------------------------------------
-- 1. LOGIN HISTORY (SRS-adjacent, auth_policy_prompt.md §5)
-- ---------------------------------------------------------------------
CREATE TABLE login_history (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT      REFERENCES app_user(id),
    username_attempted  VARCHAR(100) NOT NULL,
    login_at            TIMESTAMPTZ NOT NULL,
    logout_at           TIMESTAMPTZ,
    ip_address          VARCHAR(64),
    user_agent          VARCHAR(255),
    status              VARCHAR(10) NOT NULL CHECK (status IN ('SUCCESS', 'FAILURE')),
    failure_reason      VARCHAR(100)
);

CREATE INDEX ix_login_history_user_id_login_at ON login_history (user_id, login_at DESC);

-- ---------------------------------------------------------------------
-- 2. PASSWORD HISTORY (§1, §3 - reuse of last 5 passwords)
-- ---------------------------------------------------------------------
CREATE TABLE password_history (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT      NOT NULL REFERENCES app_user(id),
    password_hash   VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX ix_password_history_user_id_created_at ON password_history (user_id, created_at DESC);

-- ---------------------------------------------------------------------
-- 3. PASSWORD RESET TOKEN (§4)
-- ---------------------------------------------------------------------
CREATE TABLE password_reset_token (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT      NOT NULL REFERENCES app_user(id),
    token_hash      VARCHAR(64) NOT NULL UNIQUE,
    expires_at      TIMESTAMPTZ NOT NULL,
    used_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    requested_ip    VARCHAR(64)
);

CREATE INDEX ix_password_reset_token_user_id_created_at ON password_reset_token (user_id, created_at DESC);

-- ---------------------------------------------------------------------
-- 4. PASSWORD CHANGE AUDIT (§3)
-- ---------------------------------------------------------------------
CREATE TABLE password_change_audit (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT      NOT NULL REFERENCES app_user(id),
    changed_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    ip_address      VARCHAR(64),
    change_type     VARCHAR(20) NOT NULL CHECK (change_type IN ('SELF_SERVICE', 'FORCED_RESET', 'ADMIN_RESET'))
);
