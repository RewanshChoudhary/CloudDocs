CREATE TABLE users (
    id          BIGSERIAL    PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE file_metadata (
    id                   BIGSERIAL    PRIMARY KEY,
    original_name        VARCHAR(255) NOT NULL,
    cloudinary_url       VARCHAR(500) NOT NULL,
    cloudinary_public_id VARCHAR(255) NOT NULL,
    content_type         VARCHAR(100) NOT NULL,
    file_size            BIGINT       NOT NULL,
    download_count       INT          NOT NULL DEFAULT 0,
    uploaded_by_id       BIGINT       NOT NULL,
    uploaded_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_file_metadata_user FOREIGN KEY (uploaded_by_id) REFERENCES users(id)
);

CREATE TABLE shared_links (
    id           BIGSERIAL    PRIMARY KEY,
    token        VARCHAR(36)  NOT NULL UNIQUE,
    file_id      BIGINT       NOT NULL,
    shared_by_id BIGINT       NOT NULL,
    expires_at   TIMESTAMP    NOT NULL,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shared_links_file FOREIGN KEY (file_id) REFERENCES file_metadata(id),
    CONSTRAINT fk_shared_links_user FOREIGN KEY (shared_by_id) REFERENCES users(id)
);

CREATE TABLE comments (
    id         BIGSERIAL PRIMARY KEY,
    content    TEXT      NOT NULL,
    file_id    BIGINT    NOT NULL,
    author_id  BIGINT    NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comments_file FOREIGN KEY (file_id) REFERENCES file_metadata(id),
    CONSTRAINT fk_comments_user FOREIGN KEY (author_id) REFERENCES users(id)
);
