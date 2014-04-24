CREATE DATABASE cars_sbor_net;

CREATE SEQUENCE cars_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE cars(
	id_car integer NOT NULL DEFAULT nextval('cars_seq'::regclass),
	fulltext text NOT NULL,
	url character varying(250),
	CONSTRAINT cars_pkey PRIMARY KEY (id_car)
)
WITH (
  OIDS=FALSE
);
