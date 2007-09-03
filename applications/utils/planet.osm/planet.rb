#!/usr/bin/ruby -w

#$: << Dir.pwd+"/../../www.openstreetmap.org/ruby/api"

require 'mysql'
require 'time'
require 'osm/servinfo.rb'
require 'cgi'

$mysql = Mysql.real_connect $DBSERVER, $USERNAME, $PASSWORD, $DATABASE

# create a hash of entries out of a list of semi colon seperated key=value pairs
def read_tags tag_str
  tags_arr = tag_str.split(';').collect {|tag| tag =~ /=/ ? [$`,$'] : [tag,""] }
  Hash[*tags_arr.flatten]
end

# create a timestamp or nil out of a time string
def read_timestamp time_str
  (time_str.nil? or time_str == "" or time_str == "NULL") ? Time.at(0) : Time.parse(time_str)
end

def pageSQL(lastid)
  if lastid == 0
    return ""
  else
    return " and id > #{lastid}"
  end
end

# yields for every node with parameter
# id, lat, lon, timestamp, tags
# 'tags' are a hash in format {key1=>value1, key2=>value2...}
def all_nodes(lastid)
  $mysql.query "select id, latitude, longitude, timestamp, tags from current_nodes where visible = 1 #{pageSQL(lastid)} order by id limit 500000" do |rows|
    rows.each do |row|
      yield row[0].to_i, row[1].to_f, row[2].to_f, read_timestamp(row[3]), read_tags(row[4])
    end
  end
end

# yields for every segment
# id, from_id, to_id, timestamp, tags
def all_segments(lastid)
  $mysql.query "select id, node_a, node_b, timestamp, tags from current_segments where visible = 1 #{pageSQL(lastid)} order by id limit 500000" do |rows|
    rows.each do |row|
      yield row[0].to_i, row[1].to_i, row[2].to_i, read_timestamp(row[3]), read_tags(row[4])
    end
  end
end

# yields for every way
# id, [id1,id2,id3...], timestamp, tags
def all_ways(lastid)
  $mysql.query "select id, timestamp from current_ways where visible = 1 #{pageSQL(lastid)} order by id limit 500000" do |ways|
    ways.each do |row|
      id = row[0].to_i
      segs = []
      $mysql.query "select segment_id from current_way_segments where id = #{id} order by sequence_id;" do |segments|
        segments.each {|s| segs << s[0].to_i}
      end
      tags_arr = []
      $mysql.query "select k,v from current_way_tags where id = #{id};" do |tags|
        tags.each {|t| tags_arr << t[0] << t[1]}
      end
      yield id, segs, read_timestamp(row[1]), Hash[*tags_arr]
    end
  end
end

# output all tags in the hash
def out_tags tags
  tags.each {|key, value| puts %{    <tag k="#{CGI.escapeHTML(key)}" v="#{CGI.escapeHTML(value)}" />}}
end

puts '<?xml version="1.0" encoding="UTF-8"?>'
puts '<osm version="0.3" generator="OpenStreetMap planet.rb">'
puts '  <bound box="-90,-180,90,180" origin="http://www.openstreetmap.org/api/0.4" />'

done = false
lastid = 0

while not done
  done = true
  all_nodes(lastid) do |id, lat, lon, timestamp, tags|
    done = false
    lastid = id
    print %{  <node id="#{id}" lat="#{sprintf('%.7f', lat)}" lon="#{sprintf('%.7f', lon)}" timestamp="#{timestamp.xmlschema}"}
    if tags.empty?
      puts "/>"
    else
      puts ">"
      out_tags tags
      puts "  </node>"
    end
  end
end

done = false
lastid = 0

while not done
  done = true
  all_segments(lastid) do |id, from, to, timestamp, tags|
    done = false
    lastid = id
    print %{  <segment id="#{id}" from="#{from}" to="#{to}" timestamp="#{timestamp.xmlschema}"}
    if tags.empty?
      puts "/>"
    else
      puts ">"
      out_tags tags
      puts "  </segment>"
    end
  end
end

done = false
lastid = 0

while not done
  done = true
  all_ways(lastid) do |id, segs, timestamp, tags|
    done = false
    lastid = id
    print %{  <way id="#{id}" timestamp="#{timestamp.xmlschema}"}
    if tags.empty? and segs.empty?
      puts "/>"
    else
      puts ">"
      segs.each {|seg_id| puts %{    <seg id="#{seg_id}" />}}
      out_tags tags
      puts "  </way>"
    end
  end
end
puts "</osm>"
