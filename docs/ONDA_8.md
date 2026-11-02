# Onda 8 — pendências e progresso

T39/T40: consulta e lista das pessoas com valores a pagar/receber após os pagamentos registrados. T41/T42: indicadores e barra de progresso por quantidade de acertos. GET `/api/grupos/{grupoId}/progresso` retorna indicadores e participantes; `/pendencias` retorna a lista com o mesmo controle de acesso. As respostas indicam dados incompletos e não apresentam falsa quitação quando há despesa legada pendente.

Quitados = registros PAGO. Pendentes = sugestões atuais. Percentual = quitados / (quitados + pendentes), com duas casas e arredondamento para baixo; não indica 100% enquanto houver pendência. Registros CANCELADO ficam fora dos indicadores. Grupo sem acertos necessários recebe mensagem específica. Pagamentos registrados e valor ainda pendente são somados separadamente com BigDecimal. Alterações de despesas podem mudar o denominador e gerar uma devolução; o percentual não representa dinheiro transferido pelo sistema.

## Validação

Em 27/10/2026, 88 testes backend aprovados com Oracle real, zero falhas/erros/ignorados. Incluem progresso vazio, 0%, 100%, retorno a 50% após excluir despesa paga, acerto inverso, cancelamento, acesso privado e bloqueio de legado. As Ondas 5, 6 e 7 passaram respectivamente por 77, 81 e 86 testes antes do avanço. Logs locais ignorados pelo Git: `.tools/onda5-tests.log` até `.tools/onda8-tests.log`.

Retomada em 31/10/2026: pacote backend atualizado gerado com sucesso a partir do código testado. Migração 007 já aplicada, sem repetir setup ou redefinir credenciais. A suíte backend completa não foi repetida nesta retomada, pois o código permaneceu o mesmo.

## Roteiro de homologação

Conferência integrada na interface em 31/10/2026, usando somente a conta e o grupo fictícios de validação: listagem do histórico, detalhamento reconciliado com a despesa de R$ 100,05 e cópia do resumo. Um registro fictício de R$ 50,03 levou o progresso de 0% a 100%; seu cancelamento preservou o histórico e restaurou a pendência. A prévia personalizada recusou R$ 2 + R$ 7 para uma despesa de R$ 10 e aceitou R$ 2 + R$ 8, sem gravar outra despesa. Layout 390 × 844 conferido sem rolagem horizontal. Compartilhamento externo não foi executado; persistência da divisão personalizada e isolamento entre contas foram cobertos pela integração automatizada.

Retomada em 02/11/2026: 15 testes frontend, lint, build e formatação aprovados após o ajuste do botão explícito **Filtrar histórico**. A suíte backend de 88 testes permanece a executada em 27/10; nenhum código Java foi alterado nesta retomada.

Filtro validado na interface em 02/11: até 14/10 inclui os grupos de 13/10 e 14/10 e exclui o de 19/10; até 01/01/2000 mostra o estado vazio; limpar a data restaura os três grupos. O mesmo botão aplica a data e atualiza a consulta, sem ação redundante. Layout 390 × 844 sem rolagem horizontal (larguras de conteúdo e viewport iguais a 375 px, descontada a barra). Evidência local: `docs/.qa/historico-filtro-2026-11-02.png`. Docker, API e frontend reiniciados preservando os dados; frontend e proxy da API responderam HTTP 200.

Conta fictícia desconectada ao final e tamanho normal do navegador restaurado. Aplicação deixada na tela de entrada. Nenhuma despesa ou pagamento foi criado nesta retomada de 02/11.

1. Na página inicial, consultar grupos anteriores e filtrar pela data de criação.
2. Em um grupo de teste, abrir o detalhamento de cada saldo e conferir despesas pagas/consumidas.
3. Registrar uma despesa com valores individuais; conferir que uma soma incorreta é recusada e que a prévia correta é salva.
4. Consultar quem paga quem, abrir o resumo e usar Copiar resumo. Compartilhamento nativo depende do dispositivo.
5. Com uma conta envolvida no acerto, registrar um pagamento fictício e verificar histórico, pendências e progresso.
6. Cancelar o registro fictício e conferir o retorno das pendências. Despesas e pagamentos anteriores permanecem no histórico.
7. Conferir celular e uma conta sem vínculo com o grupo.

Implementação local até a Onda 8. Homologação do usuário, revisão humana, Issues e integração GitHub permanecem pendentes. F16/F19, integrações bancárias, notificações externas e deploy não foram incluídos.
