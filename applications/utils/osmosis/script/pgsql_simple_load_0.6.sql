-- Drop all primary keys and indexes to improve load speed.
ALTER TABLE nodes DROP CONSTRAINT pk_nodes;
ALTER TABLE ways DROP CONSTRAINT pk_ways;
ALTER TABLE way_nodes DROP CONSTRAINT pk_way_nodes;
ALTER TABLE relations DROP CONSTRAINT pk_relations;
DROP INDEX idx_nodes_action;
DROP INDEX idx_node_tags_node_id;
DROP INDEX idx_nodes_geom;
DROP INDEX idx_ways_action;
DROP INDEX idx_way_tags_way_id;
DROP INDEX idx_way_nodes_node_id;
DROP INDEX idx_relations_action;
DROP INDEX idx_relation_tags_relation_id;
DROP INDEX idx_ways_bbox;

SELECT DropGeometryColumn('ways', 'bbox');

-- Import the table data from the data files using the fast COPY method.
-- COPY nodes FROM E'C:\\tmp\\pgimport\\nodes.txt';
-- COPY node_tags FROM E'C:\\tmp\\pgimport\\node_tags.txt';
-- COPY ways FROM E'C:\\tmp\\pgimport\\ways.txt';
-- COPY way_tags FROM E'C:\\tmp\\pgimport\\way_tags.txt';
-- COPY way_nodes FROM E'C:\\tmp\\pgimport\\way_nodes.txt';
-- COPY relations FROM E'C:\\tmp\\pgimport\\relations.txt';
-- COPY relation_tags FROM E'C:\\tmp\\pgimport\\relation_tags.txt';
-- COPY relation_members FROM E'C:\\tmp\\pgimport\\relation_members.txt';

-- or do it this way
\copy nodes FROM 'nodes.txt'
\copy node_tags FROM 'node_tags.txt'
\copy ways FROM 'ways.txt'
\copy way_tags FROM 'way_tags.txt'
\copy way_nodes FROM 'way_nodes.txt'
\copy relations FROM 'relations.txt'
\copy relation_tags FROM 'relation_tags.txt'
\copy relation_members FROM 'relation_members.txt'

-- Add the primary keys and indexes back again (except the way bbox index).
ALTER TABLE ONLY nodes ADD CONSTRAINT pk_nodes PRIMARY KEY (id);
ALTER TABLE ONLY ways ADD CONSTRAINT pk_ways PRIMARY KEY (id);
ALTER TABLE ONLY way_nodes ADD CONSTRAINT pk_way_nodes PRIMARY KEY (way_id, sequence_id);
ALTER TABLE ONLY relations ADD CONSTRAINT pk_relations PRIMARY KEY (id);
CREATE INDEX idx_nodes_action ON nodes USING btree (action);
CREATE INDEX idx_node_tags_node_id ON node_tags USING btree (node_id);
CREATE INDEX idx_nodes_geom ON nodes USING gist (geom);
CREATE INDEX idx_ways_action ON ways USING btree (action);
CREATE INDEX idx_way_tags_way_id ON way_tags USING btree (way_id);
CREATE INDEX idx_way_nodes_node_id ON way_nodes USING btree (node_id);
CREATE INDEX idx_relations_action ON relations USING btree (action);
CREATE INDEX idx_relation_tags_relation_id ON relation_tags USING btree (relation_id);

-- Add a postgis bounding box column used for indexing the location of the way.
-- This will contain a bounding box surrounding the extremities of the way.
SELECT AddGeometryColumn('ways', 'bbox', 4326, 'GEOMETRY', 2);

-- Update the bbox column of the way table.
UPDATE ways SET bbox = (
	SELECT Envelope(Collect(geom))
	FROM nodes JOIN way_nodes ON way_nodes.node_id = nodes.id
	WHERE way_nodes.way_id = ways.id
);

-- Index the way bounding box column.
CREATE INDEX idx_ways_bbox ON ways USING gist (bbox);

-- Perform database maintenance due to large database changes.
VACUUM ANALYZE;

