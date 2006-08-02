# delivers a single node by id
require "tools"

module OSM
  def node uri, queries, session
    uri =~ /node\/([0-9]*)/
    db = SQLite3::Database.new 'planet.db'
    cached_deliver session do
      db.execute "select * from data where uid='#{id_to_uid($1)}';" do |line|
        make_osm(line).to_xml
      end
    end
  end
end
