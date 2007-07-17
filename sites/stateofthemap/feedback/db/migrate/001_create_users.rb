class CreateUsers < ActiveRecord::Migration
  def self.up
  create_table "users", :force => true do |t|
    t.column "email", :string
    t.column "first_name", :string
    t.column "second_name", :string
    t.column "feedback_complete", :boolean, :default => false
    t.column "speaker", :boolean, :default => false
    t.column "token", :string
    t.column "created_at", :datetime
    t.column "updated_at", :datetime
    t.column "banned", :binary, :limit => 1, :null => false
  end
    end
  end

  def self.down
    drop_table :users
  end
