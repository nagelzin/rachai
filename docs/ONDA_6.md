# Onda 6 — sugestões e resumo

T29/T30: algoritmo isolado em BigDecimal, ordenado por ID de participante, compensando devedores e recebedores até zerar todos os saldos. Determinístico, até N−1 transferências; não promete mínimo global. Três testes isolados aprovados antes da integração, incluindo 100 grupos gerados de forma reproduzível, múltiplos credores/devedores, centavos, zero e valores acumulados altos.

T31: GET `/api/grupos/{grupoId}/acertos/sugestoes` e painel Acertos do grupo. Mesmo acesso e lock dos saldos. Despesas incompletas bloqueiam sugestões e resumo. T32/T33: resumo textual com despesas, parcelas, saldos e sugestões, consultado na mesma transação; visualizar, copiar e compartilhar usando recursos nativos do navegador, com seleção manual como alternativa. Compartilhamento só ocorre por ação da pessoa; não envia mensagens automaticamente. Nenhuma movimentação de dinheiro.

Validação antes da Onda 7: 81 testes backend com Oracle real, 14 frontend, lint e build aprovados em 27/10/2026. Sem migração ou nova tecnologia. Revisão humana, Issues e integração GitHub pendentes. A [Onda 7](ONDA_7.md) complementa as sugestões com os pagamentos registrados; a conferência integrada está em [ONDA_8.md](ONDA_8.md).
