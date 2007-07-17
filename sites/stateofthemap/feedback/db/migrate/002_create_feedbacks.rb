class CreateFeedbacks < ActiveRecord::Migration
  def self.up
    create_table :feedbacks do |t|
      t.column :user_id, :integer, :limit => 20
      t.column :talk_id, :integer, :limit => 20
      t.column :description, :string
      t.column :score, :string
      t.column :comments, :text
    end
    add_index "feedbacks", ["user_id"], :name => "feedbacks_user_id_index"
  end

  def self.down
    drop_table :feedbacks
  end
end
