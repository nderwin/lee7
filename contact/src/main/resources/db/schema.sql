
    alter table contact.user_role 
        drop constraint FK_it77eq964jhfqtu54081ebtio;

    alter table contact.user_role 
        drop constraint FK_apcc8lxk2xnug8377fatvbn04;

    drop table if exists contact.organization cascade;

    drop table if exists contact.person cascade;

    drop table if exists contact.role cascade;

    drop table if exists contact.user cascade;

    drop table if exists contact.user_role cascade;

    drop sequence legal_entity_seq;

    drop sequence role_seq;

    drop sequence user_seq;

    create table contact.organization (
        id int8 not null,
        name varchar(50) not null,
        primary key (id)
    );

    create table contact.person (
        id int8 not null,
        givenname varchar(50) not null,
        surname varchar(50),
        primary key (id)
    );

    create table contact.role (
        id int8 not null,
        name varchar(255) not null,
        primary key (id)
    );

    create table contact.user (
        id int8 not null,
        password varchar(255) not null,
        username varchar(255) not null,
        primary key (id)
    );

    create table contact.user_role (
        user_id int8 not null,
        role_id int8 not null,
        primary key (user_id, role_id)
    );

    alter table contact.role 
        add constraint UK_8sewwnpamngi6b1dwaa88askk  unique (name);

    alter table contact.user 
        add constraint UK_sb8bbouer5wak8vyiiy4pf2bx  unique (username);

    alter table contact.user_role 
        add constraint FK_it77eq964jhfqtu54081ebtio 
        foreign key (role_id) 
        references contact.role;

    alter table contact.user_role 
        add constraint FK_apcc8lxk2xnug8377fatvbn04 
        foreign key (user_id) 
        references contact.user;

    create sequence legal_entity_seq;

    create sequence role_seq;

    create sequence user_seq;
