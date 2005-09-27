module OSM

  require 'mysql'
  require 'singleton'

  class Point

    def initialize(latitude, longitude, uid, visible)
      @latitude = latitude
      @longitude = longitude
      @uid = uid
      @visible = visible
    end

    def to_s
      "Point #@uid at #@latitude, #@longitude, #@visible"
    end

    attr_reader :latitude, :longitude, :uid, :visible

  end # Point

  class Trackpoint
  
    def initialize(latitude, longitude)
      @latitude = latitude
      @longitude = longitude
    end

    attr_reader :latitude, :longitude

  end

 
 class Linesegment
    # this is a holding class for holding a segment and its nodes
    def initialize(uid, node_a, node_b, visible)
      @uid = uid
      @node_a = node_a
      @node_b = node_b
      @visible = visible
    end

    def to_s
      "Linesegment #@uid between #{@node_a.to_s} and #{@node_b.to_s}"
    end

    attr_reader :uid, :visible, :node_a, :node_b

  end #Linesegment
 
 
  class UIDLinesegment
    # this is a holding class for just a segment and it's node UID's
    def initialize(uid, node_a_uid, node_b_uid)
      @uid = uid
      @node_a_uid = node_a_uid
      @node_b_uid = node_b_uid
    end

    def to_s
      "UIDLinesegment #@uid between #@node_a_uid and #@node_b_uid"
    end

    attr_reader :uid, :node_a_uid, :node_b_uid

  end #UIDLinesegment

  class Dao
    include Singleton

    MYSQL_SERVER = "128.40.59.181"
    MYSQL_USER = "openstreetmap"
    MYSQL_PASS = "openstreetmap"
    MYSQL_DATABASE = "openstreetmap"

    def mysql_error(e)
      print "Error code: ", e.errno, "\n"
      print "Error message: ", e.error, "\n"
    end


    def get_connection
      begin
        return Mysql.real_connect(MYSQL_SERVER, MYSQL_USER, MYSQL_PASS, MYSQL_DATABASE)
      
      rescue MysqlError => e
        mysql_error(e)

      end
      
    end

    ## quote
      # escape characters in the string which might affect the
    # mysql query
    def quote(string)
      return Mysql.quote(string)
    end

    
    ## check_user?
    # returns whether the given username and password are correct and active
    def check_user?(email, pass)
      dbh = get_connection
      # sanitise the incoming variables
      name = quote(email)
      pass = quote(pass)
      # get the result
      result = dbh.query("select uid from user where user='#{email}' and pass_crypt=md5('#{pass}') and active = true")
      # should only be one result, as user name is unique
      if result.num_rows == 1
        set_timeout(email)
        return true
      end
      # otherwise, return false
      return false
    end

    def make_token
      chars = 'abcdefghijklmnopqrtuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'

      confirmstring = ''
      
      for i in 1..30
        confirmstring += chars[(rand * chars.length).to_i].chr
      end

      return confirmstring
    end


    def set_timeout(email)
      email = quote(email)

      begin
        dbh = get_connection
        token = make_token

        dbh.query("update user set timeout = #{(Time.new.to_i * 1000) + (1000 * 60 * 60 * 24)} where user = '#{email}' and active = true")

        return token

      rescue MysqlError => e
        mysql_error(e)

      ensure
        dbh.close if dbh
      end
    end
 

    def logout(user_uid)
      begin
        dbh = get_connection
        token = make_token

        dbh.query("update user set token = '#{make_token()}' where uid = '#{user_uid}' and active = true")

      rescue MysqlError => e
        mysql_error(e)

      ensure
        dbh.close if dbh
      end
    end


    def activate_account(email, token)
      email = quote(email)
      token = quote(token)
      
      begin
        dbh = get_connection

        dbh.query("update user set active = true where user = '#{email}' and token = '#{token}' and active = false")

        result = dbh.query("select * from user where user = '#{email}' and token = '#{token}' and active = true")

        return result.num_rows == 1
        

      rescue MysqlError => e
        mysql_error(e)

      ensure
        dbh.close if dbh
      end

      return false

    end


    def create_account(email, pass)
      email = quote(email)
      pass = quote(pass)

      if !does_user_exist?(email)
        begin
          dbh = get_connection
          token = make_token
          sql = "insert into user(user, timeout, token, active, pass_crypt, creation_time) values ('#{email}', #{Time.new.to_i * 1000}, '#{token}', false, md5('#{pass}'), NOW())"
          dbh.query(sql)

          return token

        rescue MysqlError => e
          mysql_error(e)

        ensure
          dbh.close if dbh
        end


      end

      return ''

    end


    def login(email, pass)
      email = quote(email)
      pass = quote(pass)

      if check_user?(email,pass)
        begin
          dbh = get_connection
          token = make_token
          
          dbh.query("update user set token = '#{token}' where user = '#{email}' and pass_crypt=md5('#{pass}') and active = true")
          set_timeout(email)

          return token

        rescue MysqlError => e
          mysql_error(e)

        ensure
          dbh.close if dbh
        end


      end

      return ''

    end

    
    def does_user_exist?(email)
      email = quote(email)

      begin
        dbh = get_connection

        res = dbh.query("select uid from user where user = '#{email}' and active = true")
        return res.num_rows == 1

      rescue MysqlError => e
        mysql_error(e)
      end

      return false

    end
    

    ## check_user_token?
    # checks a user token to see if it is active
    def check_user_token?(token)
      dbh = get_connection
      token = quote(token)

      res = dbh.query("select uid from user where active = 1 and token = '#{token}' and timeout > #{Time.new.to_i * 1000}")
      if res.num_rows == 1
        res.each_hash do |row|
          return row['uid'].to_i
        end
      end
      # otherwise, return false
      return false
    end


    def email_from_user_uid(user_uid)
       begin
        dbh = get_connection

        res = dbh.query("select user from user where active = 1 and uid = #{user_uid}")
      
        if res.num_rows == 1
          res.each_hash do |row|
            return row['user']
          end
        end
        
      rescue MysqlError => e
        mysql_error(e)

      ensure
        dbh.close if dbh
      end
      return false
    end
    

    def gpx_details_for_user(user_uid)
  
      begin
        dbh = get_connection

        res = dbh.query("select uid, timestamp, name from points_meta_table where user_uid = #{user_uid} and visible = 1 order by timestamp desc")
      
        return res
      rescue MysqlError => e
        mysql_error(e)

      ensure
        dbh.close if dbh
      end
       
    end


    def does_user_own_gpx?(user_uid, gpx_uid)
      begin
        dbh = get_connection

        res = dbh.query("select uid from points_meta_table where user_uid = #{user_uid} and uid = #{gpx_uid} and visible = 1")
      
        return res.num_rows == 1

      rescue MysqlError => e
        mysql_error(e)

      ensure
        dbh.close if dbh
      end
      
      return false

    end


    def schedule_gpx_delete(gpx_uid)
      begin
        dbh = get_connection

        res = dbh.query("update points_meta_table set visible = false where uid = #{gpx_uid}")
        
      rescue MysqlError => e
        mysql_error(e)

      ensure
        dbh.close if dbh
      end

      return false

    end


    def schedule_gpx_upload?(originalname, tmpname, user_uid)
      originalname = quote(originalname)
      begin
        dbh = get_connection

        dbh.query("insert into gpx_to_insert (originalname, tmpname, user_uid) values ('#{originalname}', '#{tmpname}', #{user_uid})")

        return true
        
      rescue MysqlError => e
        mysql_error(e)

      ensure
        dbh.close if dbh
      end

      return false

    end


   def get_scheduled_gpx_uploads()
      begin
        dbh = get_connection

        return dbh.query('select * from gpx_to_insert')

      rescue MysqlError => e
        mysql_error(e)

      ensure
        dbh.close if dbh
      end

      return false

    end


    def delete_sheduled_gpx_files()
      begin
        dbh = get_connection

        dbh.query('delete from points_meta_table, tempPoints using points_meta_table, tempPoints where points_meta_table.uid = tempPoints.gpx_id and points_meta_table.visible = false')

        return true

      rescue MysqlError => e
        mysql_error(e)

      ensure
        dbh.close if dbh
      end

      return false

    end    


    def new_gpx_file(user_uid, filename)
      
      begin
        dbh = get_connection
        dbh.query("insert into points_meta_table (timestamp, user_uid, visible, name) values (#{Time.new.to_i * 1000}, #{user_uid}, 1, '#{filename}')")
        res = dbh.query('select last_insert_id()') 
        
        res.each do |row|
          return row[0]
        end

        return -1
       rescue MysqlError => e
        mysql_error(e)

      ensure
        dbh.close if dbh
      end
      return nil
    end

    def useruidfromcreds(user, pass)
      if user == 'token'
        uid = check_user_token?(pass)
        if uid
          return uid
        else
          return -1
        end
      else
        useruidfromemail(user)
      end
    end


    def useruidfromemail(email)
      email = quote(email)
      
      begin
        dbh = get_connection
        res = dbh.query("select uid from user where user='#{email}' and active = true")

        res.each_hash do |row|
          return row['uid'].to_i
        end

        return -1
       rescue MysqlError => e
        mysql_error(e)

      ensure
        dbh.close if dbh
      end
    end

    
    def getnodes(lat1, lon1, lat2, lon2)
      nodes = {}

      begin

        dbh = get_connection

#        q = "select uid, latitude, longitude, visible from (select * from (select uid,latitude,longitude,timestamp,visible from nodes where latitude < #{lat1} and latitude > #{lat2}  and longitude > #{lon1} and longitude < #{lon2} order by timestamp desc) as a group by uid) as b where b.visible = 1 limit 5000"

        q = "select uid, latitude, longitude, visible from (select * from (select nodes.uid, nodes.latitude, nodes.longitude, nodes.visible from nodes, nodes as a where a.latitude > #{lat2}  and a.latitude < #{lat1}  and a.longitude > #{lon1} and a.longitude < #{lon2} and nodes.uid = a.uid order by nodes.timestamp desc) as b group by uid) as c where visible = true and latitude > #{lat2}  and latitude < #{lat1}  and longitude > #{lon1} and longitude < #{lon2}"



        res = dbh.query(q)
        
        visible = true
        
        res.each_hash do |row|
          
          uid = row['uid'].to_i
          nodes[uid] = Point.new(row['latitude'].to_f, row['longitude'].to_f, uid, visible)
        end

        return nodes

      rescue MysqlError => e
        mysql_error(e)

      ensure
        dbh.close if dbh
      end

    end



    def get_track_points(lat1, lon1, lat2, lon2, page)
      points = Array.new

      page = page * 5000

      begin

        dbh = get_connection

        
        q = "select latitude, longitude from tempPoints where latitude > #{lat1} and latitude < #{lat2} and longitude > #{lon1} and longitude < #{lon2} order by timestamp desc limit #{page}, #{page + 5000}"

        res = dbh.query(q)
        
        res.each_hash do |row|
          points.push Trackpoint.new(row['latitude'].to_f, row['longitude'].to_f)
        end

        return points

      rescue MysqlError => e
        mysql_error(e)

      ensure
        dbh.close if dbh
      end

    end

    
    def getlines(nodes)
      clausebuffer = '('
      first = true

      nodes.each do |uid, p|
        if !first
          clausebuffer += ',' + uid.to_s
        else
          clausebuffer += uid.to_s
          first = false
        end
        
      end
      clausebuffer += ')'

      begin
        conn = get_connection

        q = "SELECT segment.uid, segment.node_a, segment.node_b FROM (
               select * from
                  (SELECT * FROM street_segments where node_a IN #{clausebuffer} OR node_b IN #{clausebuffer} ORDER BY timestamp DESC)
               as a group by uid) as segment where visible = true"

        res = conn.query(q)
        
        segments = {}
        
        res.each_hash do |row|
          uid = row['uid'].to_i
          segments[uid] = UIDLinesegment.new(uid, row['node_a'].to_i, row['node_b'].to_i)

        end

        return segments

      rescue MysqlError => e
        mysql_error(e)
      ensure
        conn.close if conn
      end

    end



    def getnode(uid)
      begin
        conn = get_connection

        q = "select latitude, longitude, visible from nodes where uid=#{uid} order by timestamp desc limit 1"

        res = conn.query(q)

        res.each_hash do |row|
          visible = false

          if row['visible'] == '1' then visible = true end

          return Point.new(row['latitude'].to_f, row['longitude'].to_f, uid, visible)
        end

        return nil

      rescue MysqlError => e
        mysql_error(e)
      ensure
        conn.close if conn
      end

    end

    def delete_node?(uid, user_uid)

      begin
        dbh = get_connection
        res = dbh.query("select latitude, longitude from nodes where uid = #{uid} order by timestamp desc limit 1")

        res.each_hash do |row|
          dbh.query("insert into nodes (uid,latitude,longitude,timestamp,user_uid,visible) values (#{uid} , #{row['latitude']}, #{row['longitude']}, #{Time.new.to_i * 1000}, #{user_uid}, 0)")
          return true
        end

      rescue MysqlError => e
        mysql_error(e)

      ensure
        dbh.close if dbh
      end

      return false
    end

    

    def update_node?(uid, user_uid, latitude, longitude)

      begin
        dbh = get_connection

        dbh.query("insert into nodes (uid,latitude,longitude,timestamp,user_uid,visible) values (#{uid} , #{latitude}, #{longitude}, #{Time.new.to_i * 1000}, #{user_uid}, 1)")

        return true

      rescue MysqlError => e
        mysql_error(e)

      ensure
        dbh.close if dbh
      end

      return false
    end


    def create_node(lat, lon, user_uid)
      begin
        dbh = get_connection

        sql = "insert into node_meta_table (timestamp, user_uid, visible) values (#{Time.new.to_i * 1000}, #{user_uid}, 1)"
        dbh.query(sql)

        sql = "set @id = last_insert_id(); "
        dbh.query(sql)
        
        sql = "insert into nodes (uid, latitude, longitude, timestamp, user_uid, visible) values ( last_insert_id(), #{lat}, #{lon}, #{Time.new.to_i * 1000}, #{user_uid}, 1)"
        dbh.query(sql)
        
        res = dbh.query('select @id') 
        
        res.each do |row|
          return row[0]
        end

        return -1
       rescue MysqlError => e
        mysql_error(e)

      ensure
        dbh.close if dbh
      end
      return nil
    end


    def create_segment(node_a_uid, node_b_uid, user_uid)
      begin
        dbh = get_connection

        sql = "insert into street_segment_meta_table (timestamp, user_uid, visible) values ( #{Time.new.to_i * 1000} , #{user_uid}, 1)"
        dbh.query(sql)
 
        sql = "set @id = last_insert_id(); "
        dbh.query(sql)
        
        sql = "insert into street_segments (uid, node_a, node_b, timestamp, user_uid, visible) values (last_insert_id(), #{node_a_uid}, #{node_b_uid}, #{Time.new.to_i * 1000}, #{user_uid},1)"
        dbh.query(sql)

        res = dbh.query('select @id') 
        
        res.each do |row|
          return row[0]
        end

        return -1
       rescue MysqlError => e
        mysql_error(e)

      ensure
        dbh.close if dbh
      end
      return nil
    end


    
    def update_segment?(uid, user_uid, node_a, node_b)

      begin
        dbh = get_connection

        dbh.query("insert into street_segments (uid, node_a, node_b, timestamp, user_uid, visible) values (#{uid} , #{node_a}, #{node_b}, #{Time.new.to_i * 1000}, #{user_uid}, 1)")

        return true

      rescue MysqlError => e
        mysql_error(e)

      ensure
        dbh.close if dbh
      end

      return false
    end


    def getsegment(uid)
      begin
        conn = get_connection

        q = "select node_a, node_b, visible from street_segments where uid=#{uid} order by timestamp desc limit 1"

        res = conn.query(q)

        res.each_hash do |row|

          visible = false

          if row['visible'] == '1' then visible = true end

          return Linesegment.new(uid, getnode(row['node_a'].to_i), getnode(row['node_b'].to_i), visible)
        end

        return nil

      rescue MysqlError => e
        mysql_error(e)
      ensure
        conn.close if conn
      end

    end # getsegment


    def delete_segment?(uid, user_uid)
      
      begin
        dbh = get_connection
        res = dbh.query("select node_a, node_b from street_segments where uid = #{uid} order by timestamp desc limit 1")

        res.each_hash do |row|
          dbh.query("insert into street_segments (uid,node_a,node_b,timestamp,user_uid,visible) values (#{uid} , #{row['node_a']}, #{row['node_b']}, #{Time.new.to_i * 1000}, #{user_uid}, 0)")
          return true
        end

      rescue MysqlError => e
        mysql_error(e)

      ensure
        dbh.close if dbh
      end

      return false
    end # deletesegment



  end
end
 
