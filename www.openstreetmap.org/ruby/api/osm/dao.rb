module OSM

  require 'mysql'
  require 'singleton'
  require 'time'
  require 'osm/servinfo.rb'
  require 'osm/osmlog.rb'

  class Mercator
    include Math

    def initialize(lat, lon, degrees_per_pixel, width, height)
      #init me with your centre lat/lon, the number of degrees per pixel and the size of your image
      @clat = lat
      @clon = lon
      @degrees_per_pixel = degrees_per_pixel
      @width = width
      @height = height
      @dlon = width / 2 * degrees_per_pixel
      @dlat = height / 2 * degrees_per_pixel  * cos(@clat * PI / 180)

      @tx = xsheet(@clon - @dlon)
      @ty = ysheet(@clat - @dlat)

      @bx = xsheet(@clon + @dlon)
      @by = ysheet(@clat + @dlat)

    end

    #the following two functions will give you the x/y on the entire sheet

    def kilometerinpixels
      return 40008.0  / 360.0 * @degrees_per_pixel
    end

    def ysheet(lat)
      log(tan(PI / 4 +  (lat  * PI / 180 / 2)))
    end

    def xsheet(lon)
      lon
    end

    #and these two will give you the right points on your image. all the constants can be reduced to speed things up. FIXME

    def y(lat)
      return @height - ((ysheet(lat) - @ty) / (@by - @ty) * @height)
    end

    def x(lon)
      return  ((xsheet(lon) - @tx) / (@bx - @tx) * @width)
    end
  end




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

    def initialize(latitude, longitude, id, visible, tags, timestamp=nil)
      @latitude, @longitude, @id, @visible, @tags, @timestamp = [latitude, longitude, id, visible, tags, timestamp]
    end

    def to_s
      "Point #@id at #@latitude, #@longitude, #@visible"
    end

    attr_reader :latitude, :longitude, :id, :visible, :tags, :timestamp

  end # Point


  class Street
    def initialize(id, tags, segs, visible, timestamp)
      @id, @tags, @segs, @visible, @timestamp = [id, tags, segs, visible, timestamp]
    end

    def to_s
      "Street #{@id} with visible=#{visible}, segs #{segs.to_s} and tags #{tags.to_s}"
    end

    attr_reader :id, :tags, :segs, :visible, :timestamp
  end # Street


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
    def initialize(id, node_a, node_b, visible, tags, timestamp=nil)
      @id, @node_a, @node_b, @visible, @node_a_id, @node_b_id, @tags, @timestamp = [id, node_a, node_b, visible, node_a.id, node_b.id, tags, timestamp]
    end

    def to_s
      "Linesegment #@id between #{@node_a.to_s} and #{@node_b.to_s}"
    end

    attr_reader :id, :visible, :node_a, :node_b, :node_a_id, :node_b_id, :tags, :timestamp
  end #Linesegment



  class UIDLinesegment
    # this is a holding class for just a segment and it's node UID's
    def initialize(id, node_a_id, node_b_id, tags, visible=false, timestamp=nil)
      @id, @node_a_id, @node_b_id, @tags, @visible, @timestamp = [id, node_a_id, node_b_id, tags, visible, timestamp]
    end

    def to_s
      "UIDLinesegment #@id between #@node_a_id and #@node_b_id"
    end

    attr_reader :id, :node_a_id, :node_b_id, :tags, :visible, :timestamp

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

    def get_local_connection
      return get_connection

      #whilst local db's are down, just talk to the main server
      #begin
      #  return Mysql.real_connect('localhost', $USERNAME, $PASSWORD, $DATABASE)
      #rescue MysqlError => e
      #  mysql_error(e)
      #end
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

      res = call_local_sql { "select id from users where email='#{q(email)}' and pass_crypt=md5('#{q(pass)}') and active = true" }
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

    def call_local_sql  # FIXME this should be wrapped up with the other call_sql
      dbh = nil
      begin
        dbh = get_local_connection
        sql = yield
        #@@log.log sql
        res = dbh.query(sql)
        if res.nil? then return true else return res end
      rescue MysqlError =>ex
        mysql_error(ex)
      ensure
        dbh.close unless dbh.nil?
      end
      nil
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
      res = call_local_sql { "select id from users where email = '#{q(email)}' and active = true" }
      return res.num_rows == 1
    end


    ## check_user_token?
    # checks a user token to see if it is active
    def check_user_token?(token)
      res = call_local_sql { "select id from users where active = 1 and token = '#{q(token)}' and timeout > NOW()" }
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
      r.headers_out.add('Set-Cookie', 'openwaymap=' + token + '; path=/; expires=' + ed)
    end


    def check_cookie?(cgi)
      cvalues = cgi.cookies['openwaymap']
      if !cvalues.empty?
        token = cvalues[0]
        if token
          user_id = check_user_token?(token)
        end
      end
      return [token,user_id]
    end


    def email_from_token(token)
      res = call_local_sql { "select email from users where active = 1 and token = '#{q(token)}'" }

      if res.nil?
        return nil
      end

      if res.num_rows == 1
        res.each_hash do |row|
          return row['email']
        end
      end

    end


    def save_display_name(user_id, display_name)
      res = call_local_sql { "select id from users where display_name = '#{q(display_name)}'" }
      return false if res.num_rows > 0
      call_sql {"update users set display_name = '#{q(display_name)}' where id = #{user_id}"}
      return true
    end


    def details_from_user_id(user_id)
      res = call_sql { "select email, display_name from users where active = 1 and id = #{user_id}" }

      if res.nil?
        return nil
      end

      if res.num_rows == 1
        res.each_hash do |row|
          return {'display_name' => row['display_name'], 'email' => row['email'] }
        end
      end
    end

    def details_from_email(email)
      res = call_local_sql { "select email, display_name from users where active = 1 and email = '#{email}'" }

      if res.nil?
        return nil
      end

      if res.num_rows == 1
        res.each_hash do |row|
          return {'display_name' => row['display_name'], 'email' => row['email'] }
        end
      end
    end


    def gpx_ids_for_user(user_id)
      return call_local_sql { "select id from gpx_files where user_id = #{q(user_id.to_s)}" }
    end

    def gpx_files(bpublic, display_name, tag, user_id, page=0, limit=false)
      clause = ''
      clause += " and private = 0 and users.display_name != '' " if bpublic==true
      clause += " and user_id = #{q(user_id.to_s)} " if user_id.to_i != 0
      clause += " and gpx_files.user_id in (select id from users where display_name='#{q(display_name)}') " if display_name != ''
      clause += " and gpx_files.id in (select gpx_id from gpx_file_tags where tag='#{q(tag)}') " if tag != ''

      limit = ''
      limit = ' limit 20 ' if limit==true

      return call_local_sql { "
        select * from (
        select gpx_files.inserted, gpx_files.id, gpx_files.timestamp, gpx_files.name, gpx_files.size, gpx_files.latitude, gpx_files.longitude, gpx_files.private, gpx_files.description, users.display_name from gpx_files, users where visible = 1 and gpx_files.user_id = users.id #{clause} order by timestamp desc) as a left join (select gpx_id,group_concat(tag SEPARATOR ' ') as tags from gpx_file_tags group by gpx_id) as t  on a.id=t.gpx_id #{limit}" }

    end

    def gpx_get(user_id, gpx_id)
      return call_local_sql { "select id, timestamp, name, size, latitude, longitude, private, description from gpx_files where user_id = #{q(user_id.to_s)} and id = #{q(gpx_id.to_s)} and visible = 1" }
    end

    def gpx_public_get(gpx_id)
      return call_local_sql { "select users.display_name, gpx_files.id, gpx_files.timestamp, gpx_files.name, gpx_files.size, gpx_files.latitude, gpx_files.longitude, gpx_files.private, gpx_files.description from gpx_files, users  where gpx_files.id = #{q(gpx_id.to_s)} and gpx_files.visible = 1 and gpx_files.private = 0 and gpx_files.user_id = users.id" }
    end

    def gpx_tags(gpx_id)
      res = call_local_sql { "select tag from gpx_file_tags where gpx_id = #{q(gpx_id.to_s)} order by sequence_id asc" }
      tags = []
      res.each { |tag| tags << tag[0] }
      return tags
    end

    def gpx_user_tags(user_id)
      return call_local_sql { "select distinct tag from gpx_file_tags where gpx_id in (select id from gpx_files where user_id = #{q(user_id.to_s)} and visible=1) order by tag;" }
    end

    def gpx_update_desc(gpx_id, description='', tags=[])
      call_sql { "update gpx_files set description = '#{q(description)}' where id = #{q(gpx_id.to_s)}" }
      call_sql { "delete from gpx_file_tags where gpx_id = #{q(gpx_id.to_s)}" }

      tags.split.each do |tag|
        call_sql { "insert into gpx_file_tags (gpx_id, tag) values (#{q(gpx_id.to_s)}, '#{q(tag.to_s)}')" }
      end
    end

    def gpx_pending_details_for_user(user_id)
      @@log.log('getting gpx files for user ' + user_id.to_s)
      return call_local_sql { "select originalname from gpx_pending_files where user_id = #{q(user_id.to_s)}" }
    end

    def gpx_set_private(gpx_id, private=false)
      call_sql { "update gpx_files set private=#{q(private.to_s)} where id=#{q(gpx_id.to_s)}" }
    end

    def gpx_size(gpx_id)
      res = call_local_sql { "select count(*) as count from gps_points where gpx_id = #{q(gpx_id.to_s)}" }
      res.each_hash do |row|
        return row['count']
      end
    end


    def does_user_own_gpx?(user_id, gpx_id)
      res = call_local_sql { "select id from gpx_files where user_id = #{q(user_id.to_s)} and id = #{q(gpx_id.to_s)} and visible = 1" }
      return res && res.num_rows == 1
    end

    def gpx_public?(gpx_id)
      res = call_local_sql { "select id from gpx_files where id = #{q(gpx_id.to_s)} and private = 0 and visible = 1" }
      return res && res.num_rows == 1
    end



    def schedule_gpx_delete(gpx_id)
      call_sql { "update gpx_files set visible = false where id = #{q(gpx_id.to_s)}" }
    end


    def schedule_gpx_upload(originalname, tmpname, user_id, description, tags, pub)
      begin
        dbh = get_connection

        dbh.query( "insert into gpx_files (name, tmpname, user_id, description, visible, inserted, timestamp, size, private) values ('#{q(originalname)}', '#{q(tmpname)}', #{user_id}, '#{q(description)}', 1, 0, NOW(), 0, #{!pub})")
        res = dbh.query( "select last_insert_id()")

        gpx_id = -1

        res.each do |row|
          gpx_id = row[0].to_i
        end

        gpx_update_desc(gpx_id, description, tags) unless gpx_id == 0

      rescue MysqlError => e
        mysql_error(e)

      ensure
        dbh.close if dbh
      end

    end


    def get_scheduled_gpx_uploads()
      call_local_sql { "select * from gpx_files where inserted = 0" }
    end

    def delete_sheduled_gpx_files()
      call_sql { 'delete from gpx_files, gps_points using gpx_files, gps_points where gpx_files.id = gps_points.gpx_id and gpx_files.visible = false' }
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

      res = call_local_sql { "select id from users where email='#{email}' and active = true" }

      res.each_hash do |row|
        return row['id'].to_i
      end

      return -1
    end


    def getnodes(lat1, lon1, lat2, lon2, to=nil)
      nodes = {}

      timeclause = get_time_clause(nil, to).gsub('timestamp','nodes.timestamp')

      res = call_sql { "select id, timestamp, latitude, longitude, visible, tags from current_nodes where latitude > #{lat2}  and longitude > #{lon1} and latitude < #{lat1} and longitude < #{lon2} and visible = 1" }

      if !res.nil?
        res.each_hash do |row|

          node_id = row['id'].to_i
          nodes[node_id] = Point.new(row['latitude'].to_f, row['longitude'].to_f, node_id, true, row['tags'], row['timestamp'])
        end

        return nodes
      end

    end


    def get_nodes_by_ids(node_ids, to=nil)
      nodes = {}
      timeclause = get_time_clause(nil, to)

      res = call_sql { "select id, latitude, longitude, visible, tags, timestamp from current_nodes where id in (#{node_ids.join(',')}) #{timeclause}" }

      if !res.nil?
        res.each_hash do |row|
          node_id = row['id'].to_i
          vis = '1' == row['visible']
          nodes[node_id] = Point.new(row['latitude'].to_f, row['longitude'].to_f, node_id, vis, row['tags'], row['timestamp'])
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

      res = call_local_sql { "select distinctrow latitude, longitude from gps_points where latitude > #{lat1} and latitude < #{lat2} and longitude > #{lon1} and longitude < #{lon2} order by timestamp desc limit #{page}, 5000" }

      return nil unless res

      res.each_hash do |row|
        points.push Trackpoint.new(row['latitude'].to_f, row['longitude'].to_f)
      end
      return points
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



    def get_segments_from_nodes(nodes, to=nil)
      timeclause = get_time_clause(nil, to)
      ids = nodes.collect { |id, node| id }
      cbuff = "(#{ids.join(',')})"

      res = call_sql {"SELECT id, node_a, node_b, tags FROM current_segments where (node_a IN #{cbuff} OR node_b IN #{cbuff}) #{timeclause} and visible = true"}

      segments = {}

      res.each_hash do |row|
        segment_id = row['id'].to_i
        segments[segment_id] = UIDLinesegment.new(segment_id, row['node_a'].to_i, row['node_b'].to_i, row['tags'])
      end

      return segments
    end


    def getlinesbydate(nodes, date)
      clausebuffer = "("
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
      res = call_sql {"select latitude, longitude, visible, tags from current_nodes where id=#{node_id}" }

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
        res = dbh.query("select latitude, longitude from current_nodes where id = #{node_id}")

        res.each_hash do |row|
          dbh.query("set @now = NOW()")
          dbh.query("insert into nodes (id,latitude,longitude,timestamp,user_id,visible) values (#{node_id} , #{row['latitude']}, #{row['longitude']}, @now, #{user_id}, 0)")
          dbh.query("update current_nodes set latitude = #{row['latitude']}, longitude =  #{row['longitude']}, timestamp = @now , user_id = #{user_id}, visible = 0 where id = #{node_id}")
        end

      rescue MysqlError => e
        mysql_error(e)
        return false
      ensure
        dbh.close if dbh
      end
      return true
    end


    def update_node?(node_id, user_id, latitude, longitude, tags)
      begin
        dbh = get_connection
        dbh.query("set @now = NOW()")
        dbh.query("insert into nodes (id,latitude,longitude,timestamp,user_id,visible,tags) values (#{node_id} , #{latitude}, #{longitude}, NOW(), #{user_id}, 1, '#{q(tags)}')" )
        dbh.query("update current_nodes set latitude = #{latitude}, longitude =  #{longitude}, timestamp = @now , user_id = #{user_id}, tags = '#{q(tags)}', visible =  1 where id = #{node_id}")
      rescue MysqlError => e
        mysql_error(e)
        return false
      ensure
        dbh.close if dbh
      end
      return true
    end


    def create_node(lat, lon, user_id, tags)
      #@@log.log("creating node at #{lat},#{lon} for user #{user_id} with tags '#{tags}'")
      begin
        dbh = get_connection
        dbh.query("set @now = NOW()")
        dbh.query( "insert into meta_nodes (timestamp, user_id) values (@now, #{user_id})" )
        dbh.query( "insert into nodes (id, latitude, longitude, timestamp, user_id, visible, tags) values ( last_insert_id(), #{lat}, #{lon}, @now, #{user_id}, 1, '#{q(tags)}')" )
        dbh.query( "insert into current_nodes (id, latitude, longitude, timestamp, user_id, visible, tags) values ( last_insert_id(), #{lat}, #{lon}, @now, #{user_id}, 1, '#{q(tags)}')" )
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

        dbh.query( "insert into meta_segments (timestamp, user_id) values (NOW() , #{user_id})")
        dbh.query("set @ins_time = NOW();" )
        dbh.query( "insert into segments (id, node_a, node_b, timestamp, user_id, visible, tags) values (last_insert_id(), #{node_a_id}, #{node_b_id}, @ins_time, #{user_id},1, '#{q(tags)}')")
        dbh.query( "insert into current_segments (id, node_a, node_b, timestamp, user_id, visible, tags) values (last_insert_id(), #{node_a_id}, #{node_b_id}, @ins_time, #{user_id},1, '#{q(tags)}')")
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
      begin
        dbh = get_connection
        dbh.query("set @ins_time = NOW();" )
        dbh.query( "insert into segments (id, node_a, node_b, timestamp, user_id, visible, tags) values (#{q(segment_id.to_s)}, #{q(node_a.to_s)}, #{q(node_b.to_s)}, @ins_time, #{q(user_id.to_s)}, 1, '#{q(tags)}')" )
        dbh.query( "update current_segments set node_a = #{q(node_a.to_s)}, node_b = #{q(node_b.to_s)}, timestamp = @ins_time, user_id = #{user_id}, visible = 1, tags='#{q(tags)}' where id = #{q(segment_id.to_s)}" )

        return true


      rescue MysqlError => e
        mysql_error(e)

      ensure
        dbh.close if dbh
      end

      return false
    end # update_segment?


    def getsegment(segment_id)
      res = call_sql { "select node_a, node_b, visible, tags, timestamp from current_segments where id=#{segment_id}" }

      res.each_hash do |row|
        visible = false
        if row['visible'] == '1' then visible = true end
        return Linesegment.new(segment_id, getnode(row['node_a'].to_i), getnode(row['node_b'].to_i), visible, row['tags'], row['timestamp'])
      end
      return nil
    end # getsegment


    def delete_segment?(segment_id, user_id)
      begin
        dbh = get_connection
        res = dbh.query("select node_a, node_b from current_segments where id = #{q(segment_id.to_s)} and visible = 1")

        res.each_hash do |row|
          dbh.query("set @ins_time = NOW();" )
          dbh.query("insert into segments (id,node_a,node_b,timestamp,user_id,visible) values (#{q(segment_id.to_s)} , #{row['node_a']}, #{row['node_b']}, @ins_time, #{user_id}, 0)")
          dbh.query("update current_segments set node_a = #{row['node_a']}, node_b = #{row['node_b']}, timestamp = @ins_time, user_id = #{user_id}, visible = 0 where id = #{q(segment_id.to_s)}")
          return true
        end

      rescue MysqlError => e
        mysql_error(e)

      ensure
        dbh.close if dbh
      end

      return false
    end # deletesegment


    def get_multi(multi_id, type=:way)
      res = call_sql { "select visible, timestamp from current_#{type}s where id = #{q(multi_id.to_s)};"}

      return nil if res.num_rows == 0

      visible = true
      timestamp = ''

      res.each_hash do |row|
        timestamp = row['timestamp']
        visible = false unless row['visible'] == '1'
      end

      res = call_sql { "select k,v from current_#{type}_tags where id = #{q(multi_id.to_s)};" }

      tags = []
      res.each_hash do |row|
        tags << [row['k'],row['v']]
      end

      res = call_sql { "select segment_id from current_#{type}_segments, current_segments where current_#{type}_segments.id = #{q(multi_id.to_s)} and current_#{type}_segments.segment_id = current_segments.id and current_segments.visible = 1 order by sequence_id;" }

      segs = []
      res.each_hash do |row|
        segs << [row['segment_id'].to_i]
      end

      return Street.new(multi_id, tags, segs, visible, timestamp)
    end # get_multi


    def update_multi(user_id, tags, segs, type=:way, new=false, multi_id=0)
      begin
        dbh = get_connection
        dbh.query( "set @ins_time = NOW();" )
        dbh.query( "set @user_id = #{q(user_id.to_s)};" )

        # get version number

        if new
          dbh.query( "insert into meta_#{type}s (user_id, timestamp) values (@user_id, @ins_time)" )
          dbh.query( "set @id = last_insert_id() ")
        else
          return nil unless get_multi(multi_id, type)
          dbh.query("set @id = #{q(multi_id.to_s)}")
        end

        # update master
        dbh.query( "insert into #{type}s (id, user_id, timestamp,visible) values (@id, @user_id, @ins_time,1)" )
        dbh.query( "insert into current_#{type}s (id, user_id, timestamp, visible) values (@id, @user_id, @ins_time, 1)" ) if new
        dbh.query( "set @version = last_insert_id()")

        # update tags
        unless tags.empty?
          tags_sql = "insert into #{type}_tags(id, k, v, version) values "
          current_tags_sql = "insert into current_#{type}_tags(id, k, v) values "
          first = true
          tags.each do |k,v|
            tags_sql += ',' unless first
            current_tags_sql += ',' unless first
            first = false unless !first
            tags_sql += "(@id, '#{q(k.to_s)}', '#{q(v.to_s)}', @version)"
            current_tags_sql += "(@id, '#{q(k.to_s)}', '#{q(v.to_s)}')"
          end

          @@log.log(tags_sql)
          dbh.query(tags_sql)
          @@log.log("delete from current_way_tags where id = @id")
          dbh.query("delete from current_way_tags where id = @id")
          @@log.log(current_tags_sql)
          dbh.query(current_tags_sql)
        end

        # update segments
        segs_sql = "insert into #{type}_segments (id, segment_id, version) values "
        current_segs_sql = "insert into current_#{type}_segments (id, segment_id, sequence_id) values "

        co = 1
        first = true
        segs.each do |n|
          segs_sql += ',' unless first
          current_segs_sql += ',' unless first
          first = false unless !first
          segs_sql += "(@id, #{q(n.to_s)}, @version)"
          current_segs_sql += "(@id, #{q(n.to_s)}, #{co})"
          co += 1
        end

        dbh.query( segs_sql )
        dbh.query("delete from current_way_segments where id = @id")
        dbh.query( current_segs_sql )

        res = dbh.query( "select @id as id" )

        res.each_hash do |row|
          return row['id'].to_i
        end
      rescue MysqlError =>ex
        mysql_error(ex)
      ensure
        dbh.close unless dbh.nil?
      end

      return nil

    end

    def new_multi(user_id, tags, segs, type=:way)
      return update_multi(user_id, tags, segs, type, true)
    end

    def delete_multi(multi_id, user_id, type=:way)
      begin
        dbh = get_connection
        multi = get_multi(multi_id, type)

        if multi
          dbh.query('set @now = NOW()')
          dbh.query("insert into #{type}s (id, user_id, timestamp, visible) values (#{q(multi_id.to_s)}, #{q(user_id.to_s)},@now,0)")
          dbh.query("update current_#{type}s set user_id = #{q(user_id.to_s)}, timestamp = @now, visible = 0 where id = #{q(multi_id.to_s)}")
          dbh.query("delete from current_#{type}_segments where id = #{q(multi_id.to_s)}")
          dbh.query("delete from current_#{type}_tags where id = #{q(multi_id.to_s)}")
          return true
        end
      rescue MysqlError =>ex
        mysql_error(ex)
      ensure
        dbh.close unless dbh.nil?
      end

      return nil

    end

    def get_node_history(node_id, from=nil, to=nil)
      res = call_sql { "select latitude as lat, longitude as lon, visible, tags, timestamp from nodes where id = #{node_id} " + get_time_clause(from, to) }
      history = []
      res.each_hash do |row|
        visible = '1' == row['visible']
        history << Point.new( row['lat'], row['lon'], node_id, visible, row['tags'], row['timestamp'] )
      end
      return history
    end

    def get_segment_history(segment_id, from=nil, to=nil)

      res = call_sql { "select node_a, node_b, visible, tags, timestamp from segments where id = #{segment_id} " + get_time_clause(from,to) }
      history = []
      res.each_hash do |row|
        visible = '1' == row['visible']
        history << UIDLinesegment.new(segment_id, row['node_a'], row['node_b'], row['tags'], visible, row['timestamp'] )
      end
      return history
    end

    def get_multi_history(multi_id, type=:way, from=nil, to=nil)
      res = call_sql { "select version from #{type}s where id = #{multi_id} " + get_time_clause(from,to) }
      history = []
      res.each_hash do |row|
        visible = '1' == row['visible']
        history << get_multi(multi_id, type, row['version'].to_i)
      end
      return history
    end

    def get_time_clause(from, to)
      clause = ''
      clause += " and timestamp > '#{from.strftime('%Y-%m-%d %H:%M:%S')}' " unless from.nil?
      clause += " and timestamp < '#{  to.strftime('%Y-%m-%d %H:%M:%S')}' " unless to.nil?
      return clause
    end

    def get_multis_from_segments(segment_ids, type=:way)

      id_list = segment_ids.join(',')

      ress = call_sql { "select id from current_way_segments where segment_id in (#{id_list}) group by id" }

      multis = []

      ress.each_hash do |row|
        multis << get_multi(row['id'].to_i)
      end

      return multis
    end

    def commify(number)
      c = { :value => "", :length => 0 }
      r = number.to_s.reverse.split("").inject(c) do |t, e|
        iv, il = t[:value], t[:length]
        iv += ',' if il % 3 == 0 && il != 0
        { :value => iv + e, :length => il + 1 }
      end
      r[:value].reverse!
    end
  end
end
