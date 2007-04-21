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
require 'tiger/tiger'

THIS_DIR = File.expand_path(File.dirname(__FILE__))
TMP_DIR = "#{THIS_DIR}/tmp"

$stderr.puts "About to re-create the database...!"; $stderr.flush
sleep(5)
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

sql("DROP TABLE IF EXISTS fips;")
sql("CREATE TABLE fips (id INTEGER AUTO_INCREMENT UNIQUE KEY, code VARCHAR(5) UNIQUE, priority FLOAT);")
sql("CREATE UNIQUE INDEX fips_id_index ON fips (id);")
sql("CREATE UNIQUE INDEX fips_code_index ON fips (code);")
sql("CREATE INDEX fips_priority_index ON fips (priority);")

FIRST_FIPS = [
    "06075",  # San Francisco
    "06097",  # Sonoma (greater San Francisco, CA)
    "06041",  # Marin (greater San Francisco, CA)
    "06001",  # Alameda (greater San Francisco, CA)
    "06085",  # Santa Clara (greater San Francisco, CA)
    "06081",  # San Mateo (greater San Francisco, CA)
    "17031",  # Cook (Chicago, IL)
    "36061",  # New York (Manhattan)
    "11001",  # District of Columbia (Washington DC)
    "48201",  # Harris (Houston, TX)
    "48167",  # Galveston (greater Houston, TX)
    "48039",  # Brazoria (greater Houston, TX)
    "48071",  # Chambers (greater Houston, TX)
    "18029",  # Dearborn (greater Cincinatti, OH)
    "21015",  # Boone (greater Cincinatti, OH)
    "21117",  # Kenton (greater Cincinatti, OH)
    "21037",  # Campbell (greater Cincinatti, OH)
    "21191",  # Pendleton (greater Cincinatti, OH)
    "21023",  # Bracken (greater Cincinatti, OH)
    "39061",  # Hamilton (greater Cincinatti, OH)
    "39025",  # Clermont (greater Cincinatti, OH)
    "39015",  # Brown (greater Cincinatti, OH)
    "39001",  # Adams (greater Cincinatti, OH)
    "39017",  # Butler (greater Cincinatti, OH)
    "39165",  # Warren (greater Cincinatti, OH)
    "39027",  # Clinton (greater Cincinatti, OH)
    "39057",  # Greene (greater Cincinatti, OH)
    "06037",  # California (for Blars, blarson@blars.org)
    "06059",  # California (for Blars, blarson@blars.org)
    "06073",  # California (for Blars, blarson@blars.org)
    "06071",  # California (for Blars, blarson@blars.org)
    "06065",  # California (for Blars, blarson@blars.org)
    "06111",  # California (for Blars, blarson@blars.org)
    "06029",  # California (for Blars, blarson@blars.org)
    "41029",  # California (for Blars, blarson@blars.org)
    "06085",  # California (for Blars, blarson@blars.org)
    "51059",  # Clifton, VA (for Michael Robinson, robinson@fuzzymuzzle.com)
    "26163",  # Wayne, MI (for Andrew Turner, ajturner@highearthorbit.com)
    "26125",  # Oakland, MI (for Andrew Turner, ajturner@highearthorbit.com)
    "26161",  # Washtenaw, MI (for Andrew Turner, ajturner@highearthorbit.com)
    "36089",  # St. Lawrence, NY (for Russ Nelson, nelson@crynwr.com)
    "36045",  # Jefferson, NY (for Russ Nelson, nelson@crynwr.com)
    "36049",  # Lewis, NY (for Russ Nelson, nelson@crynwr.com)
    "36033",  # Franklin, NY (for Russ Nelson, nelson@crynwr.com)
    "36019",  # Clinton, NY (for Russ Nelson, nelson@crynwr.com)
    "36043",  # Herkimer, NY (for Russ Nelson, nelson@crynwr.com)
    "36041",  # Hamilton, NY (for Russ Nelson, nelson@crynwr.com)
    "36031",  # Essex, NY (for Russ Nelson, nelson@crynwr.com)
]

all_fips = FIRST_FIPS
rs = sql("SELECT DISTINCT from_fips FROM street ORDER BY from_fips ASC;")
while row = rs.fetch_row do
	fips = row[0]
	all_fips << fips
end
rs.free
all_fips.uniq!

all_fips.each_index do |i|
	fips = all_fips[i]
	sql("INSERT INTO fips (code, priority) VALUES ('#{esc(fips)}', #{i.to_f});")
end

$dbh.close

