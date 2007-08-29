require 'lib/migrate'

class RemoveSegments < ActiveRecord::Migration
  def self.up
    prefix = File.join Dir.tmpdir, "006_remove_segments.#{$$}."

    cmd = "db/migrate/006_remove_segments_helper"
    src = "#{cmd}.cc"
    if not File.exists? cmd or File.mtime(cmd) < File.mtime(src) then 
      system 'c++ -O3 -Wall `mysql_config --cflags --libs` ' +
	"#{src} -o #{cmd}" or fail
    end

    conn_opts = ActiveRecord::Base.connection.
      instance_eval { @connection_options }
    args = conn_opts.map { |arg| arg.to_s } + [prefix]
    fail "#{cmd} failed" unless system cmd, *args

    tempfiles = ['ways', 'way_nodes', 'way_tags',
      'relations', 'relation_members', 'relation_tags'].
      map { |base| prefix + base }
    ways, way_nodes, way_tags,
      relations, relation_members, relation_tags = tempfiles

    drop_table :segments
    drop_table :way_segments
    create_table :way_nodes, myisam_table do |t|
      t.column :id,          :bigint, :limit => 64, :null => false
      t.column :node_id,     :bigint, :limit => 64, :null => false
      t.column :version,     :bigint, :limit => 20, :null => false
      t.column :sequence_id, :bigint, :limit => 11, :null => false
    end
    add_primary_key :way_nodes, [:id, :version, :sequence_id]

    drop_table :current_segments
    drop_table :current_way_segments
    create_table :current_way_nodes, innodb_table do |t|
      t.column :id,          :bigint, :limit => 64, :null => false
      t.column :node_id,     :bigint, :limit => 64, :null => false
      t.column :sequence_id, :bigint, :limit => 11, :null => false
    end
    add_primary_key :current_way_nodes, [:id, :sequence_id]

    execute "DELETE FROM way_tags"
    execute "DELETE FROM ways"
    execute "DELETE FROM current_way_tags"
    execute "DELETE FROM current_ways"

    # now get the data back
    csvopts = "FIELDS TERMINATED BY ',' ENCLOSED BY '\"' ESCAPED BY '\"' LINES TERMINATED BY '\\n'"

    tempfiles.each { |fn| File.chmod 0644, fn }

    execute "LOAD DATA LOCAL INFILE '#{ways}' INTO TABLE ways #{csvopts} (id, user_id, timestamp) SET visible = 1, version = 1"
    execute "LOAD DATA LOCAL INFILE '#{way_nodes}' INTO TABLE way_nodes #{csvopts} (id, node_id, sequence_id) SET version = 1"
    execute "LOAD DATA LOCAL INFILE '#{way_tags}' INTO TABLE way_tags #{csvopts} (id, k, v) SET version = 1"

    execute "INSERT INTO current_ways SELECT id, user_id, timestamp, visible FROM ways"
    execute "INSERT INTO current_way_nodes SELECT id, node_id, sequence_id FROM way_nodes"
    execute "INSERT INTO current_way_tags SELECT id, k, v FROM way_tags"

    # and then readd the index
    add_index :current_way_nodes, [:node_id], :name => "current_way_nodes_node_idx"

    execute "LOAD DATA LOCAL INFILE '#{relations}' INTO TABLE relations #{csvopts} (id, user_id, timestamp) SET visible = 1, version = 1"
    execute "LOAD DATA LOCAL INFILE '#{relation_members}' INTO TABLE relation_members #{csvopts} (id, member_type, member_id, member_role) SET version = 1"
    execute "LOAD DATA LOCAL INFILE '#{relation_tags}' INTO TABLE relation_tags #{csvopts} (id, k, v) SET version = 1"

    # FIXME: This will only work if there were no relations before the
    # migration!
    execute "INSERT INTO current_relations SELECT id, user_id, timestamp, visible FROM relations"
    execute "INSERT INTO current_relation_members SELECT id, member_type, member_id, member_role FROM relation_members"
    execute "INSERT INTO current_relation_tags SELECT id, k, v FROM relation_tags"

    tempfiles.each { |fn| File.unlink fn }
  end

  def self.down
    raise IrreversibleMigration.new
  end
end
