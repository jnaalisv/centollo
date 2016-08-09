delete from order_item;
delete from purchase_order;

insert
into purchase_order(id, lastModified)
values(1, CURRENT_TIMESTAMP);


