module OSM

  require 'mysql'
  require 'singleton'
  require 'time'
  require 'osm/servinfo.rb'
  load 'osm/osmlog.rb'
  
  class StringIO
  # helper class for gzip encoding
    attr_reader :buf
    def initialize
      @buf = ''
    end

    def write(s = '')
      unless (s.nil?)
        @buf << s
        s.length
      end
    end

    def length
      @buf.length
    end
    def to_s
      @buf
    end
  end

  class Point

    def initialize(latitude, longitude, id, visible, tags)
      @latitude, @longitude, @id, @visible, @tags = [latitude, longitude, id, visible, tags]
    end

    def to_s
      "Point #@id at #@latitude, #@longitude, #@visible"
    end

    attr_reader :latitude, :longitude, :id, :visible, :tags

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
      # FIXME this should inherit from UIDLinesegment or something
      def initialize(id, node_a, node_b, visible, tags)
        @id, @node_a, @node_b, @visible, @node_a_id, @node_b_id, @tags = [id, node_a, node_b, visible, node_a.id, node_b.id, tags]
      end

      def to_s
        "Linesegment #@id between #{@node_a.to_s} and #{@node_b.to_s}"
      end

      attr_reader :id, :visible, :node_a, :node_b, :node_a_id, :node_b_id, :tags
    end #Linesegment
 
 
 
  class UIDLinesegment
    # this is a holding class for just a segment and it's node UID's
    def initialize(id, node_a_id, node_b_id, tags)
      @id, @node_a_id, @node_b_id, @tags = [id, node_a_id, node_b_id, tags]
    end

    def to_s
      "UIDLinesegment #@id between #@node_a_id and #@node_b_id"
    end

    attr_reader :id, :node_a_id, :node_b_id, :tags

  end #UIDLinesegment



  class Dao
    include Singleton

    @@log = Osmlog.instance

    def mysql_error(e)
      puts "Error code: ", e.errno, "\n"
      puts "Error message: ", e.error, "\n"
      @@log.log("Error code: " + e.errno.to_s)
      @@log.log("Error message: "+ e.error)
    end

    def get_connection
      begin
        return Mysql.real_connect($DBSERVER, $USERNAME, $PASSWORD, $DATABASE)
      
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

    def q(s); quote(s); end

    
    ## check_user?
    # returns whether the given username and password are correct and active
    def check_user?(email, pass)
      @@log.log('checking user ' + email)

      res = call_sql { "select id from users where email='#{q(email)}' and pass_crypt=md5('#{q(pass)}') and active = true" }
      # should only be one result, as user name is unique
      if res.num_rows == 1
        set_timeout(email)
        return true
      end
      # otherwise, return false
      return false
    end
    

    def make_token
      chars = 'abcdefghijklmnopqrtuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'

      confirmstring = ''
      
      30.times do
        confirmstring += chars[(rand * chars.length).to_i].chr
      end

      return confirmstring
    end
    

  	def call_sql
	  	dbh = nil
		  begin
  			dbh = get_connection
        sql = yield
        @@log.log sql
	  		res = dbh.query(sql)
        if res.nil? then return true else return res end
  		rescue MysqlError =>ex
	  		mysql_error(ex)
		  ensure
  			dbh.close unless dbh.nil?
	  	end
		  nil
  	end


    def set_timeout(email)
	  	call_sql { "update users set timeout = NOW() + INTERVAL 1 DAY where email = '#{q(email)}' and active = true" }
  	end


    def logout(user_id)
      @@log.log("logging out user #{user_id}")
      call_sql { "update users set token = '#{make_token()}' where id = '#{user_id}' and active = true" }
    end


    def activate_account(email, token)
      email = quote(email)
      token = quote(token)
      
      begin
        dbh = get_connection

        dbh.query("update users set active = true where email = '#{email}' and token = '#{token}' and active = false")

        result = dbh.query("select * from users where email = '#{email}' and token = '#{token}' and active = true")

        return result.num_rows == 1
        

      rescue MysqlError => e
        mysql_error(e)

      ensure
        dbh.close if dbh
      end

      return false

    end


    def create_account(email, pass)
      @@log.log 'creating account for ' + email.to_s
      if !does_user_exist?(email)
        token = make_token
        res = call_sql { "insert into users(email, timeout, token, active, pass_crypt, creation_time) values ('#{q(email)}', NOW(), '#{q(token)}', false, md5('#{pass}'), NOW())" }
          
        if res.nil?
          return ''
        else
          return token
        end
      end
      return ''
    end


    def set_token_for_user(email)
      token = make_token
      call_sql { "update users set token = '#{token}' where email = '#{q(email)}' and active = true" }
      return token
    end 


    def login(email, pass)
      if check_user?(email,pass)
        token = make_token
        res = call_sql { "update users set token = '#{token}' where email = '#{q(email)}' and pass_crypt=md5('#{q(pass)}') and active = true" }
        
        if res.nil?
          return ''
        else
          set_timeout(email)
          return token
        end
      end
      return ''
    end


    def set_password_for_user(email, pass, token)
      return call_sql { "update users set pass_crypt = md5('#{q(pass)}') where email = '#{q(email)}' and token = '#{q(token)}' and active = true" }
    end


    def does_user_exist?(email)
      res = call_sql { "select id from users where email = '#{q(email)}' and active = true" }
      return res.num_rows == 1
    end
    

    ## check_user_token?
    # checks a user token to see if it is active
    def check_user_token?(token)
      res = call_sql { "select id from users where active = 1 and token = '#{q(token)}' and timeout > NOW()" }
      if res.num_rows == 1
        res.each_hash do |row|
          return row['id'].to_i
        end
      end
      # otherwise, return false
      return false
    end


    def set_cookie(r,token)
      ed = (Time.now + (60 * 60 * 24)).rfc2822()
	    r.headers_out.add('Set-Cookie', 'openstreetmap=' + token + '; path=/; expires=' + ed)
    end


    def check_cookie?(cgi)
      cvalues = cgi.cookies['openstreetmap']
        if !cvalues.empty?
      		token = cvalues[0]
  	    	if token
    		  	user_id = check_user_token?(token)
      		end
      	end
      return [token,user_id]
    end


    def email_from_token(token)
      res = call_sql { "select email from users where active = 1 and token = '#{q(token)}'" }
  
      if res.nil?
        return nil
      end
      
      if res.num_rows == 1
        res.each_hash do |row|
          return row['email']
        end
      end
    
    end


    def email_from_user_id(user_id)
      res = call_sql { "select email from users where active = 1 and id = #{user_id}" }
      
      if res.nil?
        return nil
      end
      
      if res.num_rows == 1
        res.each_hash do |row|
          return row['email']
        end
      end
    end
    

    def gpx_details_for_user(user_id)
      return call_sql { "select id, timestamp, name, size, latitude, longitude from gpx_files where user_id = #{q(user_id.to_s)} and visible = 1 order by timestamp desc" }
    end

    def gpx_pending_details_for_user(user_id)
      @@log.log('getting gpx files for user ' + user_id.to_s)
      return call_sql { "select originalname from gpx_pending_files where user_id = #{q(user_id.to_s)}" }
    end


    def gpx_size(gpx_id)
      res = call_sql { "select count(*) as count from gps_points where gpx_id = #{q(gpx_id.to_s)}" }
      res.each_hash do |row|
        return row['count']
      end
    end


    def does_user_own_gpx?(user_id, gpx_id)
      res = call_sql { "select id from gpx_files where user_id = #{q(user_id.to_s)} and id = #{q(gpx_id.to_s)} and visible = 1" }
      if res && res.num_rows ==1
        return true
      end
      false
    end


    def schedule_gpx_delete(gpx_id)
      call_sql { "update gpx_files set visible = false where id = #{q(gpx_id.to_s)}" }
    end


    def schedule_gpx_upload?(originalname, tmpname, user_id)
      call_sql { "insert into gpx_pending_files (originalname, tmpname, user_id) values ('#{q(originalname)}', '#{q(tmpname)}', #{user_id})" }
    end


    def get_scheduled_gpx_uploads()
      call_sql { "select * from gpx_pending_files" }
    end

    def delete_sheduled_gpx_files()
      begin
        dbh = get_connection

        dbh.query('delete from gpx_files, gps_points using gpx_files, gps_points where gpx_files.id = gps_points.gpx_id and gpx_files.visible = false')

        return true

      rescue MysqlError => e
        mysql_error(e)

      ensure
        dbh.close if dbh
      end

      return false

    end    


    def new_gpx_file(user_id, filename)
      
      begin
        dbh = get_connection
        dbh.query("insert into gpx_files (timestamp, user_id, visible, name) values (NOW(), #{q(user_id.to_s)}, 1, '#{q(filename.to_s)}')")
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

    def update_gpx_meta(gpx_id)
      call_sql { "update gpx_files set size = (select count(*) from gps_points where gps_points.gpx_id = #{gpx_id}) where id = #{gpx_id};" }
      call_sql { "update gpx_files set latitude = (select latitude from gps_points where gps_points.gpx_id = #{gpx_id} limit 1),
                    longitude = (select longitude from gps_points where gps_points.gpx_id = #{gpx_id} limit 1) where id = #{gpx_id};" }
    end
                    

    def useridfromcreds(email, pass)
      if email == 'token'
        id = check_user_token?(pass)
        if id
          return id
        else
          return -1
        end
      else
        useridfromemail(email)
      end
    end


    def useridfromemail(email)
      email = quote(email)
      
      begin
        dbh = get_connection
        res = dbh.query("select id from users where email='#{email}' and active = true")

        res.each_hash do |row|
          return row['id'].to_i
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

      res = call_sql { "select id, latitude, longitude, visible, tags from (select * from (select nodes.id, nodes.latitude, nodes.longitude, nodes.visible, nodes.tags from nodes, nodes as a where a.latitude > #{lat2}  and a.latitude < #{lat1}  and a.longitude > #{lon1} and a.longitude < #{lon2} and nodes.id = a.id order by nodes.timestamp desc) as b group by id) as c where visible = true and latitude > #{lat2}  and latitude < #{lat1}  and longitude > #{lon1} and longitude < #{lon2}" }

      if !res.nil? 
        res.each_hash do |row|
          
          node_id = row['id'].to_i
          nodes[node_id] = Point.new(row['latitude'].to_f, row['longitude'].to_f, node_id, true, row['tags'])
        end

        return nodes
      end

    end

    
    
    def getnodesbydate(lat1, lon1, lat2, lon2, date)
      nodes = {}

      res = call_sql { "select id, latitude, longitude, visible, tags from (select * from (select nodes.id, nodes.latitude, nodes.longitude, nodes.visible, nodes.tags from nodes, nodes as a where a.latitude > #{lat2}  and a.latitude < #{lat1}  and a.longitude > #{lon1} and a.longitude < #{lon2} and date < #{date.strftime('%Y-%m-%d %H:%M:%S')} and nodes.id = a.id order by nodes.timestamp desc) as b group by id) as c where visible = true and latitude > #{lat2}  and latitude < #{lat1}  and longitude > #{lon1} and longitude < #{lon2} and date < #{date.strftime('%Y-%m-%d %H:%M:%S')}" }

      if !res.nil? 
        res.each_hash do |row|
          
          node_id = row['id'].to_i
          nodes[node_id] = Point.new(row['latitude'].to_f, row['longitude'].to_f, node_id, true, row['tags'])
        end

        return nodes
      end

    end

    #{date.strftime('%Y-%m-%d %H:%M:%S')}

    
    def get_track_points(lat1, lon1, lat2, lon2, page)
      points = Array.new

      page = page * 5000

      begin

        dbh = get_connection

        
        q = "select distinctrow latitude, longitude from gps_points where latitude > #{lat1} and latitude < #{lat2} and longitude > #{lon1} and longitude < #{lon2} order by timestamp desc limit #{page}, 5000"

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


    def get_gpx_points(gpx_id)
      points = Array.new

      begin

        dbh = get_connection
        
        q = "select latitude, longitude from gps_points where gpx_id = #{gpx_id} limit 5000"

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

      nodes.each do |node_id, p|
        if !first
          clausebuffer += ',' + node_id.to_s
        else
          clausebuffer += node_id.to_s
          first = false
        end
        
      end
      clausebuffer += ')'

      begin
        conn = get_connection

        q = "SELECT segment.id, segment.node_a, segment.node_b, segment.tags FROM (
               select * from
                  (SELECT * FROM segments where node_a IN #{clausebuffer} OR node_b IN #{clausebuffer} ORDER BY timestamp DESC)
               as a group by id) as segment where visible = true"

        res = conn.query(q)
        
        segments = {}
        
        res.each_hash do |row|
          segment_id = row['id'].to_i
          segments[segment_id] = UIDLinesegment.new(segment_id, row['node_a'].to_i, row['node_b'].to_i, row['tags'])
        end

        return segments

      rescue MysqlError => e
        mysql_error(e)
      ensure
        conn.close if conn
      end

    end


    def getlinesbydate(nodes, date)
      clausebuffer = '('
      first = true

      nodes.each do |node_id, p|
        if !first
          clausebuffer += ',' + node_id.to_s
        else
          clausebuffer += node_id.to_s
          first = false
        end
        
      end
      clausebuffer += ')'

      begin
        conn = get_connection

        q = "SELECT segment.id, segment.node_a, segment.node_b, segment.tags FROM (
               select * from
                  (SELECT * FROM segments where node_a IN #{clausebuffer} OR node_b IN #{clausebuffer} ORDER BY timestamp DESC)
               as a group by id) as segment where visible = true and date < #{date.strftime('%Y-%m-%d %H:%M:%S')}"

        res = conn.query(q)
        
        segments = {}
        
        res.each_hash do |row|
          segment_id = row['id'].to_i
          segments[segment_id] = UIDLinesegment.new(segment_id, row['node_a'].to_i, row['node_b'].to_i, row['tags'])
        end

        return segments

      rescue MysqlError => e
        mysql_error(e)
      ensure
        conn.close if conn
      end

    end


    def getnode(node_id)
      res = call_sql {"select latitude, longitude, visible, tags from nodes where id=#{node_id} order by timestamp desc limit 1" }

      if !res.nil?
        res.each_hash do |row|
          visible = false

          if row['visible'] == '1' then visible = true end

          return Point.new(row['latitude'].to_f, row['longitude'].to_f, node_id, visible, row['tags'])
        end

        return nil

      end
    end
    

    def delete_node?(node_id, user_id)

      begin
        dbh = get_connection
        res = dbh.query("select latitude, longitude from nodes where id = #{node_id} order by timestamp desc limit 1")

        res.each_hash do |row|
          dbh.query("insert into nodes (id,latitude,longitude,timestamp,user_id,visible) values (#{node_id} , #{row['latitude']}, #{row['longitude']}, NOW(), #{user_id}, 0)")
          return true
        end

      rescue MysqlError => e
        mysql_error(e)

      ensure
        dbh.close if dbh
      end

      return false
    end 


    def update_node?(node_id, user_id, latitude, longitude, tags)
      call_sql { "insert into nodes (id,latitude,longitude,timestamp,user_id,visible,tags) values (#{node_id} , #{latitude}, #{longitude}, NOW(), #{user_id}, 1, '#{q(tags)}')" }
    end


    def create_node(lat, lon, user_id, tags)
      @@log.log("creating node at #{lat},#{lon} for user #{user_id} with tags '#{tags}'")
      begin
        dbh = get_connection

        dbh.query( "insert into meta_nodes (timestamp, user_id) values (NOW(), #{user_id})" )
      
        dbh.query( "insert into nodes (id, latitude, longitude, timestamp, user_id, visible, tags) values ( last_insert_id(), #{lat}, #{lon}, NOW(), #{user_id}, 1, '#{q(tags)}')" )
 
        res = dbh.query( "select last_insert_id() " )

        res.each do |row|
          @@log.log 'returning new node id ' + row[0].to_s
          return row[0]
        end

      rescue MysqlError =>ex
	  		mysql_error(ex)
		  ensure
  			dbh.close unless dbh.nil?
	  	end

      return -1
    end


    def create_segment(node_a_id, node_b_id, user_id, tags)
      @@log.log("Creating segment #{node_a_id} -> #{node_b_id} for user #{user_id} with tags '#{tags}'")
      begin
        dbh = get_connection

        sql = "insert into meta_segments (timestamp, user_id) values (NOW() , #{user_id})"
        dbh.query(sql)
        
        sql = "insert into segments (id, node_a, node_b, timestamp, user_id, visible, tags) values (last_insert_id(), #{node_a_id}, #{node_b_id}, NOW(), #{user_id},1, '#{q(tags)}')"
        dbh.query(sql)

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

    
    def update_segment?(segment_id, user_id, node_a, node_b, tags)
      call_sql { "insert into segments (id, node_a, node_b, timestamp, user_id, visible, tags) values (#{q(segment_id.to_s)}, #{q(node_a.to_s)}, #{q(node_b.to_s)}, NOW(), #{q(user_id.to_s)}, 1, '#{q(tags)}')" }
    end


    def getsegment(segment_id)
      res = call_sql { "select node_a, node_b, visible, tags from segments where id=#{segment_id} order by timestamp desc limit 1" }

      res.each_hash do |row|
        visible = false
        if row['visible'] == '1' then visible = true end
        return Linesegment.new(id, getnode(row['node_a'].to_i), getnode(row['node_b'].to_i), visible, row['tags'])
      end
      return nil
    end # getsegment


    def delete_segment?(segment_id, user_id)
      
      begin
        dbh = get_connection
        res = dbh.query("select node_a, node_b from segments where id = #{q(segment_id.to_s)} order by timestamp desc limit 1")

        res.each_hash do |row|
          dbh.query("insert into segments (id,node_a,node_b,timestamp,user_id,visible) values (#{q(segment_id.to_s)} , #{row['node_a']}, #{row['node_b']}, NOW(), #{user_id}, 0)")
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

