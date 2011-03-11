-- drop the existing pk on the nodes
ALTER TABLE ONLY nodes DROP CONSTRAINT pk_nodes;

-- add the new pk on the nodes that respects the node version
ALTER TABLE ONLY nodes ADD CONSTRAINT pk_nodes PRIMARY KEY (id, version);



-- drop the existing pk on the ways
ALTER TABLE ONLY ways DROP CONSTRAINT pk_ways;

-- add the new pk on the nodes that respects the way version
ALTER TABLE ONLY ways ADD CONSTRAINT pk_ways PRIMARY KEY (id, version);



-- Add the way_version column to the way_nodes table
ALTER TABLE ONLY way_nodes ADD COLUMN way_version int NOT NULL;

-- drop the existing pk on the way_nodes
ALTER TABLE ONLY way_nodes DROP CONSTRAINT pk_way_nodes;

-- add the new pk on the way_nodes that respects the way version
ALTER TABLE ONLY way_nodes ADD CONSTRAINT pk_way_nodes PRIMARY KEY (way_id, way_version, sequence_id);



-- drop the existing pk on the relations table
ALTER TABLE ONLY relations DROP CONSTRAINT pk_relations;

-- add the new pk on the relations that respects the relation version
ALTER TABLE ONLY relations ADD CONSTRAINT pk_relations PRIMARY KEY (id, version);



-- Add the member_version column to the relation_members table
ALTER TABLE ONLY relation_members ADD COLUMN relation_version int NOT NULL;

-- drop the existing pk on the way_nodes
ALTER TABLE ONLY relation_members DROP CONSTRAINT pk_relation_members;

-- add the new pk on the way_nodes that respects the way version
ALTER TABLE ONLY relation_members ADD CONSTRAINT pk_relation_members PRIMARY KEY (relation_id, relation_version, sequence_id);
