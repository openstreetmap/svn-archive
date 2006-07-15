#!/usr/bin/ruby

# Read in planet.osm and create a sqlite3 database file named "planet.db"

require 'data/core'
require 'rexml/document'
require 'sqlite3'
require 'time'


# define from_db in data classes which retrieve the data from the just created database
def Node.from_db uid, tags, time, reference, minlat, minlon, maxlat, maxlon
  Node.new minlat, minlon, uid.to_i>>3, time
end
def Node.from_db_id uid
  Node.from_db(*$db.execute("select * from data where uid=#{uid};")[0].to_a)
end
def Segment.from_db uid, tags, time, reference, minlat, minlon, maxlat, maxlon
  fid, tid = reference.split ','
  Segment.new(Node.from_db_id(fid.to_i), Node.from_db_id(tid.to_i), ((uid.to_i)>>3).to_s, time)
end
def Segment.from_db_id uid
  q = $db.execute("select * from data where uid=#{uid};")[0].to_a
  throw :incomplete_way if q.empty?
  Segment.from_db(*q)
end

# define load_references in segment and way, which replaces the id-references by their real data
class Segment < OsmPrimitive
  def load_references
    self.from = Node.from_db_id Node.to_uid(self.from)
    self.to = Node.from_db_id Node.to_uid(self.to)
  end
end
class Way < OsmPrimitive
  def load_references
    segment.collect! do |id|
      Segment.from_db_id Segment.to_uid(id)
    end
  end
end


# writes the data object into the database.
def write_sql data
  tags = data.tags ? data.tags.to_a.join("\n") : "null"
  tags.gsub!(/\"/, '')
  time = data.timestamp ? '"'+data.timestamp.xmlschema+'"' : "null"
  case data.class.name
  when "Node"
  	reference = ""
  when "Segment"
  	reference = Node.to_uid(data.from).to_s + "," + Node.to_uid(data.to).to_s
  	data.load_references
  when "Way"
  	reference = data.segment.collect {|s| Segment.to_uid(s).to_s}.join ','
  	data.load_references
  end
  sql = %Q{insert into data values (#{data.to_uid}, "#{tags}", #{time}, "#{reference}", #{data.bbox.join(',')});}
  $tmpsql << sql << "\n"
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
      @current = Node.new a['lat'].to_f, a['lon'].to_f, id, time
    when "segment"
      @current = Segment.new a['from'].to_i, a['to'].to_i, id, time
    when "way"
      @current = Way.new [], id, time
    when "tag"
      @current[a['k']] = a['v']
    when "seg"
      @current.segment << a['id'].to_i
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
$db = SQLite3::Database.new PLANET_DB
$db.execute 'BEGIN'
open('planet.sql').each {|sql| $db.execute sql}
REXML::Document.parse_stream File.new('planet.osm'), XmlReader.new
$db.execute 'COMMIT'
