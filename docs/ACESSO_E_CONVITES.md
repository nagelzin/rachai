# Contas e convites

Decisão aprovada pelo usuário em 12/10/2026. Complemento da Onda 1 antes da Onda 2. Cadastro com nome, e-mail e senha; login; grupos privados; criador administrador; convite pelo e-mail de uma conta existente; caixa de convites para aceitar ou recusar. Envio de e-mail, recuperação de senha e verificação de propriedade do endereço não fazem parte deste incremento local. E-mail é identificador da conta, não identidade verificada.

## Regras

- Administrador também é membro. Somente ele convida e remove membros. Não pode remover a si próprio.
- Membros podem consultar grupo e participantes e cadastrar despesas. Cada operação valida o vínculo atual no servidor. Quem não pertence ao grupo recebe 404, sem seus dados.
- Convites são vinculados ao ID da conta encontrada pelo e-mail normalizado. Apenas o destinatário responde. Aceitar cria o vínculo numa transação; recusar não concede acesso. Um convite por grupo/destinatário, com estado PENDENTE, ACEITO ou RECUSADO. Reenvio após recusa/remoção reabre o mesmo convite.
- E-mails únicos, sem distinção entre maiúsculas/minúsculas. Nomes iguais são permitidos para pessoas diferentes. O administrador deve confirmar com a pessoa o endereço da conta usada no aplicativo.
- Grupos e dados antigos permanecem no Oracle. Usuários antigos sem credenciais ficam sem login; nenhuma conta recém-criada herda grupos de demonstração automaticamente.

## Implementação

Spring Security, versão gerenciada pelo Spring Boot 3.5.16; PBKDF2 SHA-256 para hash de senha; sessões no servidor, cookie HttpOnly/SameSite=Lax, expiração por inatividade de 30 minutos e renovação do ID no login. CSRF obrigatório inclusive no login, cadastro e logout. Frontend busca token via GET /api/auth/csrf e envia no cabeçalho; não armazena senha ou sessão no localStorage. Limitação local de tentativas por IP, mensagens de login genéricas e respostas sem cache. HTTPS e cookie Secure são obrigatórios numa futura publicação; desenvolvimento continua restrito ao localhost.

Migração SQL versionada e incremental, sem apagar os registros anteriores. Hibernate mantém validate. Cadastro público atribui apenas usuário comum; administrador deriva da criação do grupo, nunca de um campo enviado pelo cliente.

## Tarefas adicionais

T43 cadastro e credenciais; T44 login/sessão/CSRF; T45 migração e vínculo de membros; T46 autorização e meus grupos; T47 convites transacionais; T48 telas de conta; T49 telas de grupos/convites; T50 testes de isolamento e atualização documental. Responsáveis da equipe a definir, Issues ainda não criadas. T01–T42 preservam IDs; T05/T06 passam a usar convites em lugar da inclusão livre por nome.

## Critérios de aceite

Testar cadastro, senha incorreta, logout e expiração; acesso anônimo; CSRF; lista privada; criador administrador; membro sem poderes administrativos; terceiro sem acesso; convite pendente sem acesso; destinatário aceita/recusa; tentativa de responder convite de outro; respostas repetidas e concorrentes sem vínculo duplicado; remoção revoga acesso; mesma pessoa não entra duas vezes; centavos preservados.

Fontes técnicas: [Spring Security CSRF](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html), [form login](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/form.html), [password storage](https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html).

## Resultado da validação de 14/10/2026

Implementação de T43–T50 concluída localmente; revisão e integração da equipe pendentes. Maven verify gerou o pacote com 44 testes aprovados, zero falhas/erros/ignorados. Frontend passou em 9 testes, lint, build e formatação. Migração reaplicada sem erro no Oracle real.

Na interface, a conta convidada aceitou, entrou no grupo e registrou despesa de R$ 123,45 sem poderes administrativos. Um segundo convite foi recusado e não concedeu acesso. Link direto antes do aceite foi negado. Cancelar a confirmação de remoção preservou o membro. Login com hash antigo abriu Meus grupos. Layout mobile e desktop conferido; evidência em `docs/.qa/contas-convites.png`. Detalhes e IDs fictícios em `ONDA_1.md`.

Foram ajustados prioridade dos convites no celular, indicação de atualização, destino após login e conferência de sessão ao recuperar foco da janela. A revisão manual da equipe ainda deve ampliar testes de teclado, textos longos e troca de conta entre abas.

README, contrato API, roteiro de testes, guia Oracle, contexto e stack foram atualizados. O Word foi validado por extração de conteúdo; revisão visual pendente porque o renderizador LibreOffice não está disponível. Nenhuma Issue, commit ou PR foi criado.
