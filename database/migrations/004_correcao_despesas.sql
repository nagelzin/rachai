-- Onda 2 / T14. Versão para detectar edições e exclusões desatualizadas.
WHENEVER SQLERROR EXIT FAILURE ROLLBACK;
DECLARE
    n NUMBER;
    s VARCHAR2(128) := SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA');
BEGIN
    IF s <> 'RACHAI' THEN RAISE_APPLICATION_ERROR(-20002, 'Selecione o schema RACHAI.'); END IF;
    SELECT COUNT(*) INTO n FROM all_tab_columns WHERE owner=s AND table_name='DESPESAS' AND column_name='VERSAO';
    IF n=0 THEN EXECUTE IMMEDIATE 'ALTER TABLE despesas ADD versao NUMBER(19,0) DEFAULT 0 NOT NULL'; END IF;
END;
/
