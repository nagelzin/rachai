# Onda 4 — histórico e saldos

Autorizada em 19/10/2026 após a validação da Onda 3 pelo usuário. Implementação e testes realizados em 19–20/10; fechamento documental retomado em 27/10/2026 após a pausa solicitada. Implementação local pronta para homologação. Nenhuma tarefa da Onda 5 foi iniciada.

| Tarefa | Responsável no backlog | Entrega |
|---|---|---|
| T20 | Éder | Endpoint de histórico, antecipado em 18/10 a pedido do usuário |
| T21 | Arthur | Cartões de despesas abertos automaticamente, já validados pelo usuário |
| T22 | Lucas | Serviço de cálculo derivado e endpoint de saldos |
| T23 | Éder | Testes de saldos positivos, negativos, zerados, subconjuntos e múltiplas despesas |
| T24 | Arthur | Saldos na tela do grupo, atualização automática e tratamento do legado |

## Regras implementadas

Saldo = total pago nas despesas − total das parcelas da pessoa. Positivo significa a receber; negativo, a pagar; zero, ausência de diferença nas despesas. O saldo não representa confirmação de transferência entre pessoas. Cálculo em BigDecimal, sem float/double, com duas casas decimais. Totais acumulados podem superar o limite de valor de uma despesa individual.

O cálculo usa as parcelas já persistidas pela Onda 3, sem redistribuí-las. Participantes são identificados pelo vínculo no grupo, mesmo quando possuem nomes iguais. Quem não tem movimento aparece com zero. O pagador pode estar fora dos envolvidos no gasto. Com dados completos, a soma dos saldos do grupo é exatamente zero.

Nenhum saldo é persistido como fonte primária. A consulta não altera versões nem carrega comprovantes BLOB. A leitura das despesas e membros ocorre sob o mesmo lock do grupo usado nas mutações, com autorização revalidada depois de obter o lock. Assim, as operações da aplicação não misturam estados de cadastro/edição/exclusão durante o cálculo. Sem novas tabelas, migrações, bibliotecas ou frameworks; STACK.docx permanece como referência da stack vigente.

Despesas sem pagador, sem envolvidos, com parcelas ausentes/negativas, participantes inválidos/repetidos ou soma diferente do total impedem a exibição dos saldos. A API retorna os IDs pendentes e nenhuma lista parcial. A interface direciona ao cartão para correção explícita, sem inventar dados antigos.

## Uso da interface

Ao abrir um grupo, Saldos do grupo mostra cada pessoa, sua situação, o total pago e sua parte nas despesas. O usuário atual recebe a indicação Você. Cores complementam os textos A pagar/A receber/Saldo zerado.

Salvar ou excluir despesas e atualizar os dados na tela provoca nova consulta de saldos. O botão Atualizar saldos consulta mudanças feitas em outra sessão. Durante nova consulta, os valores anteriores são ocultados; erros não mantêm valores antigos como atuais. AbortController e a chave da consulta impedem respostas atrasadas de substituir o resultado corrente. Não há sincronização em tempo real entre dispositivos.

## Verificações realizadas

- Backend: **75 testes, zero falhas, erros ou ignorados**, com Oracle real, em 19/10/2026. A primeira execução falhou somente ao reempacotar o JAR em uso. Após encerrar a API anterior, o empacotamento terminou com BUILD SUCCESS sem alterações no código testado.
- Frontend: **14 testes**, lint, build e formatação aprovados em 19/10/2026.
- `CalculoSaldoTest`: sete cenários cobrindo sinais, ausência de movimento, consumo próprio, múltiplas despesas, subconjuntos, pagador fora do consumo, centavos, parcelas zero, totais grandes, ordem das entradas e inconsistências. Verifica conservação da soma dos saldos.
- `OracleSaldoTest`: três cenários com consulta após leitura real do banco, cadastro/edição/exclusão, nomes iguais em contas diferentes, preservação de versões, dados legados e correção explícita, acesso anônimo/externo e revogação de membro. Respostas monetárias textuais e no-store verificados.
- Teste da API frontend: sessão por cookie, sinal de cancelamento, ausência de mutações e cache desativado na consulta de saldos.
- HTTP/navegador em 19–20/10: grupo fictício 327 com membros sem movimento; despesa 224 de R$ 100,03 criada e corrigida para R$ 100,05. Saldos mudaram de R$ 50,02 para R$ 50,03 a pagar/receber sem recarregar a página. Parcelas finais de R$ 50,03 + R$ 50,02.
- Grupo fictício 54 preservado: a despesa legada 61 bloqueia os saldos e o botão Revisar despesa posiciona o foco no cartão correto. Nenhum dado legado foi preenchido automaticamente.
- Layout mobile com viewport 390 × 844 sem rolagem horizontal. Evidência local ignorada pelo Git: `.qa/onda-4.png`. Sessão fictícia encerrada ao concluir a verificação.

Logs locais: `.tools/onda4-unit-tests.log`, `.tools/onda4-backend-tests.log` e `.tools/onda4-package.log`. Registros desta seção distinguem testes anteriores da retomada documental; não constituem uma nova execução em 27/10.

### Conferência da retomada em 27/10/2026

Docker Desktop estava desligado; foi iniciado e somente o contêiner Oracle existente do Rachaí foi acionado, ficando healthy. API compilada e frontend local iniciados. Consulta HTTP confirmou frontend 200 e proxy `/api` funcionando; saldos sem login retornaram 401. Com a conta fictícia existente, o grupo 327 retornou saldos completos de -50.03 e 50.03 (soma zero); o grupo 54 continuou sinalizando a despesa legada 61, sem saldos parciais. Sessão de teste encerrada ao final. Nenhuma despesa ou participante foi alterado nessa conferência, nem houve nova migração ou execução integral das suítes. Os arquivos de código permaneceram como na execução anteriormente aprovada; esta retomada concluiu os registros da entrega.

## Roteiro de homologação pelo usuário

1. Abrir um grupo sem despesas: todos devem ter saldo zerado.
2. Registrar R$ 100,03, pagos por uma pessoa e divididos entre duas; conferir total pago, parcelas e os saldos opostos.
3. Editar valor, pagador ou envolvidos e confirmar a prévia: os saldos devem mudar automaticamente após salvar.
4. Excluir uma despesa fictícia e conferir o recálculo. A exclusão é definitiva.
5. Registrar outra despesa com outro pagador e um subconjunto de envolvidos; conferir as somas.
6. Em grupo com dados antigos incompletos, conferir o aviso e corrigir explicitamente os campos ausentes antes de esperar saldos.
7. Alterar despesas em outra sessão e usar Atualizar despesas ou Atualizar saldos para consultar os novos dados.

## Estado da entrega

O escopo funcional do MVP está implementado até a Onda 4. Ainda faltam homologação desta onda pelo usuário, revisão humana, Issues reais e integração GitHub. Rascunhos em [ISSUES_ONDA_4.md](ISSUES_ONDA_4.md). Nenhum commit, push ou publicação foi feito nesta retomada.

As Ondas 5–8 permanecem pendentes: detalhamento individual, histórico específico de grupos/eventos, sugestões e registro de acertos, divisão personalizada e acompanhamento. O backend e frontend só devem avançar nessas funcionalidades conforme a próxima autorização.
