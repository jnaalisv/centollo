delete from product;

insert
into product(id, name, productType)
values(NEXTVAL('hibernate_sequence'), 'Java', 'BEANS')
  , (NEXTVAL('hibernate_sequence'), 'Kona', 'BEANS')
  , (NEXTVAL('hibernate_sequence'), 'Harar', 'BEANS')
  , (NEXTVAL('hibernate_sequence'), 'Sidamo', 'BEANS')
  , (NEXTVAL('hibernate_sequence'), 'Yirgacheffe', 'BEANS')
  , (NEXTVAL('hibernate_sequence'), 'Gesha', 'BEANS')
  , (NEXTVAL('hibernate_sequence'), 'Colombian', 'BEANS')
  , (NEXTVAL('hibernate_sequence'), 'Compak E8', 'GRINDERS');
