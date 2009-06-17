--
-- PostgreSQL database dump
--

SET client_encoding = 'sql_ascii';
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

--
-- Name: pensja_pen_id_seq; Type: SEQUENCE SET; Schema: public; Owner: ares-system_8
--

SELECT pg_catalog.setval('pensja_pen_id_seq', 1, false);


--
-- Name: pracownicy_prc_id_seq; Type: SEQUENCE SET; Schema: public; Owner: ares-system_8
--

SELECT pg_catalog.setval('pracownicy_prc_id_seq', 1, false);


--
-- Data for Name: pensja; Type: TABLE DATA; Schema: public; Owner: ares-system_8
--

COPY pensja (pen_id, pen_kwota, pen_prc_id) FROM stdin;
\.


--
-- Data for Name: pracownicy; Type: TABLE DATA; Schema: public; Owner: ares-system_8
--

COPY pracownicy (prc_id, prc_imie, prc_nazwisko, prc_pen_id) FROM stdin;
\.


--
-- PostgreSQL database dump complete
--

