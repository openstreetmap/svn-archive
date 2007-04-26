
drop table airport_place_node;

create table airport_place_node (id SERIAL,
	osm_id integer,
	name varchar,
	icao varchar,
	lat double precision, lon double precision,
	min_lat double precision, min_lon double precision,
	max_lat double precision, max_lon double precision
	);

\d airport_place_node;



drop table airport_ways;

create table airport_ways (id SERIAL,
	parent_id integer,
	osm_id integer,
	runway_number varchar,
	heading double precision,
	length double precision,
	width double precision,
	center_lat double precision, center_lon double precision
	);

\d airport_ways;




drop table airport_tags;

create table airport_tags (id SERIAL,
	parent_id integer,
	type char,
	key varchar,
	value varchar
	);

\d airport_tags;




drop table airport_nodes;

create table airport_nodes (id SERIAL,
	parent_id integer,
	type char,
	lat double precision,
	lon double precision
	);

\d airport_tags;


\d;
