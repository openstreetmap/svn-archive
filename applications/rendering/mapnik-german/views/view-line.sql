DROP VIEW view_osmde_line;
CREATE VIEW view_osmde_line AS
SELECT
osm_id,
tags->'access' as "access",
tags->'addr:housename' as "addr:housename",
"addr:housenumber" as "addr:housenumber",
tags->'addr:interpolation' as "addr:interpolation",
tags->'admin_level' as "admin_level",
"aerialway" as "aerialway",
"aeroway" as "aeroway",
"amenity" as "amenity",
"barrier" as "barrier",
tags->'bicycle' as "bicycle",
"boundary" as "boundary",
tags->'brand' as "brand",
tags->'bridge' as "bridge",
"building" as "building",
tags->'capital' as "capital",
tags->'construction' as "construction",
tags->'covered' as "covered",
tags->'culvert' as "culvert",
tags->'cutting' as "cutting",
tags->'denomination' as "denomination",
tags->'disused' as "disused",
tags->'embankment' as "embankment",
tags->'foot' as "foot",
tags->'generator:source' as "generator:source",
tags->'harbour' as "harbour",
"highway" as "highway",
"historic" as "historic",
tags->'horse' as "horse",
tags->'intermittent' as "intermittent",
tags->'junction' as "junction",
"landuse" as "landuse",
tags->'layer' as "layer",
"leisure" as "leisure",
"lock" as "lock",
"man_made" as "man_made",
"military" as "military",
tags->'motorcar' as "motorcar",
tags->'motorroad' as "motorroad",
tags->'name' as "name",
tags->'name:de' as "name:de",
tags->'int_name' as "int_name",  
tags->'name:en' as "name:en",
"natural" as "natural",
tags->'oneway' as "oneway",
tags->'operator' as "operator",
"place" as "place",
tags->'poi' as "poi",
CASE WHEN tags->'population' ~ '^[0-9]+$' THEN (tags->'population')::bigint ELSE 0 END as "population",
"power" as "power",
tags->'power_source' as "power_source",
tags->'proposed' as "proposed",
"railway" as "railway",
tags->'ref' as "ref",
tags->'religion' as "religion",
"route" as "route",
tags->'service' as "service",
"shop" as "shop",
tags->'sport' as "sport",
tags->'surface' as "surface",
tags->'toll' as "toll",
"tourism" as "tourism",
tags->'tower:type' as "tower:type",
tags->'tracktype' as "tracktype",
tags->'tunnel' as "tunnel",
tags->'water' as "water",
"waterway" as "waterway",
tags->'wetland' as "wetland",
tags->'width' as "width",
tags->'wood' as "wood",
way as "way",
way_area as way_area,
z_order as z_order,
tags as tags
FROM planet_osm_line;

GRANT select ON view_osmde_line to public;
