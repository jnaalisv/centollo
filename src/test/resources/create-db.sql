drop table Product if exists;

create table Product (
    id bigint not null,
    productCode varchar(255),
    name varchar(255),
    primary key (id)
);