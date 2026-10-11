CREATE TABLE saved_question (
                                id             BIGINT AUTO_INCREMENT PRIMARY KEY,
                                user_id        BIGINT       NOT NULL,
                                question_text  MEDIUMTEXT   NOT NULL,
                                question_hash  CHAR(64)     NOT NULL,
                                question_type  VARCHAR(30)  NOT NULL,
                                difficulty     VARCHAR(30)  NOT NULL,
                                created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_saved_question_user FOREIGN KEY (user_id)
                                    REFERENCES user_account (id) ON DELETE CASCADE,
                                CONSTRAINT uq_saved_question_user_hash UNIQUE (user_id, question_hash)
);

CREATE TABLE code_submission (
                                 id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 user_id            BIGINT      NOT NULL,
                                 saved_question_id  BIGINT      NULL,
                                 question_text      MEDIUMTEXT  NOT NULL,
                                 code               MEDIUMTEXT  NOT NULL,
                                 language           VARCHAR(30) NOT NULL DEFAULT 'java',
                                 feedback           MEDIUMTEXT  NULL,
                                 correct            BOOLEAN     NULL,
                                 submitted_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT fk_code_submission_user FOREIGN KEY (user_id)
                                     REFERENCES user_account (id) ON DELETE CASCADE,
                                 CONSTRAINT fk_code_submission_question FOREIGN KEY (saved_question_id)
                                     REFERENCES saved_question (id) ON DELETE SET NULL
);

CREATE INDEX idx_code_submission_user_submitted ON code_submission (user_id, submitted_at);
