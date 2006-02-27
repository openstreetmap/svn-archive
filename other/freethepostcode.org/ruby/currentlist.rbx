#!/usr/bin/ruby

require 'mysql'

MYSQL_SERVER = '127.0.0.1'
MYSQL_USER = 'postcode'
MYSQL_PASS = 'kc8dFusmw'
MYSQL_DATABASE = 'postcode'

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

connection = get_connection

if connection
  sql = 'select * from codes where confirmed = 1 order by part1, part2'
  res = connection.query(sql)
  
  res.each_hash do |row|
    puts row['lat'] + ' ' + row['lon'] + ' ' + row['part1'] + ' ' + row['part2']
  end



end
