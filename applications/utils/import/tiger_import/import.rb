#!/usr/bin/env ruby

=begin Copyright (C) 2006 Ben Gimpert (ben@somethingmodern.com)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

=end

require 'fileutils'
require 'find'
require 'mysql'
require 'net/http'
require 'tiger/tiger'
require 'timeout'
require 'uri'

LOGIN = "ben_tiger101@somethingmodern.com"
PASSWORD = "january"
GENERATOR = "TIGER Import v101"

SHOULD_ACTUALLY_DEMONIZE = true
THIS_DIR = File.expand_path(File.dirname(__FILE__))
TMP_DIR = "#{THIS_DIR}/tmp"

def usage
	$stderr.print <<EOF
TIGER -> OSM Import

Pass the root of the TIGER 2005 download as the first argument.
Usage: $ ./#{File.basename(__FILE__)} /some_dir/tiger2005fe/

EOF
	exit(1)
end

def demonize
	if SHOULD_ACTUALLY_DEMONIZE
		# from "http://blade.nagaokaut.ac.jp/cgi-bin/scat.rb/ruby/ruby-talk/87467"
		exit if fork                   # Parent exits, child continues.
		Process.setsid                 # Become session leader.
		exit if fork                   # Zap session leader.
		Dir.chdir "/"                  # Release old working directory.
		File.umask 0000                # Ensure sensible umask.
		STDIN.reopen "/dev/null"       # Free file descriptors and point them at the log.
		STDOUT.reopen "#{THIS_DIR}/import.log", "a"
		STDERR.reopen STDOUT
		File.open("#{THIS_DIR}/import.pid", "w") { |f| f.puts $$ }
	end
	yield
end

def osm(method_class, url_snippet, req_body = nil)
	$stderr.puts "#{method_class.name}(\"#{url_snippet}\") =\n#{req_body}"
	Net::HTTP.start("www.openstreetmap.org") do |http|
		url = "/api/0.3/#{url_snippet}"
		req = method_class.new(url)
		req.basic_auth(LOGIN, PASSWORD)
		resp = http.request(req, req_body)
		$stderr.puts "request(\"#{url_snippet}\") =\n#{req_body}"
		$stderr.puts "resp.body = #{resp.body}"
		case resp.code.to_i
		when 400
			throw "Bad Request(400) on \"#{url_snippet}\""
		when 401
			throw "Authorization Required(401) on \"#{url_snippet}\" with #{LOGIN}/#{PASSWORD}"
		when 404
			throw "Not Found(404) on \"#{url_snippet}\""
		when 410
			throw "Gone(410) on \"#{url_snippet}\""
		when 500
			throw "Internal Server Error(500) on \"#{url_snippet}\""
		when 200
			return resp.body
		end
	end
end

def osm_get(url_snippet)
	osm(Net::HTTP::Get, url_snippet)
end

def osm_put(url_snippet, req_body)
	osm(Net::HTTP::Put, url_snippet, req_body)
end

def hash_to_tags(h)
	s = ''
	h.each_pair do |key, value|
		s += "<tag k=\"#{key}\" v=\"#{value}\"/>\n"
	end
	s
end

def osm_xml
	<<EOF
<?xml version="1.0"?>
<osm version="0.3" generator="#{GENERATOR}">
#{yield}
</osm>
EOF
end

def create_node(lat, long, tags = {})
	xml = osm_xml do
		"<node lat=\"#{lat}\" lon=\"#{long}\" id=\"0\">\n" +
		hash_to_tags(tags) +
		"</node>"
	end
	node = osm_put("node/0", xml)
	node.to_i
end

def create_segment(from_node, to_node, tags = {})
	xml = osm_xml do
		"<segment from=\"#{from_node}\" to=\"#{to_node}\" id=\"0\">\n" +
		hash_to_tags(tags) +
		"</segment>"
	end
	segment = osm_put("segment/0", xml)
	segment.to_i
end

def create_way(seg_ids, tags = {})
	xml = osm_xml do
		"<way id=\"0\">\n" +
		seg_ids.map { |seg_id| "<seg id=\"#{seg_id}\"/>\n" }.join +
		hash_to_tags(tags) +
		"</way>"
	end
	way = osm_put("way/0", xml)
	way.to_i
end

def backoff
	delay = 1.25  # seconds, must be > 1
	loop do
		begin
			return yield
		rescue TimeoutError =>ex
			$stderr.puts "timeout in backoff block, \"#{ex.message}\" #{ex.backtrace.join(" -> ")}"
		rescue =>ex
			$stderr.puts "problem in backoff block, \"#{ex.message}\" #{ex.backtrace.join(" -> ")}"
		end
		$stderr.puts "[#{Time.now}] sleeping #{delay} seconds"
		sleep(delay)
		delay = delay ** 2  # exponential backoff
		throw "Backoff delay over four hours" if delay > 60 * 60 * 4
	end
end

def create_street_raw(street)
	prev_node_id = nil
	seg_ids = []
	total_lat, total_long = 0, 0
	street.points.each do |pt|
		lat, long = pt.lat, pt.long
		total_lat += lat
		total_long += long
		if prev_node_id.nil?
			prev_node_id = create_node(lat, long)
		else
			next_node_id = create_node(lat, long)
			seg_ids << create_segment(prev_node_id, next_node_id)
			prev_node_id = next_node_id
		end
	end
	av_lat = total_lat / street.points.length
	av_long = total_long / street.points.length
	$stderr.puts "\tcenter lat, long = #{av_lat}, #{av_long}"
	$stderr.puts "\tURL = http://www.openstreetmap.org/index.html?lat=#{av_lat}&lon=#{av_long}&zoom=12"
	tags = {
		"name" => street.name,
		"created_by" => GENERATOR,
		"from_zip" => street.from_zip,
		"to_zip" => street.to_zip,
	}
	tags["highway"] = street.road_type unless street.road_type.nil?
	way_id = create_way(seg_ids, tags)
end

def create_street(street)
	# with error checking and backoff
	throw "Attempt to create nil street" if street.nil?
	throw "Attempt to create street with less than two coordinates" if street.points.nil? || (street.points.length < 2)
	way_id = backoff { create_street_raw(street) }
	$stderr.puts "\tcreated way with ID #{way_id}"
	way_id
end

def read_rt(zip)
	FileUtils.rm_rf(TMP_DIR) if File.exist?(TMP_DIR)
	FileUtils.mkdir_p(TMP_DIR)
	$stderr.puts "reading RT1 and RT2 from \"#{File.basename(zip)}\""
	FileUtils.cp(zip, "#{TMP_DIR}/current.zip")
	rt1_s, rt2_s = nil, nil
	Dir.chdir(TMP_DIR) do
		`/usr/bin/unzip ./current.zip`
		Dir["*.RT1"].each { |rt1_f| rt1_s = IO.read(rt1_f) }
		Dir["*.RT2"].each { |rt2_f| rt2_s = IO.read(rt2_f) }
	end
	[rt1_s, rt2_s]
end

def read_tiger_county_zip(zip)
	$stderr.puts "reading county ZIP file \"#{File.basename(zip)}\""
	rt1, rt2 = read_rt(zip)
	streets = Tiger::import(rt1, rt2).sort
	$stderr.puts "\tnumber of streets = #{streets.length}"
	streets
end

def sql(sql_code)
	$stderr.puts "SQL(#{sql_code[0...1000].inspect})"
	$dbh.query(sql_code)
end

def esc(s)
	$dbh.escape_string(s)
end

usage unless ARGV.length >= 1
path = File.expand_path(ARGV.first)
usage unless File.exist?(path) && File.directory?(path)

demonize do
	$stderr.puts "[#{Time.now}] starting the #{GENERATOR}"
	$dbh = Mysql.real_connect("localhost", "", "", "tiger")
	begin
		rs = sql("SELECT code FROM fips ORDER BY priority ASC;")
		all_fips = []
		while row = rs.fetch_row do
			fips = row[0]
			all_fips << fips
		end
		rs.free
		all_fips.each do |fips|
			streets = nil
			Find.find(path) do |f|
				next unless f =~ /#{fips}\.zip$/i
				streets = read_tiger_county_zip(f)
				break
			end
			throw "Could not find TIGER .ZIP for FIPS #{fips.inspect}" if streets.nil?
			all_todo = []
			rs = sql("SELECT id, fips_street_index FROM street WHERE from_fips = '#{esc(fips)}' AND osm_way_id IS NULL;")
			while row = rs.fetch_row do
				street_id, fips_index = row[0], row[1].to_i
				all_todo << [street_id, fips_index]
			end
			rs.free
			all_todo.each do |todo|
				street_id, fips_index = todo
				before = Time.now
				osm_way_id = create_street(streets[fips_index])
				sql("UPDATE street SET osm_way_id = #{osm_way_id} WHERE id = #{street_id};") if osm_way_id > 0
				after = Time.now
				sleep((after - before) / 2)  # let the server breath
				$stderr.puts "[#{Time.now}] created street[#{street_id}], #{fips_index+1} in FIPS #{fips}"
			end
		end
	ensure
		$dbh.close unless $dbh.nil?
	end
end

