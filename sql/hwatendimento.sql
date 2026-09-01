create database if not exists hwatendimento;
use hwatendimento;

create table if not exists cliente(
  id int auto_increment primary key,
  tipo char(1) not null,
  nome varchar(120) not null,
  nome_empresa varchar(120) null,
  telefone varchar(20) null,
  criado_em datetime not null default current_timestamp,
  constraint ck_cliente_tipo check (tipo in ('F','J')),
  index ix_cliente_nome (nome),
  index ix_cliente_telefone (telefone)
);

create table if not exists equipamento(
  id int auto_increment primary key,
  modelo varchar(20) not null,
  numero_serie varchar(30) null,
  cliente_id int null,
  criado_em datetime not null default current_timestamp,
  unique key uk_equipamento_serie (numero_serie),
  index ix_equipamento_modelo (modelo),
  constraint fk_equipamento_cliente
    foreign key (cliente_id) references cliente(id)
);

create table if not exists atendimento(
  id int auto_increment primary key,
  equipamento_id int not null,
  cliente_id int not null,
  data_hora_inicio datetime not null,
  data_hora_fim datetime null,
  descricao text not null,
  criado_em datetime not null default current_timestamp,
  index ix_atendimento_inicio (data_hora_inicio),
  index ix_atendimento_equipamento (equipamento_id, data_hora_inicio),
  constraint fk_atendimento_equipamento
    foreign key (equipamento_id) references equipamento(id),
  constraint fk_atendimento_cliente
    foreign key (cliente_id) references cliente(id)
);
