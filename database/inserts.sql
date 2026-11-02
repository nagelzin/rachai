-- Executar uma vez, depois do ddl.sql, no schema novo e vazio.
-- O primeiro usuário recebe ID 1. Não é uma conta autenticada.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK;
INSERT INTO usuarios (nome) VALUES ('Equipe Rachaí');
COMMIT;
SELECT id, nome FROM usuarios;
