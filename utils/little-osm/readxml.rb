#!/usr/bin/ruby

require 'data/core'
require 'rexml/document'
require 'sqlite3'
require 'time'

planet_osm = Dir.pwd+"/planet.osm"
unless File.exist? planet_osm
  candidates = Dir.glob "planet*.osm"
  planet_osm = candidates[0] unless candidates.empty?
end

abort "#{Dir.pwd}/#{planet_osm} not found." unless File.exist? planet_osm

# cleanup database
File.delete 'planet.db' if File.exist? 'planet.db'
$db = SQLite3::Database.new 'planet.db'
$db.execute 'BEGIN'
open('planet.sql').each {|sql| $db.execute sql}


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

$db.execute 'COMMIT'
