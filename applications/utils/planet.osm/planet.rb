#!/usr/bin/ruby -w

#$: << Dir.pwd+"/../../www.openstreetmap.org/ruby/api"

require 'mysql'
require 'time'
require 'osm/servinfo.rb'
require 'cgi'

$mysql = Mysql.init
# If you have a UTF-8 clean setup then you may need to enable the following line
# $mysql.options(Mysql::SET_CHARSET_NAME, "utf8")
$mysql.real_connect $DBSERVER, $USERNAME, $PASSWORD, $DATABASE

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
      yield row[0].to_i, row[1].to_i, row[2].to_i, read_timestamp(row[3]), read_tags(row[4])
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
      all_way_segments(id) do |s|
        segs << s.to_i
      end
      tags_arr = all_way_tags(id)
      yield id, segs, read_timestamp(row[1]), Hash[*tags_arr]
    end
  end
end

# Here we produce the segments associated with a way. How it works is that
# instead of doing the segments query for each way, it gets all the data
# from the beginning in groups of 50000. Since this is sorted we can perform
# a sort of "Merge join". The caller provides the ID they are interesting in
# and we scan forward in the table to find it, yield each segment and
# return. The only hard part is that we when we note we're too far, we have
# to jump back one row so the next iteration sees it again. That's what all
# the seek/tell is about.
$way_segments_data = nil
$way_segments_current = [0,0]
$way_segments_done = false
# yields each segment, one at a time
def all_way_segments(curr_id)
  loop do
    if $way_segments_data == nil
      $way_segments_data = $mysql.query "select id, sequence_id, segment_id from current_way_segments 
                                                                   where id > #{$way_segments_current[0]} 
                                                                   or (id = #{$way_segments_current[0]} and sequence_id >  #{$way_segments_current[1]})
                                                                   order by id, sequence_id limit 500000;" 
      if $way_segments_data == nil
        return
      end
      $way_segments_done = true
    end
    pos = $way_segments_data.row_tell()
    $way_segments_data.each() do |$way_segments_current|
      $way_segments_done = false
      id = $way_segments_current[0].to_i
      if id < curr_id 
        pos = $way_segments_data.row_tell()
        next
      end
      if id == curr_id
        pos = $way_segments_data.row_tell()
        yield $way_segments_current[2]
        next
      end
      # Need to seek back one so we get this row again...
      $way_segments_data.row_seek( pos )
      return
    end
    $way_segments_data = nil
    if $way_segments_done
      return
    end
  end
end
    
$way_tags_data = nil
$way_tags_current = [0]
$way_tags_first = false
# Way tags are more irritating because there's no unique key sort by. So we
# have to collect the results for an ID in an array and if it turns out to
# hit the end, we toss out what we've collected and start again with a new
# query...

# Because of this detecting the end of the table becomes tricky, since when
# we reach the end of the resultset and it's the end of the table, we'd keep
# requesting the last bit over and over again. So the rule is, if the ID
# being returned is the *only* ID in this set, we're done. That's what
# $way_tags_first is tracking.

# yields the tags, all in one go as an array
def all_way_tags(curr_id)
  loop do
    if $way_tags_data == nil
      $way_tags_data = $mysql.query "select id,k,v from current_way_tags where id >= #{$way_tags_current[0]} order by id limit 50000;" 
      $way_tags_first = true
      if $way_tags_data == nil
        return tags
      end
    end
    pos = $way_tags_data.row_tell()
    tags = []
    $way_tags_data.each() do |$way_tags_current|
      id = $way_tags_current[0].to_i
      if id < curr_id 
        pos = $way_tags_data.row_tell()
        next
      end
      if id == curr_id
        pos = $way_tags_data.row_tell()
        tags << $way_tags_current[1] << $way_tags_current[2]
        next
      end
      # Need to seek back one so we get this row again...
      $way_tags_data.row_seek( pos )
      $way_tags_first = false
      return tags
    end
    # So we've hit the end of this dataset. If it's the end of the table, we
    # return tags, otherwise we clear tags and continue...
    
    $way_tags_data = nil
    if $way_tags_first
      return tags
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
    print %{  <node id="#{id}" lat="#{sprintf('%.7f', lat/10000000.0)}" lon="#{sprintf('%.7f', lon/10000000.0)}" timestamp="#{timestamp.xmlschema}"}
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
