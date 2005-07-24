require 'singleton'
require 'osm/dao.rb'

module OSM

  MYSQL_HOST = 'localhost'
  MYSQL_USER = 'openstreetmap'
  MYSQL_PASS = 'openstreetmap'
  MYSQL_DB = 'openstreetmap'

  class DB

    include Singleton

    ## get_connection
    # grab the existing connection, unless one already exists
    def get_connection
      if @dbh
        return @dbh
      end
      return @dbh = Mysql.real_connect(MYSQL_HOST, MYSQL_USER,
                                       MYSQL_PASS, MYSQL_DB)
    end



  end

end
