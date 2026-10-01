--
-- PostgreSQL database dump
--

\restrict HJjzOqlOM77UoYbTrul6lvHe7lc5ltvory0LtrcgHXjI1MZQeld6NOzazf13hrX

-- Dumped from database version 17.10
-- Dumped by pg_dump version 17.11 (Debian 17.11-1.pgdg13+2)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: -
--

-- *not* creating schema, since initdb creates it


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: basket_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.basket_items (
    basket_item_id bigint NOT NULL,
    basket_id bigint NOT NULL,
    item_id bigint NOT NULL,
    quantity integer NOT NULL,
    CONSTRAINT basket_items_quantity_check CHECK ((quantity > 0))
);


--
-- Name: basket_items_basket_item_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.basket_items ALTER COLUMN basket_item_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.basket_items_basket_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: checkouts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.checkouts (
    checkout_id bigint NOT NULL,
    user_uuid uuid NOT NULL,
    total_amount numeric(12,2) NOT NULL,
    checkout_status character varying(30) NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT checkouts_checkout_status_check CHECK (((checkout_status)::text = ANY ((ARRAY['CREATED'::character varying, 'PAID'::character varying, 'FAILED'::character varying, 'CANCELLED'::character varying, 'PARTIALLY_REFUNDED'::character varying, 'REFUNDED'::character varying])::text[]))),
    CONSTRAINT checkouts_total_amount_check CHECK ((total_amount >= (0)::numeric))
);


--
-- Name: checkouts_checkout_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.checkouts ALTER COLUMN checkout_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.checkouts_checkout_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: coupons; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.coupons (
    coupon_code character varying(20) NOT NULL,
    store_uuid uuid NOT NULL,
    item_code bigint NOT NULL,
    discount_type character varying(10) NOT NULL,
    discount_amount numeric(12,2) NOT NULL,
    discount_limit numeric(12,2),
    due_date timestamp with time zone NOT NULL,
    CONSTRAINT coupons_discount_amount_check CHECK ((discount_amount >= (0)::numeric)),
    CONSTRAINT coupons_discount_limit_check CHECK ((discount_limit >= (0)::numeric)),
    CONSTRAINT coupons_discount_type_check CHECK (((discount_type)::text = ANY ((ARRAY['PERCENT'::character varying, 'PRICE'::character varying])::text[])))
);


--
-- Name: items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.items (
    item_code bigint NOT NULL,
    store_uuid uuid NOT NULL,
    item_name character varying(150) NOT NULL,
    price numeric(12,2) NOT NULL,
    upload_date timestamp with time zone NOT NULL,
    delivery_type character varying(30) NOT NULL,
    main_images jsonb DEFAULT '[]'::jsonb NOT NULL,
    describe_file jsonb,
    available integer DEFAULT 0 NOT NULL,
    CONSTRAINT items_available_check CHECK ((available >= 0)),
    CONSTRAINT items_price_check CHECK ((price >= (0)::numeric))
);


--
-- Name: items_item_code_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.items ALTER COLUMN item_code ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.items_item_code_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: order_coupons; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.order_coupons (
    order_item_id bigint NOT NULL,
    coupon_code character varying(20) NOT NULL,
    discount_type_snapshot character varying(10) NOT NULL,
    discount_amount_snapshot numeric(12,2) NOT NULL,
    discount_limit_snapshot numeric(12,2),
    applied_discount_amount numeric(12,2) NOT NULL,
    CONSTRAINT order_coupons_applied_discount_amount_check CHECK ((applied_discount_amount >= (0)::numeric)),
    CONSTRAINT order_coupons_discount_amount_snapshot_check CHECK ((discount_amount_snapshot >= (0)::numeric)),
    CONSTRAINT order_coupons_discount_limit_snapshot_check CHECK ((discount_limit_snapshot >= (0)::numeric)),
    CONSTRAINT order_coupons_discount_type_snapshot_check CHECK (((discount_type_snapshot)::text = ANY ((ARRAY['PERCENT'::character varying, 'PRICE'::character varying])::text[])))
);


--
-- Name: order_events; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.order_events (
    event_id bigint NOT NULL,
    order_id bigint NOT NULL,
    event_type character varying(30) NOT NULL,
    from_status character varying(30),
    to_status character varying(30),
    actor_type character varying(20) NOT NULL,
    actor_id uuid,
    event_at timestamp with time zone NOT NULL,
    meta jsonb DEFAULT '{}'::jsonb NOT NULL,
    CONSTRAINT order_events_actor_type_check CHECK (((actor_type)::text = ANY ((ARRAY['USER'::character varying, 'SELLER'::character varying, 'SYSTEM'::character varying, 'COURIER'::character varying])::text[]))),
    CONSTRAINT order_events_event_type_check CHECK (((event_type)::text = ANY ((ARRAY['STATUS_CHANGED'::character varying, 'TRACKING_ADDED'::character varying, 'TRACKING_UPDATED'::character varying, 'CONFIRM_EXTENDED'::character varying, 'DELIVERED_MARKED'::character varying, 'PURCHASE_CONFIRMED'::character varying, 'CANCEL_REQUESTED'::character varying, 'REFUND_REQUESTED'::character varying])::text[]))),
    CONSTRAINT order_events_from_status_check CHECK (((from_status)::text = ANY ((ARRAY['PAID'::character varying, 'SELLER_CONFIRMED'::character varying, 'PREPARING'::character varying, 'SHIPPED'::character varying, 'IN_TRANSIT'::character varying, 'DELIVERED'::character varying, 'PURCHASE_CONFIRMED'::character varying, 'CANCEL_REQUESTED'::character varying, 'CANCELLED'::character varying, 'RETURN_REQUESTED'::character varying, 'RETURN_APPROVED'::character varying, 'RETURN_PICKED_UP'::character varying])::text[]))),
    CONSTRAINT order_events_to_status_check CHECK (((to_status)::text = ANY ((ARRAY['PAID'::character varying, 'SELLER_CONFIRMED'::character varying, 'PREPARING'::character varying, 'SHIPPED'::character varying, 'IN_TRANSIT'::character varying, 'DELIVERED'::character varying, 'PURCHASE_CONFIRMED'::character varying, 'CANCEL_REQUESTED'::character varying, 'CANCELLED'::character varying, 'RETURN_REQUESTED'::character varying, 'RETURN_APPROVED'::character varying, 'RETURN_PICKED_UP'::character varying])::text[])))
);


--
-- Name: order_events_event_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.order_events ALTER COLUMN event_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.order_events_event_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: order_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.order_items (
    order_item_id bigint NOT NULL,
    order_id bigint NOT NULL,
    item_code bigint NOT NULL,
    quantity integer NOT NULL,
    unit_price_at_purchase numeric(12,2) NOT NULL,
    item_name_snapshot character varying(255) NOT NULL,
    CONSTRAINT order_items_quantity_check CHECK ((quantity > 0)),
    CONSTRAINT order_items_unit_price_at_purchase_check CHECK ((unit_price_at_purchase >= (0)::numeric))
);


--
-- Name: order_items_order_item_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.order_items ALTER COLUMN order_item_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.order_items_order_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: orders; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.orders (
    order_id bigint NOT NULL,
    checkout_id bigint NOT NULL,
    user_uuid uuid NOT NULL,
    store_uuid uuid NOT NULL,
    status character varying(30) NOT NULL,
    ordered_at timestamp with time zone NOT NULL,
    courier_code character varying(20),
    tracking_number character varying(50),
    delivered_date timestamp with time zone,
    delivered_marked_by character varying(20) DEFAULT 'COURIER'::character varying,
    confirm_deadline_date timestamp with time zone,
    confirm_extended boolean DEFAULT false NOT NULL,
    confirm_date timestamp with time zone,
    confirmed_by character varying(10),
    version bigint DEFAULT 0 NOT NULL,
    shipping_address_snapshot character varying(150) NOT NULL,
    delivery_address character varying(255) NOT NULL,
    CONSTRAINT orders_confirmed_by_check CHECK (((confirmed_by)::text = ANY ((ARRAY['USER'::character varying, 'SELLER'::character varying, 'SYSTEM'::character varying])::text[]))),
    CONSTRAINT orders_delivered_marked_by_check CHECK (((delivered_marked_by)::text = ANY ((ARRAY['USER'::character varying, 'SELLER'::character varying, 'SYSTEM'::character varying, 'COURIER'::character varying])::text[]))),
    CONSTRAINT orders_status_check CHECK (((status)::text = ANY ((ARRAY['PAID'::character varying, 'SELLER_CONFIRMED'::character varying, 'PREPARING'::character varying, 'SHIPPED'::character varying, 'IN_TRANSIT'::character varying, 'DELIVERED'::character varying, 'PURCHASE_CONFIRMED'::character varying, 'CANCEL_REQUESTED'::character varying, 'CANCELLED'::character varying, 'RETURN_REQUESTED'::character varying, 'RETURN_APPROVED'::character varying, 'RETURN_PICKED_UP'::character varying])::text[])))
);


--
-- Name: orders_order_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.orders ALTER COLUMN order_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.orders_order_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: payments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.payments (
    payment_id bigint NOT NULL,
    checkout_id bigint NOT NULL,
    payment_provider character varying(30) NOT NULL,
    payment_method character varying(20) NOT NULL,
    installment_months smallint DEFAULT 0 NOT NULL,
    amount_authorized numeric(12,2) NOT NULL,
    amount_captured numeric(12,2) DEFAULT 0 NOT NULL,
    payment_status character varying(30) NOT NULL,
    provider_tx_id character varying(100),
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT payments_amount_authorized_check CHECK ((amount_authorized >= (0)::numeric)),
    CONSTRAINT payments_amount_captured_check CHECK ((amount_captured >= (0)::numeric)),
    CONSTRAINT payments_installment_months_check CHECK ((installment_months >= 0)),
    CONSTRAINT payments_payment_status_check CHECK (((payment_status)::text = ANY ((ARRAY['AUTHORIZED'::character varying, 'CAPTURED'::character varying, 'VOIDED'::character varying, 'REFUNDED'::character varying, 'PARTIALLY_REFUNDED'::character varying, 'FAILED'::character varying])::text[])))
);


--
-- Name: payments_payment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.payments ALTER COLUMN payment_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.payments_payment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: refunds; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.refunds (
    refund_id bigint NOT NULL,
    payment_id bigint NOT NULL,
    order_id bigint NOT NULL,
    refund_type character varying(30) NOT NULL,
    refund_amount numeric(12,2) NOT NULL,
    deduction_amount numeric(12,2) DEFAULT 0 NOT NULL,
    deduction_reason character varying(50),
    refund_status character varying(20) NOT NULL,
    requested_by_type character varying(20) NOT NULL,
    requested_by_uuid uuid,
    reason character varying(200),
    created_at timestamp with time zone NOT NULL,
    done_at timestamp with time zone,
    provider_refund_id character varying(100),
    CONSTRAINT refunds_deduction_amount_check CHECK ((deduction_amount >= (0)::numeric)),
    CONSTRAINT refunds_refund_amount_check CHECK ((refund_amount >= (0)::numeric)),
    CONSTRAINT refunds_refund_status_check CHECK (((refund_status)::text = ANY ((ARRAY['REQUESTED'::character varying, 'APPROVED'::character varying, 'DONE'::character varying, 'FAILED'::character varying])::text[]))),
    CONSTRAINT refunds_refund_type_check CHECK (((refund_type)::text = ANY ((ARRAY['FULL_REFUND'::character varying, 'PARTIAL_REFUND'::character varying, 'DELIVERY_FEE_ONLY'::character varying])::text[]))),
    CONSTRAINT refunds_requested_by_type_check CHECK (((requested_by_type)::text = ANY ((ARRAY['USER'::character varying, 'SELLER'::character varying, 'SYSTEM'::character varying])::text[])))
);


--
-- Name: refunds_refund_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.refunds ALTER COLUMN refund_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.refunds_refund_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: seller_info; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.seller_info (
    store_uuid uuid NOT NULL,
    store_name character varying(100) NOT NULL,
    generated_date date NOT NULL,
    store_tin character varying(20),
    store_crn character varying(20),
    store_brn character varying(20)
);


--
-- Name: seller_logins; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.seller_logins (
    id character varying(50) NOT NULL,
    password_crypted text NOT NULL,
    store_uuid uuid NOT NULL
);


--
-- Name: user_address_saved; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_address_saved (
    address_id bigint NOT NULL,
    uuid uuid NOT NULL,
    address character varying(255) NOT NULL
);


--
-- Name: user_address_saved_address_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.user_address_saved ALTER COLUMN address_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.user_address_saved_address_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: user_basket; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_basket (
    basket_id bigint NOT NULL,
    user_uuid uuid NOT NULL
);


--
-- Name: user_basket_basket_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.user_basket ALTER COLUMN basket_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.user_basket_basket_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: user_info; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_info (
    uuid uuid NOT NULL,
    nickname character varying(50) NOT NULL,
    username character varying(50) NOT NULL,
    birthday date NOT NULL,
    email character varying(254),
    phone character varying(20) NOT NULL,
    default_address_id bigint
);


--
-- Name: user_logins; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_logins (
    id character varying(50) NOT NULL,
    password_crypted text NOT NULL,
    uuid uuid NOT NULL
);


--
-- Name: user_payment_saved; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_payment_saved (
    payment_id bigint NOT NULL,
    uuid uuid NOT NULL,
    payment_encrypted_data jsonb NOT NULL
);


--
-- Name: user_payment_saved_payment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.user_payment_saved ALTER COLUMN payment_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.user_payment_saved_payment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: basket_items basket_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.basket_items
    ADD CONSTRAINT basket_items_pkey PRIMARY KEY (basket_item_id);


--
-- Name: checkouts checkouts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkouts
    ADD CONSTRAINT checkouts_pkey PRIMARY KEY (checkout_id);


--
-- Name: coupons coupons_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.coupons
    ADD CONSTRAINT coupons_pkey PRIMARY KEY (coupon_code);


--
-- Name: items items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.items
    ADD CONSTRAINT items_pkey PRIMARY KEY (item_code);


--
-- Name: order_coupons order_coupons_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_coupons
    ADD CONSTRAINT order_coupons_pkey PRIMARY KEY (order_item_id);


--
-- Name: order_events order_events_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_events
    ADD CONSTRAINT order_events_pkey PRIMARY KEY (event_id);


--
-- Name: order_items order_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_items
    ADD CONSTRAINT order_items_pkey PRIMARY KEY (order_item_id);


--
-- Name: orders orders_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (order_id);


--
-- Name: payments payments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_pkey PRIMARY KEY (payment_id);


--
-- Name: refunds refunds_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refunds
    ADD CONSTRAINT refunds_pkey PRIMARY KEY (refund_id);


--
-- Name: seller_info seller_info_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.seller_info
    ADD CONSTRAINT seller_info_pkey PRIMARY KEY (store_uuid);


--
-- Name: seller_info seller_info_store_brn_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.seller_info
    ADD CONSTRAINT seller_info_store_brn_key UNIQUE (store_brn);


--
-- Name: seller_info seller_info_store_crn_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.seller_info
    ADD CONSTRAINT seller_info_store_crn_key UNIQUE (store_crn);


--
-- Name: seller_info seller_info_store_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.seller_info
    ADD CONSTRAINT seller_info_store_name_key UNIQUE (store_name);


--
-- Name: seller_info seller_info_store_tin_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.seller_info
    ADD CONSTRAINT seller_info_store_tin_key UNIQUE (store_tin);


--
-- Name: seller_logins seller_logins_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.seller_logins
    ADD CONSTRAINT seller_logins_pkey PRIMARY KEY (id);


--
-- Name: seller_logins seller_logins_store_uuid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.seller_logins
    ADD CONSTRAINT seller_logins_store_uuid_key UNIQUE (store_uuid);


--
-- Name: basket_items uq_basket_items_basket_item; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.basket_items
    ADD CONSTRAINT uq_basket_items_basket_item UNIQUE (basket_id, item_id);


--
-- Name: checkouts uq_checkouts_checkout_id_user_uuid; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkouts
    ADD CONSTRAINT uq_checkouts_checkout_id_user_uuid UNIQUE (checkout_id, user_uuid);


--
-- Name: payments uq_payments_provider_tx_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT uq_payments_provider_tx_id UNIQUE (provider_tx_id);


--
-- Name: refunds uq_refunds_provider_refund_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refunds
    ADD CONSTRAINT uq_refunds_provider_refund_id UNIQUE (provider_refund_id);


--
-- Name: user_address_saved uq_user_address_saved_address_id_uuid; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_address_saved
    ADD CONSTRAINT uq_user_address_saved_address_id_uuid UNIQUE (address_id, uuid);


--
-- Name: user_address_saved user_address_saved_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_address_saved
    ADD CONSTRAINT user_address_saved_pkey PRIMARY KEY (address_id);


--
-- Name: user_basket user_basket_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_basket
    ADD CONSTRAINT user_basket_pkey PRIMARY KEY (basket_id);


--
-- Name: user_basket user_basket_user_uuid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_basket
    ADD CONSTRAINT user_basket_user_uuid_key UNIQUE (user_uuid);


--
-- Name: user_info user_info_nickname_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_info
    ADD CONSTRAINT user_info_nickname_key UNIQUE (nickname);


--
-- Name: user_info user_info_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_info
    ADD CONSTRAINT user_info_pkey PRIMARY KEY (uuid);


--
-- Name: user_logins user_logins_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_logins
    ADD CONSTRAINT user_logins_pkey PRIMARY KEY (id);


--
-- Name: user_logins user_logins_uuid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_logins
    ADD CONSTRAINT user_logins_uuid_key UNIQUE (uuid);


--
-- Name: user_payment_saved user_payment_saved_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_payment_saved
    ADD CONSTRAINT user_payment_saved_pkey PRIMARY KEY (payment_id);


--
-- Name: idx_basket_items_basket_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_basket_items_basket_id ON public.basket_items USING btree (basket_id);


--
-- Name: idx_basket_items_item_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_basket_items_item_id ON public.basket_items USING btree (item_id);


--
-- Name: idx_checkouts_created_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_checkouts_created_at ON public.checkouts USING btree (created_at);


--
-- Name: idx_checkouts_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_checkouts_status ON public.checkouts USING btree (checkout_status);


--
-- Name: idx_checkouts_user_uuid; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_checkouts_user_uuid ON public.checkouts USING btree (user_uuid);


--
-- Name: idx_coupons_item_code; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_coupons_item_code ON public.coupons USING btree (item_code);


--
-- Name: idx_coupons_store_uuid; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_coupons_store_uuid ON public.coupons USING btree (store_uuid);


--
-- Name: idx_items_item_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_items_item_name ON public.items USING btree (item_name);


--
-- Name: idx_items_store_uuid; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_items_store_uuid ON public.items USING btree (store_uuid);


--
-- Name: idx_order_events_event_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_order_events_event_at ON public.order_events USING btree (event_at);


--
-- Name: idx_order_events_event_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_order_events_event_type ON public.order_events USING btree (event_type);


--
-- Name: idx_order_events_order_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_order_events_order_id ON public.order_events USING btree (order_id);


--
-- Name: idx_order_items_item_code; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_order_items_item_code ON public.order_items USING btree (item_code);


--
-- Name: idx_order_items_order_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_order_items_order_id ON public.order_items USING btree (order_id);


--
-- Name: idx_orders_checkout_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_orders_checkout_id ON public.orders USING btree (checkout_id);


--
-- Name: idx_orders_confirm_deadline_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_orders_confirm_deadline_date ON public.orders USING btree (confirm_deadline_date);


--
-- Name: idx_orders_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_orders_status ON public.orders USING btree (status);


--
-- Name: idx_orders_store_uuid; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_orders_store_uuid ON public.orders USING btree (store_uuid);


--
-- Name: idx_orders_user_uuid; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_orders_user_uuid ON public.orders USING btree (user_uuid);


--
-- Name: idx_payments_checkout_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payments_checkout_id ON public.payments USING btree (checkout_id);


--
-- Name: idx_payments_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payments_status ON public.payments USING btree (payment_status);


--
-- Name: idx_refunds_order_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_refunds_order_id ON public.refunds USING btree (order_id);


--
-- Name: idx_refunds_payment_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_refunds_payment_id ON public.refunds USING btree (payment_id);


--
-- Name: idx_refunds_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_refunds_status ON public.refunds USING btree (refund_status);


--
-- Name: idx_user_address_saved_uuid; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_user_address_saved_uuid ON public.user_address_saved USING btree (uuid);


--
-- Name: idx_user_payment_saved_uuid; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_user_payment_saved_uuid ON public.user_payment_saved USING btree (uuid);


--
-- Name: basket_items fk_basket_items_basket; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.basket_items
    ADD CONSTRAINT fk_basket_items_basket FOREIGN KEY (basket_id) REFERENCES public.user_basket(basket_id) ON DELETE CASCADE;


--
-- Name: basket_items fk_basket_items_item; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.basket_items
    ADD CONSTRAINT fk_basket_items_item FOREIGN KEY (item_id) REFERENCES public.items(item_code);


--
-- Name: checkouts fk_checkouts_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkouts
    ADD CONSTRAINT fk_checkouts_user FOREIGN KEY (user_uuid) REFERENCES public.user_info(uuid);


--
-- Name: coupons fk_coupons_item; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.coupons
    ADD CONSTRAINT fk_coupons_item FOREIGN KEY (item_code) REFERENCES public.items(item_code) ON DELETE SET NULL;


--
-- Name: coupons fk_coupons_seller; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.coupons
    ADD CONSTRAINT fk_coupons_seller FOREIGN KEY (store_uuid) REFERENCES public.seller_info(store_uuid) ON DELETE CASCADE;


--
-- Name: items fk_items_seller; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.items
    ADD CONSTRAINT fk_items_seller FOREIGN KEY (store_uuid) REFERENCES public.seller_info(store_uuid) ON DELETE CASCADE;


--
-- Name: order_coupons fk_order_coupons_coupon; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_coupons
    ADD CONSTRAINT fk_order_coupons_coupon FOREIGN KEY (coupon_code) REFERENCES public.coupons(coupon_code);


--
-- Name: order_coupons fk_order_coupons_order_item; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_coupons
    ADD CONSTRAINT fk_order_coupons_order_item FOREIGN KEY (order_item_id) REFERENCES public.order_items(order_item_id) ON DELETE CASCADE;


--
-- Name: order_events fk_order_events_order; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_events
    ADD CONSTRAINT fk_order_events_order FOREIGN KEY (order_id) REFERENCES public.orders(order_id) ON DELETE CASCADE;


--
-- Name: order_items fk_order_items_item; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_items
    ADD CONSTRAINT fk_order_items_item FOREIGN KEY (item_code) REFERENCES public.items(item_code);


--
-- Name: order_items fk_order_items_order; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_items
    ADD CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES public.orders(order_id) ON DELETE CASCADE;


--
-- Name: orders fk_orders_checkout; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT fk_orders_checkout FOREIGN KEY (checkout_id) REFERENCES public.checkouts(checkout_id) ON DELETE CASCADE;


--
-- Name: orders fk_orders_checkout_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT fk_orders_checkout_user FOREIGN KEY (checkout_id, user_uuid) REFERENCES public.checkouts(checkout_id, user_uuid);


--
-- Name: orders fk_orders_seller; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT fk_orders_seller FOREIGN KEY (store_uuid) REFERENCES public.seller_info(store_uuid);


--
-- Name: orders fk_orders_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT fk_orders_user FOREIGN KEY (user_uuid) REFERENCES public.user_info(uuid);


--
-- Name: payments fk_payments_checkout; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT fk_payments_checkout FOREIGN KEY (checkout_id) REFERENCES public.checkouts(checkout_id) ON DELETE CASCADE;


--
-- Name: refunds fk_refunds_order; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refunds
    ADD CONSTRAINT fk_refunds_order FOREIGN KEY (order_id) REFERENCES public.orders(order_id) ON DELETE CASCADE;


--
-- Name: refunds fk_refunds_payment; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refunds
    ADD CONSTRAINT fk_refunds_payment FOREIGN KEY (payment_id) REFERENCES public.payments(payment_id) ON DELETE CASCADE;


--
-- Name: seller_logins fk_seller_logins_seller; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.seller_logins
    ADD CONSTRAINT fk_seller_logins_seller FOREIGN KEY (store_uuid) REFERENCES public.seller_info(store_uuid) ON DELETE CASCADE;


--
-- Name: user_address_saved fk_user_address_saved_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_address_saved
    ADD CONSTRAINT fk_user_address_saved_user FOREIGN KEY (uuid) REFERENCES public.user_info(uuid) ON DELETE CASCADE;


--
-- Name: user_basket fk_user_basket_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_basket
    ADD CONSTRAINT fk_user_basket_user FOREIGN KEY (user_uuid) REFERENCES public.user_info(uuid) ON DELETE CASCADE;


--
-- Name: user_info fk_user_info_default_address; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_info
    ADD CONSTRAINT fk_user_info_default_address FOREIGN KEY (default_address_id, uuid) REFERENCES public.user_address_saved(address_id, uuid);


--
-- Name: user_logins fk_user_logins_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_logins
    ADD CONSTRAINT fk_user_logins_user FOREIGN KEY (uuid) REFERENCES public.user_info(uuid) ON DELETE CASCADE;


--
-- Name: user_payment_saved fk_user_payment_saved_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_payment_saved
    ADD CONSTRAINT fk_user_payment_saved_user FOREIGN KEY (uuid) REFERENCES public.user_info(uuid) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

\unrestrict HJjzOqlOM77UoYbTrul6lvHe7lc5ltvory0LtrcgHXjI1MZQeld6NOzazf13hrX

