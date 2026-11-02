# API das Ondas 1 a 8 e contas

Base local: `http://127.0.0.1:8081/api`. JSON UTF-8, exceto login em formulário URL encoded. A interface usa o proxy `/api` do Vite na mesma origem.

## Ondas 5–8: detalhe, histórico, acertos e acompanhamento

Todas as rotas exigem sessão; grupos sem vínculo retornam 404. GETs não alteram dados; operações de gravação exigem CSRF. Dinheiro continua como string decimal com duas casas na resposta.

| Método e rota (após `/api`) | Contrato |
|---|---|
| GET `/grupos/historico?ate=AAAA-MM-DD` | Lista de grupos com vínculo atual, criada até a data inclusiva opcional, ordenada por criação/ID decrescentes. Não classifica grupos como quitados. |
| GET `/grupos/{g}/saldos/{p}` | `completo`, `despesasPendentes`, `participante` (mesmo resumo do saldo) e `despesas`: despesaId, descricao, data, totalDespesa, pagadorNome, pago, parte, impacto. Somente despesas pagas ou consumidas pela pessoa. Impactos somam seu saldo. |
| GET `/grupos/{g}/acertos/sugestoes` | `completo`, `despesasPendentes`, `sugestoes`, `resumo`, `revisao`, `registros`. Cada sugestão contém IDs dos vínculos e usuários, nomes devedor/recebedor e valor. A revisão é opaca e deve ser devolvida ao registrar. |
| POST `/grupos/{g}/acertos` | Corpo `{devedorId, recebedorId, valor, revisao}`. Registra PAGO e retorna 201, somente por uma das partes, para o valor integral de uma sugestão atual. Outra parte não envolvida: 403. Revisão/valor desatualizado: 409. Reenvio da mesma revisão não duplica pagamento. |
| POST `/grupos/{g}/acertos/{id}/cancelamento` | Corpo `{versao}`. Retorna registro CANCELADO, sem apagar histórico; somente uma das partes. Conflito de versão/status: 409. |
| GET `/grupos/{g}/pendencias` | `completo`, `despesasPendentes`, `participantes` com participanteId, nome, aPagar e aReceber após considerar pagamentos. |
| GET `/grupos/{g}/progresso` | Os campos acima mais quitados, pendentes, percentualConcluido, valorPago, valorPendente e semAcertos. Percentual por quantidade, com duas casas truncadas. Incompleto retorna indicadores null, não zero. |

Registro de acerto: id, devedorId, recebedorId, devedorUsuarioId, recebedorUsuarioId, nomes, valor, status, registradoPorId, criadoEm, canceladoEm e versao. PAGO altera o residual das sugestões, não o saldo original das despesas; CANCELADO não conta no acompanhamento. Alterar/excluir despesa preserva pagamentos e pode gerar devolução. Não há pagamento bancário, verificação bancária ou exigência de dupla confirmação.

Cadastro/edição e prévia de despesas aceitam `valoresIndividuais` opcional: por exemplo, `{"101":"2.00","102":"8.00"}` para total 10.00 e participanteIds [101,102]. Ausente/null significa divisão igual. Presente exige exatamente os mesmos IDs, valores não negativos e soma exata; soma/IDs incompatíveis retornam 409 e escala/valor inválido 400. Resposta da despesa inclui tipoDivisao IGUAL/PERSONALIZADA. Migração 007 necessária.

Consultas financeiras e gravações usam o lock do grupo e revalidam o vínculo. Despesas incompletas bloqueiam detalhes/sugestões/progresso sem apresentar valores parciais. Histórico de pagamentos já registrado continua consultável, inclusive para cancelar um registro incorreto.

## Onda 4: saldos do grupo

`GET /grupos/{grupoId}/saldos` exige sessão e vínculo atual. Não exige CSRF por ser consulta. Sem sessão: 401; grupo inexistente ou sem vínculo: 404. Resposta sem cache. Não grava saldos nem altera versões.

Exemplo de grupo com despesa de 100.03 paga pelo participante 2 e parcelas de 50.02 para o participante 1 e 50.01 para o participante 2 (IDs ilustrativos):

```json
{
  "completo": true,
  "despesasPendentes": [],
  "participantes": [
    {
      "participanteId": 1,
      "usuarioId": 10,
      "nome": "Ana",
      "totalPago": "0.00",
      "totalDevido": "50.02",
      "saldo": "-50.02",
      "situacao": "PAGAR"
    },
    {
      "participanteId": 2,
      "usuarioId": 11,
      "nome": "Bruno",
      "totalPago": "100.03",
      "totalDevido": "50.01",
      "saldo": "50.02",
      "situacao": "RECEBER"
    }
  ]
}
```

Ordem crescente do ID do vínculo no grupo. `totalDevido` significa a soma das parcelas da pessoa, antes de descontar o que ela pagou. `saldo = totalPago - totalDevido`. `situacao` é RECEBER, PAGAR ou ZERADO; este último usa saldo "0.00". Todas as quantias são strings com duas casas. Totais agregados podem exceder o máximo permitido para uma despesa individual. `usuarioId` pode ser null para um vínculo legado; nomes iguais não agrupam contas diferentes.

Grupo sem despesas retorna todos os participantes com totais zero. Uma despesa incompleta/inconsistente bloqueia o resultado completo, com HTTP 200 e corpo como `{"completo":false,"despesasPendentes":[61],"participantes":[]}`. Não retorna saldos parciais nem completa os registros automaticamente. Após corrigir a despesa, consultar novamente.

A transação obtém o mesmo lock do grupo usado pelas mutações, revalida o acesso e lê membros/despesas de forma consistente. Detalhamento e acertos possuem rotas próprias descritas nas Ondas 5–8; o saldo base continua explicando somente as despesas.

## Sessão e CSRF

Antes de cada POST, PUT ou DELETE, consultar `GET /auth/csrf`, preservar o cookie recebido e enviar o token no cabeçalho indicado por `headerName` (X-CSRF-TOKEN). Renovar o token após login/logout. O frontend já executa essa sequência. Login mantém a sessão no cookie HttpOnly; não retorna JWT. Inatividade de 30 minutos expira a sessão. Reiniciar o backend encerra as sessões, preservando as contas no Oracle.

| Método | Rota | Entrada | Sucesso |
|---|---|---|---|
| GET | `/auth/csrf` | — | 200: token e headerName |
| POST | `/auth/cadastro` | nome, email, senha | 201: id, nome, email |
| POST | `/auth/login` | formulário `email=...&password=...` | 204 |
| GET | `/auth/eu` | sessão | 200: id, nome, email |
| POST | `/auth/logout` | sessão + CSRF | 204 |

Cadastro exige nome até 120 caracteres, e-mail válido até 254 e senha de 15 a 128 caracteres. E-mail é normalizado para minúsculas, sem espaços nas extremidades; deve ser único. Cadastro não autentica automaticamente pela API: a interface chama login em seguida. Senhas e hashes nunca são retornados.

## Grupos, membros e convites

Todas as rotas abaixo exigem sessão. O servidor obtém a conta autenticada; IDs de usuário enviados pelo cliente não concedem acesso.

| Método | Rota | Permissão | Sucesso |
|---|---|---|---|
| GET | `/grupos` | Conta autenticada; apenas seus grupos | 200, lista |
| POST | `/grupos` | Conta autenticada | 201 + Location |
| GET | `/grupos/{id}` | Membro | 200 |
| GET | `/grupos/{id}/participantes` | Membro | 200, lista |
| DELETE | `/grupos/{id}/participantes/{participanteId}` | Administrador, exceto a si mesmo | 204 |
| POST | `/grupos/{id}/convites` | Administrador | 201 |
| GET | `/grupos/{id}/convites` | Administrador | 200, enviados com status |
| GET | `/convites` | Destinatário | 200, recebidos pendentes |
| POST | `/convites/{id}/resposta` | Destinatário | 200 |
| POST | `/grupos/{id}/despesas` | Membro | 201 |
| GET | `/grupos/{grupoId}/despesas` | Membro | 200, lista completa, data decrescente e ID decrescente |
| GET | `/grupos/{grupoId}/despesas/{id}` | Membro | 200 |
| PUT | `/grupos/{grupoId}/despesas/{id}` | Membro | 200 |
| DELETE | `/grupos/{grupoId}/despesas/{id}?versao=0` | Membro | 204 |

Criar grupo: `{"nome":"Churrasco","descricao":"Domingo com a turma"}`. A resposta contém `id`, `nome`, `descricao`, `usuarioResponsavelId`, `responsavelNome`, `criadoEm`. O responsável é o criador da sessão e também se torna membro. Não enviar `usuarioResponsavelId` no cadastro.

Participantes retornam `id`, `nome`, `usuarioId`, `administrador`. O ID do vínculo é usado na remoção. Pessoas distintas podem ter o mesmo nome. A inclusão livre por nome foi removida; POST em `/participantes` retorna 405 para requisição autenticada com CSRF válido.

Convidar: `{"email":"pessoa@example.com"}`. A pessoa precisa ter uma conta previamente cadastrada. A resposta contém `id`, `grupoId`, `grupoNome`, `administradorNome`, `destinatarioEmail`, `status`, `criadoEm`. Estados: PENDENTE, ACEITO, RECUSADO. Não há envio de e-mail externo.

Responder: `{"aceitar":true}` ou `{"aceitar":false}`. Somente aceitar concede acesso. Resposta repetida retorna 409. Um convite por grupo/destinatário: novo envio após recusa ou remoção reabre o registro. Aceite e inclusão do membro são transacionais; concorrência não duplica vínculos.

## Despesas

Entrada: `{"descricao":"Mercado","valorTotal":"123.45","data":"2026-10-17","pagadorId":8,"participanteIds":[9]}`. Os IDs são dos vínculos de participantes retornados por GET /grupos/{id}/participantes, não dos usuários.

Resposta: `{"id":1,"grupoId":1,"descricao":"Mercado","valorTotal":"123.45","data":"2026-10-17","pagador":{"id":8,"nome":"Ana","usuarioId":2},"participantes":[{"id":9,"nome":"Bruno","usuarioId":3}],"versao":0}`.

Pagador obrigatório e pelo menos um envolvido, todos membros atuais do grupo. Pagador pode não estar nos envolvidos. Lista vazia/campo ausente recebe 400; vínculo de outro grupo, removido ou duplicado recebe 409. Oracle também valida o grupo por FKs compostas.

PUT recebe `{"versao":0,"dados":{...}}`, sendo dados o mesmo formato completo do cadastro. A versão retornada aumenta em cada correção, inclusive quando só muda a lista de envolvidos. DELETE exige a versão da última consulta no parâmetro `versao`. Após uma alteração concorrente, a versão antiga recebe 409 e não sobrescreve nem apaga dados. Reconsultar antes de reenviar; não há repetição automática de gravação.

Consulta/edição/exclusão validam grupo, despesa e vínculo da conta. Despesa inexistente, de outro grupo ou inacessível retorna 404. Exclusão remove a despesa e seus vínculos de envolvidos, preservando membros e contas. Despesas anteriores podem retornar pagador null e participantes []; ao editar, devem receber os campos completos.

Dinheiro usa string decimal com ponto e sempre duas casas no JSON, BigDecimal no Java e NUMBER(12,2) no Oracle. Valor mínimo 0.01, máximo 9999999999.99, até duas casas, sem arredondamento silencioso. UI aceita vírgula ou ponto, sem separador de milhar. Data obrigatória e válida; datas futuras são permitidas. Nome de grupo até 120 caracteres, descrição opcional até 500; descrição de despesa obrigatória até 200.

## Erros e limites

| Código | Motivo |
|---|---|
| 400 | Campos inválidos, desconhecidos ou JSON malformado |
| 401 | Sem sessão ou credenciais incorretas |
| 403 | CSRF ausente/inválido ou operação administrativa sem permissão |
| 404 | Grupo sem vínculo, recurso inexistente ou convite de outro destinatário |
| 405 | Método não disponibilizado, como inclusão livre de participante |
| 409 | E-mail duplicado, convite pendente/repetido, membro já presente ou conflito de integridade |
| 429 | Limite local de tentativas de login/cadastro por IP |

Exemplo: `{"mensagem":"Confira os campos informados.","campos":{"valorTotal":"O valor deve ser maior que zero."}}`. Mensagens de banco e stack traces não são expostos. CSRF é validado antes da autorização; mutação anônima sem token pode receber 403.

F10 foi antecipada por pedido de 18/10/2026. GET da coleção retorna [] para grupo vazio; contas sem vínculo recebem 404, mesmo se não houver despesas. Cada item tem o mesmo DTO do detalhe, incluindo pagador, participantes e versão. Divisão e comprovantes estão implementados na Onda 3; saldos na Onda 4; acertos nas Ondas 6–8. Remoção de membro usado como pagador ou envolvido recebe 409; corrigir os vínculos antes de remover. Dados legados sem conta permanecem no banco e não concedem acesso a novos cadastros.

## Onda 3: divisão e comprovantes

`POST /grupos/{grupoId}/despesas/previa-divisao` recebe `{"valorTotal":"100.00","participanteIds":[1,2,3]}`. Exige sessão, membro atual e CSRF, mas não grava. Retorna uma lista de `{id,nome,usuarioId,valorParte}` em ordem de ID; no exemplo, 33.34, 33.33 e 33.33. Validações monetárias e de envolvidos iguais às do cadastro. IDs repetidos ou de outro grupo: 409; entradas inválidas: 400. O pagador não entra na divisão se não estiver entre os envolvidos.

O DTO de despesa agora inclui `participantes[].valorParte` (string de duas casas) e `comprovante` (null ou `{nome,tipo,tamanho}`, tamanho em bytes). Cadastro/edição recalculam e persistem as parcelas. O conteúdo binário não faz parte da listagem JSON.

| Método | Rota relativa à despesa `/grupos/{grupoId}/despesas/{id}` | Entrada | Sucesso |
|---|---|---|---|
| POST | `/comprovante?versao=0` | Multipart, campo `arquivo`, CSRF no cabeçalho | 200, despesa com nova versão/metadados |
| GET | `/comprovante` | Sessão de membro | 200, imagem inline ou PDF como attachment |
| GET | `/comprovante?baixar=true` | Sessão de membro | 200, binário com disposição attachment |
| DELETE | `/comprovante?versao=1` | Sessão de membro e CSRF | 200, despesa com nova versão e comprovante null |

POST substitui o único comprovante existente. Toda mutação confere versão sob lock do grupo. O arquivo e os metadados são gravados/removidos na mesma transação. Uma edição dos dados da despesa preserva o comprovante. Arquivo ausente: 404; arquivo inválido: 400; limite multipart excedido: 413; versão desatualizada: 409. FormData não deve receber Content-Type manual: o navegador define o boundary. Tamanho máximo 5 MB, requisição máxima 6 MB; JPG/PNG/PDF, imagens até 16 MP. A conferência PDF é por assinatura, sem parser completo/antivírus. Imagens abrem no navegador; PDFs são sempre attachment para leitura no dispositivo. Download tem Cache-Control no-store, nosniff e CSP sandbox. Não há rota pública de arquivo.
