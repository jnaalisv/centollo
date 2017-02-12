drop table order_item if exists;
drop table Product if exists;
drop table purchase_order if exists;

-- hibernate specific stuff
drop sequence if exists hibernate_sequence;
create sequence hibernate_sequence start with 1 increment by 1;

CREATE TABLE purchase_order (
    id INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    version BIGINT not null DEFAULT 0,
    lastModified TIMESTAMP,
    primary key (id)
);

create table Product (
    id INTEGER NOT NULL,
    version BIGINT not null DEFAULT 0,
    lastModified TIMESTAMP,
    name varchar(255),
    productType varchar(255),
    primary key (id)
);

CREATE TABLE order_item (
    id INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    order_id INTEGER NOT NULL,
    product_id VARCHAR(255),
    itemCount INTEGER NOT NULL,
    CONSTRAINT item_order_fk FOREIGN KEY (order_id) REFERENCES purchase_order (id),
    CONSTRAINT item_product_fk FOREIGN KEY (product_id) REFERENCES product (id)
);
