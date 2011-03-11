-- Add the node_version column to the way_nodes table
ALTER TABLE ONLY way_nodes ADD COLUMN node_version int NOT NULL;

-- Add an index on node-id/version-id
CREATE INDEX way_nodes_node_version ON way_nodes USING btree (node_id, node_version);

-- TODO: here the member_version column needs to be added once the import supports member_version_builder
