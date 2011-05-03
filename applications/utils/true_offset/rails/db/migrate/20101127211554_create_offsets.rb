class CreateOffsets < ActiveRecord::Migration
  def self.up
    create_table :offsets do |t|
      t.string      :name
      t.string      :provider,     :null => false
      t.integer     :zoom_min
      t.integer     :zoom_max
      t.decimal     :offset_north, :null => false, :precision => 15, :scale => 10
      t.decimal     :offset_east,  :null => false, :precision => 15, :scale => 10
      t.line_string :boundary,     :null => false
      t.timestamps
    end
  end

  def self.down
    drop_table :offsets
  end
end
