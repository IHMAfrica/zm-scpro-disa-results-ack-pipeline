-- Create crt schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS crt;

-- Table: crt.result_ack

-- DROP TABLE IF EXISTS crt.result_ack;

CREATE TABLE IF NOT EXISTS crt.result_ack
(
    id bigint NOT NULL,
    code character varying(50) COLLATE pg_catalog."default" NOT NULL,
    result_message_ref_id character varying(100) COLLATE pg_catalog."default" NOT NULL,
    message character varying(250) COLLATE pg_catalog."default",
    created_at timestamp without time zone,
    received_at timestamp without time zone,
    CONSTRAINT result_ack_pkey PRIMARY KEY (id),
    CONSTRAINT crt_result_ack_id_fk FOREIGN KEY (id)
        REFERENCES data.message (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS crt.result_ack
    OWNER to hie_manager_user;

-- Index: idx_result_ack_result_message_ref_id

-- DROP INDEX IF EXISTS crt.idx_result_ack_result_message_ref_id;

CREATE INDEX IF NOT EXISTS idx_result_ack_result_message_ref_id
    ON crt.result_ack USING btree
    (result_message_ref_id COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;
