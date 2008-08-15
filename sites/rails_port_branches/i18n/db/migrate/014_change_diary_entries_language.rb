class ChangeDiaryEntriesLanguage < ActiveRecord::Migration
  def self.up
    change_column "diary_entries", "language", :string, :default => "en-US", :null => false
  end

  def self.down
    change_column "diary_entries", "language", :string, :limit => 3, :default => nil, :null => true
  end
end
