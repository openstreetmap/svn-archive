#!/usr/bin/ruby -w

$: << File.dirname(__FILE__)+"/../osm-data/lib"
#$: << File.dirname(__FILE__)+"/../../www.openstreetmap.org/ruby/api"

require 'mysql'
require 'time'
#require 'osm/servinfo.rb'
require 'servinfo.rb'
require 'cgi'

require 'osm/data'
require 'osm/rexml'

$mysql = Mysql.real_connect $DBSERVER, $USERNAME, $PASSWORD, $DATABASE

class Hash
  def to_tags_str
    if self.empty?
      nil
    else
      self.collect{|k,v| "#{k}=#{v}"}.join(";").gsub('"', '\"')
    end
  end
end

class OSM::OsmPrimitive
  def xmldate
    if @timestamp
      '"' + @timestamp.strftime("%Y-%m-%d %H:%M:%S") + '"'
    else
      "NULL"
    end
  end
end

class OSM::Node
  def save_mysql
    $mysql.query "insert into current_nodes values (#@id, #@lat, #@lon, 1, 1, \"#{@tags.to_tags_str}\", #{self.xmldate});"
  end
end

class OSM::Segment
  def save_mysql
    $mysql.query "insert into current_segments values (#@id, #{@from.to_i}, #{@to.to_i}, 1, 1, \"#{@tags.to_tags_str}\", #{self.xmldate});"
  end
end

class OSM::Way
  def save_mysql
    $mysql.query "insert into current_ways values (#@id, 1, #{self.xmldate}, 1);"
    @tags.each do |key, value|
      $mysql.query "insert into current_way_tags values (#@id, \"#{key}\", \"#{value}\");"
    end
    @segments.each_with_index do |s,i|
      $mysql.query "insert into current_way_segments values (#@id, #{s.to_i}, #{i+1});"
    end
  end
end

$mysql.query "set autocommit = 0;"
current = ""
open(ARGV[0]).each do |line|
  next if line =~ /\<osm|\<\/osm\>|\<\?xml/
  current << line
  case line
  when /\<(\/(node|segment|way)|(node|segment|way).*\/)\>$/
    OSM::OsmPrimitive.from_xml(current).save_mysql
    current = ""
  end
end

$mysql.query "commit;"
