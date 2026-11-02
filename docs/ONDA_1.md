# Execução da Onda 1

Atualizado em 14/10/2026. Escopo: T01–T09 (F01/F02/F03) e complemento aprovado T43–T50. Os documentos de referência foram lidos integralmente antes das alterações. Ondas posteriores e Parking Lot permanecem fora desta entrega.

## Estado geral

Implementação local validada com Oracle real. Revisão humana, Issues e integração da equipe ainda pendentes. Git local, Maven Wrapper, Vite, VS Code e Oracle oficial em Docker preparados. Pacote original de handoff preservado. O complemento substitui o responsável fixo e a inclusão livre de participantes por contas individuais e convites internos.

| Tarefa | Implementação atual | Evidência |
|---|---|---|
| T01 | Usuario/Grupo, responsável da sessão e repositories | DDL, migração, JPA validate e Oracle |
| T02 | Criar/consultar/listar somente grupos com vínculo ativo | MockMvc, services e isolamento Oracle |
| T03 | Formulário e Meus grupos após login | Criação, atualização e navegação na UI |
| T04 | GrupoParticipante com FK e unicidade por grupo/conta | Oracle; duas contas com mesmo nome permitidas |
| T05 | Convites, listagem e remoção com autorização | Aceite/recusa, revogação, reenvio e concorrência |
| T06 | Lista de membros e convites, remoção com confirmação | Aceite/recusa no navegador; cancelar mantém membro |
| T07 | Despesa com BigDecimal, LocalDate, grupo e NUMBER(12,2) | Valores mínimo/máximo e centavos no Oracle |
| T08 | Cadastro de despesa restrito a membro, validação e erros | MockMvc e integração Oracle |
| T09 | Formulário integrado com decimal textual e data local | Despesa #61 criada pelo membro após aceite |

T01–T09 foram implementadas antes do complemento; o histórico anterior por nome não representa mais o contrato vigente. T43–T50 e suas regras estão em `ACESSO_E_CONVITES.md`, e os rascunhos originais foram ajustados em `ISSUES_ONDA_1.md`.

## Verificação final de 14/10/2026

- Maven `verify`: BUILD SUCCESS, JAR gerado, **44 testes, zero falhas/erros/ignorados**, incluindo Oracle real e concorrência de convite/aceite.
- Frontend: **9 testes aprovados**, lint, build e Prettier aprovados.
- Migração 001 reaplicada com sucesso: cinco tabelas, dados anteriores preservados. SQL Developer conectado e consultas confirmadas pelo usuário.
- Duas contas fictícias: administrador #87 e membro #88. Grupo #54 persistido desde a sessão anterior; convite aceito na UI, dois membros, controles administrativos ausentes para o convidado.
- Despesa #61 do grupo #54, cadastrada pelo membro na UI: R$ 123,45, 14/10/2026. SQL confirmou `123.45`. Despesa legada #3 continua com `123.45` e data 12/10/2026.
- Grupo fictício #81: link direto negado enquanto convite pendente; recusa pela UI não adicionou o membro. SQL confirmou estados ACEITO (#54) e RECUSADO (#81).
- Confirmação de remoção aberta e cancelada; atualização mostrou o membro preservado. Logout e login com hash antigo encaminharam a Meus grupos.
- Layout conferido em viewport de 390 e 1366 px: convites antes do formulário de criação no celular, sem rolagem horizontal; navegação por Tab entre nome/e-mail conferida. Evidência local: `docs/.qa/contas-convites.png`, ignorada pelo Git.
- Sessões ficam em memória; após reinício é necessário entrar novamente. Dados persistidos permaneceram disponíveis após retomada de Oracle/backend.

Os dados fictícios acima foram mantidos para demonstração, isolados em suas próprias contas. Nenhuma nova conta herda esses grupos ou os dados legados. A tela final foi deixada no cadastro para uso do usuário.

## Documentação e limites

STACK v1.4 registra Spring Security e a formatação Java local. BACKLOG contém as 42 tarefas originais e T43–T50 na aba ACESSO; T05/T06 refletem convites. Responsáveis originais e ondas futuras preservados; responsáveis do complemento a definir, Issues sem número.

O conteúdo do Word foi atualizado e lido novamente. A renderização foi tentada e falhou por ausência de `soffice.exe` no runtime: revisão visual do Word permanece pendente. A planilha foi renderizada e inspecionada na etapa anterior.

O email identifica uma conta, mas não é identidade verificada. Convites aparecem dentro da plataforma e exigem cadastro prévio. Recuperação de senha, confirmação de e-mail e envio externo precisam de escopo próprio. O ambiente está restrito ao computador local; publicação exige HTTPS/cookie Secure e revisão adequada.

Não há pagador, envolvidos, divisão, edição/exclusão/histórico de despesas, saldos, comprovantes ou acertos. As restrições financeiras sobre remoção de membro evoluem junto de F04/F05. F16/F19 continuam no Parking Lot.

## Integração da equipe

Revisar e compreender o código antes de commit, conforme o contexto e a declaração de IA. Criar Issues reais para T01–T09 e T43–T50, preencher o backlog, revisar o Word e organizar commits/PRs. Nada foi commitado ou enviado ao GitHub. Branch local inicial `task/onda-1`; nenhuma funcionalidade da Onda 2 iniciada.

Na revisão manual da equipe, ampliar a inspeção por teclado, textos longos e troca de sessão entre abas. O roteiro completo está em `TESTES.md`; os testes automatizados de autorização não substituem revisão de acessibilidade.
