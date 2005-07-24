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

    ## quote
    # escape characters in the string which might affect the
    # mysql query
    def quote(string)
      return Mysql.quote(string)
    end

    ## check_user?
    # returns whether the given username and password are
    # correct and active
    def check_user?(name, pass)
      dbh = get_connection
      # sanitise the incoming variables
      name = quote(name)
      pass = quote(pass)
      # get the result
      result = dbh.query("select uid, active from user where user='#{name}' and pass_crypt=md5('#{pass}')")
      # should only be one result, as user name is unique
      if result.num_rows == 1
        result.each_hash do |row|
          # make a token (compatibility -- remove this in future?)
          dao = Dao.new
          token = dao.create_token
          # update the database
          dbh.query("update user set timeout=#{dao.get_timeout}, token='#{token}' where uid=#{row['uid']}")
          # error check
          raise RuntimeError("Error updating database: '#{dbh.error}'") if dbh.errno != 0
          # everything is ok, so return success
          return true
        end
      end
      # otherwise, return false
      return false
    end

  end

end
