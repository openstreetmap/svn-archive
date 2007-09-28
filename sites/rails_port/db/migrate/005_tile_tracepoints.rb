class TileTracepoints < ActiveRecord::Migration
  def self.up
    add_column "gps_points", "tile", :integer, :null => false, :unsigned => true
    add_index "gps_points", ["tile"], :name => "points_tile_idx"
    remove_index "gps_points", :name => "points_idx"

    begin
      Tracepoint.update_all("latitude = latitude * 10, longitude = longitude * 10, tile = tile_for_point(latitude * 10, longitude * 10)")
    rescue ActiveRecord::StatementInvalid => ex
      Tracepoint.find(:all).each do |tp|
        tp.latitude = tp.latitude * 10
        tp.longitude = tp.longitude * 10
        tp.save!
      end
    end
  end

  def self.down
    add_index "gps_points", ["latitude", "longitude"], :name => "points_idx"
    remove_index "gps_points", :name => "points_tile_idx"
    remove_column "gps_points", "tile"
  end
end
