
    create sequence reservations_SEQ start with 1 increment by 50;

    create sequence tenant_services_SEQ start with 1 increment by 50;

    create sequence tenant_users_SEQ start with 1 increment by 50;

    create sequence tenants_SEQ start with 1 increment by 50;

    create table reservations (
        reserved_date date,
        start_time time(0),
        id uuid not null,
        tenant_service_id uuid,
        customer_name varchar(255),
        customer_phone varchar(255),
        description TEXT,
        status varchar(255),
        primary key (id)
    );

    create table tenant_services (
        is_active boolean,
        price numeric(38,2),
        id uuid not null,
        tenant_id uuid,
        description TEXT,
        name varchar(255),
        primary key (id)
    );

    create table tenant_users (
        id uuid not null,
        tenant_id uuid,
        password varchar(255),
        role varchar(255),
        username varchar(255),
        primary key (id)
    );

    create table tenants (
        capacity_per_slot integer,
        close_time time(0),
        open_time time(0),
        id uuid not null,
        address varchar(255),
        description TEXT,
        email varchar(255),
        name varchar(255),
        phone_number varchar(255),
        regularly_closed varchar(255),
        primary key (id)
    );

    alter table if exists reservations 
       add constraint FK1rid7h549gic2wnjjhm8vsttb 
       foreign key (tenant_service_id) 
       references tenant_services;

    alter table if exists tenant_services 
       add constraint FKnptg8fw1t601gflyqx489v3kf 
       foreign key (tenant_id) 
       references tenants;

    alter table if exists tenant_users 
       add constraint FK7tf2f7scsqdlja6wbo80h95wi 
       foreign key (tenant_id) 
       references tenants;
