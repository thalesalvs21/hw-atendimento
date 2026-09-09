# HW Atendimento

Aplicativo de desktop para registrar os atendimentos técnicos da HW. Cada atendimento guarda o cliente, o equipamento, a data e hora de início e fim, e a descrição do que foi feito. Também tem uma tela de pesquisa para consultar os atendimentos já registrados.

O projeto está em desenvolvimento.

## Tecnologias

- **Java 17** com **JavaFX 17** (interface montada em arquivos `.fxml`)
- **Maven** para gerenciar as dependências
- **MySQL** como banco de dados, acessado via JDBC

## Estrutura do projeto

```
src/main/java/br/com/hw/hwatendimento/
├── HWApplication.java      # inicia a aplicação JavaFX
├── Launcher.java           # ponto de entrada
├── model/                  # Cliente, Equipamento, Atendimento
├── repositories/           # acesso ao banco (Conexao + repositórios)
└── controller/             # lógica das telas

src/main/resources/br/com/hw/hwatendimento/
├── atendimento-view.fxml   # tela de registro
├── pesquisa-view.fxml      # tela de pesquisa
├── style.css
└── assets/                 # ícones do app

sql/hwatendimento.sql       # script que cria o banco e as tabelas
```

## Banco de dados

O script `sql/hwatendimento.sql` cria o banco `hwatendimento` com três tabelas:

- **cliente** — pessoa física ou jurídica (campo `tipo` com `F` ou `J`), nome, nome da empresa e telefone
- **equipamento** — modelo e número de série, ligado a um cliente
- **atendimento** — ligado a um equipamento e a um cliente, com data/hora de início e fim e descrição

Rode o script no MySQL antes de abrir o app:

```bash
mysql -u root -p < sql/hwatendimento.sql
```

## Como rodar

Pré-requisitos: JDK 17 ou superior e um servidor MySQL rodando.

```bash
./mvnw javafx:run
```

No Windows, use `mvnw.cmd javafx:run`.
