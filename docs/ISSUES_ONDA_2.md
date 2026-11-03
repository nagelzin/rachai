# Critérios das Issues da Onda 2

> Atualização de 02/11/2026: issues publicadas. Números e situação atual em [ISSUES_GITHUB.md](ISSUES_GITHUB.md). O texto abaixo preserva os critérios e o estado histórico anterior à publicação.

Destino: https://github.com/nagelzin/rachai. Rascunhos locais, sem Issues publicadas. Preencher números reais no backlog após criação; revisão humana antes do commit.

| Tarefa | Responsável | Estimativa | Critérios de aceite |
|---|---|---|---|
| T10 Relacionar pagador | Lucas | M | Pagador é membro do mesmo grupo; obrigatório nos novos registros; migração preserva legado |
| T11 Selecionar/exibir pagador | Arthur | P | Seleção mostra membros; confirmação e consulta mostram quem pagou |
| T12 Relacionar envolvidos | Éder | M | Pelo menos um membro, sem duplicação; vínculo correto no Oracle; sem parcelas nesta onda |
| T13 Seleção múltipla | Arthur | M | Cadastro/edição permitem subconjunto; pagador pode ficar fora; seleção preservada ao editar |
| T14 Editar/excluir despesa | Lucas | M | Mesmo grupo, conta autorizada, validações, versão, conflitos e integridade; exclusão limpa só seus vínculos |
| T15 Ações de correção | Arthur | M | Consulta por número, edição preenchida, confirmação/cancelamento de exclusão e mensagens de conflito |

Todas precisam passar pelas verificações em TESTES.md, revisão da equipe e PR/integração. Evidências locais em ONDA_2.md não substituem essas etapas.
