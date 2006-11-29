#!/usr/bin/env ruby

require 'fileutils'
require 'find'
require 'mysql'
require 'tiger/tiger'

THIS_DIR = File.expand_path(File.dirname(__FILE__))
TMP_DIR = "#{THIS_DIR}/tmp"

`echo "DROP DATABASE IF EXISTS tiger; CREATE DATABASE tiger;" |sudo mysql mysql`

def sql(sql_code)
	$stderr.puts "Running #{sql_code[0...1000].inspect}"  # for debugging
	$dbh.query(sql_code)
end

def esc(s)
	$dbh.escape_string(s)
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

$dbh = Mysql.real_connect("localhost", "", "", "tiger")
sql("DROP TABLE IF EXISTS street;")
sql("CREATE TABLE street (id INTEGER AUTO_INCREMENT UNIQUE KEY, from_fips VARCHAR(5), fips_street_index INTEGER, osm_way_id INTEGER);")
sql("CREATE UNIQUE INDEX street_id_index ON street (id);")
sql("CREATE INDEX street_from_fips_index ON street (from_fips);")
sql("CREATE INDEX street_fips_street_index_index ON street (fips_street_index);")
sql("CREATE INDEX street_osm_way_id_index ON street (osm_way_id);")

Find.find("#{THIS_DIR}/spider/tiger2005fe") do |f|
	next unless f =~ /tgr(\d{5})\.zip$/
	fips = $1
	rt1, rt2 = read_rt(f)
	streets = Tiger::import(rt1, rt2)
	streets.each_index do |i|
		st = streets[i]
		sql("INSERT INTO street (from_fips, fips_street_index, osm_way_id) VALUES ('#{esc(fips)}', #{i}, NULL);")
	end
end

