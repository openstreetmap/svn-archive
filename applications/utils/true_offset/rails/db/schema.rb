# This file is auto-generated from the current state of the database. Instead of editing this file, 
# please use the migrations feature of Active Record to incrementally modify your database, and
# then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your database schema. If you need
# to create the application database on another system, you should be using db:schema:load, not running
# all the migrations from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended to check this file into your version control system.

ActiveRecord::Schema.define(:version => 20101127211554) do

# Could not dump table "geography_columns" because of following StandardError
#   Unknown type 'name' for column 'f_table_catalog' /var/lib/gems/1.8/gems/postgis_adapter-0.7.8/lib/postgis_adapter/common_spatial_adapter.rb:52:in `table'/var/lib/gems/1.8/gems/postgis_adapter-0.7.8/lib/postgis_adapter/common_spatial_adapter.rb:50:in `each'/var/lib/gems/1.8/gems/postgis_adapter-0.7.8/lib/postgis_adapter/common_spatial_adapter.rb:50:in `table'/var/lib/gems/1.8/gems/activerecord-2.3.8/lib/active_record/schema_dumper.rb:72:in `tables'/var/lib/gems/1.8/gems/activerecord-2.3.8/lib/active_record/schema_dumper.rb:63:in `each'/var/lib/gems/1.8/gems/activerecord-2.3.8/lib/active_record/schema_dumper.rb:63:in `tables'/var/lib/gems/1.8/gems/activerecord-2.3.8/lib/active_record/schema_dumper.rb:25:in `dump'/var/lib/gems/1.8/gems/activerecord-2.3.8/lib/active_record/schema_dumper.rb:19:in `dump'/var/lib/gems/1.8/gems/rails-2.3.8/lib/tasks/databases.rake:256/var/lib/gems/1.8/gems/rails-2.3.8/lib/tasks/databases.rake:255:in `open'/var/lib/gems/1.8/gems/rails-2.3.8/lib/tasks/databases.rake:255/usr/lib/ruby/1.8/rake.rb:636:in `call'/usr/lib/ruby/1.8/rake.rb:636:in `execute'/usr/lib/ruby/1.8/rake.rb:631:in `each'/usr/lib/ruby/1.8/rake.rb:631:in `execute'/usr/lib/ruby/1.8/rake.rb:597:in `invoke_with_call_chain'/usr/lib/ruby/1.8/monitor.rb:242:in `synchronize'/usr/lib/ruby/1.8/rake.rb:590:in `invoke_with_call_chain'/usr/lib/ruby/1.8/rake.rb:583:in `invoke'/var/lib/gems/1.8/gems/rails-2.3.8/lib/tasks/databases.rake:113/usr/lib/ruby/1.8/rake.rb:636:in `call'/usr/lib/ruby/1.8/rake.rb:636:in `execute'/usr/lib/ruby/1.8/rake.rb:631:in `each'/usr/lib/ruby/1.8/rake.rb:631:in `execute'/usr/lib/ruby/1.8/rake.rb:597:in `invoke_with_call_chain'/usr/lib/ruby/1.8/monitor.rb:242:in `synchronize'/usr/lib/ruby/1.8/rake.rb:590:in `invoke_with_call_chain'/usr/lib/ruby/1.8/rake.rb:583:in `invoke'/usr/lib/ruby/1.8/rake.rb:2051:in `invoke_task'/usr/lib/ruby/1.8/rake.rb:2029:in `top_level'/usr/lib/ruby/1.8/rake.rb:2029:in `each'/usr/lib/ruby/1.8/rake.rb:2029:in `top_level'/usr/lib/ruby/1.8/rake.rb:2068:in `standard_exception_handling'/usr/lib/ruby/1.8/rake.rb:2023:in `top_level'/usr/lib/ruby/1.8/rake.rb:2001:in `run'/usr/lib/ruby/1.8/rake.rb:2068:in `standard_exception_handling'/usr/lib/ruby/1.8/rake.rb:1998:in `run'/usr/bin/rake:28

  create_table "offsets", :force => true do |t|
    t.column "name", :string
    t.column "provider", :string, :null => false
    t.column "zoom_min", :integer
    t.column "zoom_max", :integer
    t.column "offset_north", :decimal, :precision => 15, :scale => 10, :null => false
    t.column "offset_east", :decimal, :precision => 15, :scale => 10, :null => false
    t.column "created_at", :datetime
    t.column "updated_at", :datetime
    t.column "boundary", :line_string, :null => false
  end

end
