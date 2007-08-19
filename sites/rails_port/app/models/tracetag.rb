class Tracetag < ActiveRecord::Base
  set_table_name 'gpx_file_tags'

  validates_format_of :tag, :with => /^[^\/;.,?]*$/

  belongs_to :trace, :foreign_key => 'gpx_id'
end
