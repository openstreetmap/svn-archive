-- Add the minor_version column to the way table
ALTER TABLE ONLY way ADD COLUMN minor_version int NOT NULL;


-- drop the existing pk on the ways
ALTER TABLE ONLY ways DROP CONSTRAINT pk_ways;

-- add the new pk on the nodes that respects the way minor version
ALTER TABLE ONLY ways ADD CONSTRAINT pk_ways PRIMARY KEY (id, version, minor_version);



-- Add the minor_version column to the way_nodes table
ALTER TABLE ONLY way_nodes ADD COLUMN minor_version int NOT NULL;

-- drop the existing pk on the way_nodes
ALTER TABLE ONLY way_nodes DROP CONSTRAINT pk_way_nodes;

-- add the new pk on the way_nodes that respects the way version
ALTER TABLE ONLY way_nodes ADD CONSTRAINT pk_way_nodes PRIMARY KEY (way_id, way_version, minor_version, sequence_id);



-- TODO: here the minor_version column needs to be added to the relation table, once minor versions for relations are supported
