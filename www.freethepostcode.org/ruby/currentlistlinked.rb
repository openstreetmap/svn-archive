#!/usr/bin/ruby


#
#
#
#  This is meant to be run by cron daily and piped to current_list_links in the 
#  html root like
#
#  2 0 * * * /home/steve/bin/currentlistlinked.rb > /var/www/freethepostcode.org/currentlistlinked
#


require 'singleton'
require 'cgi'
load '/var/www/nickb/ruby/dao.rb' 

print '<html><body>'
puts '# Generated daily. Click a postcode to see its location.'

dao = OSM::Dao.instance

res = dao.call_sql { 'select format(avg(lat),6) as lat, format(avg(lon),6) as lon, part1, part2 from codes where confirmed = 1 group by part1, part2;' }


res.each_hash do |row|
  print "<p>"
  print ( '<a href="http://www.kirit.com/Postcode/UK:/' )
  print row['part1']
  print ( '%20' )
  print row['part2']
  print ( '">' )
  print row['lat'] + ' ' + row['lon'] + ' ' + row['part1'] + ' ' + row['part2']
  print ( '</a>' )
 end
  puts( '</body></html>' )
