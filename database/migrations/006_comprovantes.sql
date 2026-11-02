-- Onda 3 / T18. Metadados leves na despesa; conteúdo BLOB em tabela separada.
WHENEVER SQLERROR EXIT FAILURE ROLLBACK;
DECLARE
    n NUMBER;
    s VARCHAR2(128) := SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA');
BEGIN
    IF s <> 'RACHAI' THEN RAISE_APPLICATION_ERROR(-20002, 'Selecione o schema RACHAI.'); END IF;
    SELECT COUNT(*) INTO n FROM all_tab_columns WHERE owner=s AND table_name='DESPESAS' AND column_name='COMPROVANTE_NOME';
    IF n=0 THEN EXECUTE IMMEDIATE 'ALTER TABLE despesas ADD comprovante_nome VARCHAR2(120 CHAR)'; END IF;
    SELECT COUNT(*) INTO n FROM all_tab_columns WHERE owner=s AND table_name='DESPESAS' AND column_name='COMPROVANTE_TIPO';
    IF n=0 THEN EXECUTE IMMEDIATE 'ALTER TABLE despesas ADD comprovante_tipo VARCHAR2(100 CHAR)'; END IF;
    SELECT COUNT(*) INTO n FROM all_tab_columns WHERE owner=s AND table_name='DESPESAS' AND column_name='COMPROVANTE_TAMANHO';
    IF n=0 THEN EXECUTE IMMEDIATE 'ALTER TABLE despesas ADD comprovante_tamanho NUMBER(19,0)'; END IF;
    SELECT COUNT(*) INTO n FROM all_tables WHERE owner=s AND table_name='COMPROVANTES';
    IF n=0 THEN EXECUTE IMMEDIATE 'CREATE TABLE comprovantes (
        despesa_id NUMBER(19,0) PRIMARY KEY,
        conteudo BLOB NOT NULL,
        CONSTRAINT fk_comprovante_despesa FOREIGN KEY(despesa_id) REFERENCES despesas(id) ON DELETE CASCADE
    )'; END IF;
END;
/
