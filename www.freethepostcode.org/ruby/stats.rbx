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

puts '<html>'

if connection
  sql = 'select count(*) as c from (select * from codes where confirmed = 1 group by part1, part2) as a;'
  res = connection.query(sql)
  res.each_hash do |row|
    puts 'total postcodes in system: ' +  row['c']
  end

  puts '<br><br>'
end

if connection
  sql = 'select email, count(*) as a from codes where confirmed = 1 group by email order by a desc limit 10;'
  res = connection.query(sql)
  puts '<table border="1">'
  res.each_hash do |row|
    puts '<tr><td>' +  row['email'].split('@')[0] + '</td><td align="right">' + row['a'] + "</td><td><img src='/redpixel.gif' width='#{row['a']}' height='10' /></td></tr>"
  end

  puts '</table><br><br>'



if connection
  sql = 'select part1, 10-count(*) as c from (select part1, left(part2, 1) as b  from codes where confirmed = 1 group by part1, b) as a group by part1 order by c, part1;'
  res = connection.query(sql)
  puts "To map postcodes exahustively, we should first get a point for each of the first char of the second part of the postcode (eg '0' for NW1 0AA). So something at NW1 0something, NW1 1somethinh, NW1 2something and so on. This gets the data reasonably accurate for many applications. The list below tells you how many we are missing for each postcode first part.<br><br>"
  puts '<table border="1"><tr><th>First part of<br />postcode</th><th>number of<br />missing codes</th></tr>'
  res.each_hash do |row|
    puts '<tr><td>' +  row['part1'] + '</td><td align="right">' + row['c'] + "</td><td><img src='/redpixel.gif' width='#{row['c'].to_i * 10}' height='10' /></td></tr>"
  end

  puts '</table><br><br></html>'

end


end

