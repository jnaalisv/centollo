
delete from ORDER_ITEM;
delete from product;
delete from purchase_order;

insert
into product(id, productCode, name, productType)
values(1, 'J1', 'Java', 'BEANS')
  , (2, 'K2', 'Kona', 'BEANS')
  , (3, 'H3', 'Harar', 'BEANS')
  , (4, 'S4', 'Sidamo', 'BEANS')
  , (5, 'Y5', 'Yirgacheffe', 'BEANS')
  , (6, 'G6', 'Gesha', 'BEANS')
  , (7, 'C7', 'Colombian', 'BEANS')
  , (8, 'CE8', 'Compak E8', 'GRINDERS');