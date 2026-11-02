-- Onda 3 / T16. Calcula somente vínculos já existentes, sem criar envolvidos.
WHENEVER SQLERROR EXIT FAILURE ROLLBACK;
DECLARE
    n NUMBER;
    s VARCHAR2(128) := SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA');
BEGIN
    IF s <> 'RACHAI' THEN RAISE_APPLICATION_ERROR(-20002, 'Selecione o schema RACHAI.'); END IF;
    SELECT COUNT(*) INTO n FROM all_tab_columns WHERE owner=s AND table_name='DESPESA_PARTICIPANTES' AND column_name='VALOR_PARTE';
    IF n=0 THEN EXECUTE IMMEDIATE 'ALTER TABLE despesa_participantes ADD valor_parte NUMBER(12,2)'; END IF;
    SELECT COUNT(*) INTO n FROM all_constraints WHERE owner=s AND constraint_name='CK_PARCELA_NAO_NEGATIVA';
    IF n=0 THEN EXECUTE IMMEDIATE 'ALTER TABLE despesa_participantes ADD CONSTRAINT ck_parcela_nao_negativa CHECK(valor_parte >= 0)'; END IF;
    EXECUTE IMMEDIATE 'UPDATE despesas d SET versao=versao+1 WHERE EXISTS (SELECT 1 FROM despesa_participantes p WHERE p.despesa_id=d.id AND p.valor_parte IS NULL)';
    EXECUTE IMMEDIATE 'MERGE INTO despesa_participantes alvo USING (
        SELECT p.id, d.valor_total*100 centavos,
            COUNT(*) OVER (PARTITION BY p.despesa_id) qtd,
            ROW_NUMBER() OVER (PARTITION BY p.despesa_id ORDER BY p.participante_id) pos
        FROM despesa_participantes p JOIN despesas d ON d.id=p.despesa_id
    ) origem ON (alvo.id=origem.id)
    WHEN MATCHED THEN UPDATE SET alvo.valor_parte =
        (TRUNC(origem.centavos/origem.qtd) + CASE WHEN origem.pos <= MOD(origem.centavos,origem.qtd) THEN 1 ELSE 0 END)/100
    WHERE alvo.valor_parte IS NULL';
    COMMIT;
END;
/
