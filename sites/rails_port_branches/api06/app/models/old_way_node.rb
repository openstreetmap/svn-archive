class OldWayNode < ActiveRecord::Base
  set_table_name 'way_nodes'

  set_primary_keys :id, :version, :sequence_id

  belongs_to :way, :foreign_key=> :id
end
