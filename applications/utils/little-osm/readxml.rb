#!/usr/bin/ruby

# Read in planet.osm and create a database "little-osm"

$: << File.dirname(__FILE__)+"/../osm-data/lib"

require 'osm/data'
require 'rexml/document'
require 'mysql'
require 'time'


module OSM
  class Node < OsmPrimitive
    def self.from_db uid, tags, time, reference, min, max
			min =~ /POINT\(([^ ]* [^)]*)\)/
      Node.new $1.to_f, $2.to_f, OSM::uid_to_id(uid), time
    end
    def self.from_db_id uid
      complete_node = $db.query("select uid, tags, time, reference, astext(min), astext(max) from data where uid=#{uid};").fetch_row
      if complete_node.nil?
        $current = nil
        throw :incomplete
      end
      Node.from_db(*complete_node)
    end
  end
  

  # define load_references in segment and way, which replaces the id-references by their real data
  class Segment < OsmPrimitive
    def load_references
      self.from = Node.from_db_id OSM::idclass_to_uid(self.from, Node)
      self.to = Node.from_db_id OSM::idclass_to_uid(self.to, Node)
    end
    def self.from_db uid, tags, time, reference
      fid, tid = reference.split ','
      Segment.new(Node.from_db_id(fid.to_i), Node.from_db_id(tid.to_i), OSM::uid_to_id(uid).to_s, time)
    end
    def self.from_db_id uid
      q = $db.query("select uid, tags, time, reference from data where uid=#{uid};").fetch_row
      if q.nil?
        $current = nil
        throw :incomplete
      end
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
  if (data.timestamp)
    time = data.timestamp.xmlschema
    time = time[0..-7] if time.include? "+"
    time = 'TIMESTAMP("'+time+'")'
	else
		time = 'NULL'
  end
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
  sql = %Q{insert into data values (#{data.to_uid}, "#{tags}", #{time}, "#{reference}", GeomFromText('POINT(#{data.bbox[0..1].join(' ')})'), GeomFromText('POINT(#{data.bbox[2..3].join(' ')})'));}
	$db.query sql
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
    catch :incomplete do
			return unless name =~ /node|segment|way/
			$obj = $obj + 1
			puts $obj if $obj % 100000 == 0
      write_sql @current
    end
  end
end

$obj = 0

puts Time.now
$stdout.flush
$stdout.sync = true
abort "planet.osm not found." unless File.exist? "planet.osm"
$db = Mysql.real_connect "localhost", "root", "", "little-osm"
$db.query "delete from data;"
REXML::Document.parse_stream File.new('planet.osm'), XmlReader.new
