#!/usr/bin/ruby

# Read in planet.osm and create a sqlite3 database file named "planet.db"

$: << File.dirname(__FILE__)+"/../osm-data/lib"

require 'osm/data'
require 'rexml/document'
require 'sqlite3'
require 'time'


module OSM
  class Node < OsmPrimitive
    def self.from_db uid, tags, time, reference, minlat, minlon, maxlat, maxlon
      Node.new minlat, minlon, OSM::uid_to_id(uid), time
    end
    def self.from_db_id uid
      complete_node = $db.execute("select * from data where uid=#{uid};")[0].to_a
      raise uid.to_s+" not found" if complete_node.empty?
      Node.from_db(*complete_node)
    end
  end
  

  # define load_references in segment and way, which replaces the id-references by their real data
  class Segment < OsmPrimitive
    def load_references
      self.from = Node.from_db_id OSM::idclass_to_uid(self.from, Node)
      self.to = Node.from_db_id OSM::idclass_to_uid(self.to, Node)
    end
    def self.from_db uid, tags, time, reference, minlat, minlon, maxlat, maxlon
      fid, tid = reference.split ','
      Segment.new(Node.from_db_id(fid.to_i), Node.from_db_id(tid.to_i), OSM::uid_to_id(uid).to_s, time)
    end
    def self.from_db_id uid
      q = $db.execute("select * from data where uid=#{uid};")[0].to_a
      throw :incomplete_way if q.empty?
      Segment.from_db(*q)
    end
  end


  class Way < OsmPrimitive
    def load_references
      self.segments.collect! do |id|
        Segment.from_db_id OSM::idclass_to_uid(id, Segment)
      end
    end
  end
end

# writes the data object into the database.
def write_sql data
  tags = data.tags ? data.tags.to_a.join("\n") : "null"
  tags.gsub!(/\"/, '')
  time = data.timestamp ? '"'+data.timestamp.xmlschema+'"' : "null"
  case data.class.name
  when "OSM::Node"
  	reference = ""
  when "OSM::Segment"
  	reference = OSM::idclass_to_uid(data.from, OSM::Node).to_s + "," + OSM::idclass_to_uid(data.to, OSM::Node).to_s
  	data.load_references
  when "OSM::Way"
  	reference = data.segments.collect {|s| OSM.idclass_to_uid(s, OSM::Segment).to_s}.join ','
  	data.load_references
  end
  sql = %Q{insert into data values (#{data.to_uid}, "#{tags}", #{time}, "#{reference}", #{data.bbox.join(',')});}
  $db.execute sql
end

# parses the input data and call to write_sql for each object.
class XmlReader
  def method_missing sym, *args; end
  def tag_start name, a
    time = Time.parse(a['timestamp']) if a.include? 'timestamp'
    id = a['id'].to_i

    case name
    when "node"
      @current = OSM::Node.new :lat => a['lat'].to_f, :lon => a['lon'].to_f, :id => id, :timestamp => time
    when "segment"
      @current = OSM::Segment.new :from => a['from'].to_i, :to => a['to'].to_i, :id => id, :timestamp => time
    when "way"
      @current = OSM::Way.new :segments => [], :id => id, :timestamp => time
    when "tag"
      @current[a['k']] = a['v']
    when "seg"
      @current.segments << a['id'].to_i
    end
  end

  def tag_end name
    catch :incomplete_way do
      write_sql @current if name =~ /node|segment|way/
    end
  end
end



abort "planet.osm not found." unless File.exist? "planet.osm"
File.delete "planet.db" if File.exist? "planet.db"
$db = SQLite3::Database.new "planet.db"
$db.execute 'BEGIN'
open('planet.sql').each {|sql| $db.execute sql}
REXML::Document.parse_stream File.new('planet.osm'), XmlReader.new
$db.execute 'COMMIT'
