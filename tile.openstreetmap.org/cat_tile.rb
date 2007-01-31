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
x = cgi['x']
y = cgi['y']
z = cgi['z']

if z and (z.to_i > 18 or z.to_i < 2)
  exit
end

fb = Foo::Bar.new

res = fb.call_local_sql { "select data, dirty, created_at from tiles where x = #{x} and y=#{y} and z=#{z} limit 1" }

if res.num_rows == 0
  fb.call_local_sql { "insert into tiles (x,y,z,dirty, created_at) values (#{x},#{y},#{z},1,NOW())" }
else
  res.each_hash do |row|
    puts row['data']
    if row['dirty'] == '0' and Time.parse(row['created_at']) < (Time.now - (60*60*24*7))
      fb.call_local_sql { "update tiles set dirty = 1 where x = #{x} and y=#{y} and z=#{z}" }
    end
  end
end

