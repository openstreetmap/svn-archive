require 'cgi'

cgi = CGI.new

module Foo
  require 'mysql'
  require 'date'
  require 'time'
  require 'singleton'

  class Bar
    def call_local_sql
      dbh = nil
      begin
        dbh = Mysql.real_connect('localhost', 'tile', 'tile', 'tile')
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

x = cgi['x'].to_i
y = cgi['y'].to_i
z = cgi['z'].to_i

if z and (z > 18 or z < 0)
  exit
end

# valid x/y for tiles are 0 ... 2^zoom-1
limit = (2 ** z) - 1

if x and (x < 0 or x > limit)
  print IO.read("/home/www/tile/images/blank-000000.png")
  exit
end

if y and (y < 0 or y > limit)
  print IO.read("/home/www/tile/images/blank-000000.png")
  exit
end

fb = Foo::Bar.new

res = fb.call_local_sql { "select data, dirty_t, created_at from tiles where x = #{x} and y=#{y} and z=#{z} limit 1" }
if res.nil?
  exit
end
if res.num_rows == 0
  fb.call_local_sql { "insert into tiles (x,y,z,dirty_t, created_at) values (#{x},#{y},#{z},'true',NOW())" }
  cgi.header("text/plain")
  print "Tile added to database and list of tiles to be rendered"
  exit
else
  res.each_hash do |row|
    if row['dirty_t'] == 'false'
      fb.call_local_sql { "update tiles set dirty_t = 'true' where x = #{x} and y=#{y} and z=#{z}" }
      cgi.header("text/plain")
      print "Tile added to rendering list"
      exit
    else
      cgi.header("text/plain")
      print "Tile was already on list to be rendered"
      exit
    end
  end
end
