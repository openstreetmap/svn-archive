#!/usr/bin/env ruby

links = `lynx --dump http://www.census.gov/geo/www/tiger/tiger2005fe/tgr2005fe.html 2>/dev/null`

state_pages = []
until links.empty?
	break unless links =~ /(http:\/\/www2\.census\.gov\/geo\/tiger\/tiger2005fe\/..\/)/
	state_pages << $1 unless state_pages.member?($1)
	links = $'
end
state_pages.sort!
state_pages.each do |page|
	system("wget --recursive --wait=3 --limit-rate=150k -erobots=off --span-hosts --accept zip '#{page}'")
end

