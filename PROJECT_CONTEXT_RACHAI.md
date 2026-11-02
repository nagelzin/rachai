# Rachaí — Contexto Completo do Projeto para ChatGPT Work

**Objetivo deste arquivo:** funcionar como documento de handoff e fonte de contexto para o ChatGPT Work executar o projeto de forma consistente com tudo o que já foi definido pela equipe. Este arquivo deve ser colocado na raiz do repositório e consultado antes de qualquer alteração estrutural.

**Atualização:** 02/11/2026
**Equipe:** Arthur Felipe De Chaves Da Silva; Éder Antônio dos Santos do Nascimento; Lucas Gabriel Nagel

## 1. Regras operacionais para o ChatGPT Work

- Tratar `STACK.docx`, `BACKLOG.xlsx` e este arquivo como fontes de verdade do projeto.
- Desenvolver por ondas e tarefas do backlog. Em 27/10/2026, após o fechamento local da Onda 4, o usuário autorizou continuar as próximas ondas (5–8). Implementar, testar e documentar cada onda em ordem antes de avançar. F16/F19 continuam fora do escopo.
- Manter a arquitetura como aplicação web responsiva mobile-first: React no frontend, Java + Spring Boot no backend e Oracle no banco.
- Não trocar a stack, adicionar frameworks/bibliotecas relevantes, alterar regras de negócio ou ampliar o escopo silenciosamente. Qualquer tecnologia adicional deve ser registrada em `STACK.docx`.
- Priorizar código simples, legível e didático, adequado a uma equipe iniciante. Evitar abstrações prematuras e overengineering.
- Toda regra financeira deve ser testável. Usar `BigDecimal` no Java e `NUMBER(...,2)` no Oracle; nunca usar `float`/`double` para dinheiro.
- Antes de cada feature: ler a tarefa correspondente, entender dependências, implementar backend + frontend quando aplicável, testar e documentar.
- F16 e F19 permanecem no Parking Lot e não devem ser implementadas sem nova decisão da equipe.
- Cadastro/login e convites internos foram aprovados em 12/10/2026 como complemento da Onda 1 (T43–T50). Integração bancária/Pix, app nativo, microserviços, notificações push, CI/CD e deploy não fazem parte do escopo atual, salvo nova decisão explícita.
- Atualizar documentação e backlog quando uma decisão concreta mudar o que está registrado.

## 2. Produto e problema

**Produto:** Rachaí — aplicativo colaborativo de gestão e divisão de despesas em grupo, implementado como aplicação web responsiva.

**Problema:** grupos de amigos que organizam viagens, churrascos, festas, jantares e outros eventos têm dificuldade para reunir os gastos, identificar quem pagou, determinar quem participou de cada despesa, calcular saldos e acompanhar pagamentos. O problema fica mais forte quando há muitas pessoas, várias despesas e diferentes pagadores.

**Visão do Produto v2:** Para grupos de amigos que compartilham despesas em viagens, festas e outros eventos, que precisam organizar diferentes gastos e dividir os valores de forma justa, o Rachaí é um aplicativo colaborativo de gestão de despesas que centraliza os gastos, permite definir quem participa de cada despesa e calcula automaticamente quanto cada pessoa deve pagar ou receber. Diferente do uso combinado de WhatsApp, calculadora e planilhas, o produto reúne todo o processo em um único lugar, tornando o acerto mais simples, organizado e transparente.

### 2.1 Limites de escopo
**É:** aplicação colaborativa de gestão de despesas compartilhadas; ferramenta de organização financeira em grupo; sistema de controle de gastos compartilhados; plataforma de acerto entre participantes.

**Não é:** banco/carteira digital; aplicativo de finanças pessoais; planilha financeira; aplicativo de mensagens.

**Faz:** cria grupos; adiciona participantes; registra despesas e pagador; seleciona participantes de cada gasto; divide valores; calcula saldos; mostra histórico, acertos e pendências.

**Não faz no escopo atual:** processar Pix/transferências; gerenciar investimentos ou finanças pessoais; realizar compras; substituir mensageria; executar F16/F19 enquanto estiverem no Parking Lot.

## 3. Evidências e insights da pesquisa O1

- Os usuários usam várias ferramentas simultaneamente (WhatsApp, calculadora, Pix, bloco de notas e planilhas), deixando informações espalhadas.
- Nem todas as despesas devem ser divididas igualmente: alguns participantes chegam depois ou não consomem determinados itens.
- Cálculos manuais aumentam a chance de erro; houve relato de gasto duplicado em planilha e Pix incorreto.
- Cobrar participantes gera desconforto e sobrecarga para quem organizou as contas.
- Registrar despesas durante o evento reduz confusões e evita reconstruir todos os gastos no final.

## 4. Objetivos mensuráveis do produto

- **Obj-1:** Permitir que 80% dos usuários registrem uma nova despesa, informando valor, quem pagou e participantes envolvidos, em até 30 segundos.  
  **Motivação:** Responde à perda de tempo e ao uso combinado de várias ferramentas.
- **Obj-2:** Permitir que 100% das despesas registradas de um evento sejam consultadas em um único histórico, com valor, responsável pelo pagamento e participantes.  
  **Motivação:** Responde às informações espalhadas entre WhatsApp, planilhas, calculadora e Pix.
- **Obj-3:** Permitir que 100% das despesas sejam atribuídas somente aos participantes envolvidos no gasto.  
  **Motivação:** Responde à dor de divisões injustas quando nem todos participam ou consomem o mesmo.
- **Obj-4:** Calcular automaticamente 100% dos saldos dos participantes, indicando valores a pagar ou receber sem cálculos manuais.  
  **Motivação:** Responde a erros de cálculo e retrabalho.
- **Obj-5:** Reduzir em 50% a necessidade de mensagens individuais de cobrança, tornando saldos e pendências visíveis aos participantes.  
  **Motivação:** Responde ao desconforto e à sobrecarga de cobrança do organizador.

## 5. Personas

### 5.1 Gabriel Ferreira — Persona Primária / Organizador
**Perfil:** 25 anos, Analista de TI, Novo Hamburgo/RS. Participa de viagens e encontros e, por ter facilidade com tecnologia e organização, costuma centralizar gastos, cálculos e cobranças.

**Objetivos:** registrar gastos rapidamente; saber automaticamente quem paga/recebe; centralizar informações.

**Frustrações:** informações espalhadas; responsabilidade concentrada; erros em cálculos manuais; precisar cobrar amigos.

**Citação representativa:** “Acho que o problema é precisar usar várias ferramentas diferentes para fazer uma coisa só.”

**Necessidade central:** organizar as despesas do grupo sem virar o “contador” do evento.

### 5.2 Mariana Costa — Persona Secundária / Participante
**Perfil:** 33 anos, Assistente Administrativa, Novo Hamburgo/RS. Participa de churrascos, aniversários, jantares e viagens, mas normalmente não organiza as contas.

**Objetivos:** saber exatamente quanto deve; entender a origem do valor; pagar apenas pelos gastos dos quais participou.

**Frustrações:** pagar por algo que não consumiu; informações espalhadas; não entender uma pendência.

**Citação representativa:** “Para mim o mais difícil é quando nem todo mundo consumiu as mesmas coisas.”

**Necessidade central:** transparência e segurança de que sua parte foi calculada corretamente.

## 6. Jornadas do usuário

### 6.1 Gabriel — objetivo: fechar corretamente a divisão das despesas
| # | Ação | Emoção | Sat. | Crítico |
|---:|---|---|---:|---|
| 1 | Percebe, ao final da viagem ou evento, que várias pessoas pagaram despesas diferentes ao longo dos dias. | neutro | 0 | não |
| 2 | Envia mensagem no grupo pedindo que todos informem o que pagaram e mandem comprovantes. | esperançoso | 0 | não |
| 3 | Recebe informações espalhadas no WhatsApp, com mensagens fora de ordem e alguns gastos faltando. | frustrado | -2 | ★ sim |
| 4 | Abre calculadora, bloco de notas ou planilha para reunir manualmente todas as despesas. | sobrecarregado | -2 | ★ sim |
| 5 | Tenta identificar quais despesas devem ser divididas por todos e quais envolvem apenas parte do grupo. | inseguro | -2 | ★ sim |
| 6 | Calcula quanto cada pessoa já pagou e quanto ainda deve pagar ou receber. | tenso | -2 | ★ sim |
| 7 | Revisa os cálculos para evitar erros, valores duplicados ou cobranças injustas. | apreensivo | -1 | não |
| 8 | Envia o resumo final no grupo e responde dúvidas ou questionamentos sobre os valores. | pressionado | -1 | não |
| 9 | Cobra individualmente quem ainda não fez o Pix ou esqueceu de pagar. | constrangido | -3 | ★ sim |
| 10 | Recebe os pagamentos pendentes e confirma que todos os valores foram acertados. | aliviado | +2 | não |

### 6.2 Mariana — objetivo: entender sua parte e pagar apenas pelo que participou
| # | Ação | Emoção | Sat. | Crítico |
|---:|---|---|---:|---|
| 1 | Participa de um churrasco, viagem ou encontro sabendo que os gastos serão divididos entre o grupo. | neutra | 0 | não |
| 2 | Observa que diferentes pessoas estão pagando itens como mercado, bebidas, combustível ou aluguel. | tranquila | 0 | não |
| 3 | No final do evento, acompanha mensagens no grupo com valores, comprovantes e explicações. | confusa | -1 | ★ sim |
| 4 | Tenta entender quais despesas incluem sua participação e quais não deveriam ser cobradas dela. | insegura | -2 | ★ sim |
| 5 | Questiona se itens que não consumiu foram incluídos em sua parte. | desconfortável | -2 | ★ sim |
| 6 | Recebe o valor final calculado por quem organizou as contas. | cautelosa | -1 | não |
| 7 | Procura mensagens antigas, comprovantes ou explicações para conferir se o valor faz sentido. | ansiosa | -2 | ★ sim |
| 8 | Faz o Pix da sua parte e informa no grupo que o pagamento foi realizado. | aliviada | +1 | não |
| 9 | Aguarda confirmação de que o valor estava correto e não será necessário recalcular. | apreensiva | -1 | não |
| 10 | Recebe a confirmação de que a conta foi encerrada corretamente. | satisfeita | +2 | não |

## 7. Catálogo de Features

Convenções: `E/EE/EEE` = esforço técnico; `$ / $$ / $$$` = valor de negócio; `<3` repetido = valor de UX. A cor representa a revisão técnica/negócio/UX. `G` e `M` nos passos são Gabriel e Mariana.

| ID | Feature | Personas | Passos | Esforço | Negócio | UX | Revisão | Sequência |
|---|---|---|---|---|---|---|---|---|
| F01 | Criar grupo de despesas para uma viagem, festa ou evento | Gabriel / Mariana | G1, M1 | E | $$ | <3<3 | Verde | Onda 1 |
| F02 | Adicionar participantes ao grupo de despesas | Gabriel / Mariana | G1, M1 | E | $$ | <3<3 | Verde | Onda 1 |
| F03 | Registrar despesa com valor, descrição e data | Gabriel | G3★, G4★ | E | $$$ | <3<3<3 | Verde | Onda 1 |
| F04 | Identificar quem realizou o pagamento de cada despesa | Gabriel / Mariana | G3★, G6★, M3★ | E | $$$ | <3<3<3 | Verde | Onda 2 |
| F05 | Selecionar quais participantes fazem parte de cada despesa | Gabriel / Mariana | G5★, M4★, M5★ | EE | $$$ | <3<3<3 | Amarelo | Onda 2 |
| F06 | Dividir automaticamente uma despesa entre os participantes selecionados | Gabriel / Mariana | G5★, G6★, M4★, M5★ | EE | $$$ | <3<3<3 | Amarelo | Onda 3 |
| F07 | Ajustar individualmente os valores quando a divisão não for igual | Gabriel / Mariana | G5★, M4★, M5★ | EE | $$ | <3<3 | Vermelho | Onda 7 |
| F08 | Editar ou excluir uma despesa registrada incorretamente | Gabriel | G7 | E | $$ | <3<3 | Amarelo | Onda 2 |
| F09 | Anexar comprovante ou foto a uma despesa registrada | Gabriel / Mariana | G3★, M3★, M7★ | EE | $ | <3<3 | Vermelho | Onda 3 |
| F10 | Visualizar todas as despesas do grupo em um único histórico | Gabriel / Mariana | G3★, G4★, M3★, M7★ | E | $$$ | <3<3<3 | Verde | Onda 4 |
| F11 | Calcular automaticamente o saldo de cada participante | Gabriel / Mariana | G6★, G7, M6, M7★ | EEE | $$$ | <3<3<3 | Vermelho | Onda 4 |
| F12 | Visualizar o detalhamento das despesas que formam o saldo individual | Gabriel / Mariana | G6★, M4★, M7★ | EE | $$$ | <3<3<3 | Amarelo | Onda 5 |
| F13 | Indicar automaticamente quem deve pagar e quem deve receber | Gabriel / Mariana | G6★, G8, M6 | EEE | $$$ | <3<3<3 | Vermelho | Onda 6 |
| F14 | Marcar um acerto entre participantes como pago ou recebido | Gabriel / Mariana | G9★, G10, M8, M9 | EE | $$ | <3<3 | Amarelo | Onda 7 |
| F15 | Visualizar participantes com pagamentos pendentes | Gabriel / Mariana | G9★, G10, M9 | E | $$ | <3<3<3 | Amarelo | Onda 8 |
| F16 | Enviar lembrete de pagamento pendente ao participante | Gabriel / Mariana | G9★, M9 | EE | $$ | <3<3 | X / Parking Lot | Fora do sequenciador |
| F17 | Compartilhar o resumo final das despesas e acertos com o grupo | Gabriel / Mariana | G8, G10, M6, M10 | E | $$ | <3<3 | Amarelo | Onda 6 |
| F18 | Consultar eventos e grupos de despesas anteriores | Gabriel / Mariana | Pós-evento | E | $ | <3 | Vermelho | Onda 5 |
| F19 | Receber notificação quando uma nova despesa for adicionada ao grupo | Gabriel / Mariana | Durante o evento | EE | $ | <3<3 | X / Parking Lot | Fora do sequenciador |
| F20 | Visualizar o progresso do acerto do grupo | Gabriel / Mariana | G10, M9, M10 | EE | $$ | <3<3 | Vermelho | Onda 8 |

## 8. Sequenciador e releases

- **Onda 1:** F01, F02, F03 — Base: grupos, participantes e registro de despesas.
- **Onda 2:** F04, F05, F08 — Estrutura correta da despesa: pagador, participantes envolvidos e correções.
- **Onda 3:** F06, F09 — Divisão automática e comprovante da despesa.
- **Onda 4:** F10, F11 — Histórico e cálculo de saldos. Marco de MVP ao final desta onda.
- **Onda 5:** F12, F18 — Detalhamento do saldo e histórico de grupos/eventos.
- **Onda 6:** F13, F17 — Sugestão de acertos e compartilhamento do resumo. Incremento 1.
- **Onda 7:** F14, F07 — Registro de acertos e divisão personalizada.
- **Onda 8:** F15, F20 — Pendências e progresso dos acertos. Incremento 2.

**MVP:** concluído ao final da Onda 4 (F01, F02, F03, F04, F05, F08, F06, F09, F10, F11).

**Incremento 1:** ao final da Onda 6.  
**Incremento 2:** ao final da Onda 8.  
**Parking Lot:** F16 e F19.

## 9. Stack e arquitetura

- **Frontend:** React, executado no navegador, layout responsivo e mobile-first.
- **Backend:** Java + Spring Boot, API REST e regras de negócio.
- **Banco:** Oracle Database.
- **Comunicação:** HTTP + JSON.
- **Persistência:** Spring Data JPA / JPA-Hibernate dentro do ecossistema Spring Boot.
- **Build/pacotes:** Maven no backend; npm no frontend.
- **IDE:** Visual Studio Code (decisão da equipe em 12/10/2026).
- **Testes backend:** JUnit 5.
- **Lint/format:** ESLint + Prettier no frontend; formatação Java pela IDE; Google Java Format 1.24.0 (estilo AOSP) usado na revisão local.
- **CI/CD:** não utilizado inicialmente.
- **Infraestrutura/deploy:** ainda não definida.

```text
Browser / Smartphone
  └─► React (Frontend responsivo)
         └─► HTTP/JSON
                └─► Spring Boot REST API (Java)
                       └─► JPA/Hibernate
                              └─► Oracle Database
```

### 9.1 Decisões do setup de 12/10/2026
- Java 21 LTS, Spring Boot 3.5.16, React/React DOM 19.3.0, Vite 8.3.4; versões complementares no `STACK.docx` e `frontend/package-lock.json`.
- Oracle AI Database 26ai Free Lite (23.26.0.0), imagem oficial Docker, substitui o alvo inicial 23ai Free. O Oracle XE 11g encontrado no computador é preservado. Contêiner próprio `rachai-oracle`, porta local 1522, serviço FREEPDB1 e volume persistente. Procedimento em `docs/ORACLE_LOCAL.md`.
- Repositório: https://github.com/nagelzin/rachai (vazio na verificação inicial).
- Editor da equipe: Visual Studio Code, com extensões e tarefas em `.vscode/`.
- Node 24 LTS e Maven Wrapper 3.9.11. Ferramentas portáteis em `.tools/` são locais e não versionadas.
- Docker Engine 27.4.0 e Compose 2.31.0 são ferramentas locais de desenvolvimento. SQL Developer é o cliente SQL. A API usa 8081 porque o Oracle antigo ocupa 8080; frontend permanece em 5173. Credenciais novas ficam apenas no `.env` ignorado pelo Git.
- Frontend em JavaScript/JSX, CSS nativo, navegação por estado/hash e cliente HTTP `fetch`, sem biblioteca de rotas nesta onda.
- Testes frontend de valores monetários com `node:test`, além de roteiro de homologação manual. Testes backend com JUnit 5/Mockito/MockMvc; integração Oracle opt-in, sem banco substituto.
- Contas individuais e grupos privados: responsável obtido da sessão; criador é administrador e membro. Convites a contas existentes são aceitos ou recusados dentro da plataforma. A decisão substitui o responsável fixo e a inclusão livre por nome; detalhes em `docs/ACESSO_E_CONVITES.md`.
- Na Onda 1, Despesa contém somente grupo, descrição, valor e data. O modelo completo da seção 11 é evolutivo: pagador e participantes envolvidos só serão implementados na Onda 2.
- A remoção de participante da Onda 1 valida o vínculo ao grupo; deve ganhar as restrições financeiras correspondentes quando F04/F05 forem ativadas.

### 9.2 Decisões ainda não fechadas
- Infraestrutura de deploy.
- Recuperação de senha, verificação de e-mail e envio de mensagens externos permanecem para decisão posterior; cadastro/login e convites internos estão aprovados.
- Estratégia de testes de interface automatizados para as próximas ondas.

O Work pode propor uma opção simples e estável para cada item acima, mas deve registrar a decisão em `STACK.docx` antes de consolidá-la no projeto.

Rastreabilidade: `docs/ONDA_1.md` e `docs/ACESSO_E_CONVITES.md`. Em 14/10/2026, Maven verify passou com 44 testes backend (incluindo Oracle real e concorrência), e frontend com 9 testes, lint, build e formatação aprovados. Cadastro/login, grupos privados e convites complementam a Onda 1. Aceite, recusa, bloqueio de acesso pendente, registro de despesa pelo membro e cancelamento da remoção foram demonstrados na interface. Ainda faltam revisão humana, Issues e integração GitHub para satisfazer integralmente a definição de pronto. Números de Issue permanecem `--` até sua criação efetiva.

## 10. Estrutura recomendada do repositório

```text
rachai/
├── backend/                 # Spring Boot
│   └── src/main/java/...
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── model/
│       ├── dto/
│       ├── exception/
│       └── config/
├── frontend/                # React
│   └── src/
│       ├── components/
│       ├── pages/
│       ├── services/
│       ├── hooks/
│       ├── routes/
│       └── assets/
├── database/
│   ├── ddl.sql
│   ├── inserts.sql
│   └── diagrams/
├── docs/
├── STACK.docx
├── BACKLOG.xlsx
├── PROJECT_CONTEXT_RACHAI.md
└── README.md
```

## 11. Modelo de dados recomendado

O modelo abaixo concretiza as regras já definidas. É uma recomendação de implementação coerente com o backlog; ajustes de nomenclatura são permitidos sem alterar o significado do domínio.

### 11.1 Entidades principais
- **Usuario:** conta individual com `id`, `nome`, `email` único normalizado e `senha_hash`. Registros legados sem credenciais permanecem sem login.
- **Grupo:** `id`, `nome`, `descricao` opcional, `usuario_responsavel_id`, `criado_em`.
- **GrupoParticipante:** vínculo único entre grupo e conta (`usuario_id` obrigatório para novos vínculos). O criador também participa. Registros legados sem conta não concedem acesso. **Convite:** grupo, destinatário, status e datas; só o destinatário responde.
- **Despesa:** `id`, `grupo_id`, `pagador_participante_id`, `descricao`, `valor_total`, `data_despesa`, `comprovante`/referência (quando F09 estiver ativa), timestamps.
- **DespesaParticipante:** `id`, `despesa_id`, `participante_id`, `grupo_id` na Onda 2. `valor_parte` acrescentado na Onda 3, persistido como NUMBER(12,2). É essencial para representar uma despesa que envolve somente parte do grupo.
- **Acerto:** `id`, `grupo_id`, `devedor_participante_id`, `recebedor_participante_id`, `valor`, `status`, `pago_em` opcional.

### 11.2 Relações
```text
Usuario 1 ── N Grupo
Grupo 1 ── N GrupoParticipante
Grupo 1 ── N Despesa
GrupoParticipante 1 ── N Despesa (como pagador)
Despesa N ── N GrupoParticipante (via DespesaParticipante)
Grupo 1 ── N Acerto
GrupoParticipante 1 ── N Acerto (devedor/recebedor)
```

## 12. Regras de negócio obrigatórias

- Todo valor monetário no backend deve usar `BigDecimal`; no Oracle, usar tipo numérico com duas casas decimais.
- Toda despesa pertence a exatamente um grupo.
- O pagador de uma despesa deve ser um participante ativo daquele mesmo grupo.
- Somente participantes do grupo podem ser incluídos em `DespesaParticipante`.
- F06: na divisão igual, a soma das parcelas deve ser exatamente igual ao valor total. Centavos restantes por arredondamento devem ser distribuídos de forma determinística.
- F07: na divisão personalizada, a soma dos valores individuais deve ser exatamente igual ao valor total; caso contrário, rejeitar com mensagem clara.
- F11: `saldo = total_pago - total_devido`. Saldo positivo = tem a receber; saldo negativo = tem a pagar; zero = quitado em termos de despesas.
- F13: gerar sugestões de acerto entre devedores e recebedores até zerar os saldos. Não movimentar dinheiro de verdade.
- O total de saldos de um grupo deve resultar em zero, respeitando centavos.
- F14: marcar um acerto como pago/recebido altera o status de acompanhamento, mas não deve apagar nem modificar o histórico das despesas.
- F15/F20 usam os acertos/status para identificar pendências e progresso.
- Exclusões que afetem cálculos devem ser validadas e recalculadas; nunca deixar saldos derivados desatualizados.
- Evitar persistir saldo como verdade primária quando ele puder ser recalculado das despesas; preferir cálculo derivado para consistência.
- Erros devem retornar mensagens claras ao frontend e códigos HTTP adequados.

## 13. API REST — contrato inicial recomendado

Contrato evolutivo: implementado até a Onda 8. O contrato vigente, incluindo rotas de registro e cancelamento de acertos, está em `docs/API.md`. As seções de entregas anteriores abaixo preservam seus estados históricos.

- `POST /api/grupos` — criar grupo.
- `GET /api/grupos/{id}` — consultar grupo.
- `GET /api/grupos` — listar grupos da conta autenticada com vínculo ativo.
- `POST /api/grupos/{id}/convites` — administrador convida uma conta por e-mail.
- `GET /api/convites` e `POST /api/convites/{id}/resposta` — destinatário consulta pendentes e aceita/recusa.
- Cadastro/login/logout e demais detalhes vigentes: `docs/API.md`. Inclusão livre por nome foi substituída por convites.
- `GET /api/grupos/{id}/participantes` — listar participantes.
- `DELETE /api/grupos/{id}/participantes/{participanteId}` — remover participante quando permitido.
- `POST /api/grupos/{id}/despesas` — registrar despesa.
- `GET /api/grupos/{id}/despesas` — histórico cronológico.
- `GET /api/grupos/{grupoId}/despesas/{id}` — detalhe individual da despesa.
- `PUT /api/grupos/{grupoId}/despesas/{id}` — editar com versão e dados.
- `DELETE /api/grupos/{grupoId}/despesas/{id}?versao={versao}` — excluir com versão.
- `GET /api/grupos/{id}/saldos` — saldos dos participantes.
- `GET /api/grupos/{id}/saldos/{participanteId}` — detalhamento individual.
- `GET /api/grupos/{id}/acertos/sugestoes` — gerar lista de quem paga quem.
- `POST /api/grupos/{id}/acertos` ou endpoint equivalente — registrar/confirmar acerto.
- `PATCH /api/acertos/{id}/status` — marcar como pago/recebido.
- `GET /api/grupos/{id}/pendencias` — listar pendências.
- `GET /api/grupos/{id}/progresso` — indicadores de acerto.

Usar DTOs de entrada/saída e não expor entidades JPA diretamente. Validar request bodies com Bean Validation.

## 14. Frontend e UX

A interface deve ser **mobile-first** e continuar confortável em desktop. Não é um app nativo. O uso principal ocorre no celular durante ou após eventos.

### 14.1 Telas principais
- Lista de grupos / eventos.
- Criação de grupo.
- Detalhe do grupo com navegação para participantes, despesas, saldos e acertos.
- Gerenciamento de participantes.
- Nova despesa / editar despesa.
- Histórico de despesas.
- Saldos dos participantes.
- Detalhamento do saldo individual.
- Acertos: quem paga, quem recebe e quanto.
- Pendências e progresso.
- Histórico de grupos/eventos anteriores.

### 14.2 Princípios de UX
- Registro de despesa deve ser rápido e exigir poucos passos.
- Valor final nunca deve aparecer sem possibilidade de detalhamento.
- Mostrar claramente pagador, participantes e divisão antes de confirmar.
- Usar mensagens de confirmação em exclusões e ações irreversíveis.
- Exibir estados de loading, vazio e erro.
- Priorizar legibilidade em telas pequenas; botões e campos adequados ao toque.
- Não exigir conhecimento financeiro do usuário; usar linguagem simples como “você deve”, “você recebe”, “pago”, “pendente”.

## 15. Estratégia de implementação

Desenvolver **incrementos verticais**: Oracle → Spring Boot → API → React → testes. Evitar construir “todo o banco”, depois “todo o backend” e apenas no final o frontend.

### 15.1 Ordem
1. Preparar repositório, toolchain e estrutura base.
2. Fechar modelo de dados inicial e scripts Oracle.
3. Executar Onda 1 (F01–F03).
4. Concluir T43–T50 (contas e convites aprovados) e validar fluxo de ponta a ponta.
5. Seguir Onda 2, Onda 3 e Onda 4 até o MVP.
6. Só então executar Incrementos 1 e 2.

### 15.2 Atenção especial
- **F11** e **F13** são críticas e tecnicamente arriscadas. Implementar primeiro a lógica Java isolada e cobri-la com testes unitários antes de conectar às telas.
- Para F09, o backlog atual prevê comprovante armazenado no Oracle. Se optar por outra estratégia de armazenamento, isso é mudança de arquitetura e deve ser aprovada e registrada.
- F16 e F19 não devem ser implementadas no momento.

## 16. Git e processo de equipe

- `main` deve permanecer estável.
- Uma branch por tarefa/Issue, exemplo: `feature/f03-cadastro-despesa` ou `task/t08-endpoint-despesa`.
- Commits pequenos e descritivos.
- Cada tarefa do backlog deve virar Issue quando a onda for ativada; a coluna `Issue #` do BACKLOG deve ser atualizada.
- Abrir Pull Request para integração; outro integrante deve revisar quando possível.
- Não misturar várias features sem relação no mesmo PR.
- Não commitar credenciais, senhas, connection strings reais ou arquivos `.env` com segredo.
- Manter README com instruções para executar frontend, backend e banco localmente.

## 17. Testes e qualidade

- JUnit 5 para services e regras financeiras.
- Testar saldos: positivo, negativo, zero, múltiplas despesas e diferentes subconjuntos de participantes.
- Testar arredondamento em divisões como R$ 100,00 / 3.
- Testar F13 com múltiplos devedores e recebedores e verificar que todos os saldos sugeridos são zerados.
- Testar validações: pagador fora do grupo, participante duplicado, lista vazia, valor <= 0, soma personalizada diferente do total.
- Testar endpoints com respostas 2xx/4xx/404 conforme cenário.
- No frontend, validar manualmente o fluxo completo de cada onda e adicionar testes automatizados quando a estratégia for definida.
- Antes de concluir uma onda, executar build e testes do backend e build/lint do frontend.

## 18. Backlog detalhado

Estimativa: `P` = até 2h; `M` = 2–6h; `G` = >6h e deve ser subdividida.

### Onda 1
- **T01 / F01 / Backend / M / Lucas:** Criar entidade Grupo, repository JPA e relacionamento com o usuário responsável
- **T02 / F01 / Backend / M / Éder:** Implementar endpoints REST para criar e consultar grupos
- **T03 / F01 / Frontend / M / Arthur:** Desenvolver tela/formulário React para criação de grupo com validações
- **T04 / F02 / Backend / M / Éder:** Criar entidade GrupoParticipante e relacionamento entre grupo e participantes
- **T05 / F02 / Backend / M / Lucas:** Substituir inclusão livre por convites; listar e remover membros com autorização
- **T06 / F02 / Frontend / M / Arthur:** Interface de participantes e convites internos vinculados a contas
- **T07 / F03 / Backend / M / Lucas:** Criar entidade Despesa com valor, descrição, data e vínculo ao grupo
- **T08 / F03 / Backend / M / Éder:** Implementar endpoint REST de cadastro de despesa com validações de negócio
- **T09 / F03 / Frontend / M / Arthur:** Desenvolver formulário React para registrar despesa e integrar com a API

### Complemento aprovado da Onda 1
- T43–T50: contas, sessões, membros, autorização, convites, telas e testes. Escopo detalhado em `docs/ACESSO_E_CONVITES.md` e no backlog. Executar antes da Onda 2.

### Onda 2
- **T10 / F04 / Backend / M / Lucas:** Relacionar a despesa ao participante que realizou o pagamento e validar o pagador
- **T11 / F04 / Frontend / P / Arthur:** Adicionar seleção do pagador no formulário de despesa e exibir essa informação
- **T12 / F05 / Backend / M / Éder:** Criar entidade DespesaParticipante para relacionar cada despesa aos participantes envolvidos
- **T13 / F05 / Frontend / M / Arthur:** Criar seletor múltiplo de participantes na inclusão e edição de despesas
- **T14 / F08 / Backend / M / Lucas:** Implementar endpoints PUT e DELETE de despesa com regras de validação
- **T15 / F08 / Frontend / M / Arthur:** Adicionar ações de editar/excluir despesa com confirmação antes da exclusão

### Onda 3
- **T16 / F06 / Backend / M / Lucas:** Implementar serviço de divisão automática do valor entre os participantes selecionados
- **T17 / F06 / Frontend / M / Arthur:** Exibir prévia da divisão e valor individual antes de confirmar a despesa
- **T18 / F09 / Backend / M / Éder:** Implementar upload e armazenamento de comprovante vinculado à despesa no Oracle
- **T19 / F09 / Frontend / M / Arthur:** Adicionar upload, visualização e remoção de comprovante na tela da despesa

### Onda 4
- **T20 / F10 / Backend / M / Éder:** Criar endpoint para listar despesas do grupo em ordem cronológica com dados do pagador
- **T21 / F10 / Frontend / M / Arthur:** Desenvolver histórico de despesas do grupo com valor, descrição, data e pagador
- **T22 / F11 / Backend / M / Lucas:** Implementar serviço de cálculo do saldo de cada participante a partir das despesas registradas
- **T23 / F11 / Backend / M / Éder:** Criar testes unitários para cenários de saldo positivo, negativo, zerado e múltiplas despesas
- **T24 / F11 / Frontend / M / Arthur:** Desenvolver tela de saldos dos participantes consumindo o cálculo da API

### Onda 5
- **T25 / F12 / Backend / M / Lucas:** Criar endpoint de detalhamento do saldo com as despesas que compõem o valor individual
- **T26 / F12 / Frontend / M / Arthur:** Desenvolver visão detalhada do saldo individual por despesa
- **T27 / F18 / Backend / P / Éder:** Criar consulta de grupos/eventos anteriores vinculados ao usuário
- **T28 / F18 / Frontend / M / Arthur:** Desenvolver tela de histórico de grupos/eventos anteriores

### Onda 6
- **T29 / F13 / Backend / M / Lucas:** Implementar algoritmo que gera a lista de acertos entre devedores e recebedores
- **T30 / F13 / Backend / M / Éder:** Criar testes unitários para múltiplos devedores, recebedores e saldos zerados
- **T31 / F13 / Frontend / M / Arthur:** Desenvolver tela de acertos mostrando quem paga, quem recebe e o valor
- **T32 / F17 / Frontend / P / Arthur:** Gerar resumo textual do evento com despesas, saldos e acertos
- **T33 / F17 / Frontend / P / Arthur:** Implementar ação de copiar/compartilhar o resumo final pelo dispositivo

### Onda 7
- **T34 / F14 / Backend / M / Éder:** Criar entidade/status de Acerto e endpoint para registrar pagamento como realizado/recebido
- **T35 / F14 / Frontend / M / Arthur:** Adicionar ação de marcar acerto como pago e atualizar o status visual
- **T36 / F07 / Backend / M / Lucas:** Permitir valores individuais personalizados por participante e validar a soma da despesa
- **T37 / F07 / Backend / M / Éder:** Criar testes para divisão personalizada, arredondamentos e valores inválidos
- **T38 / F07 / Frontend / M / Arthur:** Desenvolver formulário para editar o valor individual de cada participante

### Onda 8
- **T39 / F15 / Backend / P / Lucas:** Criar consulta de participantes com acertos pendentes no grupo
- **T40 / F15 / Frontend / M / Arthur:** Desenvolver lista de pagamentos pendentes com participante e valor
- **T41 / F20 / Backend / P / Éder:** Criar endpoint com indicadores de progresso dos acertos do grupo
- **T42 / F20 / Frontend / M / Arthur:** Exibir progresso dos acertos com quantidade quitada, pendente e percentual concluído

## 19. Definição de pronto (Definition of Done)

- Tarefa/Issue implementada conforme feature e regra de negócio.
- Código compila e aplicação executa localmente.
- Testes pertinentes passam.
- Endpoint validado quando aplicável.
- Frontend trata sucesso, loading, vazio e erro quando aplicável.
- Nenhuma credencial foi commitada.
- Documentação atualizada se houver decisão nova.
- PR revisado e mergeado sem quebrar funcionalidades anteriores.
- Feature demonstrável na perspectiva do usuário.

## 20. Declaração sobre IA

A equipe utiliza ChatGPT/ChatGPT Work para estruturar o projeto, esclarecer dúvidas, auxiliar na geração e revisão de código, documentação e testes. Todo código gerado por IA deve ser revisado, testado e compreendido por um integrante antes do commit. O Work deve explicar decisões técnicas relevantes e evitar gerar grandes blocos de arquitetura opaca que a equipe não consiga defender.

## 21. Fontes de verdade e documentos do projeto

- `STACK.docx` — stack, arquitetura e declaração de IA.
- `BACKLOG.xlsx` — 42 tarefas originais e T43–T50 na aba ACESSO; ondas, origem, tipo, estimativa e responsável.
- `PROJECT_CONTEXT_RACHAI.md` — este handoff consolidado.
- Relatório da O1 — pesquisa, entrevistas, insights e mapa de empatia.
- Artefatos Lean Inception no Miro — visão, matriz, objetivos, personas, jornadas, features, revisão e sequenciador.

**Em caso de conflito:** decisões mais recentes explicitamente aprovadas pela equipe prevalecem; atualizar os documentos para eliminar a inconsistência.

## 22. Entrega da Onda 2 em 17/10/2026

T10–T15 implementadas após a autorização para prosseguir. Pagador e envolvidos são membros do mesmo grupo; pelo menos um envolvido, sem repetição. O pagador pode não consumir. Todos os membros podem consultar/corrigir despesas; demais contas recebem 404. Remover um membro vinculado a despesa é bloqueado.

Migrações 002–004 preservam dados existentes. Despesas legadas mantêm campos ausentes até correção explícita. Edição/exclusão conferem versão sob lock transacional do grupo, evitando sobrescrita de alterações simultâneas. A correção usa o número da despesa e a confirmação após cadastro; não há histórico F10 nesta etapa.

53 testes backend com Oracle real e 11 frontend aprovados, lint/build/formatação aprovados. Cadastro e edição na interface, cancelamento da exclusão e layout mobile/desktop conferidos. Evidências em `docs/ONDA_2.md`. Revisão humana, Issues reais e integração GitHub continuam pendentes. Ondas 3–8 não iniciadas.

## 23. Ajuste de usabilidade aprovado em 18/10/2026

O usuário solicitou exibir automaticamente todas as despesas ao abrir o grupo, em cartões abertos um abaixo do outro, sem pesquisa obrigatória por número. Isso antecipa apenas F10 / T20–T21 da Onda 4. A origem e os responsáveis das tarefas permanecem no backlog; a nota na aba INSTRUÇÕES registra a antecipação. Esta decisão substitui a restrição de histórico das etapas anteriores; não ativa saldos nem outras tarefas futuras.

GET /api/grupos/{grupoId}/despesas retorna a lista completa, ordenada por data decrescente e ID decrescente no empate, somente para membros atuais. Cada cartão mostra descrição, valor, data, pagador e envolvidos e mantém editar/excluir/recarregar. A lista reflete imediatamente as operações da própria tela; Atualizar despesas consulta mudanças de outros membros. Estados de carregamento, vazio e erro são explícitos. Despesas antigas são preservadas.

Valor no JSON sempre tem duas casas decimais, inclusive após leitura do Oracle; o formatador também aceita strings inteiras sem produzir “undefined”. Sem novas bibliotecas, tecnologias ou migrações. A arquitetura registrada em STACK.docx permanece vigente. Evidências em docs/LISTAGEM_DESPESAS.md.

## 24. Onda 3 autorizada em 18/10/2026

T16/T17: divisão exata em centavos com BigDecimal, resto atribuído em ordem crescente do ID do vínculo no grupo. Parcelas podem ser zero se houver menos centavos que participantes. A soma sempre corresponde ao total. A prévia não grava dados; a tela exige conferir a divisão antes de confirmar. Alterar qualquer campo invalida a prévia. Cadastro e edição recalculam e persistem todas as parcelas. Migração 005 preenche somente vínculos existentes e aumenta a versão dos registros afetados; despesas sem envolvidos permanecem sem divisão.

T18/T19: um comprovante opcional JPG/PNG/PDF até 5 MB por despesa; imagens até 16 MP. Armazenamento no Oracle BLOB em tabela separada, metadados leves na despesa. Upload, substituição e remoção usam versão, CSRF e lock do grupo. Visualização de imagem/download exigem membro atual. PDF é disponibilizado para baixar e abrir no leitor do dispositivo; a visualização integrada não foi compatível com sandbox. Remover comprovante preserva a despesa e sua divisão; excluir despesa apaga o arquivo por cascade. Não há novos frameworks. A quota Oracle local segue 100 MB. PDF é validado por assinatura, sem antivírus ou parser completo; conteúdo servido com sandbox/nosniff e sem cache. Referências, testes e evidências em docs/ONDA_3.md.

65 testes backend (Oracle real incluído) e 13 frontend aprovados; lint/build/formatação aprovados. Issues e revisão humana permanecem pendentes. T22 em diante não iniciadas. T20/T21 continuam a antecipação aprovada da listagem; Onda 4 inteira ainda não concluída.

## 25. Entrega local da Onda 4

Após validar a Onda 3, o usuário autorizou T22–T24 em 19/10/2026. A pausa de 20/10 foi encerrada pelo pedido de retomada em 27/10/2026. Esta seção atualiza o estado histórico das seções anteriores: T20/T21 já estavam entregues; T22/T23/T24 estão implementadas e testadas localmente. Homologação da Onda 4 pelo usuário e revisão/integração da equipe ainda são necessárias para encerrar formalmente o MVP.

GET /api/grupos/{grupoId}/saldos calcula, para cada vínculo do grupo, total pago menos as parcelas persistidas das despesas. Valores com BigDecimal e duas casas no JSON; soma dos saldos igual a zero. Contas de mesmo nome permanecem distintas. Membros sem movimento aparecem com saldo zero. Saldo é derivado, sem tabela ou coluna nova, sem armazenar resultado calculado e sem movimentar dinheiro.

A leitura usa o mesmo lock transacional do grupo empregado nas mutações e revalida a autorização após obtê-lo. Não altera versões das despesas nem lê BLOBs. Lançamento sem pagador, sem envolvidos ou com parcelas inválidas/incompatíveis bloqueia os saldos do grupo: a API retorna completo=false, os IDs para correção e nenhuma lista parcial. Dados legados não são preenchidos automaticamente.

A tela mostra A pagar, A receber ou Saldo zerado, total pago e sua parte nas despesas. Cadastro, edição, exclusão e atualização dos dados na própria tela solicitam novos saldos. Atualizar saldos consulta alterações externas. Requisições anteriores são canceladas/ignoradas e valores anteriores ficam ocultos durante atualização ou erro. O aviso de dados incompletos oferece atalho ao cartão correspondente. Não há detalhamento individual por despesa, sugestões de acerto ou registro de pagamentos.

75 testes backend (incluindo Oracle real) e 14 frontend aprovados em 19/10/2026. Empacotamento final aprovado após liberar o JAR em uso no Windows; lint/build/formatação frontend aprovados. Conferência da interface em 19–20/10: grupo vazio, cadastro, edição, centavos, legado, foco no atalho de correção e layout de celular. Registros consolidados em 27/10/2026 em docs/ONDA_4.md. Stack vigente preservada, sem novas dependências ou migrações. A nota INSTRUÇÕES!A15:B15 do backlog registra a ativação. T25 em diante não iniciadas.


## 26. Entrega local das Ondas 5–8

Autorizadas em 27/10/2026 pelo pedido para continuar as próximas ondas; conferência integrada em 31/10 e retomada em 02/11/2026. T25–T42 implementadas em ordem, com testes por onda. Esta seção substitui as indicações históricas de funcionalidades futuras nas seções anteriores. F16/F19 permanecem no Parking Lot.

Onda 5: detalhamento individual reconcilia despesas com o saldo, incluindo pagador fora do consumo e parcelas zero. Histórico de grupos é consultado por data de criação, pois não existe data de encerramento no modelo; não implica quitação/arquivamento. Acesso sempre depende de vínculo atual.

Onda 6: algoritmo determinístico de acertos em BigDecimal, compensando devedores/credores por ID, sem promessa de mínimo global de transferências. Resumo textual consistente com despesas, parcelas, saldos e acertos; copiar/compartilhar apenas por ação da pessoa. Nenhuma mensagem externa é enviada automaticamente.

Onda 7: pagamentos registrados por uma das partes como PAGO; cancelamento com versão e histórico CANCELADO. Acertos pendentes são derivados de saldo das despesas + pagamentos feitos − recebidos. Revisão da consulta, validação da sugestão e lock do grupo impedem valores desatualizados/duplicidade. Alterar/excluir despesas não apaga pagamentos e pode gerar acerto inverso. Participante com histórico de pagamentos não é removido. Não há movimentação bancária nem verificação externa do pagamento. Divisão personalizada exige exatamente as parcelas dos envolvidos, valores não negativos e soma exata; o modo fica salvo na despesa. Migração 007 aplicada sem modificar parcelas existentes.

Onda 8: pendências por pessoa, total pago/pendente e progresso por quantidade de acertos. Cancelados são excluídos do cálculo, mas preservados no histórico. Percentual considera registros PAGO e sugestões atuais, podendo mudar com novas despesas/correções. Dados incompletos bloqueiam indicadores em vez de apresentar uma falsa quitação.

77, 81, 86 e 88 testes backend aprovados ao concluir cada onda, com Oracle real e zero ignorados. Documentação em docs/ONDA_5.md até docs/ONDA_8.md. Não houve nova tecnologia; STACK.docx permanece vigente. Homologação, revisão humana, Issues e integração GitHub pendentes; nenhum commit/push foi feito.

Fechamento técnico em 02/11/2026: botão único para aplicar o filtro de histórico e atualizar os resultados; corte inclusivo, período vazio e limpeza validados pela interface, inclusive no celular. Quinze testes frontend, lint, build e formatação aprovados. Conferência de pagamentos, prévia personalizada e resumo realizada em 31/10; detalhes e limites registrados em docs/ONDA_8.md. Serviços locais retomados preservando o banco existente.
