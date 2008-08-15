class CreateStatistics < ActiveRecord::Migration
  def self.up
    create_table "statistics", innodb_table do |t|
      t.column "id",            :bigint,        :limit => 11,   :null => false
      t.column "locale",        :string,                        :null => false
      t.column "language",      :string,                        :null => false
      t.column "country",       :string
      t.column "tr_complete",   :integer,                                       :default => 0
      t.column "tr_total",      :integer,                                       :default => 0
      t.column "tr_percentage", :float,                                         :default => 0,  :precision => 2
      t.column "timestamp",     :datetime
    end

    add_primary_key "statistics", ["id"]

    change_column "statistics", "id", :bigint, :limit => 11, :null => false, :options => "AUTO_INCREMENT"

    add_column "users", "tr_status", :integer, :default => 0, :null => false

  end

  def self.down
    drop_table :statistics
    remove_column "users", "tr_status"
  end
end
