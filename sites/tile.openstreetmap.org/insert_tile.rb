#!/usr/bin/ruby

require 'mysql'


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
    if res.nil? then return true else return res end
  rescue MysqlError =>ex
    puts ex
  ensure
    dbh.close unless dbh.nil?
  end
  nil
end


x = ARGV[0]
y = ARGV[1]
z = ARGV[2]
filename = ARGV[3]

puts "tile #{x},#{y},#{z} size is #{File.size(filename)}"

if File.size(filename) == 158 # its blank
  call_local_sql { "delete from tiles where x=#{x} and y=#{y} and z=#{z};" }
else
  puts 'inserting tile'
  
  file = File.new(filename, "r")

  data = Mysql.quote(file.read)

  call_local_sql { "update tiles set data='#{data}', created_at=NOW(), dirty=false where x=#{x} and y=#{y} and z=#{z};" }
  puts `ls -l #{filename}`
end
`rm #{filename}`

