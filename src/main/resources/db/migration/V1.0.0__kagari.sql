
    create table reservations (
        id uuid not null,
        service_id uuid,
        start_time time(0),
        reserved_date date,
        customer_name varchar(255),
        customer_phone varchar(255),
        description TEXT,
        status varchar(255),
        primary key (id)
    );

    create table services (
        id uuid not null,
        name varchar(255),
        price numeric(38,2),
        description TEXT,
        primary key (id)
    );

    create table users (
        id uuid not null,
        username varchar(255),
        password varchar(255),
        role varchar(255),
        primary key (id)
    );

    alter table if exists reservations 
       add constraint FK1rid7h549gic2wnjjhm8vsttb 
       foreign key (service_id) 
       references services;
