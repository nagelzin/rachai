# Listagem automática de despesas

## Decisão de 18/10/2026

O usuário pediu que as despesas já registradas apareçam automaticamente ao abrir o grupo, com os detalhes abertos em cartões empilhados. F10 / T20–T21 foi antecipada da Onda 4 para atender esse ajuste de usabilidade. A origem das tarefas, responsáveis e estimativas foram preservados no BACKLOG.xlsx; a nota INSTRUÇÕES!A13:B13 registra a decisão. Não foram ativadas outras funcionalidades das ondas seguintes. Sem novas tecnologias ou migrações; STACK.docx continua vigente quanto à arquitetura e versões.

## Comportamento

- GET /api/grupos/{grupoId}/despesas exige sessão e vínculo atual ao grupo. Lista completa em data decrescente, com ID decrescente no empate; inclui lançamentos legados e retorna [] para grupo vazio.
- Cada cartão exibe descrição, valor, data, pagador e envolvidos, com editar, excluir e recarregar. A pesquisa obrigatória por número foi removida.
- Cadastro, edição e exclusão atualizam o estado da lista. Alterar a data reposiciona o cartão. Atualizar despesas consulta alterações feitas por outros membros. Uma edição em andamento não é descartada por atualização da lista.
- Carregamento, vazio, falha com possibilidade de tentar novamente e conflito de versão são tratados. A confirmação de exclusão identifica o lançamento e mantém o foco de teclado.
- Dinheiro no DTO sempre usa duas casas decimais, inclusive após leitura do Oracle. O formatador também aceita strings como 300 e 300.5, mostrando R$ 300,00 e R$ 300,50 sem ponto flutuante.

## Validação

- 55 testes backend aprovados com Oracle real, zero falhas/erros/ignorados. Os novos cenários cobrem lista vazia, ordenação e desempate, múltiplos envolvidos sem duplicar cartões, isolamento entre grupos, sessão ausente, ex-membro, legado, edição, exclusão e dinheiro após limpar o contexto JPA e reler do banco.
- Empacotamento aprovado. A primeira tentativa de empacotar encontrou o JAR em uso pelo servidor; a API foi parada, o pacote foi gerado com sucesso e a nova versão iniciada. Os testes já aprovados não foram repetidos nessa segunda etapa.
- 11 testes frontend aprovados, incluindo regressão para valores inteiros e com uma casa decimal; lint, build e Prettier aprovados.
- UI com conta fictícia no grupo #54: #112 e #61 apareceram ao abrir, sem informar número. #138 foi cadastrada com valor 300 e apareceu imediatamente no início da lista como R$ 300,00. Edição da descrição atualizou o mesmo cartão, sem duplicação. Confirmação de exclusão aberta e cancelada; atualização da lista preservou o lançamento. Exclusão efetiva e isolamento foram verificados nos testes de API/Oracle.
- Celular de 390 px conferido sem rolagem horizontal. Evidência em docs/.qa/listagem-despesas.png, ignorada pelo Git. Dados fictícios de demonstração mantidos; nenhum dado real alterado.
- Planilha exportada e renderizada; comparação com a cópia anterior confirmou alteração somente nas duas células da nota de antecipação. Demais tarefas preservadas.

Código disponível localmente para revisão humana. Nenhum commit, push, Issue ou PR criado.
