require 'cgi'

cgi = CGI.new

module Foo
  require 'mysql'
  require 'date'
  require 'time'
  require 'singleton'

  class Bar
    def get_local_connection
      begin
        return Mysql.real_connect('localhost', 'tile', 'tile', 'tile')
      rescue MysqlError => e
        mysql_error(e)
      end
    end


    def call_local_sql
      dbh = nil
      begin
        dbh = get_local_connection
        sql = yield
        res = dbh.query(sql)
return res #        if res.nil? then return true else return res end
      rescue MysqlError =>ex
        puts ex
      ensure
        dbh.close unless dbh.nil?
      end
      nil
    end
  end
end

#first check number of accesses

include Apache
ip = Apache.request.connection.remote_ip
fb = Foo::Bar.new

res = fb.call_local_sql { "select hits from access where ip='#{ip}'" }

if res.num_rows == 0
  res = fb.call_local_sql { "insert into access (ip,hits) values('#{ip}',1) ON DUPLICATE KEY UPDATE hits=hits+1;" }
else
  hits = 0 
  res.each_hash do |row|
    hits = row['hits'].to_i
  end
  hits += 1
  res = fb.call_local_sql { "update access set hits=#{hits} where ip='#{ip}'" }
  if hits > 10_000
    puts `cat /home/www/tile/images/limit.png`
    exit
  end
end

#now send the tile
x = cgi['x']
y = cgi['y']
z = cgi['z']

if z and (z.to_i > 18 or z.to_i < 0)
  exit
end

# valid x/y for tiles are 0 ... 2^zoom-1
limit = (2 ** z.to_i) - 1

if x and (x.to_i < 0 or x.to_i > limit)
  puts `cat /home/www/tile/images/blank-000000.png`
  exit
end

if y and (y.to_i < 0 or y.to_i > limit)
  puts `cat /home/www/tile/images/blank-000000.png`
  exit
end


res = fb.call_local_sql { "select data, dirty_t, created_at from tiles where x = #{x} and y=#{y} and z=#{z} limit 1" }

if res.num_rows == 0
  fb.call_local_sql { "insert into tiles (x,y,z,dirty_t, created_at) values (#{x},#{y},#{z},'true',NOW())" }

  res = fb.call_local_sql { "select count(dirty_t) as dirty from tiles where dirty_t='true'" }
  res.each_hash do |row|
    if row['dirty'].to_i < 16
      render = IO.popen("/home/jburgess/osm/svn.openstreetmap.org/sites/tile.openstreetmap.org/render_from_list.py > /dev/null", "w+")
      render.puts "#{x} #{y} #{z}"
      render.close
    else
      exit
    end
  end
end

res = fb.call_local_sql { "select data, dirty_t, created_at from tiles where x = #{x} and y=#{y} and z=#{z} limit 1" }
if res.num_rows == 0
  exit
else
  res.each_hash do |row|
    puts row['data']
    if row['dirty_t'] == 'false' and Time.parse(row['created_at']) < (Time.now - (60*60*24*7))
      fb.call_local_sql { "update tiles set dirty_t = 'true' where x = #{x} and y=#{y} and z=#{z}" }
    end
  end
end

