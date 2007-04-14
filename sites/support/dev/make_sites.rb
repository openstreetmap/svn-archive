#!/bin/ruby

`cat users`.each do |l|
  `sed s/USER/#{l.chomp}/ apache-user-site > /etc/apache2/sites-available/#{l.chomp}.dev.openstreetmap.org`
end

