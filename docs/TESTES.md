# Validação das Ondas 1–8 e contas

## Executar

Na raiz, usar `.\scripts\frontend.ps1 check` e `.\scripts\test-backend.ps1`. Para integração real, definir `$env:ORACLE_INTEGRATION_TEST = 'true'` antes do teste backend. Os scripts carregam o `.env` local. Usar somente o Oracle de desenvolvimento, com a migração aplicada. Não há H2.

## Cobertura automatizada

- Controllers e services: validação, DTOs, decimal exato, vínculo do grupo, criador da sessão e permissões. Testes isolados de controllers desabilitam filtros; os testes Oracle exercitam os filtros reais.
- `OracleOnda1Test`: cadastro, e-mail, hash, login por formulário, rotação da sessão, logout, CSRF, acesso anônimo, isolamento, convite pendente, destinatário correto, aceitar/recusar, resposta repetida, remoção e reenvio. Inclui nomes iguais de pessoas distintas, proteção do administrador e valores monetários mínimo/máximo.
- `OracleConcorrenciaTest`: solicitações concorrentes de convite produzem 201/409; dois aceites produzem 200/409 e somente um vínculo. Transações reais com limpeza apenas dos registros fictícios do teste.
- `TentativasFilterTest`: limite por IP, expiração e tamanho limitado da memória.
- `money.test.js`: decimal textual, precisão inválida, BRL e data local.
- `api.test.js`: CSRF antes de mutações, cookie de sessão, consultas, expiração, falha de CSRF e cancelamento.

Resultados anteriores: 44 testes backend e 9 frontend aprovados em 13/10/2026; lint, build e formatação frontend aprovados. A rodada final e a homologação visual estão registradas em `ACESSO_E_CONVITES.md`.

## Roteiro manual atual

1. Criar duas contas fictícias A/B com e-mails distintos. Sair, tentar senha incorreta e entrar novamente. Confirmar que o cadastro exige 15 caracteres de senha.
2. A cria grupo e aparece como administrador e membro. Atualizar a página e conferir persistência.
3. A convida B pelo e-mail cadastrado. B vê o convite e ainda não vê o grupo em sua lista; link direto deve negar acesso.
4. B aceita. O convite sai dos pendentes, o grupo aparece e mostra os membros. B pode cadastrar despesa, mas não convidar nem remover membros.
5. A atualiza participantes e vê B. Abrir remoção e cancelar; B permanece. A remoção confirmada deve revogar acesso (também coberta no teste Oracle).
6. Em outro convite, B recusa; não ganha acesso. A pode enviar novamente.
7. Registrar despesa de R$ 123,45 e conferir confirmação. Zero, negativo, três casas, descrição vazia e data inválida devem falhar. Consultar o valor exato no Oracle.
8. Sair de um grupo e entrar em outra conta: abrir Meus grupos, sem reutilizar o grupo anterior. Alterar conta em outra aba e voltar: conferir atualização da identidade ao recuperar o foco.
9. Reiniciar backend, entrar novamente e confirmar dados persistidos. Sessões em memória são encerradas pelo reinício.
10. Conferir desktop e celular de 390 px: convites antes de criar grupo, textos, campos, foco de teclado, estados vazio/loading/erro/sucesso e bloqueio de duplo envio.

Dados antigos da Onda 1 foram preservados e não devem aparecer em nova conta. Não criar vínculo automático para facilitar o teste. Revisão humana e integração GitHub continuam sendo etapas da definição de pronto.

## Onda 2

Rodada final em 17/10/2026: 53 testes backend, zero falhas/erros/ignorados; 11 frontend e lint/build/formatação aprovados.

`OracleOnda2Test` verifica pagador obrigatório/do mesmo grupo, lista não vazia/sem duplicados, pagador fora do consumo, vínculos no Oracle, proteção contra remoção, consulta/edição/exclusão autorizadas com CSRF, limpeza dos vínculos na exclusão, preservação dos membros, versões antigas rejeitadas, edição só da lista, correção inválida sem alteração e tratamento de despesas antigas. SQL direto também confirma as FKs entre grupos. `OracleConcorrenciaTest` verifica duas edições da mesma versão em transações distintas: 200/409, versão final 1.

Na UI, repetir cadastro com subconjunto, edição de valor/pagador/envolvidos, cancelar edição, cancelar exclusão, consultar pelo número e erro para número inexistente/de outro grupo. Campos devem persistir ao receber erro de validação. Confirmar dados antigos sem pagador/envolvidos; preenchê-los somente mediante decisão explícita. Testar conflito usando duas abas abertas na mesma versão. Não considerar cobertura automatizada como auditoria completa de acessibilidade.

## Listagem automática em 18/10/2026

F10/T20–T21 antecipada por solicitação explícita. 55 testes backend com Oracle e 11 frontend aprovados. Novos testes cobrem ordenação, desempate, grupo vazio, múltiplos envolvidos, isolamento, revogação, legado e exibição de duas casas após leitura do banco. Build/lint/formatação aprovados. Fluxo no navegador e escopo em [LISTAGEM_DESPESAS.md](LISTAGEM_DESPESAS.md).

## Onda 3 — finalizada localmente em 19/10/2026

65 testes backend com Oracle real aprovados, sem falhas/erros/ignorados. Após o ajuste final dos cabeçalhos de download do PDF, os 3 testes de `OracleComprovanteTest` foram repetidos e o JAR foi gerado com sucesso. Frontend: 13 testes e lint/build/formatação aprovados.

`DivisaoIgualTest` cobre soma, centavos restantes, ordenação, limites e entradas inválidas. `OracleOnda2Test` cobre prévia sem gravação, parcelas persistidas e recálculo na edição. `ValidacaoComprovanteTest` e `OracleComprovanteTest` cobrem formatos, BLOB, metadados, acesso, CSRF, versão, substituição, remoção e cascade. Testes da API frontend conferem multipart sem Content-Type manual e prévia.

Roteiro de homologação: conferir divisão antes de salvar; alterar valor/envolvidos e conferir invalidação; recarregar o grupo e verificar parcelas; anexar JPG/PNG/PDF de teste; visualizar imagem ou baixar PDF; substituir; cancelar e confirmar remoção; conferir que a despesa permanece. Repetir com duas abas para conflito de versão e uma conta de outro grupo para isolamento. Verificar rejeição de arquivo maior que 5 MB.

Na rodada realizada, upload/substituição/remoção efetivos foram cobertos por integração e HTTP. Na interface foram conferidos prévia/cadastro, visualização da imagem, download do PDF (bytes idênticos), cancelamento da remoção e layout mobile. Não foi validado o seletor de arquivos pela UI. Evidências detalhadas em [ONDA_3.md](ONDA_3.md).

## Onda 4 — testes em 19/10 e interface em 19–20/10/2026

75 testes backend com Oracle real aprovados, zero falhas/erros/ignorados. O empacotamento inicial falhou porque o JAR estava aberto pela API no Windows. Após encerrar a API anterior, a geração do pacote passou com o mesmo código testado. Frontend: 14 testes, lint, build e formatação aprovados. Registros consolidados em 27/10/2026, sem atribuir essa data à execução anterior.

`CalculoSaldoTest` tem sete testes da regra isolada: sinais, soma zero, múltiplas despesas/subconjuntos, pagador fora do consumo, parcelas persistidas, centavo/parcela zero, valores acumulados grandes, despesas ausentes, consumo próprio, entradas incompletas/inconsistentes e ordem estável. `OracleSaldoTest` tem três testes com persistência real, transações e filtros: cadastro/edição/exclusão refletidos no saldo, nomes iguais com IDs diferentes, versão preservada pela consulta, legado bloqueado até correção, autorização e revogação. A consulta de saldos da API frontend também verifica sinal de cancelamento, cookie e cache.

Na UI, foram verificados grupo sem movimento, cadastro de R$ 100,03 e edição para R$ 100,05 com atualização automática dos saldos; leitura após novo login; aviso de legado, atalho de correção com foco e layout 390 × 844 sem rolagem horizontal. Exclusão efetiva coberta pelo teste de integração, sem atribuí-la a uma ação manual na UI nesta rodada. Evidências e roteiro de homologação em [ONDA_4.md](ONDA_4.md).

Retomada em 27/10/2026: Oracle healthy, frontend HTTP 200, proxy `/api` operacional, consulta anônima de saldos 401, consulta autenticada do grupo fictício 327 com -50.03/+50.03 e soma zero; grupo 54 bloqueado pela despesa legada 61. Conta de teste desconectada ao final. Conferência HTTP sem alterar despesas/participantes; suítes completas não repetidas porque não houve mudança no código.



Recorte de entrega preparado em 02/11/2026 sem repetir testes ou build, por solicitação do usuário. Resultados acima são evidências históricas, não uma nova execução deste recorte.
