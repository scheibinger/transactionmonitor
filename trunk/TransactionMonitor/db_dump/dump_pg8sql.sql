--
-- PostgreSQL database dump
--

SET client_encoding = 'sql_ascii';
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: pgsql
--

COMMENT ON SCHEMA public IS 'standard public schema';


--
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: 
--

CREATE PROCEDURAL LANGUAGE plpgsql;


SET search_path = public, pg_catalog;

--
-- Name: adres_id_adr_seq; Type: SEQUENCE; Schema: public; Owner: ares-system_8
--

CREATE SEQUENCE adres_id_adr_seq
    INCREMENT BY 1
    MAXVALUE 9999999999999999
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.adres_id_adr_seq OWNER TO "ares-system_8";

--
-- Name: adres_id_adr_seq; Type: SEQUENCE SET; Schema: public; Owner: ares-system_8
--

SELECT pg_catalog.setval('adres_id_adr_seq', 27, true);


SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: adres; Type: TABLE; Schema: public; Owner: ares-system_8; Tablespace: 
--

CREATE TABLE adres (
    id_adr integer DEFAULT nextval('adres_id_adr_seq'::regclass) NOT NULL,
    imie character varying(100) DEFAULT NULL::character varying,
    nazwisko character varying(100) DEFAULT NULL::character varying,
    ulica character varying(100) DEFAULT NULL::character varying,
    nr integer DEFAULT 0
);


ALTER TABLE public.adres OWNER TO "ares-system_8";

--
-- Name: pensja; Type: TABLE; Schema: public; Owner: ares-system_8; Tablespace: 
--

CREATE TABLE pensja (
    pen_id integer NOT NULL,
    pen_kwota double precision DEFAULT 0,
    pen_prc_id integer DEFAULT 0
);


ALTER TABLE public.pensja OWNER TO "ares-system_8";

--
-- Name: pracownicy; Type: TABLE; Schema: public; Owner: ares-system_8; Tablespace: 
--

CREATE TABLE pracownicy (
    prc_id integer NOT NULL,
    prc_imie character varying(100) DEFAULT NULL::character varying,
    prc_nazwisko character varying(100) DEFAULT NULL::character varying,
    prc_pen_id integer DEFAULT 0
);


ALTER TABLE public.pracownicy OWNER TO "ares-system_8";

--
-- Name: wyksztalcenie_id_wyksztalcenie_seq; Type: SEQUENCE; Schema: public; Owner: ares-system_8
--

CREATE SEQUENCE wyksztalcenie_id_wyksztalcenie_seq
    INCREMENT BY 1
    MAXVALUE 9999999999999999
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.wyksztalcenie_id_wyksztalcenie_seq OWNER TO "ares-system_8";

--
-- Name: wyksztalcenie_id_wyksztalcenie_seq; Type: SEQUENCE SET; Schema: public; Owner: ares-system_8
--

SELECT pg_catalog.setval('wyksztalcenie_id_wyksztalcenie_seq', 5, true);


--
-- Name: wyksztalcenie; Type: TABLE; Schema: public; Owner: ares-system_8; Tablespace: 
--

CREATE TABLE wyksztalcenie (
    id_wyksztalcenie integer DEFAULT nextval('wyksztalcenie_id_wyksztalcenie_seq'::regclass) NOT NULL,
    imie character varying(100),
    nazwisko character varying(100),
    stopien_nauk character varying(100)
);


ALTER TABLE public.wyksztalcenie OWNER TO "ares-system_8";

--
-- Name: pensja_pen_id_seq; Type: SEQUENCE; Schema: public; Owner: ares-system_8
--

CREATE SEQUENCE pensja_pen_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.pensja_pen_id_seq OWNER TO "ares-system_8";

--
-- Name: pensja_pen_id_seq; Type: SEQUENCE SET; Schema: public; Owner: ares-system_8
--

SELECT pg_catalog.setval('pensja_pen_id_seq', 2, true);


--
-- Name: pracownicy_prc_id_seq; Type: SEQUENCE; Schema: public; Owner: ares-system_8
--

CREATE SEQUENCE pracownicy_prc_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.pracownicy_prc_id_seq OWNER TO "ares-system_8";

--
-- Name: pracownicy_prc_id_seq; Type: SEQUENCE SET; Schema: public; Owner: ares-system_8
--

SELECT pg_catalog.setval('pracownicy_prc_id_seq', 84, true);


--
-- Name: pen_id; Type: DEFAULT; Schema: public; Owner: ares-system_8
--

ALTER TABLE pensja ALTER COLUMN pen_id SET DEFAULT nextval('pensja_pen_id_seq'::regclass);


--
-- Name: prc_id; Type: DEFAULT; Schema: public; Owner: ares-system_8
--

ALTER TABLE pracownicy ALTER COLUMN prc_id SET DEFAULT nextval('pracownicy_prc_id_seq'::regclass);


--
-- Data for Name: adres; Type: TABLE DATA; Schema: public; Owner: ares-system_8
--

INSERT INTO adres VALUES (1, 'marcin', 'armata', 'kokociowa', 12);
INSERT INTO adres VALUES (17, NULL, 'gozny', 'moskiewska', 0);
INSERT INTO adres VALUES (18, NULL, 'gozny', 'moskiewska', 0);
INSERT INTO adres VALUES (19, NULL, 'gozny', 'moskiewska', 0);
INSERT INTO adres VALUES (20, NULL, 'gozny', 'moskiewska', 0);
INSERT INTO adres VALUES (21, NULL, 'gozny', 'moskiewska', 0);
INSERT INTO adres VALUES (22, NULL, 'gozny', 'moskiewska', 0);
INSERT INTO adres VALUES (23, NULL, 'gozny', 'moskiewska', 0);
INSERT INTO adres VALUES (24, 'hrdhydtr', NULL, NULL, 0);
INSERT INTO adres VALUES (25, 'oooooooo', NULL, NULL, 0);
INSERT INTO adres VALUES (26, 'oooooooo', NULL, NULL, 0);
INSERT INTO adres VALUES (27, 'oooooooo', NULL, NULL, 0);


--
-- Data for Name: pensja; Type: TABLE DATA; Schema: public; Owner: ares-system_8
--



--
-- Data for Name: pracownicy; Type: TABLE DATA; Schema: public; Owner: ares-system_8
--

INSERT INTO pracownicy VALUES (3, 'MARCIN', 'ARMATA', 1000);
INSERT INTO pracownicy VALUES (82, 'before', 'lock', 0);
INSERT INTO pracownicy VALUES (83, 'after', 'LOCK', 0);
INSERT INTO pracownicy VALUES (84, 'be2fore', 'lock', 0);


--
-- Data for Name: wyksztalcenie; Type: TABLE DATA; Schema: public; Owner: ares-system_8
--

INSERT INTO wyksztalcenie VALUES (1, 'marcin', 'armata', 'dr');


--
-- Name: adres_pkey; Type: CONSTRAINT; Schema: public; Owner: ares-system_8; Tablespace: 
--

ALTER TABLE ONLY adres
    ADD CONSTRAINT adres_pkey PRIMARY KEY (id_adr);


--
-- Name: pensja_pkey; Type: CONSTRAINT; Schema: public; Owner: ares-system_8; Tablespace: 
--

ALTER TABLE ONLY pensja
    ADD CONSTRAINT pensja_pkey PRIMARY KEY (pen_id);


--
-- Name: pracownicy_pkey; Type: CONSTRAINT; Schema: public; Owner: ares-system_8; Tablespace: 
--

ALTER TABLE ONLY pracownicy
    ADD CONSTRAINT pracownicy_pkey PRIMARY KEY (prc_id);


--
-- Name: wyksztalcenie_pkey; Type: CONSTRAINT; Schema: public; Owner: ares-system_8; Tablespace: 
--

ALTER TABLE ONLY wyksztalcenie
    ADD CONSTRAINT wyksztalcenie_pkey PRIMARY KEY (id_wyksztalcenie);


--
-- Name: public; Type: ACL; Schema: -; Owner: pgsql
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM pgsql;
GRANT ALL ON SCHEMA public TO pgsql;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

