create table customers(
    id bigserial primary key,
    email varchar(50) not null,
    pwd varchar(500) not null
);

create table roles(
    role_name varchar(50) primary key,
    description varchar(100),
    id_customer bigint,
    constraint fk_customer foreign key(id_customer) references customers(id)
);
