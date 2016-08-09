delete from order_item;
delete from purchase_order;

insert
into purchase_order(id, lastModified)
values(1, CURRENT_TIMESTAMP);

insert
into order_item(order_id, productCode, itemCount)
values
  (1, 'J1', 3),
  (1, 'K2', 2),
  (1, 'H3', 1);

