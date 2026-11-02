# Oracle local do Rachaí

Este banco é separado do Oracle XE 11g e dos contêineres dos outros projetos. O Docker Desktop deve estar aberto com o engine ativo.

## Criar e iniciar

Na raiz do projeto, execute uma vez:

```powershell
.\scripts\oracle.ps1 setup
```

O comando cria `.env` com duas senhas aleatórias diferentes, baixa a imagem oficial e inicia `rachai-oracle`. A imagem é Oracle AI Database 26ai Free Lite, release `23.26.0.0`, fixada pelo digest linux/amd64 no `compose.yaml`. A primeira inicialização pode demorar alguns minutos.

Veja o estado:

```powershell
.\scripts\oracle.ps1 status
```

Quando aparecer `healthy`, prepare o schema **uma única vez**:

```powershell
.\scripts\init-oracle.ps1
```

O script verifica o projeto Docker, recusa um usuário `RACHAI` já existente e aplica `database/ddl.sql`, `database/inserts.sql` e as migrações numeradas de `database/migrations/` em ordem. Em schema existente, executar somente `.\scripts\migrate-oracle.ps1`: migração incremental idempotente que preserva os dados. A imagem Lite não inclui o tablespace USERS; por isso o script cria `RACHAI_DATA`, com arquivo no volume persistente, inicialmente 20 MB e crescimento limitado a 100 MB. O administrador só é usado para preparar o schema. A aplicação se conecta como `RACHAI`, com permissões limitadas e quota de 100 MB nesse tablespace próprio.

## Conectar pelo SQL Developer

Crie uma **nova conexão**, sem editar as conexões dos exercícios:

| Campo | Valor |
|---|---|
| Nome da conexão | Rachai Local |
| Nome de usuário | RACHAI |
| Senha | Valor de `ORACLE_PASSWORD` no `.env` local |
| Tipo de conexão | Básico |
| Host | localhost |
| Porta | 1522 |
| Seleção | Nome do Serviço (não SID) |
| Nome do serviço | FREEPDB1 |
| Atribuição/Role | padrão |

Abra `.env` no VS Code e copie a senha da aplicação para a janela do SQL Developer. Não use a variável `ORACLE_ADMIN_PASSWORD` nessa conexão. Não envie o arquivo nem publique a senha.

Clique em **Testar** e depois em **Conectar**. As tabelas esperadas são `USUARIOS`, `GRUPOS`, `GRUPO_PARTICIPANTES`, `DESPESAS`, `CONVITES`, `DESPESA_PARTICIPANTES` e `COMPROVANTES`. Para conferir:

```sql
select table_name from user_tables order by table_name;
select id, nome from usuarios;
```

Se a conexão falhar, registre o código ORA e a mensagem sem a senha. O usuário confirmou a conexão e a visualização dos dados nesta instalação do SQL Developer.

## Aplicação e testes

A Onda 3 acrescenta as migrações `005_divisao_igual.sql` e `006_comprovantes.sql`. Em banco existente, use `migrate-oracle.ps1` com a API parada. A primeira calcula parcelas somente para os envolvidos já vinculados e atualiza a versão das despesas afetadas; a segunda cria o armazenamento de comprovantes. Ambas são idempotentes e foram aplicadas nesta instalação. Despesas antigas sem envolvidos permanecem sem divisão.

Os arquivos ficam em BLOB no Oracle, separados dos metadados usados na listagem. O limite por comprovante é 5 MB; a quota total local permanece 100 MB, compartilhada com os demais dados e índices. Esse ambiente serve para desenvolvimento e não dimensiona armazenamento de produção.

```powershell
# Terminal 1
.\scripts\start-backend.ps1

# Terminal 2
.\scripts\frontend.ps1 dev
```

API em `http://127.0.0.1:8081`; interface em `http://127.0.0.1:5173`. Os scripts carregam as variáveis permitidas do `.env` para o processo; o Spring Boot não lê esse arquivo sozinho. Variáveis já definidas no terminal prevalecem.

Para executar também a integração real:

```powershell
$env:ORACLE_INTEGRATION_TEST = 'true'
.\scripts\test-backend.ps1
Remove-Item Env:\ORACLE_INTEGRATION_TEST
```

## Parar e retomar

```powershell
.\scripts\oracle.ps1 stop
.\scripts\oracle.ps1 start
```

Os dados ficam no volume `rachai_oracle-data` e sobrevivem à parada/recriação do contêiner. Não apague esse volume para reiniciar o banco. O serviço não inicia automaticamente com o Docker; execute `start` quando for trabalhar.

O `.env` deve ser preservado localmente junto com o volume. Gerar novas senhas no arquivo não muda as senhas de um banco já criado. Em outro computador, copie apenas o código e faça seu próprio setup para gerar credenciais novas.

Fonte da imagem e parâmetros: [Oracle Database container images](https://github.com/oracle/docker-images/blob/main/OracleDatabase/SingleInstance/README.md).
