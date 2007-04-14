u#!/usr/bin/ruby


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
load 'dao.rb'

print '<html><body>'
puts '# Generated daily. Click a postcode to see its location and leave a <br>
        comment, or click OSM Map to see the location in OpenStreetMap'

dao = OSM::Dao.instance

res = dao.call_sql { 'select format(avg(lat),6) as lat, format(avg(lon),6) as lon, part1, part2 from codes where confirmed = 1 group by part1, part2;' }


res.each_hash do |row|
  puts "<p>  <a href=\"http://www.kirit.com/Postcode/UK:/#{row['part1']}%20#{row['part2']}\">"
  puts "#{row['lat']} #{row['lon']} #{row['part1']} #{row['part2']}</a>"

  puts "<a href=\"http://www.openstreetmap.org/index.html?lat=#{row['lat']}&lon=#{row['lon']}&zoom=11\">OSM Map</a>"
end

puts '</body></html>' 
