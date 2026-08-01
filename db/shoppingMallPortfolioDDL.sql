-- =========================================================
-- DDL.sql
-- Portfolio backend schema v1.1
-- Target DB: PostgreSQL 17
--
-- Usage:
--   Place this file at: src/main/resources/DDL.sql
--   Spring Boot application.yml example:
--     spring.sql.init.mode: always
--     spring.sql.init.schema-locations: classpath:DDL.sql
--     spring.jpa.hibernate.ddl-auto: validate
--
-- Note:
--   This file creates tables/indexes only.
--   Database/user creation should be done separately:
--     CREATE DATABASE personal_db;
--     CREATE USER personal_user WITH PASSWORD 'personal_password';
--     GRANT ALL PRIVILEGES ON DATABASE personal_db TO personal_user;
-- =========================================================

-- =========================================================
-- 1) user domain
-- =========================================================

create table if not exists user_info (
    uuid uuid primary key,
    nickname varchar(50) not null unique,
    username varchar(50) not null,
    birthday date not null,
    email varchar(254),
    phone varchar(20) not null,
    default_address_id bigint
);

create table if not exists user_address_saved (
    address_id bigint generated always as identity primary key,
    uuid uuid not null,
    address varchar(255) not null,
    constraint fk_user_address_saved_user
        foreign key (uuid) references user_info(uuid) on delete cascade,
    constraint uq_user_address_saved_address_id_uuid
        unique (address_id, uuid)
);

do $$
begin
    if not exists (
        select 1
        from pg_constraint
        where conname = 'fk_user_info_default_address'
    ) then
        alter table user_info
            add constraint fk_user_info_default_address
            foreign key (default_address_id, uuid)
            references user_address_saved(address_id, uuid);
    end if;
end $$;

create table if not exists user_logins (
    id varchar(50) primary key,
    password_crypted text not null,
    uuid uuid not null unique,
    constraint fk_user_logins_user
        foreign key (uuid) references user_info(uuid) on delete cascade
);

create table if not exists user_payment_saved (
    payment_id bigint generated always as identity primary key,
    uuid uuid not null,
    payment_encrypted_data jsonb not null,
    constraint fk_user_payment_saved_user
        foreign key (uuid) references user_info(uuid) on delete cascade
);

create index if not exists idx_user_address_saved_uuid on user_address_saved(uuid);
create index if not exists idx_user_payment_saved_uuid on user_payment_saved(uuid);


-- =========================================================
-- 2) seller domain
-- =========================================================

create table if not exists seller_info (
    store_uuid uuid primary key,
    store_name varchar(100) not null unique,
    generated_date date not null,
    store_tin varchar(20) unique,
    store_crn varchar(20) unique,
    store_brn varchar(20) unique
);

create table if not exists seller_logins (
    id varchar(50) primary key,
    password_crypted text not null,
    store_uuid uuid not null unique,
    constraint fk_seller_logins_seller
        foreign key (store_uuid) references seller_info(store_uuid) on delete cascade
);


-- =========================================================
-- 3) item / coupon domain
-- =========================================================

create table if not exists items (
    item_code bigint generated always as identity primary key,
    store_uuid uuid not null,
    item_name varchar(150) not null,
    price decimal(12,2) not null check (price >= 0),
    upload_date timestamptz not null,
    delivery_type varchar(30) not null,
    main_images jsonb not null default '[]'::jsonb,
    describe_file jsonb,
    available integer not null default 0 check (available >= 0),
    constraint fk_items_seller
        foreign key (store_uuid) references seller_info(store_uuid) on delete cascade
);

create table if not exists coupons (
    coupon_code varchar(20) primary key,
    store_uuid uuid not null,
    item_code bigint,
    discount_type varchar(10) not null
        check (discount_type in ('PERCENT', 'PRICE')),
    discount_amount decimal(12,2) not null check (discount_amount >= 0),
    discount_limit decimal(12,2) check (discount_limit >= 0),
    due_date timestamptz not null,
    constraint fk_coupons_seller
        foreign key (store_uuid) references seller_info(store_uuid) on delete cascade,
    constraint fk_coupons_item
        foreign key (item_code) references items(item_code) on delete set null
);

create index if not exists idx_items_store_uuid on items(store_uuid);
create index if not exists idx_items_item_name on items(item_name);
create index if not exists idx_coupons_store_uuid on coupons(store_uuid);
create index if not exists idx_coupons_item_code on coupons(item_code);


-- =========================================================
-- 4) basket domain
-- =========================================================

create table if not exists user_basket (
    basket_id bigint generated always as identity primary key,
    user_uuid uuid not null unique,
    constraint fk_user_basket_user
        foreign key (user_uuid) references user_info(uuid) on delete cascade
);

create table if not exists basket_items (
    basket_item_id bigint generated always as identity primary key,
    basket_id bigint not null,
    item_id bigint not null,
    quantity integer not null check (quantity > 0),
    constraint fk_basket_items_basket
        foreign key (basket_id) references user_basket(basket_id) on delete cascade,
    constraint fk_basket_items_item
        foreign key (item_id) references items(item_code),
    constraint uq_basket_items_basket_item
        unique (basket_id, item_id)
);

create index if not exists idx_basket_items_basket_id on basket_items(basket_id);
create index if not exists idx_basket_items_item_id on basket_items(item_id);


-- =========================================================
-- 5) checkout domain
-- checkout = user가 한 번 결제한 묶음
-- =========================================================

create table if not exists checkouts (
    checkout_id bigint generated always as identity primary key,
    user_uuid uuid not null,
    total_amount decimal(12,2) not null check (total_amount >= 0),
    checkout_status varchar(30) not null check (
        checkout_status in (
            'CREATED',
            'PAID',
            'FAILED',
            'CANCELLED',
            'PARTIALLY_REFUNDED',
            'REFUNDED'
        )
    ),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint fk_checkouts_user
        foreign key (user_uuid) references user_info(uuid),
    constraint uq_checkouts_checkout_id_user_uuid
        unique (checkout_id, user_uuid)
);

create index if not exists idx_checkouts_user_uuid on checkouts(user_uuid);
create index if not exists idx_checkouts_status on checkouts(checkout_status);
create index if not exists idx_checkouts_created_at on checkouts(created_at);


-- =========================================================
-- 6) order domain
-- orders = store별 실제 주문
-- v1.1: checkout 1개 아래에 store별 orders 여러 개 생성 가능
-- =========================================================

create table if not exists orders (
    order_id bigint generated always as identity primary key,
    checkout_id bigint not null,
    user_uuid uuid not null,
    store_uuid uuid not null,
    status varchar(30) not null check (
        status in (
            'PAID',
            'SELLER_CONFIRMED',
            'PREPARING',
            'SHIPPED',
            'IN_TRANSIT',
            'DELIVERED',
            'PURCHASE_CONFIRMED',
            'CANCEL_REQUESTED',
            'CANCELLED',
            'RETURN_REQUESTED',
            'RETURN_APPROVED',
            'RETURN_PICKED_UP'
        )
    ),
    ordered_at timestamptz not null,
    courier_code varchar(20),
    tracking_number varchar(50),
    delivered_date timestamptz,
    delivered_marked_by varchar(20) default 'COURIER' check (
        delivered_marked_by in ('USER', 'SELLER', 'SYSTEM', 'COURIER')
    ),
    confirm_deadline_date timestamptz,
    confirm_extended boolean not null default false,
    confirm_date timestamptz,
    confirmed_by varchar(10) check (
        confirmed_by in ('USER', 'SELLER', 'SYSTEM')
    ),
    version bigint not null default 0,
    constraint fk_orders_checkout
        foreign key (checkout_id) references checkouts(checkout_id) on delete cascade,
    constraint fk_orders_user
        foreign key (user_uuid) references user_info(uuid),
    constraint fk_orders_seller
        foreign key (store_uuid) references seller_info(store_uuid),
    constraint fk_orders_checkout_user
        foreign key (checkout_id, user_uuid)
        references checkouts(checkout_id, user_uuid)
);

create table if not exists order_items (
    order_item_id bigint generated always as identity primary key,
    order_id bigint not null,
    item_code bigint not null,
    quantity integer not null check (quantity > 0),
    unit_price_at_purchase decimal(12,2) not null check (unit_price_at_purchase >= 0),
    constraint fk_order_items_order
        foreign key (order_id) references orders(order_id) on delete cascade,
    constraint fk_order_items_item
        foreign key (item_code) references items(item_code)
);

create index if not exists idx_orders_checkout_id on orders(checkout_id);
create index if not exists idx_orders_user_uuid on orders(user_uuid);
create index if not exists idx_orders_store_uuid on orders(store_uuid);
create index if not exists idx_orders_status on orders(status);
create index if not exists idx_orders_confirm_deadline_date on orders(confirm_deadline_date);
create index if not exists idx_order_items_order_id on order_items(order_id);
create index if not exists idx_order_items_item_code on order_items(item_code);


-- =========================================================
-- 7) order coupon snapshot
-- v1.1: 쿠폰은 store별 order 단위로 적용
-- =========================================================

create table if not exists order_coupons (
    order_id bigint primary key,
    coupon_code varchar(20) not null,
    discount_type_snapshot varchar(10) not null
        check (discount_type_snapshot in ('PERCENT', 'PRICE')),
    discount_amount_snapshot decimal(12,2) not null check (discount_amount_snapshot >= 0),
    discount_limit_snapshot decimal(12,2) check (discount_limit_snapshot >= 0),
    applied_discount_amount decimal(12,2) not null check (applied_discount_amount >= 0),
    constraint fk_order_coupons_order
        foreign key (order_id) references orders(order_id) on delete cascade,
    constraint fk_order_coupons_coupon
        foreign key (coupon_code) references coupons(coupon_code)
);


-- =========================================================
-- 8) payment / refund domain
-- payments = checkout에 대한 결제 시도/성공 기록
-- refunds = 특정 payment 중 특정 order에 대한 환불 기록
-- =========================================================

create table if not exists payments (
    payment_id bigint generated always as identity primary key,
    checkout_id bigint not null,
    payment_provider varchar(30) not null,
    payment_method varchar(20) not null,
    installment_months smallint not null default 0 check (installment_months >= 0),
    amount_authorized decimal(12,2) not null check (amount_authorized >= 0),
    amount_captured decimal(12,2) not null default 0 check (amount_captured >= 0),
    payment_status varchar(30) not null check (
        payment_status in (
            'AUTHORIZED',
            'CAPTURED',
            'VOIDED',
            'REFUNDED',
            'PARTIALLY_REFUNDED',
            'FAILED'
        )
    ),
    provider_tx_id varchar(100),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint fk_payments_checkout
        foreign key (checkout_id) references checkouts(checkout_id) on delete cascade,
    constraint uq_payments_provider_tx_id
        unique (provider_tx_id)
);

create table if not exists refunds (
    refund_id bigint generated always as identity primary key,
    payment_id bigint not null,
    order_id bigint not null,
    refund_type varchar(30) not null check (
        refund_type in ('FULL_REFUND', 'PARTIAL_REFUND', 'DELIVERY_FEE_ONLY')
    ),
    refund_amount decimal(12,2) not null check (refund_amount >= 0),
    deduction_amount decimal(12,2) not null default 0 check (deduction_amount >= 0),
    deduction_reason varchar(50),
    refund_status varchar(20) not null check (
        refund_status in ('REQUESTED', 'APPROVED', 'DONE', 'FAILED')
    ),
    requested_by_type varchar(20) not null check (
        requested_by_type in ('USER', 'SELLER', 'SYSTEM')
    ),
    requested_by_uuid uuid,
    reason varchar(200),
    created_at timestamptz not null,
    done_at timestamptz,
    provider_refund_id varchar(100),
    constraint fk_refunds_payment
        foreign key (payment_id) references payments(payment_id) on delete cascade,
    constraint fk_refunds_order
        foreign key (order_id) references orders(order_id) on delete cascade,
    constraint uq_refunds_provider_refund_id
        unique (provider_refund_id)
);

create index if not exists idx_payments_checkout_id on payments(checkout_id);
create index if not exists idx_payments_status on payments(payment_status);
create index if not exists idx_refunds_payment_id on refunds(payment_id);
create index if not exists idx_refunds_order_id on refunds(order_id);
create index if not exists idx_refunds_status on refunds(refund_status);


-- =========================================================
-- 9) order event log
-- =========================================================

create table if not exists order_events (
    event_id bigint generated always as identity primary key,
    order_id bigint not null,
    event_type varchar(30) not null check (
        event_type in (
            'STATUS_CHANGED',
            'TRACKING_ADDED',
            'TRACKING_UPDATED',
            'CONFIRM_EXTENDED',
            'DELIVERED_MARKED',
            'PURCHASE_CONFIRMED',
            'CANCEL_REQUESTED',
            'REFUND_REQUESTED'
        )
    ),
    from_status varchar(30) check (
        from_status in (
            'PAID',
            'SELLER_CONFIRMED',
            'PREPARING',
            'SHIPPED',
            'IN_TRANSIT',
            'DELIVERED',
            'PURCHASE_CONFIRMED',
            'CANCEL_REQUESTED',
            'CANCELLED',
            'RETURN_REQUESTED',
            'RETURN_APPROVED',
            'RETURN_PICKED_UP'
        )
    ),
    to_status varchar(30) check (
        to_status in (
            'PAID',
            'SELLER_CONFIRMED',
            'PREPARING',
            'SHIPPED',
            'IN_TRANSIT',
            'DELIVERED',
            'PURCHASE_CONFIRMED',
            'CANCEL_REQUESTED',
            'CANCELLED',
            'RETURN_REQUESTED',
            'RETURN_APPROVED',
            'RETURN_PICKED_UP'
        )
    ),
    actor_type varchar(20) not null check (
        actor_type in ('USER', 'SELLER', 'SYSTEM', 'COURIER')
    ),
    actor_id uuid,
    event_at timestamptz not null,
    meta jsonb not null default '{}'::jsonb,
    constraint fk_order_events_order
        foreign key (order_id) references orders(order_id) on delete cascade
);

create index if not exists idx_order_events_order_id on order_events(order_id);
create index if not exists idx_order_events_event_type on order_events(event_type);
create index if not exists idx_order_events_event_at on order_events(event_at);