# HW Atendimento

Aplicativo de desktop para registrar os atendimentos técnicos da HW. Cada
atendimento guarda o cliente, o equipamento, a data e hora de início e fim, e a
descrição do que foi feito. Também tem uma tela de pesquisa para consultar os
atendimentos já registrados.

O app roda instalado em cada máquina e acessa um MySQL num servidor da rede.

## Tecnologias

- Java 17 com JavaFX 17 (interface em arquivos `.fxml`)
- Maven para as dependências
- MySQL via JDBC (`mysql-connector-j`)

## Estrutura do projeto

```
src/main/java/br/com/hw/hwatendimento/
├── HWApplication.java      # inicia a aplicação JavaFX
├── Launcher.java           # ponto de entrada
├── model/                  # Cliente, Equipamento, Atendimento
├── repositories/           # acesso ao banco (Conexao + repositórios)
├── util/                   # Mascaras, ValidadorSerie, Navegacao
└── controller/             # lógica das telas

src/main/resources/br/com/hw/hwatendimento/
├── atendimento-view.fxml   # tela de registro
├── pesquisa-view.fxml      # tela de pesquisa
├── style.css
└── assets/                 # ícones e a fonte Comfortaa

sql/hwatendimento.sql       # script que cria o banco e as tabelas
```

Regra de organização: nenhuma linha de SQL fora de `repositories`. Os
controllers chamam os repositórios, nunca o banco direto.

## Banco de dados

Três tabelas: **cliente** (física ou jurídica), **equipamento** (ligado a um
cliente) e **atendimento** (ligado aos dois).

```
mysql -u root -p < sql/hwatendimento.sql
```

Detalhes que não são óbvios:

- `numero_serie` é único mas aceita nulo, porque alguns equipamentos chegam sem
  etiqueta. O app grava `null`, não texto vazio.
- `data_hora_fim` nulo significa atendimento em aberto.
- O telefone é gravado só com dígitos. A máscara existe só na tela.

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

## Como rodar em desenvolvimento

Precisa de JDK 17 ou superior e um MySQL rodando.

```
./mvnw javafx:run
```

No Windows, `mvnw.cmd javafx:run`.

## Instalação nas máquinas

No servidor, rode o script do banco e crie o usuário do app:

```sql
CREATE USER 'hwapp'@'%' IDENTIFIED BY 'senha-aqui';
GRANT ALL PRIVILEGES ON hwatendimento.* TO 'hwapp'@'%';
FLUSH PRIVILEGES;
```

O `'%'` é essencial. Um usuário criado como `'hwapp'@'localhost'` funciona na
máquina do banco e falha em todas as outras. Também é preciso liberar a porta
3306 no firewall.

Nas máquinas, o instalador é gerado com `jpackage` e leva o Java junto. Ele só
gera instalador para o sistema em que roda, então o `.exe` precisa ser gerado
no Windows.

## Melhorias futuras

- Tirar o endereço e a senha do banco de dentro do `Conexao.java` para um
  arquivo de configuração
- Configurar o banco no servidor e gerar o instalador
- Backup agendado (`mysqldump` no Agendador de Tarefas, gravando em outra
  máquina)
- Campos de contato por atendimento, para quando o cliente é uma empresa e cada
  chamado é aberto por uma pessoa diferente
- Validar horas impossíveis como `99:99`
- Botão "ver todos" no histórico lateral
