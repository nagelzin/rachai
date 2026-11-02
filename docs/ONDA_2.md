# Execução da Onda 2

Onda iniciada após a autorização para prosseguir com as implementações. Escopo T10–T15 / F04, F05 e F08 do BACKLOG.xlsx. Trabalho iniciado em 14/10/2026 e retomado em 17/10/2026. Onda 1 e contas/convites são a base preservada.

## Regras desta entrega

- O pagador precisa ser membro atual do mesmo grupo. Pagar não obriga a participar do consumo.
- Toda nova despesa deve indicar pelo menos um envolvido, sem duplicação, todos membros do grupo.
- Membros do grupo podem consultar e corrigir uma despesa. As mesmas validações de cadastro se aplicam à edição.
- Uma pessoa usada como pagador ou envolvido não pode ser removida enquanto o vínculo existir.
- Despesas antigas permanecem sem pagador/envolvidos quando essas informações não existiam. Não inferir nem atribuir dados; completar esses campos ao editar.
- Edição e exclusão usam a versão lida para evitar sobrescrever alterações de outra pessoa. Uma exclusão exige confirmação na interface.
- Acesso à correção pelo número da despesa e pela confirmação após cadastro. O histórico completo F10 permanece para a Onda 4.
- Não implementar parcelas, divisão, comprovantes, saldos, acertos ou recursos do Parking Lot.

## Andamento

| Tarefa | Escopo | Situação |
|---|---|---|
| T10 | Pagador e validação no backend/Oracle | Implementado; suíte com 45 testes aprovada em 14/10 |
| T11 | Seleção/exibição do pagador na interface | Implementado; frontend verificado |
| T12 | Vínculos dos envolvidos | Implementado; Oracle e integridade aprovados |
| T13 | Seleção múltipla | Implementado; frontend verificado |
| T14 | Edição e exclusão com validação | Implementado; isolamento, versões, exclusão e concorrência aprovados |
| T15 | Interface de correção e confirmação | Implementado; cadastro, edição e cancelamento de exclusão conferidos na UI |

Responsáveis e estimativas permanecem os do backlog. Issues ainda não criadas; nenhum número inventado. Revisão humana antes de commit continua obrigatória conforme o contexto do projeto.

## Evidências de 17/10/2026

- Maven verify: BUILD SUCCESS, JAR gerado, 53 testes aprovados com Oracle real, zero falhas/erros/ignorados. Logs locais em `.tools/onda2-final-tests.log` (ignorados pelo Git).
- Frontend: 11 testes, lint, build e formatação aprovados. PUT/DELETE enviam CSRF e versão; conflito não dispara repetição da gravação.
- Migrações 002–004 aplicadas. Nenhuma despesa anterior recebeu pagador/envolvidos fictícios.
- Conta fictícia membro #88, grupo #54: despesa #112 criada com R$ 123,45, pagador Teste membro e envolvido Teste admin; corrigida para R$ 99,99, pagador Teste admin e envolvido Teste membro. Confirmação de exclusão aberta e cancelada; recarga preservou o registro. Exclusão efetiva e limpeza dos vínculos verificadas pelos testes de API/Oracle.
- Formulário de edição mostrou os dados anteriores e a seleção correta; a navegação dá foco à descrição ao abrir a correção. Cancelamento da exclusão retorna foco ao botão correspondente.
- Layout de celular de 390 px e desktop conferido, sem rolagem horizontal. Evidência local em `docs/.qa/onda-2.png`, ignorada pelo Git. Dados fictícios #112 mantidos para demonstração.

O backlog recebeu nota de ativação e validação local da Onda 2, preservando tarefas, responsáveis, estimativas e números de Issue. Documentação técnica e contexto foram atualizados. Revisão visual do Word continua pendente pela ausência de LibreOffice no runtime. Nenhum commit, Issue ou PR criado; a revisão humana anterior ao commit continua pendente. Onda 3 não iniciada.

## Ajuste posterior de 18/10/2026

A pedido do usuário, a consulta obrigatória por número foi substituída por listagem automática em cartões abertos. Antecipação específica de F10/T20–T21 registrada em docs/LISTAGEM_DESPESAS.md; as evidências acima descrevem a entrega anterior.
