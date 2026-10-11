CREATE TABLE history (
                         id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
                         user_id             BIGINT       NOT NULL,
                         topics              JSON         NOT NULL,
                         history_date        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         correctness_score   DECIMAL(4,2) NULL,
                         efficiency_score    DECIMAL(4,2) NULL,
                         communication_score DECIMAL(4,2) NULL,
                         code_quality_score  DECIMAL(4,2) NULL,
                         average_score       DECIMAL(4,2) NULL,

                         CONSTRAINT fk_history_user FOREIGN KEY (user_id)
                             REFERENCES user_account (id) ON DELETE CASCADE,
                         CONSTRAINT chk_history_correctness_score
                             CHECK (correctness_score IS NULL OR correctness_score BETWEEN 0 AND 10),
                         CONSTRAINT chk_history_efficiency_score
                             CHECK (efficiency_score IS NULL OR efficiency_score BETWEEN 0 AND 10),
                         CONSTRAINT chk_history_communication_score
                             CHECK (communication_score IS NULL OR communication_score BETWEEN 0 AND 10),
                         CONSTRAINT chk_history_code_quality_score
                             CHECK (code_quality_score IS NULL OR code_quality_score BETWEEN 0 AND 10),
                         CONSTRAINT chk_history_average_score
                             CHECK (average_score IS NULL OR average_score BETWEEN 0 AND 10)
);

CREATE INDEX idx_history_user_date ON history (user_id, history_date);