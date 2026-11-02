# Decisões do setup

Registro de 12/10/2026, complementar à fonte de verdade `STACK.docx`.

| Decisão | Motivo e limite |
|---|---|
| Java 21 e Spring Boot 3.5.16 | Compatibilidade com o JDK local e JUnit 5 exigido; versão efetivamente resolvida no Maven Central |
| React 19.3.0, Vite 8.3.4 | SPA simples; versões conferidas no registro npm e fixadas no lockfile |
| Node 24.21.0/npm 11.19.0 portátil local | O Node 22.11 do Windows não atende ao mínimo do Vite; não alterar a instalação global |
| Maven Wrapper 3.3.4 / Maven 3.9.11 | Build reproduzível sem instalação global de Maven |
| Oracle 26ai Free Lite (23.26.0.0) em Docker | Imagem oficial fixada por digest, porta local 1522 e volume próprio; preserva o Oracle XE 11g existente. Substitui o alvo inicial 23ai Free; sem H2 |
| Docker Engine 27.4.0 / Compose 2.31.0 | Ferramentas locais já disponíveis; compõem apenas o ambiente de desenvolvimento, sem deploy |
| SQL Developer | Cliente SQL já instalado, com nova conexão para RACHAI em localhost:1522/FREEPDB1 |
| API na porta 8081 | Evitar conflito com o listener do Oracle antigo, que ocupa 8080 |
| VS Code | Escolha explícita do usuário; extensões e tarefas recomendadas no projeto |
| Fetch, CSS nativo, estado/hash React | Suficientes para esta onda; sem roteador, framework visual ou cliente HTTP adicional |
| Contas e sessões Spring Security | Complemento aprovado em 12/10/2026; substitui responsável fixo. Criador administrador, membros por aceite e autorização no servidor |
| Decimal em string na API | Evitar perda de centavos no JavaScript; BigDecimal e NUMBER(12,2) no backend/banco |
| Meus grupos e consulta por ID | Somente grupos com vínculo ativo; não implementa histórico de eventos F18 |
| Pastas apenas quando usadas | Manter estrutura didática; não criar abstrações vazias |

As versões exatas das dependências transitivas do backend são gerenciadas pelo BOM do Spring Boot; as do frontend estão no `package-lock.json`. Testes e execução local validaram o conjunto escolhido. Atualizações futuras devem repetir as verificações e atualizar a stack.

Referências consultadas: [requisitos do Spring Boot 3.5](https://docs.spring.io/spring-boot/3.5/system-requirements.html), [guia do Vite](https://vite.dev/guide/), [versões do React](https://react.dev/versions), [registro npm do React](https://registry.npmjs.org/react/latest) e [registro npm do Vite](https://registry.npmjs.org/vite/latest). Os endpoints `latest` mudam; o lockfile registra a resolução usada nesta entrega.

## Onda 2

Sem novas bibliotecas. Migrações 002/003 adicionam pagador e envolvidos com FKs compostas para impedir vínculo entre grupos. Migração 004 acrescenta versão. Alterações de despesas e remoções de membros usam lock de escrita no mesmo grupo; o service confere a versão e a incrementa em cada edição. A conferência acontece após adquirir o lock e revalidar o membro. Despesas legadas não recebem pagador ou envolvidos por inferência.

Correção individual por número mantém F08 utilizável antes do histórico F10. A seleção de envolvidos ainda não calcula parcelas. Registro de implementação e evidências em `ONDA_2.md`.
