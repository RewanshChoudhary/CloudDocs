CREATE TABLE videos (
    id               UUID          PRIMARY KEY,
    user_id          BIGINT        NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type     VARCHAR(100)  NOT NULL,
    file_size        BIGINT        NOT NULL,
    s3_raw_key       VARCHAR(512)  NOT NULL,
    status           VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    error_message    VARCHAR(1000),
    s3_720p_key      VARCHAR(512),
    s3_480p_key      VARCHAR(512),
    s3_360p_key      VARCHAR(512),
    s3_thumbnail_key VARCHAR(512),
    created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_videos_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_videos_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'))
);

CREATE INDEX idx_videos_user_created_at ON videos(user_id, created_at DESC);
CREATE INDEX idx_videos_status ON videos(status);
