#!/usr/bin/ruby

require 'cgi'
require 'net/smtp'
require 'mysql'
include Math

cgi = CGI.new

email  = cgi['email']
lat  = cgi['lat'].to_f
lon  = cgi['lon'].to_f
postcode1 = cgi['postcode1'].upcase
postcode2 = cgi['postcode2'].upcase

puts '<html><body>'
puts 'Got this data: ' + email + ' says that ' + postcode1 + ' ' + postcode2 + ' -> ' + lat.to_s + ',' + lon.to_s + '<br>'

# roughly in a bounding box in the UK check:
if lat > 59.50 || lat < 50.0 || lon > 2.5 || lon < -8
  puts 'Looks like bad lat/lon values, please go back and try again.<br>'
  exit
else
  puts 'Latitude and Longitude look reasonable :-)<br>'
end

email =  email.upcase.match(/\b[A-Z0-9._%-]+@[A-Z0-9._%-]+\.[A-Z]{2,4}\b/)


if email
  puts 'email address looks reasonable :-)<br>'
else
  puts 'Sorry, your email address does not look good, please go back and try again<br>'
  exit
end

if (postcode1.match(/[A-Z]\d/) ||
    postcode1.match(/[A-Z][A-Z]\d/) ||
    postcode1.match(/[A-Z]\d\d/) ||
    postcode1.match(/[A-Z]\w\d\d/) ||
    postcode1.match(/[A-Z][A-Z]\d[A-Z]/) ||
    postcode1.match(/[A-Z]\d[A-Z]/) ) && postcode2.match(/\d[A-Z][A-Z]/) 

    puts 'Postcode looks reasonable :-)<br>'
else
  puts '
    Sorry, your postcode should be of one of these forms:<br>
    <br>
    LD DLL<br>
    LLD DLL<br>
    LDD DLL<br>
    LLDD DLL<br>
    LLDL DLL<br>
    LDL DLL<br>
    <br>
    Where L is a letter and D a digit, for example SW1A 0AA matches LLDL DLL<br>'
  exit
end

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
chars = 'abcdefghijklmnopqrtuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'

if connection
  confirmstring = ''
  for i in 1..20
    confirmstring += chars[(rand * chars.length).to_i].chr
  end

  sql = "insert into codes values ('#{email}', #{lat}, #{lon}, '#{postcode1}', '#{postcode2}', NOW(), '#{confirmstring}', 0);"

  connection.query(sql)

email = email.to_s.downcase

msgstr = <<END_OF_MESSAGE
From: webmaster <webmaster@freethepostcode.org>
To: #{email}
Subject: Your submitted postcode location

Hi,

Someone, hopefully you, submitted the postcode location

  #{postcode1} #{postcode2} -> #{lat},#{lon}

to www.freethepostcode.org.

We have to make sure you're really you with this confirmation email in
case we get lawyered to death by someone. Please click the following
link if you submitted the postcode and are happy for it to be in the
public domain:

http://www.freethepostcode.org/confirm?email=#{email}&confirmstring=#{confirmstring}

If this in error, then please report abuse to webmaster@freethepostcode.org

END_OF_MESSAGE


  Net::SMTP.start('127.0.0.1', 25) do |smtp|
    smtp.send_message msgstr.untaint,
                      'webmaster@vr.ucl.ac.uk'.untaint,
                      email.untaint
  end

end

puts '<br><br>You should have an email on its way to confirm that email address belongs to you. Click the confirm link in it and we are all done. Thanks!</body></html>'

