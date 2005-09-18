#!/usr/bin/ruby

require 'cgi'
require 'mysql'

cgi = CGI.new

email = cgi['email']
confirmstring = cgi['confirmstring']

puts '<html><body>'
puts 'Validating ' + email + '...<br>'

email =  email.upcase.match(/\b[A-Z0-9._%-]+@[A-Z0-9._%-]+\.[A-Z]{2,4}\b/)

if email
  puts 'email address looks reasonably formatted :-)<br>'
else
  puts 'Sorry, your email address does not look good, please go back and try again<br>'
  exit
end


confirmstring = Mysql.quote(confirmstring)

MYSQL_SERVER = '128.40.59.181'
MYSQL_USER = 'postcode'
MYSQL_PASS = 'postcode'
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

email = email.to_s.downcase

if connection
  sql = "select count(*) as count from codes where confirmed = false and email = '#{email}' and confirmstring = '#{confirmstring}' limit 1"
  res = connection.query(sql)
  
  res.each_hash do |row|
    if row['count'] == '1'
      sql = "update codes set confirmed = true where confirmed = false and email = '#{email}' and confirmstring = '#{confirmstring}'"
      connection.query(sql) 

      puts 'Thanks, we seem to have confirmed your submission!<br>'
    else
      puts 'Whoops, something went wrong confirming that submition<br>'
    end

  end
end


puts '</body></html>'
