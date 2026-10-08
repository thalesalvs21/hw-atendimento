# HW Atendimento

Aplicativo de desktop para registrar os atendimentos técnicos da HW. Cada
atendimento guarda o cliente, o equipamento, a data e hora de início e fim, e a
descrição do que foi feito.

O app roda instalado em cada máquina e acessa um MySQL num servidor da rede.
Tudo fica nesse banco, inclusive os arquivos anexados.

## O que o app faz

- **Login** com escolha do usuário e senha. Há dois tipos de usuário: **admin** e
  **comum**.
- **Cadastro de atendimento**: a busca pelo número de série preenche o cliente e
  o modelo e mostra o histórico do equipamento na lateral.
- **Detalhe do atendimento**: duplo clique num item do histórico abre uma janela
  com os dados. Atendimento em aberto pode ser **finalizado** ali (fim e
  descrição); atendimento finalizado abre só para leitura.
- **Anexos** (PDF, imagens) por atendimento, na mesma janela de detalhe.
- **Pesquisa** com filtros combinados (período, nome, série, descrição, telefone).
- **Só para admin**: cadastro de usuários, **log de auditoria** e **importação de
  clientes e equipamentos** por planilha.

## Tecnologias

- Java 17 com JavaFX 17 (interface em arquivos `.fxml`)
- Maven para as dependências
- MySQL via JDBC (`mysql-connector-j`)

## Estrutura do projeto

```
src/main/java/br/com/hw/hwatendimento/
├── HWApplication.java      # inicia a aplicação (abre a tela de login)
├── Launcher.java           # ponto de entrada
├── model/                  # Cliente, Equipamento, Atendimento, Usuario,
│                           # Auditoria, Anexo, LinhaImportacao
├── repositories/           # acesso ao banco (Conexao + um repositório por tabela)
├── util/                   # Mascaras, ValidadorSerie, Navegacao, Sessao
└── controller/             # lógica das telas

src/main/resources/br/com/hw/hwatendimento/
├── login-view.fxml                 # login
├── atendimento-view.fxml           # cadastro de atendimento
├── detalhe-atendimento-view.fxml   # janela de detalhe, finalização e anexos
├── pesquisa-view.fxml              # pesquisa
├── usuario-view.fxml               # cadastro de usuários (admin)
├── auditoria-view.fxml             # log de auditoria (admin)
├── importacao-view.fxml            # importação de planilha (admin)
├── style.css
└── assets/                         # ícones e a fonte Comfortaa

sql/hwatendimento.sql       # script que cria o banco e as tabelas
```

Regras de organização:

- Nenhuma linha de SQL fora de `repositories`. Os controllers chamam os
  repositórios, nunca o banco direto.
- Toda operação de banco roda numa `Task`, para a tela não travar.
- O usuário logado fica guardado em `util/Sessao`. As telas consultam
  `Sessao.isAdmin()` para mostrar ou esconder o que é só de admin.

## Configuração (`config.properties`)

O endereço, o usuário e a senha do banco ficam num arquivo `config.properties`,
fora do código:

```properties
db.url=jdbc:mysql://IP_DO_SERVIDOR:3306/hwatendimento
db.usuario=hwapp
db.senha=senha-aqui
```

O app procura esse arquivo em dois lugares, nesta ordem: ao lado do executável
(é o caso do app instalado) e, se não encontrar, na pasta atual (é o caso de
rodar pelo IntelliJ, com o arquivo na raiz do projeto). O arquivo é lido **uma
vez, quando o app abre**: depois de editar, feche e abra o app de novo.

Para trocar de servidor ou de senha, basta editar esse arquivo. Não é preciso
recompilar nem reinstalar.

O arquivo não vai para o repositório, porque contém a senha. O
`config.properties.exemplo` serve de modelo.

## Banco de dados

Seis tabelas:

| Tabela | Para que serve |
|---|---|
| `cliente` | Pessoa física (F) ou jurídica (J) |
| `equipamento` | Ligado a um cliente |
| `atendimento` | Ligado ao equipamento e ao cliente |
| `usuario` | Quem entra no app, se é admin e se está ativo |
| `auditoria` | Log: logins, criações e finalizações |
| `anexo` | Arquivos de cada atendimento, guardados no próprio banco |

```
mysql -u root -p < sql/hwatendimento.sql
```

Detalhes que não são óbvios:

- `numero_serie` é único mas aceita nulo, porque alguns equipamentos chegam sem
  etiqueta. O app grava `null`, não texto vazio.
- `data_hora_fim` nulo significa atendimento **em aberto**.
- O telefone é gravado só com dígitos. A máscara existe só na tela.
- `usuario.ativo = false` tira o usuário do login sem apagá-lo, para não perder o
  histórico dele no log.
- `auditoria.atendimento_id` **não** tem chave estrangeira de propósito: o
  registro do log precisa continuar existindo mesmo se o atendimento um dia for
  apagado.
- `anexo.conteudo` (`longblob`) guarda o arquivo inteiro. A coluna `caminho` é
  de uma versão antiga, em que os arquivos ficavam numa pasta da rede, e fica
  vazia nos anexos novos.
- As senhas dos usuários ficam em texto puro. Foi uma decisão do projeto, porque o
  app só roda na rede interna.

### Primeiro admin

A tela de usuários só abre para admin, então o primeiro precisa ser criado direto
no banco:

```sql
insert into usuario (nome, senha, admin) values ('admin', 'SENHA-AQUI', true);
```

Não deixe a senha real no `hwatendimento.sql`: o repositório é público.

## Número de série

Formato `XXNNNNNNNNN`: duas letras de modelo, dois dígitos de versão, dois de
ano, dois de mês e três de produção.

| Prefixo | Versão | Modelo |
|---------|--------|--------|
| EC | 10 | ECGV6 |
| EC | 11 | ECGV11 |
| TE | 10 | Ergo13 |
| TP | 10 | ErgoCP |
| TC | 10 | ErgoMET13 |

A regra fica em `util/ValidadorSerie.java`. Para adicionar um modelo novo, é
preciso mexer nesse arquivo e na lista de opções do `AtendimentoController`.

## Usuários

| | Admin | Comum |
|---|---|---|
| Criar, pesquisar e finalizar atendimentos | ✔ | ✔ |
| Anexar arquivos | ✔ | ✔ |
| Cadastrar, ativar e desativar usuários | ✔ | |
| Ver o log de auditoria | ✔ | |
| Importar planilha | ✔ | |

O caminho das telas de admin é **Cadastro → Usuários**, e de lá **Log** ou
**Importar**. Um admin não consegue desativar o próprio usuário, para o sistema
nunca ficar sem nenhum admin.

## Finalizar atendimento e conflito entre usuários

Um atendimento pode ser aberto por uma pessoa e finalizado por outra. A
finalização só grava se o atendimento **ainda estiver em aberto**
(`... where id = ? and data_hora_fim is null`). Se duas pessoas abrirem o mesmo
atendimento e a segunda tentar finalizar depois da primeira, o app avisa que ele
já foi finalizado por outro usuário, em vez de sobrescrever o que a primeira
escreveu.

## Anexos

- Ficam na janela de detalhe do atendimento: **Adicionar** escolhe o arquivo, e
  **duplo clique** abre no programa padrão do Windows.
- O arquivo é gravado **dentro do banco**, na coluna `anexo.conteudo`. Por isso
  qualquer PC que abre o app consegue anexar e abrir, esteja ou não no domínio:
  não depende de pasta compartilhada nem de permissão do Windows.
- Para abrir, o app busca o arquivo no banco, salva numa pasta temporária do PC
  com o nome original e abre de lá.
- A lista de anexos carrega só os nomes. O arquivo só é buscado no duplo clique,
  para não trazer todos os arquivos pela rede ao abrir um atendimento.
- Limite de **20 MB** por arquivo.
- O MySQL precisa aceitar envios desse tamanho: o `max_allowed_packet` do
  servidor tem que ser de pelo menos 25 MB (veja "Preparar o servidor").

Por que no banco, e não numa pasta da rede: a primeira versão copiava os arquivos
para uma pasta compartilhada, mas o acesso dependia do usuário do Windows de cada
PC. PCs com usuário local, ou já conectados ao servidor com outro usuário, não
conseguiam anexar.

## Importação de planilha

Tela **Importar** (só admin). Cadastra clientes e equipamentos a partir de um
arquivo CSV. As instruções também aparecem na própria tela.

**Formato:**

- Colunas, nesta ordem: **Nº de série; Tipo (F ou J); Nome do contato; Nome da
  empresa (só se for J); Telefone**.
- A primeira linha é o cabeçalho e é ignorada.
- Uma linha por equipamento. Se o cliente tiver vários, os dados dele se repetem
  em cada linha, **com o mesmo telefone**.
- Telefone com DDD, um por célula, sem texto junto.
- No Excel: **Salvar como → CSV UTF-8**.

**Regras:**

- O modelo é descoberto pelo número de série.
- Série inválida, tipo diferente de F/J, nome em branco, J sem empresa, telefone
  sem DDD ou série repetida na planilha viram **erro**, com o número da linha.
  As outras linhas são importadas normalmente.
- Série que **já existe no banco** é pulada. Importar o mesmo arquivo duas vezes
  não duplica nada.
- O mesmo cliente é reconhecido pelo **telefone**, na planilha e no banco: os
  equipamentos dele ficam todos ligados a um cliente só.
- O app aceita `;` ou `,` como separador e lê arquivos em UTF-8 ou no formato
  antigo do Excel (Windows-1252).

## Log de auditoria

Registra quem fez o quê e quando:

| Ação | Quando |
|---|---|
| Login | Toda entrada no app |
| Criação | Novo atendimento e importação de planilha |
| Edição | Finalização de atendimento e anexo adicionado |

A tela mostra os 500 registros mais recentes. O log é gravado na mesma transação
da operação: se o log falhar, a operação também é desfeita.

## Como rodar em desenvolvimento

Precisa de JDK 17 ou superior e um MySQL rodando, com o `config.properties` na
raiz do projeto.

```
./mvnw javafx:run
```

No Windows, `mvnw.cmd javafx:run`.

## Preparar o servidor

**Banco.** Rode o script e crie o usuário do app:

```sql
CREATE USER 'hwapp'@'%' IDENTIFIED BY 'senha-aqui';
GRANT ALL PRIVILEGES ON hwatendimento.* TO 'hwapp'@'%';
FLUSH PRIVILEGES;
```

O `'%'` é essencial. Um usuário criado como `'hwapp'@'localhost'` funciona na
máquina do banco e falha em todas as outras. Também é preciso liberar a porta
3306 no firewall.

Depois, crie o primeiro admin (veja "Primeiro admin").

**Tamanho dos anexos.** Confira o limite de envio do MySQL:

```sql
show variables like 'max_allowed_packet';
```

O valor aparece em bytes e precisa ser de pelo menos `26214400` (25 MB). No
MySQL 8 o padrão é 64 MB, então normalmente já está certo.

**Rodar SQL no servidor.** Se o `mysql` não for reconhecido no Prompt, ele fica
em `C:\Program Files\MySQL\MySQL Server <versão>\bin`, ou use o atalho "MySQL
Command Line Client" do Iniciar. Se o terminal não deixar colar, salve o SQL num
arquivo e rode `source C:/caminho/arquivo.sql` dentro do `mysql`.

## Gerar o instalador

Precisa ser feito **numa máquina Windows**: o `jpackage` só gera instalador para
o sistema em que roda. Também é preciso ter o
[WiX Toolset v3](https://wixtoolset.org/releases) instalado.

**1.** Gere o jar com as dependências dentro:

```
mvnw.cmd clean package
```

Tem que aparecer `BUILD SUCCESS`.

**2.** Monte a pasta `dist` com dois arquivos:

```
dist/
├── HWAtendimento.jar       (copiado de target/, sempre o mais recente)
└── config.properties       (já apontando para o servidor)
```

Tudo que estiver em `dist` vai junto para dentro do app instalado.

**3.** Apague ou renomeie a pasta `instalador` antiga e rode o `jpackage`,
**aumentando o `--app-version`** a cada versão:

```
jpackage --type exe --input dist --dest instalador --name "HW Atendimento" --main-jar HWAtendimento.jar --main-class br.com.hw.hwatendimento.Launcher --icon src\main\resources\br\com\hw\hwatendimento\assets\icone.ico --app-version 1.2.8 --vendor "Thales Alves" --win-shortcut --win-menu --win-dir-chooser
```

No PowerShell, coloque `&` na frente do comando. O instalador sai na pasta
`instalador`. Se o caminho do projeto tiver acento (por exemplo `Usuário`) e o
`jpackage` der erro, copie o projeto para uma pasta sem acento.

O `jpackage` embute o Java no instalador, então as máquinas não precisam ter
Java instalado.

### Instalar nos PCs

- **Desinstale a versão anterior antes** (Configurações → Aplicativos). O
  instalador não atualiza por cima.
- Se aparecer o **erro 2502 ou 2503**, é permissão do instalador do Windows. Abra
  o Prompt **como administrador**, vá até a pasta do `.exe` com `cd` e rode o
  instalador por ali, entre aspas: `"HW Atendimento-1.2.8.exe"`.
- Cada pessoa precisa de um usuário cadastrado no app (tela de Usuários).