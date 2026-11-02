# Rascunhos das Issues da Onda 1

Destino: https://github.com/nagelzin/rachai. Estes são rascunhos locais; nenhuma Issue foi criada e nenhum número foi atribuído.

Após criar cada Issue, preencher o número real na coluna H do BACKLOG.xlsx. Preservar ondas posteriores.

## [T01] Criar entidade Grupo, repository JPA e relacionamento com o usuário responsável

Onda: Onda 1. Feature: F01. Tipo: Backend. Estimativa: M. Responsável: Lucas.
Dependências: Setup e schema Oracle.

Critérios de aceite:
- [ ] Grupo persiste nome, descrição opcional, responsável e criação.
- [ ] Responsável existe; FK e mapeamento são validados no Oracle.
- [ ] Executar verificações relevantes e roteiro Oracle em docs/TESTES.md.
- [ ] Revisão humana do código, PR e integração conforme definição de pronto.

Estado local: implementada; pendências e evidências em docs/ONDA_1.md. Não marcar como concluída antes da homologação.

## [T02] Implementar endpoints REST para criar e consultar grupos

Onda: Onda 1. Feature: F01. Tipo: Backend. Estimativa: M. Responsável: Éder.
Dependências: T01.

Critérios de aceite:
- [ ] POST /api/grupos responde 201 com Location; GET por ID responde 200/404.
- [ ] Entradas inválidas recebem 400 e mensagem clara.
- [ ] Executar verificações relevantes e roteiro Oracle em docs/TESTES.md.
- [ ] Revisão humana do código, PR e integração conforme definição de pronto.

Estado local: implementada; pendências e evidências em docs/ONDA_1.md. Não marcar como concluída antes da homologação.

## [T03] Desenvolver tela/formulário React para criação de grupo com validações

Onda: Onda 1. Feature: F01. Tipo: Frontend. Estimativa: M. Responsável: Arthur.
Dependências: T02.

Critérios de aceite:
- [ ] Criar grupo no navegador e mostrar seu número.
- [ ] Tratar validação, loading, erro e sucesso em celular e desktop.
- [ ] Executar verificações relevantes e roteiro Oracle em docs/TESTES.md.
- [ ] Revisão humana do código, PR e integração conforme definição de pronto.

Estado local: implementada; pendências e evidências em docs/ONDA_1.md. Não marcar como concluída antes da homologação.

## [T04] Criar entidade GrupoParticipante e relacionamento entre grupo e participantes

Onda: Onda 1. Feature: F02. Tipo: Backend. Estimativa: M. Responsável: Éder.
Dependências: T01.

Critérios de aceite:
- [ ] Participante pertence a exatamente um grupo.
- [ ] FK e unicidade por grupo/conta validadas no Oracle; nomes iguais entre pessoas distintas permitidos.
- [ ] Executar verificações relevantes e roteiro Oracle em docs/TESTES.md.
- [ ] Revisão humana do código, PR e integração conforme definição de pronto.

Estado local: implementada; pendências e evidências em docs/ONDA_1.md. Não marcar como concluída antes da homologação.

## [T05] Substituir inclusão livre por convites; listar e remover membros com autorização

Onda: Onda 1. Feature: F02. Tipo: Backend. Estimativa: M. Responsável: Lucas.
Dependências: T04.

Critérios de aceite:
- [ ] Convidar por e-mail; incluir membro somente após aceite; listar e remover com autorização.
- [ ] Duplicidade recebe 409; remoção fora do grupo recebe 404.
- [ ] Executar verificações relevantes e roteiro Oracle em docs/TESTES.md.
- [ ] Revisão humana do código, PR e integração conforme definição de pronto.

Estado local: implementada; pendências e evidências em docs/ONDA_1.md. Não marcar como concluída antes da homologação.

## [T06] Interface de participantes e convites internos vinculados a contas

Onda: Onda 1. Feature: F02. Tipo: Frontend. Estimativa: M. Responsável: Arthur.
Dependências: T03 e T05.

Critérios de aceite:
- [ ] Convidar contas existentes e visualizar membros; apresentar estados de convite pendente/aceito/recusado.
- [ ] Remover exige confirmação; tratar erro e bloquear duplo envio.
- [ ] Executar verificações relevantes e roteiro Oracle em docs/TESTES.md.
- [ ] Revisão humana do código, PR e integração conforme definição de pronto.

Estado local: implementada; pendências e evidências em docs/ONDA_1.md. Não marcar como concluída antes da homologação.

## [T07] Criar entidade Despesa com valor, descrição, data e vínculo ao grupo

Onda: Onda 1. Feature: F03. Tipo: Backend. Estimativa: M. Responsável: Lucas.
Dependências: T01.

Critérios de aceite:
- [ ] Despesa tem grupo, descrição, BigDecimal e LocalDate.
- [ ] Oracle usa NUMBER(12,2), valor positivo e FK; sem pagador nesta onda.
- [ ] Executar verificações relevantes e roteiro Oracle em docs/TESTES.md.
- [ ] Revisão humana do código, PR e integração conforme definição de pronto.

Estado local: implementada; pendências e evidências em docs/ONDA_1.md. Não marcar como concluída antes da homologação.

## [T08] Implementar endpoint REST de cadastro de despesa com validações de negócio

Onda: Onda 1. Feature: F03. Tipo: Backend. Estimativa: M. Responsável: Éder.
Dependências: T07.

Critérios de aceite:
- [ ] POST de despesa valida valor, precisão, descrição, data e grupo.
- [ ] Centavos são exatos; erro não cria registro.
- [ ] Executar verificações relevantes e roteiro Oracle em docs/TESTES.md.
- [ ] Revisão humana do código, PR e integração conforme definição de pronto.

Estado local: implementada; pendências e evidências em docs/ONDA_1.md. Não marcar como concluída antes da homologação.

## [T09] Desenvolver formulário React para registrar despesa e integrar com a API

Onda: Onda 1. Feature: F03. Tipo: Frontend. Estimativa: M. Responsável: Arthur.
Dependências: T03 e T08.

Critérios de aceite:
- [ ] Formulário envia decimal textual e data local para a API.
- [ ] Mostrar confirmação/erro e validar em mobile com backend real.
- [ ] Executar verificações relevantes e roteiro Oracle em docs/TESTES.md.
- [ ] Revisão humana do código, PR e integração conforme definição de pronto.

Estado local: implementada; pendências e evidências em docs/ONDA_1.md. Não marcar como concluída antes da homologação.
