TRUNCATE placex;
TRUNCATE search_name;
TRUNCATE place_addressline;
TRUNCATE location_point;
TRUNCATE location_point_26;
TRUNCATE location_point_25;
TRUNCATE location_point_24;
TRUNCATE location_point_23;
TRUNCATE location_point_22;
TRUNCATE location_point_21;
TRUNCATE location_point_20;
TRUNCATE location_point_19;
TRUNCATE location_point_18;
TRUNCATE location_point_17;
TRUNCATE location_point_16;
TRUNCATE location_point_15;
TRUNCATE location_point_14;
TRUNCATE location_point_13;
TRUNCATE location_point_12;
TRUNCATE location_point_11;
TRUNCATE location_point_10;
TRUNCATE location_point_9;
TRUNCATE location_point_8;
TRUNCATE location_point_7;
TRUNCATE location_point_6;
TRUNCATE location_point_5;
TRUNCATE location_point_4;
TRUNCATE location_point_3;
TRUNCATE location_point_2;
TRUNCATE location_point_1;
TRUNCATE location_area;

DROP SEQUENCE seq_place;
CREATE SEQUENCE seq_place start 1;

DROP SEQUENCE seq_progress_updates;
CREATE SEQUENCE seq_progress_updates start 1;

select 'now'::timestamp;
--insert into placex select null,'E',null,'place','county',ARRAY[ROW('name',county)::keyvalue],null,null,null,null,null,'us',null,null,null,false,ST_Transform(geometryn(the_geom, generate_series(1, numgeometries(the_geom))), 4326) from us_statecounty;
--insert into placex select null,'E',null,'place','state',ARRAY[ROW('ref',state)::keyvalue],null,null,null,null,null,'us',null,null,null,false,ST_Transform(geometryn(the_geom, generate_series(1, numgeometries(the_geom))), 4326) from us_statecounty;
--insert into placex select null,'E',null,'place','state',ARRAY[ROW('name',state)::keyvalue],null,null,null,null,null,'us',null,null,null,false,ST_Transform(geometryn(the_geom, generate_series(1, numgeometries(the_geom))), 4326) from us_state;
--insert into placex select null,'E',nextval,'place','postcode',null,null,null,null,false,postcode,lower(countrycode),null,null,null,null,geometry from gb_postcode;
--insert into placex select null,'E',nextval,'place','postcode',null,null,null,null,false,substring(postcode from '^([A-Z][A-Z]?[0-9][0-9A-Z]?) [0-9]$'),lower(countrycode),null,null,null,null,geometry from gb_postcode where postcode ~ '^[A-Z][A-Z]?[0-9][0-9A-Z]? [0-9]$' and ST_GeometryType(geometry) = 'ST_Polygon';
--insert into placex select null,'X',nextval,'place','postcodearea',ARRAY[ROW('name',postcodeareaname)::keyvalue],null,null,null,null,null,'gb',null,15,23,false,geometry from gb_postcode join gb_postcodearea on (substring(postcode from '^([A-Z][A-Z]?)[0-9][0-9A-Z]? [0-9]$') = postcodeareaid) where postcode ~ '^[A-Z][A-Z]?[0-9][0-9A-Z]? [0-9]$' and ST_GeometryType(geometry) = 'ST_Polygon';

select 'now'::timestamp;
insert into placex select * from place where osm_type = 'N';-- order by geometry_sector(geometry);
select 'now'::timestamp;
insert into placex select * from place where osm_type = 'W';-- order by geometry_sector(geometry);
select 'now'::timestamp;
insert into placex select * from place where osm_type = 'R';-- order by geometry_sector(geometry);
select 'now'::timestamp;

-- use this to do a simple index - for the full planet use 'reindex.php'
--update placex set indexed = true where not indexed and rank_search <= 26 and name is not null;
--select 'finished <= 26','now'::timestamp;
--update placex set indexed = true where not indexed and rank_search > 26 and name is not null;
--select 'finished > 26','now'::timestamp;
