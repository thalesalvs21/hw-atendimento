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

## Configuração da conexão

O endereço, o usuário e a senha do banco ficam num arquivo `config.properties`,
fora do código:

```properties
db.url=jdbc:mysql://IP_DO_SERVIDOR:3306/hwatendimento
db.usuario=hwapp
db.senha=senha-aqui
```

O app procura esse arquivo em dois lugares, nesta ordem: ao lado do executável
(é o caso do app instalado) e, se não encontrar, na pasta atual (é o caso de
rodar pelo IntelliJ, com o arquivo na raiz do projeto).

Para trocar de servidor ou de senha, basta editar esse arquivo. Não é preciso
recompilar nem reinstalar.

O arquivo não vai para o repositório, porque contém a senha. O
`config.properties.exemplo` serve de modelo.

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

Precisa de JDK 17 ou superior e um MySQL rodando, com o `config.properties` na
raiz do projeto.

```
./mvnw javafx:run
```

No Windows, `mvnw.cmd javafx:run`.

## Preparar o servidor

Rode o script do banco e crie o usuário do app:

```sql
CREATE USER 'hwapp'@'%' IDENTIFIED BY 'senha-aqui';
GRANT ALL PRIVILEGES ON hwatendimento.* TO 'hwapp'@'%';
FLUSH PRIVILEGES;
```

O `'%'` é essencial. Um usuário criado como `'hwapp'@'localhost'` funciona na
máquina do banco e falha em todas as outras. Também é preciso liberar a porta
3306 no firewall.

## Gerar o instalador

Precisa ser feito **numa máquina Windows**: o `jpackage` só gera instalador para
o sistema em que roda. Também é preciso ter o
[WiX Toolset v3](https://wixtoolset.org/releases) instalado.

**1.** Gere o jar com as dependências dentro:

```
mvnw.cmd clean package
```

**2.** Monte a pasta `dist` com dois arquivos:

```
dist/
├── HWAtendimento.jar       (copiado de target/)
└── config.properties       (já apontando para o servidor)
```

Tudo que estiver em `dist` vai junto para dentro do app instalado.

**3.** Rode o `jpackage`:

```
jpackage --type exe --input dist --dest instalador --name "HW Atendimento" --main-jar HWAtendimento.jar --main-class br.com.hw.hwatendimento.Launcher --icon icone.ico --app-version 1.0 --vendor "HW" --win-shortcut --win-menu --win-dir-chooser
```

No PowerShell, coloque `&` na frente do comando. O instalador sai na pasta
`instalador`.

O `jpackage` embute o Java no instalador, então as máquinas não precisam ter
Java instalado.

## Melhorias futuras

- Validar horas impossíveis como `99:99`
- Avisar quando o término for preenchido pela metade (só a data ou só a hora)
- Botão "ver todos" no histórico lateral
