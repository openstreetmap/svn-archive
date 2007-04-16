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

res = fb.call_local_sql { "select dirty_t, created_at from tiles where x = #{x} and y=#{y} and z=#{z} limit 1" }

res.each_hash do |row|
  puts "tile created at #{row['created_at']}"
  puts "tile is awaiting re-render: #{row['dirty_t']}"
end

