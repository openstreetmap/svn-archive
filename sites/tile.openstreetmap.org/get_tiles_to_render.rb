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
    mysql_error(ex)
  ensure
    dbh.close unless dbh.nil?
  end
  nil
end

res = call_local_sql { "select x,y,z from tiles where dirty=true order by created_at asc limit 1000" }

res.each_hash do |row|
  puts "#{row['x']} #{row['y']} #{row['z']}"
end
