insert into customers (email, pwd) VALUES
('account@debuggeandoieas.com', 'to_be_encoded'),
('cards@debuggeandoieas.com', 'to_be_encoded'),
('loans@debuggeandoieas.com', 'to_be_encoded'),
('balance@debuggeandoieas.com', 'to_be_encoded');

insert into roles (role_name, description, id_customer)
values ('VIEW_ACCOUNT','can view account endpoint', 1),
       ('VIEW_CARDS','can view cards endpoint', 2),
       ('VIEW_LOANS','can view loans endpoint', 3),
       ('VIEW_BALANCE','can view balance endpoint', 4);
