-- Onda 2 / T10. Pagador nulo somente para preservar despesas anteriores.
WHENEVER SQLERROR EXIT FAILURE ROLLBACK;
DECLARE
    n NUMBER;
    s VARCHAR2(128) := SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA');
BEGIN
    IF s <> 'RACHAI' THEN RAISE_APPLICATION_ERROR(-20002, 'Selecione o schema RACHAI.'); END IF;
    SELECT COUNT(*) INTO n FROM all_tab_columns WHERE owner=s AND table_name='DESPESAS' AND column_name='PAGADOR_PARTICIPANTE_ID';
    IF n=0 THEN EXECUTE IMMEDIATE 'ALTER TABLE despesas ADD pagador_participante_id NUMBER(19,0)'; END IF;
    SELECT COUNT(*) INTO n FROM all_constraints WHERE owner=s AND constraint_name='UK_PARTICIPANTE_ID_GRUPO';
    IF n=0 THEN EXECUTE IMMEDIATE 'ALTER TABLE grupo_participantes ADD CONSTRAINT uk_participante_id_grupo UNIQUE(id,grupo_id)'; END IF;
    SELECT COUNT(*) INTO n FROM all_constraints WHERE owner=s AND constraint_name='FK_DESPESA_PAGADOR_GRUPO';
    IF n=0 THEN EXECUTE IMMEDIATE 'ALTER TABLE despesas ADD CONSTRAINT fk_despesa_pagador_grupo FOREIGN KEY(pagador_participante_id,grupo_id) REFERENCES grupo_participantes(id,grupo_id)'; END IF;
    SELECT COUNT(*) INTO n FROM all_indexes WHERE owner=s AND index_name='IDX_DESPESA_PAGADOR';
    IF n=0 THEN EXECUTE IMMEDIATE 'CREATE INDEX idx_despesa_pagador ON despesas(pagador_participante_id,grupo_id)'; END IF;
END;
/
