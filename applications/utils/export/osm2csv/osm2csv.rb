require 'rubygems'
require 'nokogiri'
require 'csv'

#
# Simple osm2csv conversion in ruby
#
# This does not match the functionality of the perl version osm2csv.pl
#
# Currently this ignores ways & relations, it spews out a csv line each time
# a node is encountered which passes the "filterNode" test. csv values are
# filled from OSM tag values.
#
# Requires the nokogiri gem for XML parsing.
#

@outfile = $stdout
@infile = $stdin

# Define output columns
# Mapping strings are matched against tag keys
# (apart from the special strings $OSMid, $OSMuser, $NODElat, $NODElon) 
@column_mappings = ["$OSMid",
              "$NODElat",
              "$NODElat",
              "name",
              "cuisine"]

#Where no matching tag key is present
@nil_value = "nil"

# This class handles osm objects as they are encountered and outputs csv
class CSVObjectProcessor
      
   #Pass in the settings
   #Better way to access these directly from a nested class?
   def initialize(outfile, column_mappings, nil_value)
      @outfile = outfile
      @column_mappings = column_mappings
      @nil_value = nil_value
   end
      
   #array to csv string
   def to_csv(value_array)
      str=""
      CSV::Writer.generate(str) do |csv|
            csv << value_array
      end
      return str
   end

   # OSM element processing functions.
   # Modify these to process OSM elements as required
   # In this case we output CSV rows for nodes
   def processNode(osm_id, user, lat, lon, tags)
      
      if filterNode(osm_id, user, lat, lon, tags) :
            
         row = [ ]
         @column_mappings.each do |col_name|
            
            if col_name=='$OSMid' :
               row << osm_id
            
            elsif col_name=='$NODEuser' :
               row << user
            
            elsif col_name=='$NODElat' :
               row << lat
            
            elsif col_name=='$NODElon' :
               row << lon
            
            elsif tags[col_name].nil?
               row << @nil_value
               
            else
               row << tags[col_name]
            end
         end
         @outfile.write( to_csv(row) ) 
         
      end
   end
   
   def processWay(osm_id, user, refs, tags)
      #do nothing
   end
   
   #return true if the node should pass the filter
   #e.g. based on presence of amenity=restaurant tag
   def filterNode(osm_id, user, lat, lon, tags)
      if ( not tags['highway'].nil? and tags['amenity']=='restaurant' ) :
         return true
      else
         return false
      end
   end
        
end

# SAX handler for dealing with the raw XML as stream. Assembles more
# recognisably osm structured data, and passes them to the "object processor"
# class. This is quite generic and could be re-used for other things.
class OsmSAXHandler < Nokogiri::XML::SAX::Document

   @obj_processor = nil
   def initialize(set_obj_processor)
      @obj_processor = set_obj_processor
   end
   
   def start_element( name, attributes = [])
      attributes_hash = Hash[*attributes]
      if name=="node" :
         @osm_id = attributes_hash['id'].to_s.to_i
         @user = attributes_hash['user'].to_s
         
         @on_node_lat = attributes_hash['lat'].to_s
         @on_node_lon = attributes_hash['lon'].to_s
         
         @tags  = { }
         
      elsif name=="way" :
         @osm_id = attributes_hash['id'].to_s.to_i
         @user = attributes_hash['user'].to_s
         
         @refs = [ ]
         @tags  = { }
         
      elsif name=="tag" :
         #tags go into a ruby hash
         @tags[ attributes_hash['k'].to_s ] = attributes_hash['v'].to_s 
         
      elsif name=="nd" :
         #way->node refs go into an array of int ids
         @refs <<  attributes_hash['ref'].to_s.to_i
      end
            
      #puts "#{name} started"
   end
   
   
   def end_element( name )
      if name=="node" :
         @obj_processor.processNode(@osm_id, @user, @on_node_lat, @on_node_lon, @tags)
      
      elsif name=="way" :
         @obj_processor.processWay(@osm_id, @user, @refs, @tags)
      
      end
   end
   
end #end of class OsmSAXHandler


#write header row              
header_row = @column_mappings.dup
#TODO cleaner names for headings?


csvObjectProcessor = CSVObjectProcessor.new(@outfile, @column_mappings, @nil_value)

@outfile.write( csvObjectProcessor.to_csv(header_row) ) 

osmSAXHandler = OsmSAXHandler.new(csvObjectProcessor)


#kick off the SAX parsing
parser = Nokogiri::XML::SAX::Parser.new(osmSAXHandler)
parser.parse(@infile)

@outfile.close
