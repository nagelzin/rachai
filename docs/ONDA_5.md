# Onda 5 — detalhamento e histórico

Autorizada em 27/10/2026 junto à continuidade das próximas ondas. T25/T26: GET `/api/grupos/{grupoId}/saldos/{participanteId}` e detalhamento expansível por pessoa. Mostra despesas pagas ou consumidas, inclusive parcela zero e pagador fora do consumo. Impacto = pago menos parcela; soma reconcilia com o saldo. Mantém lock, autorização e bloqueio de legado da Onda 4; não altera dados.

T27/T28: GET `/api/grupos/historico?ate=AAAA-MM-DD` e histórico na página inicial. Consulta somente vínculos atuais, por criação decrescente/ID decrescente. Filtro opcional inclusivo por data de criação; o modelo não possui data de encerramento, portanto histórico não significa grupo quitado ou arquivado. Remoção do vínculo revoga também esse acesso.

Validação antes da Onda 6: 77 testes backend aprovados com Oracle real (zero ignorados), 14 frontend, lint e build aprovados. Novos testes reconciliam detalhes e verificam histórico privado, período vazio, data inválida e acesso negado. Conferência visual integrada registrada em [ONDA_8.md](ONDA_8.md). Sem migração ou nova tecnologia. Issues, revisão humana e integração GitHub pendentes.
