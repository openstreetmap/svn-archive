class WayNode < ActiveRecord::Base
  set_table_name 'current_way_nodes'

  set_primary_keys :id, :sequence_id
  belongs_to :node
end
