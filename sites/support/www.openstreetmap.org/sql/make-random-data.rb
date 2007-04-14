#!/usr/bin/ruby

require 'mysql'

scale = ARGV[0].to_i
if scale == 0
	puts "Usage: make-random-data.rb <number of data entries>"
	puts ""
	puts "WARNING! The database data will be destroyed, including history!"
	exit
end

connection = Mysql.real_connect 'localhost', 'imi', 'Ls8V3Gkd', 'imi'

puts "delete nodes"
connection.query 'delete from nodes;'
puts "delete segments"
connection.query 'delete from street_segments;'

timestart=1135960534000
id = 1
percent = 0
i = 0
(scale).times do
	id += 1
	if i/scale/100 > percent
		percent += 1
		puts "#{percent}%"
	end
	connection.query "insert into nodes values (#{id}, #{rand*180-90}, #{rand*360-180}, #{timestart + rand*10000}, 1, 1, '');"
	i += 1
end

percent = 0
i = 0
(scale).times do
	if i/scale/100 > percent
		percent += 1
		puts "#{percent}%"
	end
	rlat = rand*180-90
	rlon = rand*360-180
	result = connection.query "select uid from nodes order by (latitude-#{rlat})*(latitude-#{rlat})+(longitude-#{rlon})*(longitude-#{rlon}) limit 2;"
	id += 1;
	connection.query "insert into street_segments (uid, node_a, node_b, timestamp, user_uid, visible, tags) values (#{id}, #{result.fetch_row[0]}, #{result.fetch_row[0]}, #{timestart + rand*10000}, 1, 1, \"\");"
	i += 1
end


connection.close
