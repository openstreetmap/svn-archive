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

def pageSQL(page)
  return " limit #{page * 500000}, 500000"
end

# yields for every node with parameter
# id, lat, lon, timestamp, tags
# 'tags' are a hash in format {key1=>value1, key2=>value2...}
def all_nodes(page)
  $mysql.query "select id, latitude, longitude, timestamp, tags from current_nodes where visible = 1 order by id #{pageSQL(page)}" do |rows|
    rows.each do |row|
      yield row[0].to_i, row[1].to_f, row[2].to_f, read_timestamp(row[3]), read_tags(row[4])
    end
  end
end

# yields for every way
# id, [id1,id2,id3...], timestamp, tags
def all_ways(page)
  $mysql.query "select id, timestamp from current_ways where visible = 1 order by id #{pageSQL(page)}" do |ways|
    ways.each do |row|
      id = row[0].to_i
      nds = []
      $mysql.query "select node_id from current_way_nodes where id = #{id} order by sequence_id;" do |nodes|
        nodes.each {|s| nds << s[0].to_i}
      end
      tags_arr = []
      $mysql.query "select k,v from current_way_tags where id = #{id};" do |tags|
        tags.each {|t| tags_arr << t[0] << t[1]}
      end
      yield id, nds, read_timestamp(row[1]), Hash[*tags_arr]
    end
  end
end

# yields for every relation
# id, [[type,ref,role],...], timestamp, tags
def all_relations(page)
  $mysql.query "select id, timestamp from current_relations where visible = 1 order by id #{pageSQL(page)}" do |rels|
    rels.each do |row|
      id = row[0].to_i
      rms = []
      $mysql.query "select member_type, member_id, member_role from current_relation_members where id = #{id};" do |members|
        members.each { |rm| rms << rm }
      end
      tags_arr = []
      $mysql.query "select k,v from current_relation_tags where id = #{id};" do |tags|
        tags.each {|t| tags_arr << t[0] << t[1]}
      end
      yield id, rms, read_timestamp(row[1]), Hash[*tags_arr]
    end
  end
end

# output all tags in the hash
def out_tags tags
  tags.each {|key, value| puts %{    <tag k="#{CGI.escapeHTML(key)}" v="#{CGI.escapeHTML(value)}" />}}
end

puts '<?xml version="1.0" encoding="UTF-8"?>'
puts '<osm version="0.5" generator="OpenStreetMap planet.rb">'

done = false
page = 0

while not done
  done = true
  all_nodes(page) do |id, lat, lon, timestamp, tags|
    done = false
    print %{  <node id="#{id}" lat="#{sprintf('%.7f', lat/10000000.0).lstrip}" lon="#{sprintf('%.7f', lon/10000000.0).lstrip}" timestamp="#{timestamp.xmlschema}"}
    if tags.empty?
      puts "/>"
    else
      puts ">"
      out_tags tags
      puts "  </node>"
    end
  end
  page += 1
end

done = false
page = 0

while not done
  done = true
  all_ways(page) do |id, nds, timestamp, tags|
    done = false
    print %{  <way id="#{id}" timestamp="#{timestamp.xmlschema}"}
    if tags.empty? and nds.empty?
      puts "/>"
    else
      puts ">"
      nds.each {|nd_id| puts %{    <nd ref="#{nd_id}" />}}
      out_tags tags
      puts "  </way>"
    end
  end
  page += 1
end

done = false
page = 0

while not done
  done = true
  all_relations(page) do |id, rms, timestamp, tags|
    done = false
    print %{  <relation id="#{id}" timestamp="#{timestamp.xmlschema}"}
    if tags.empty? and rms.empty?
      puts "/>"
    else
      puts ">"
      rms.each { |type, ref, role|
	puts %{    <member type="#{type}" ref="#{ref}" role="#{CGI.escapeHTML(role)}" />}
      }
      out_tags tags
      puts "  </relation>"
    end
  end
  page += 1
end
puts "</osm>"
