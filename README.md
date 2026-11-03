# Rachaí

Aplicação web responsiva para organizar despesas em grupo. Esta entrega implementa **as Ondas 1–8 (T01–T42) e contas/convites (T43–T50)**: grupos privados, despesas, divisão igual ou personalizada, comprovantes, histórico, detalhamento dos saldos, sugestões de acerto, registro de pagamentos e acompanhamento de pendências.

O código está pronto para revisão local. O estado atual está em [Onda 8](docs/ONDA_8.md); a conclusão formal depende da homologação, das Issues e da revisão/integração da equipe. O Rachaí registra pagamentos informados pelas pessoas e não executa transferências bancárias.

## Fontes de verdade

- [PROJECT_CONTEXT_RACHAI.md](PROJECT_CONTEXT_RACHAI.md): contexto, regras e ordem das ondas.
- [STACK.docx](STACK.docx): tecnologias, versões e decisões do setup.
- [BACKLOG.xlsx](BACKLOG.xlsx): tarefas, responsáveis e vínculo com Issues.
- [Execução da Onda 1](docs/ONDA_1.md): implementação e evidências anteriores.
- [Execução da Onda 2](docs/ONDA_2.md): tarefas T10–T15, regras e validação atual.
- [Execução da Onda 3](docs/ONDA_3.md): tarefas T16–T19, divisão e comprovantes.
- [Execução da Onda 4](docs/ONDA_4.md): tarefas T20–T24, saldos e roteiro de homologação.

As cópias da raiz são as versões de trabalho. O pacote `Rachai_Pacote_Handoff_GPT_Work/` foi preservado como referência original e é ignorado pelo Git.

## Preparar o VS Code

1. Abra **a pasta Rachaí inteira** em `Arquivo → Abrir Pasta`.
2. Instale as extensões recomendadas em `.vscode/extensions.json`: Java, Spring Boot, ESLint e Prettier.
3. Use JDK **21** e Node **24 LTS** (mínimo 22.12). O Maven Wrapper baixa Maven **3.9.11** no primeiro uso. Não é necessário instalar Maven globalmente.
4. O frontend usa `package-lock.json`; execute `npm ci`, não um scaffold novo.

Nesta máquina, há JDK 21 em `C:\Program Files\Java\jdk-21` e uma cópia portátil do Node 24 em `.tools/`. Os scripts PowerShell ativam essas ferramentas somente no terminal atual. `.tools/` não é versionado. Outros integrantes devem instalar seu JDK/Node e configurar `JAVA_HOME`.

```powershell
# Na raiz do projeto
. .\scripts\use-local-tools.ps1
java -version
node --version
.\scripts\frontend.ps1 install
```

Em `Terminal → Executar Tarefa`, estão disponíveis **Frontend: iniciar**, **Frontend: verificar**, **Backend: testar** e **Backend: iniciar (Windows)**. Os scripts também podem ser executados diretamente. Se a política local do PowerShell impedir scripts, use os comandos Maven/npm abaixo num terminal com as versões corretas; não é preciso mudar a política do Windows.

## Preparar o Oracle

Usamos Oracle AI Database **26ai Free Lite (23.26.0.0)** em Docker, separado do Oracle XE 11g já instalado. Serviço `FREEPDB1`, porta local **1522**. A imagem oficial está fixada por digest em `compose.yaml`, com volume persistente próprio. O [guia do banco local](docs/ORACLE_LOCAL.md) explica setup, credenciais, SQL Developer e como parar/retomar.

Na raiz, com Docker Desktop aberto:

```powershell
.\scripts\oracle.ps1 setup
.\scripts\oracle.ps1 status
# Quando aparecer healthy, executar uma única vez:
.\scripts\init-oracle.ps1
```

O setup salva senhas aleatórias no `.env` ignorado pelo Git. O script de inicialização cria o **schema dedicado** `rachai`, com `CREATE SESSION`, `CREATE TABLE` e quota limitada. Ele aplica DDL, seed e as migrações numeradas, recusando um schema já existente. Se a equipe preparar manualmente outro Oracle compatível, os scripts SQL equivalentes são:

```sql
@database/ddl.sql
@database/inserts.sql
@database/migrations/001_contas_convites.sql
@database/migrations/002_pagador.sql
@database/migrations/003_envolvidos.sql
@database/migrations/004_correcao_despesas.sql
@database/migrations/005_divisao_igual.sql
@database/migrations/006_comprovantes.sql
@database/migrations/007_acertos_divisao_personalizada.sql
```

`ddl.sql` cria a base de usuários, grupos, participantes e despesas; as migrações acrescentam credenciais, vínculos por conta, convites, pagador, envolvidos e versão das despesas. Não faz DROP nem altera schemas existentes. Oracle faz commit implícito de DDL: em caso de falha parcial, inspecione o schema antes de repetir o script. `inserts.sql` preserva o usuário legado de demonstração, sem credenciais. Novos usuários devem criar sua conta na interface. Não repita o seed como rotina de inicialização. Para um schema já existente, execute somente `.\scripts\migrate-oracle.ps1`; não execute novamente o init.

Hibernate usa `ddl-auto=validate`: não cria nem modifica tabelas automaticamente. Não há banco em memória substituindo o Oracle.

## Executar backend e frontend

Primeiro terminal PowerShell, na raiz:

```powershell
. .\scripts\use-local-tools.ps1
.\scripts\start-backend.ps1
# Carrega a configuração do .env local. Se ausente, solicita senha sem eco.
```

Alternativamente, configure `ORACLE_URL`, `ORACLE_USERNAME`, `ORACLE_PASSWORD` e `SERVER_PORT` no ambiente e rode `.\mvnw.cmd spring-boot:run` dentro de `backend/` (Linux/macOS: `sh ./mvnw spring-boot:run`). O Spring Boot **não carrega `.env` automaticamente**; isso é feito pelos scripts PowerShell do projeto. Variáveis explícitas do terminal prevalecem sobre o arquivo.

Segundo terminal, na raiz:

```powershell
.\scripts\frontend.ps1 dev
```

Abra [http://127.0.0.1:5173](http://127.0.0.1:5173). Vite encaminha `/api` para o backend em `127.0.0.1:8081`; não é necessário CORS global. A porta 8080 já pertence ao Oracle antigo. Ambos os serviços do Rachaí ficam limitados ao computador local por padrão. Se mudar a porta do backend, ajuste o proxy no `frontend/vite.config.js`.

Crie sua conta com nome, e-mail e senha de 15 a 128 caracteres. Após entrar, crie um grupo: você será administrador e membro. A outra pessoa também cria uma conta; convide o e-mail dela no grupo. Ela verá o convite em Meus grupos e poderá aceitar ou recusar. Somente o aceite concede acesso. Não há envio de e-mail externo.

A conta vem da sessão; não existe responsável configurado no frontend. Dados legados permanecem no banco sem serem atribuídos a novas contas. Recuperação de senha e verificação de e-mail aguardam escopo próprio. O ambiente continua local; HTTPS e cookie Secure são necessários em eventual publicação. Veja [contas e convites](docs/ACESSO_E_CONVITES.md).

Sem Oracle, a tela inicial pode ser inspecionada, mas as operações de cadastro/consulta precisam do backend conectado ao banco. Não há modo simulado de produção.

## Verificar

```powershell
.\scripts\frontend.ps1 check
.\scripts\test-backend.ps1
```

Ou, nos diretórios correspondentes:

```text
frontend: npm run lint / npm test / npm run build / npm run format:check
backend:  mvnw.cmd verify (Windows) ou sh ./mvnw verify (Linux/macOS)
```

Testes Java isolados não precisam de banco. A integração real é opt-in, usa as variáveis Oracle acima, exige o schema pronto e reverte os dados de teste ao final:

```powershell
# Na raiz do projeto; o script carrega o .env local
$env:ORACLE_INTEGRATION_TEST = 'true'
.\scripts\test-backend.ps1
Remove-Item Env:\ORACLE_INTEGRATION_TEST
```

Use apenas um banco local/de testes. O roteiro manual em [docs/TESTES.md](docs/TESTES.md) cobre persistência após reiniciar, erros, navegação e layout mobile. Um build aprovado não substitui essa homologação.

## Organização

```text
backend/src/main/java/br/com/rachai/
  controller/  service/  repository/  model/  dto/  exception/  security/
frontend/src/
  components/  pages/  services/
database/       DDL, seed e migrações Oracle
docs/           contrato, decisões, testes e rastreabilidade
scripts/        comandos locais de desenvolvimento
.vscode/        extensões recomendadas e tarefas
```

Pastas sem uso (hooks, routes, assets, config) não foram criadas artificialmente. O frontend usa React com estado/hash e `fetch`; CSS nativo e layout mobile-first. Valores monetários viajam como strings decimais no JSON, são `BigDecimal` no backend e `NUMBER(12,2)` no Oracle.

## Processo de equipe

Remoto: [nagelzin/rachai](https://github.com/nagelzin/rachai). O repositório remoto estava vazio no setup. O trabalho local está na branch `task/onda-1`, ainda sem commits, push ou PR: o contexto exige revisão e compreensão humana antes de incorporar código gerado por IA.

As 50 tarefas T01–T50 têm issues reais publicadas em 02/11/2026. Todas estão implementadas localmente, com evidências anteriores nos documentos das ondas. As issues permanecem abertas para conferência dos critérios, homologação final, revisão humana e integração. Responsáveis e estimativas do backlog foram preservados; T43–T50 continuam com responsável a definir. Nenhum teste ou build foi repetido nesta atualização. Números e links em [docs/ISSUES_GITHUB.md](docs/ISSUES_GITHUB.md) e na coluna Issue # do backlog. O código permanece nos dois commits locais; seu envio ao GitHub ainda está pendente.

## Usar as despesas da Onda 2

No grupo, informe descrição, valor, data, quem pagou e pelo menos um envolvido. O pagador pode ficar fora dos envolvidos. Após registrar, confira os dados e use Editar despesa ou Excluir despesa; a exclusão exige confirmação. As despesas aparecem automaticamente em cartões abertos, da data mais recente para a mais antiga, sem pesquisar por número. Cada cartão mantém as ações de correção. Use Atualizar despesas para consultar alterações de outros membros.

Membros do grupo podem corrigir lançamentos. Se outra pessoa já alterou a despesa, a aplicação pede para recarregar os dados antes de tentar novamente. Pessoas vinculadas como pagador ou envolvido não podem ser removidas enquanto o vínculo existir. Despesas antigas não recebem informações inventadas; complete pagador e envolvidos ao editar.

Validação de 17/10/2026: 53 testes backend com Oracle real e 11 frontend, lint/build/formatação aprovados. Detalhes e limites em `docs/ONDA_2.md`.

Ajuste de listagem aprovado em 18/10/2026: veja [evidências e escopo](docs/LISTAGEM_DESPESAS.md).

## Usar a Onda 3

Preencha a despesa e clique em **Conferir divisão**. Revise o valor de cada pessoa e depois confirme. Se mudar algum campo, confira novamente. O cartão mostra as parcelas salvas; editar valor ou envolvidos recalcula a divisão.

No cartão da despesa, abra **Adicionar comprovante**, selecione JPG, PNG ou PDF até 5 MB e envie. Visualize imagens ou baixe pelo próprio cartão; PDFs são baixados para abrir no leitor do dispositivo. Trocar ou remover um comprovante exige confirmação; a remoção do arquivo mantém a despesa. A sessão deve estar ativa. Detalhes e validação em [Onda 3](docs/ONDA_3.md).

A migração 005 calcula a divisão somente para os envolvidos já registrados. Lançamentos legados sem envolvidos continuam sem divisão até uma correção explícita. A quota do schema local é 100 MB para todos os dados, incluindo arquivos; o limite individual de 5 MB não aumenta essa quota.

## Usar a Onda 4

Abra um grupo e confira **Saldos do grupo**: a pagar, a receber ou saldo zerado, acompanhados do total pago e da parte de cada pessoa nas despesas. O cálculo usa os lançamentos salvos no Oracle. Registrar, editar ou excluir uma despesa atualiza os saldos na própria tela. Use **Atualizar saldos** para consultar alterações feitas em outra sessão.

Se houver despesas antigas sem pagador ou divisão completa, a tela indica quais precisam ser corrigidas antes de calcular. **Revisar despesa** leva ao cartão correspondente. Não são exibidos saldos parciais nem inventados dados ausentes. Não há registro de pagamentos entre pessoas nesta onda.

Validação local: 75 testes backend e 14 frontend, com evidências e roteiro de homologação em [Onda 4](docs/ONDA_4.md). Sem migração adicional ou mudança de stack.

## Usar as Ondas 5–8

- **Consultar grupos anteriores** na página inicial: histórico e filtro por data de criação, somente para membros atuais.
- **Ver despesas deste saldo**: detalhamento do que a pessoa pagou e de sua parte em cada lançamento.
- **Como dividir?** no formulário: escolha divisão igual ou valores individuais. Confira a prévia antes de salvar.
- **Acertos do grupo**: sugestões que consideram os pagamentos registrados, resumo para copiar/compartilhar e histórico. Quem paga ou recebe pode registrar o pagamento ou cancelar um registro incorreto.
- **Pendências e progresso**: pessoas com valores ainda pendentes e percentual por quantidade de acertos. Atualiza após operações na própria tela; use os botões de atualização para consultar alterações de outras sessões.

Execute `scripts/migrate-oracle.ps1` antes de iniciar esta versão em uma instalação antiga: a migração 007 acrescenta ACERTOS e o tipo de divisão, preservando registros existentes. Não executar `init-oracle.ps1` em schema já criado. Mesma stack, sem bibliotecas adicionais. Evidências: [Onda 5](docs/ONDA_5.md), [Onda 6](docs/ONDA_6.md), [Onda 7](docs/ONDA_7.md), [Onda 8](docs/ONDA_8.md).
