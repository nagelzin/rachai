# Onda 7 — pagamentos registrados e divisão personalizada

T34/T35: tabela ACERTOS, registro de um acerto sugerido como PAGO por uma das duas partes e cancelamento com histórico preservado. Registrar significa informar um pagamento já realizado fora da plataforma, não executar transferência. Não há dupla confirmação bancária. Outros membros podem consultar, mas não registrar/cancelar pagamentos alheios. CSRF, vínculo, lock e revisão da situação atual impedem pagamento duplicado, valor adulterado e sugestão desatualizada. Cancelamento exige versão atual.

Acertos pendentes são recalculados: saldo das despesas + pagamentos feitos − pagamentos recebidos. Registros PAGO permanecem; CANCELADO não altera o saldo residual. Correções e exclusões de despesas não apagam pagamentos; podem gerar novos acertos, inclusive na direção inversa. Não persistimos propostas pendentes que poderiam ficar desatualizadas. A remoção de participante com histórico de pagamentos é bloqueada para preservar os vínculos.

T36/T37/T38: `valoresIndividuais` opcional no cadastro, edição e prévia. Ausente mantém divisão igual. Presente deve conter exatamente os IDs envolvidos, valores não negativos de até duas casas e soma idêntica ao total. Zero permitido; não há arredondamento silencioso. A despesa guarda IGUAL/PERSONALIZADA para preservar o modo ao editar. A confirmação da prévia continua obrigatória na interface.

Migração 007 aplicada no Oracle local em 27/10/2026. Acrescenta tabela e coluna sem recalcular dados existentes. Mesma stack. Antes da Onda 8: 86 testes backend com Oracle real e 15 frontend, lint e build aprovados. Conferência visual integrada registrada em [ONDA_8.md](ONDA_8.md). Revisão humana, Issues e integração GitHub pendentes.
