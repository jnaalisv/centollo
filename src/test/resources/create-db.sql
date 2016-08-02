drop table Product if exists;

create table Product (
    id bigint not null,
    name varchar(255),
    primary key (id)
);