insert into customers (email, pwd) VALUES
('account@debuggeandoieas.com', '$2a$10$NqT7XpTGPlsYWyNXfsyYsePAXZ5YgXWUYFWKi6wziuiI4HmFrmKla'),
('cards@debuggeandoieas.com', '$2a$10$NqT7XpTGPlsYWyNXfsyYsePAXZ5YgXWUYFWKi6wziuiI4HmFrmKla'),
('loans@debuggeandoieas.com', '$2a$10$NqT7XpTGPlsYWyNXfsyYsePAXZ5YgXWUYFWKi6wziuiI4HmFrmKla'),
('balance@debuggeandoieas.com', '$2a$10$NqT7XpTGPlsYWyNXfsyYsePAXZ5YgXWUYFWKi6wziuiI4HmFrmKla');

insert into roles (role_name, description, id_customer)
values ('ROLE_ADMIN','can view account endpoint', 1),
       ('ROLE_ADMIN','can view cards endpoint', 2),
       ('ROLE_USER','can view loans endpoint', 3),
       ('ROLE_USER','can view balance endpoint', 4);

insert into partners(
    client_id, client_name, client_secret, scopes,
    grant_types, authentication_methods, redirect_uri, redirect_uri_logout
)
values ('debuggeandoideas',
        'debuggeando ideas',
        '$2a$10$ePAcFcYoJTgrtOsskdapquryO2mdgjZ34E7vM39vigP.FjF9JVUX2',
        'read,write',
        'authorization_code,refresh_token',
        'client_secret_basic,client_secret_jwt',
        'https://oauthdebugger.com/debug',
        'https://springone.io/authorized'),
       ('romLab',
        'romLab incorporation',
        '$2a$10$ePAcFcYoJTgrtOsskdapquryO2mdgjZ34E7vM39vigP.FjF9JVUX2',
        'read,write',
        'authorization_code,refresh_token',
        'client_secret_basic,client_secret_jwt',
        'https://oauthdebugger.com/debug',
        'https://springone.io/authorized')
