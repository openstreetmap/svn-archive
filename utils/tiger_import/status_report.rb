#!/usr/bin/env ruby

require 'find'
require 'mysql'

THIS_DIR = File.expand_path(File.dirname(__FILE__))

def sql(sql_code)
	$stderr.puts "Running #{sql_code[0...1000].inspect}"  # for debugging
	$dbh.query(sql_code)
end

def esc(s)
	$dbh.escape_string(s)
end

$dbh = Mysql.real_connect("localhost", "", "", "tiger")

state_to_fips = {}
fips_to_state = {}
Find.find("#{THIS_DIR}/spider/tiger2005fe") do |f|
	next unless f =~ /(..)\/tgr(\d{5})\.zip$/
	state, fips = $1, $2
	if state_to_fips.has_key?(state)
		state_to_fips[state] << fips
	else
		state_to_fips[state] = [fips]
	end
	if fips_to_state.has_key?(fips)
		fips_to_state[fips] << state
	else
		fips_to_state[fips] = [state]
	end
end

state_totals = {}
state_to_fips.keys.sort.each do |state|
	total = 0
	state_to_fips[state].each do |fips|
		rs = sql("SELECT COUNT(*) FROM street WHERE from_fips = '#{fips}';")
		total += rs.fetch_row[0].to_i
	end
	state_totals[state] = total
end

done_state_totals = {}
state_to_fips.keys.sort.each do |state|
	total = 0
	state_to_fips[state].each do |fips|
		rs = sql("SELECT COUNT(*) FROM street WHERE from_fips = '#{fips}' AND osm_way_id IS NOT NULL;")
		total += rs.fetch_row[0].to_i
	end
	state_totals[state] = total
end

fips_totals = {}
fips_to_state.keys.sort.each do |fips|
	rs = sql("SELECT COUNT(*) FROM street WHERE from_fips = '#{fips}';")
	fips_totals[fips] = rs.fetch_row[0].to_i
end

done_fips_totals = {}
fips_to_state.keys.sort.each do |fips|
	rs = sql("SELECT COUNT(*) FROM street WHERE from_fips = '#{fips}' AND osm_way_id IS NOT NULL;")
	done_fips_totals[fips] = rs.fetch_row[0].to_i
end

all_total = 0
state_totals.keys.each do |state|
	all_totals += state_totals[state]
end

done_all_total = 0
done_state_totals.keys.each do |state|
	done_all_totals += done_state_totals[state]
end

def percent(num, denom)
	(num.to_f / denom * 100).to_s[0..5] + "%"
end

puts "TIGER -> OSM Import Status"
puts "(as of #{Time.now})"
puts
puts "Overall\t#{done_all_total} / #{all_total}\t#{percent(done_all_total, all_total)}"
state_totals.keys.sort.each do |state|
	puts "State of #{state}\t#{done_state_totals[state]} / #{state_totals[state]}\t#{percent(done_state_totals[state], state_totals[state])}"
	state_to_fips[state].sort.each do |fips|
		puts "#{fips}\t#{done_fips_totals[fips]} / #{fips_totals[fips]}\t#{percent(done_fips_totals[fips], fips_totals[fips])}"
	end
	puts
end

