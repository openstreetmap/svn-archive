module OSM

  require 'mysql'


  class Point

    def initialize(latitude, longitude, uid)
      @latitude = latitude
      @longitude = longitude
      @uid = uid
    end

    attr_reader :latitude, :longitude, :uid

  end # Point

  class Linesegment
    
    def initialize(uid, node_a_uid, node_b_uid)
      @uid = uid
      @node_a_uid = node_a_uid
      @node_b_uid = node_b_uid
    end

    def to_s
      "Linesegment #@uid between #@node_a_uid and #@node_b_uid"
    end

    attr_reader :uid, :node_a_uid, :node_b_uid

  end #Linesegment

  class Dao

    MYSQL_SERVER = "128.40.59.181"
    MYSQL_USER = "openstreetmap"
    MYSQL_PASS = "openstreetmap"
    MYSQL_DATABASE = "openstreetmap"
    TOKEN_LETTERS = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
    

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

    
    def sanitize(string, *rest)
      
      string.gsub!(';', '')
      string.gsub!('\'', '`')

      if rest
        rest.each do |s|
          s.gsub!(';', '')
          s.gsub!('\'', '`')
        end
      end
      
    end


    def create_token
      #FIXME make me nicer! get rid of TOKEN_LETTERS or something
    
      token = ''
      for i in 1..30
        token += TOKEN_LETTERS[ rand(TOKEN_LETTERS.length) ].chr
      end

      return token
    end

    def get_timeout
      # return ten minutes hence in milliseconds since last epoch
      # FIXME make the database use date columns and everything else too
      return (Time.now + (60 * 10)).to_i * 1000
    end

    def get_time_now
      return Time.now.to_i * 1000
    end


    def login(user, pass)
      
      sanitize(user, pass)

      begin

        conn = get_connection
        q = "select uid,active from user where user='#{user}' and pass_crypt=md5('#{pass}')"
        res = conn.query(q)

        if res.num_rows() == 1

          res.each_hash do |row|
            token = create_token
          
            q = "update user set timeout=#{get_timeout}, token='#{token}' where uid=#{row['uid']}"
            conn.query(q)

            return token

          end
        end

      rescue MysqlError => e
        mysql_error(e)
        
      ensure
        conn.close if conn
      end

      return 'ERROR'
    end

    
    def validate_token(token)

      sanitize(token)
    
      begin
        conn = get_connection
        q = "select uid from user where token='#{token}' and timeout > #{get_time_now}"

        res = conn.query(q)

        if res.num_rows() == 1
          res.each_hash do |row|
            token = create_token
            q = "update user set timeout=#{get_timeout} where uid=#{row['uid']}"
            conn.query(q)
            return true

          end
        end

      rescue MysqlError => e
        mysql_error(e)
        
      ensure
        conn.close if conn
      end

      return false
    end

    
    def getnodes(lat1, lon1, lat2, lon2)
      nodes = {}

      begin

        conn = get_connection

        q = "select uid, latitude, longitude from (select * from (select uid,latitude,longitude,timestamp,visible from nodes where latitude < #{lat1} and latitude > #{lat2}  and longitude > #{lon1} and longitude < #{lon2} order by timestamp desc) as a group by uid) as b where b.visible = 1 limit 5000"

        res = conn.query(q)
        
        res.each_hash do |row|
          uid = row['uid'].to_i
          nodes[uid] = Point.new(row['latitude'].to_f, row['longitude'].to_f, uid)
        end

        return nodes

      rescue MysqlError => e
        mysql_error(e)

      ensure
        conn.close if conn
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

        q = "SELECT segment.uid, segment.node_a, segment.node_b FROM (SELECT uid, node_a, node_b FROM street_segments WHERE visible = TRUE AND (node_a IN #{clausebuffer} OR node_b IN #{clausebuffer}) ORDER BY timestamp DESC) as segment"

        res = conn.query(q)
        
        segments = {}
        
        res.each_hash do |row|
          uid = row['uid'].to_i
          segments[uid] = Linesegment.new(uid, row['node_a'].to_i, row['node_b'].to_i)

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

        q = "select latitude,longitude from nodes where uid=#{uid} order by timestamp desc limit 1"

        res = conn.query(q)

        res.each_hash do |row|
          return Point.new(row['latitude'].to_f, row['longitude'].to_f, uid)

        end

      rescue MysqlError => e
        mysql_error(e)
      ensure
        conn.close if conn
      end

    end

  end
end
 
