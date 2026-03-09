CREATE TABLE leagues (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    season          VARCHAR(50)  NOT NULL,
    matchday_cost   INTEGER      NOT NULL,
    jackpot_start   INTEGER      NOT NULL DEFAULT 0,
    bet_deadline_minutes INTEGER NOT NULL DEFAULT 5,
    status          VARCHAR(20)  NOT NULL
);

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(255) NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    enabled       BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE league_memberships (
    id         BIGSERIAL PRIMARY KEY,
    league_id  BIGINT       NOT NULL REFERENCES leagues(id),
    user_id    BIGINT       NOT NULL REFERENCES users(id),
    balance    INTEGER      NOT NULL DEFAULT 0,
    joined_at  TIMESTAMP    NOT NULL
);

CREATE TABLE fanta_teams (
    id                    BIGSERIAL PRIMARY KEY,
    league_id             BIGINT       NOT NULL REFERENCES leagues(id),
    league_membership_id  BIGINT       NOT NULL REFERENCES league_memberships(id),
    name                  VARCHAR(255) NOT NULL
);

CREATE TABLE matchdays (
    id                BIGSERIAL PRIMARY KEY,
    league_id         BIGINT    NOT NULL REFERENCES leagues(id),
    number            INTEGER   NOT NULL,
    start_at          TIMESTAMP,
    end_at            TIMESTAMP,
    deadline_override TIMESTAMP,
    status            VARCHAR(30) NOT NULL,
    jackpot_snapshot  INTEGER
);

CREATE TABLE matchday_fixtures (
    id                  BIGSERIAL PRIMARY KEY,
    matchday_id         BIGINT  NOT NULL REFERENCES matchdays(id),
    home_fanta_team_id  BIGINT  NOT NULL REFERENCES fanta_teams(id),
    away_fanta_team_id  BIGINT  NOT NULL REFERENCES fanta_teams(id),
    home_score          INTEGER,
    away_score          INTEGER,
    result_loaded       BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE bet_templates (
    id                    BIGSERIAL PRIMARY KEY,
    league_id             BIGINT           NOT NULL REFERENCES leagues(id),
    outcome_type          VARCHAR(30)      NOT NULL,
    required_count        INTEGER          NOT NULL,
    over_under_threshold  DOUBLE PRECISION NOT NULL DEFAULT 2.5,
    order_index           INTEGER          NOT NULL
);

CREATE TABLE bet_slips (
    id                BIGSERIAL PRIMARY KEY,
    matchday_id       BIGINT      NOT NULL REFERENCES matchdays(id),
    fanta_team_id     BIGINT      NOT NULL REFERENCES fanta_teams(id),
    submitted_at      TIMESTAMP   NOT NULL,
    is_auto_submitted BOOLEAN     NOT NULL DEFAULT FALSE,
    is_admin_modified BOOLEAN     NOT NULL DEFAULT FALSE,
    admin_modified_at TIMESTAMP,
    status            VARCHAR(20) NOT NULL,
    amount_charged    INTEGER     NOT NULL,
    UNIQUE (fanta_team_id, matchday_id)
);

CREATE TABLE bet_picks (
    id                   BIGSERIAL PRIMARY KEY,
    bet_slip_id          BIGINT      NOT NULL REFERENCES bet_slips(id),
    matchday_fixture_id  BIGINT      NOT NULL REFERENCES matchday_fixtures(id),
    outcome_type         VARCHAR(30) NOT NULL,
    picked_outcome       VARCHAR(10) NOT NULL,
    is_correct           BOOLEAN
);

CREATE TABLE bet_slip_snapshots (
    id            BIGSERIAL PRIMARY KEY,
    bet_slip_id   BIGINT      NOT NULL REFERENCES bet_slips(id),
    snapshot_at   TIMESTAMP   NOT NULL,
    admin_user_id BIGINT      NOT NULL REFERENCES users(id),
    picks_json    TEXT        NOT NULL,
    note          VARCHAR(500)
);

CREATE TABLE credit_transactions (
    id                    BIGSERIAL PRIMARY KEY,
    league_membership_id  BIGINT      NOT NULL REFERENCES league_memberships(id),
    matchday_id           BIGINT      REFERENCES matchdays(id),
    type                  VARCHAR(30) NOT NULL,
    amount                INTEGER     NOT NULL,
    balance_after         INTEGER     NOT NULL,
    created_at            TIMESTAMP   NOT NULL,
    note                  VARCHAR(500)
);

CREATE TABLE invites (
    id          BIGSERIAL PRIMARY KEY,
    league_id   BIGINT       NOT NULL REFERENCES leagues(id),
    user_id     BIGINT       REFERENCES users(id),
    token       VARCHAR(255) NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL,
    expires_at  TIMESTAMP    NOT NULL,
    used_at     TIMESTAMP,
    status      VARCHAR(20)  NOT NULL
);

CREATE TABLE jackpots (
    id                        BIGSERIAL PRIMARY KEY,
    league_id                 BIGINT  NOT NULL UNIQUE REFERENCES leagues(id),
    current_amount            INTEGER NOT NULL DEFAULT 0,
    last_updated_matchday_id  BIGINT  REFERENCES matchdays(id)
);
