class CreateNotes < ActiveRecord::Migration
  def self.up
    create_table "current_notes", innodb_table do |t|
      t.column "id",          :bigint, :limit => 64, :null => false
      t.column "latitude",    :double
      t.column "longitude",   :double
      t.column "user_id",     :bigint, :limit => 20
      t.column "visible",     :boolean
      t.column "timestamp",   :datetime
      t.column "date",        :datetime
      t.column "tile",        :integer
      t.column "summary",     :string
      t.column "description", :string
      t.column "email",       :string
      t.column "status",      :string
      t.column "trace",       :bigint, :limit => 64
      t.column "node",        :bigint, :limit => 64
    end

    add_primary_key "current_notes", ["id"] 
    add_index "current_notes", ["latitude", "longitude"], :name => "current_notes_lat_lon_idx"
    add_index "current_notes", ["tile"], :name => "current_notes_tile_idx"
    
    change_column "current_notes", "id", :bigint, :limit => 64, :null => false, :options => "AUTO_INCREMENT"
   
    create_table "notes", innodb_table do |t|
      t.column "id",          :bigint, :limit => 64, :null => false, :options => "AUTO_INCREMENT"
      t.column "latitude",    :double
      t.column "longitude",   :double
      t.column "user_id",     :bigint, :limit => 20
      t.column "visible",     :boolean
      t.column "timestamp",   :datetime
      t.column "tile",        :integer
      t.column "summary",     :string
      t.column "description", :string
      t.column "email",       :string
      t.column "status",      :string
      t.column "trace",       :bigint, :limit => 64
      t.column "node",        :bigint, :limit => 64
    end

    add_primary_key "notes", ["id"] 
    add_index "notes", ["tile"], :name => "notes_tile_idx"
  end

  def self.down
    drop_table :current_notes
    drop_table :notes
  end
end
