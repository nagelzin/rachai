# Onda 3 — divisão e comprovantes

Autorizada em 18/10/2026; verificação final em 19/10/2026. Implementação local concluída para revisão e homologação do usuário. Não foram iniciados saldos, acertos ou outras funcionalidades futuras. T20/T21 continuam sendo somente a antecipação da listagem já aprovada.

| Tarefa | Responsável no backlog | Entrega local |
|---|---|---|
| T16 | Lucas | Divisão igual exata, persistida e recalculada na edição |
| T17 | Arthur | Prévia obrigatória antes da confirmação e parcelas nos cartões |
| T18 | Éder | Comprovante opcional em Oracle BLOB, protegido por acesso ao grupo |
| T19 | Arthur | Anexar, substituir, visualizar imagem, baixar e remover comprovante |

## Divisão

O cálculo usa centavos inteiros e BigDecimal. O resto é distribuído pela ordem crescente do ID do vínculo do participante no grupo. A diferença entre parcelas é de no máximo um centavo e sua soma corresponde ao total. Uma parcela pode ser zero quando houver menos centavos que pessoas. O pagador pode ficar fora dos envolvidos.

Conferir divisão não grava dados. Alterar o formulário invalida a prévia; confirmar envia os dados para recálculo no servidor. Não são aceitas parcelas calculadas pelo cliente como fonte de verdade. A migração 005 preenche apenas vínculos existentes e aumenta a versão das despesas alteradas. Registros antigos sem envolvidos são preservados.

## Comprovantes

Cada despesa aceita um JPG, PNG ou PDF de até 5 MB. Imagens têm limite de 16 milhões de pixels e validação por decodificação. Nome do arquivo é normalizado e MIME informado pelo cliente não determina o tipo servido. PDF passa por validação de assinatura e marcador final; isso não equivale a parser completo ou antivírus.

Imagens podem ser visualizadas em nova aba e baixadas. PDF é entregue como download para abrir no leitor do dispositivo: a visualização integrada apresentou incompatibilidade com o sandbox, portanto o botão é explicitamente “Baixar PDF”. A resposta usa tipo validado, nosniff, sandbox e no-store. Downloads e visualizações exigem sessão de membro atual.

Upload, substituição e remoção exigem CSRF, autorização e versão atual sob lock transacional. BLOB e metadados são atualizados na mesma transação; a leitura recupera ambos em uma consulta. Remover o comprovante preserva a despesa e a divisão. Excluir a despesa remove seu comprovante por cascade. Listagens não carregam o BLOB.

A migração 006 foi aplicada. A quota de desenvolvimento segue em 100 MB; não foi ampliada automaticamente. Não foram adicionados frameworks ou dependências.

## Evidências

- Suíte backend com Oracle real: **65 testes, zero falhas, erros ou ignorados**. Depois do ajuste final de PDF como attachment, os **3 testes de OracleComprovanteTest** foram repetidos com sucesso, assim como a geração do JAR.
- Frontend: **13 testes**, lint, build e formatação aprovados.
- Divisão: soma exata, desempate estável, valores extremos, duplicados, prévia sem gravação, persistência no Oracle, edição e isolamento de grupos.
- Comprovantes: BLOB real, substituição, conflito de versão, exclusão em cascade, revogação de acesso, CSRF, formatos inválidos e preservação da despesa na remoção.
- HTTP real: upload PNG e PDF, conteúdo devolvido idêntico, arquivo acima de 5 MB rejeitado com 413 sem alteração do registro.
- Navegador: prévia de R$ 100,01, invalidação após alterar para R$ 100,03, nova divisão de R$ 50,02 + R$ 50,01 e cadastro confirmado. Imagem visualizada; PDF baixado pelo botão e comparado byte a byte com o arquivo fictício original. Confirmação de remoção aberta e cancelada, preservando o comprovante.
- Layout de celular conferido com viewport solicitado de 390 × 844, sem rolagem horizontal. Evidência local: `.qa/onda-3.png`.

Os testes usaram somente contas e arquivos fictícios. O envio efetivo de arquivos foi verificado por HTTP e testes de integração; não se atribui a esta rodada validação do seletor de arquivos pelo navegador. Logs locais ignorados pelo Git: `.tools/onda3-comprovantes-tests.log` e `.tools/onda3-download-tests.log`.

## Revisão e próximos passos

Revisão humana, Issues reais e integração GitHub permanecem pendentes, conforme definição de pronto do projeto. Os rascunhos estão em [ISSUES_ONDA_3.md](ISSUES_ONDA_3.md). Nenhum commit ou publicação foi realizado. A próxima implementação deve seguir o restante da Onda 4, após autorização.

Fontes de verdade atualizadas: contexto, STACK.docx e nota da aba INSTRUÇÕES do BACKLOG.xlsx. Conteúdo do DOCX conferido; a renderização visual não pôde ser concluída porque LibreOffice não está instalado/disponível no PATH. A nota do backlog foi renderizada e inspecionada; as demais células foram preservadas.

Referências técnicas: [Spring Boot MultipartProperties](https://docs.spring.io/spring-boot/3.5/api/java/org/springframework/boot/autoconfigure/web/servlet/MultipartProperties.html), [guia Spring de upload](https://spring.io/guides/gs/uploading-files/) e [OWASP File Upload Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/File_Upload_Cheat_Sheet.html).
