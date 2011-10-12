#!/usr/bin/ruby -w
require 'open-uri'
require 'zlib'
require 'rubygems'
require 'sqlite3'

#
# This script consumes the minutely diffs and processes them in some way
#
# It was created by Harry Wood initially to power the wimbledon tennis edit
# tracker displays. The same basic parsing principle could be used for all
# kinds of things but this is simply finding mention of a particular tag, and
# recording details of these matching edits to an SQLite DB & CSV 
#
# It includes a loop with a 60 second wait, so like this it's designed to run
# forever. The other way would be to set up a minutely cronjob to run this
# script and have it only do a single iteration each time.
#
# Within each minute iteration it has an inner loop which will run once or
# hoever many times is needed to "catch up" to the latest minutely diff. This
# is based on the contents of 'on_seq.txt' which should contain the sequence
# number of the changefile we are on (processed already). You will need to 
# create this file and set it with the "current" sequence number.
#
# This mechanism is good for allow you to restart the processing later if the
# process dies.  TODO: make it request hourly diffs instead of minutely if
# appropriate in these circumstances.
#

#figure out the AAA/BBB/CCC filename from the given integer sequence number 
def seq_to_filename(seq)
   seqs = seq.to_s()
   while seqs.length<9 :
      seqs = "0" + seqs 
   end
   return seqs[0..2] + "/" + seqs[3..5] + "/" + seqs[6..8]
end

#Give string of an opening xml tag, parse out the attributes
#String parsing of XML is very bad practice, full of brittle assumptions...   but it's fast.
def parse_attributes(tag) 
   #puts tag
   attributes = { }
   pos = tag.index("<")
   space_pos = tag.index(" ",pos)
   element_name = tag[pos+1..space_pos-1]
   attributes["element_name"] = element_name
   pos = space_pos
   while (not tag.index("=\"", pos + 1).nil?) :
      eq_pos = tag.index("=\"", pos)
      key = tag[pos..eq_pos-1].strip()
      
      end_quote_pos = tag.index("\" ", eq_pos+2)
      end_quote_pos = tag.index("\">", eq_pos+2) if end_quote_pos.nil?
      value = tag[(eq_pos+2)..(end_quote_pos-1)].strip()
      
      key="osm_id" if key=="id"
      
      attributes[key]=value

      pos = end_quote_pos + 1
   end
   
   return attributes
end

# Process a change file. Unzipping it and parsing the XML.
def process_change_file(url)
   puts "Fetching " + url
   
   begin
      diff_file = Zlib::GzipReader.new(open(url) ) 
      
      mode=""
      element_line = ""
      
      diff_file.each_line do |line|
         mode = "create" if line.include?("<create>")
         mode = "modify" if line.include?("<modify>")
         mode = "delete" if line.include?("<delete>")
         
         line_type = nil
         line_type = "node"     if line.include?("<node") 
         line_type = "way"      if line.include?("<way") 
         line_type = "relation" if line.include?("<relation") 
         
         if not line_type.nil?  :
            element_line = line   #store for later
         end
      
         if line.include?("<tag") :
               
            #Filter by tag
            if line.include?("<tag k=\"sport\" v=\"baseball\"/>") :
                  
               #We have a match. Spit out CSV and insert DB record
               attributes = parse_attributes(element_line)
            
               osm_id = attributes["osm_id"]
               osm_id = "-" if osm_id.nil?
                              
               csv = attributes['timestamp'] + ", " + mode + ", " +
                     attributes['element_name'] + ":" + osm_id + ", " +
                     "\"" + attributes['user'] + "\", " +
                     attributes['changeset']
               @csv_file.write csv  + "\n"  
               @csv_file.flush()                 

               @db.execute( "INSERT INTO edits (timestamp, op_type, element_type, osm_id, user_name, changeset) VALUES (?, ?, ?, ?, ?, ?);",
                        [ attributes['timestamp'],
                          mode,
                          attributes['element_name'],
                          osm_id,
                          attributes['user'],
                          attributes['changeset'] ] ) unless @db.nil?
            end
         end    
      end
       
   rescue EOFError
      $stderr.puts "EOFError " + $! + " when fetching " + url
      
   rescue OpenURI::HTTPError        
      $stderr.puts "OpenURI::HTTPError " + $! + " when fetching " + url
   end
   
end

#-------------

# If this script is causing load problems on the OSM server then the sysadmins
# would like to know who's running it, so we put the email address of this
# person in the URLs when we're doing all changefile download requests.
# This is polite, but it's unlikely get used.
contact = <<<your email address here>>>

@csv_file = File.new("./baseball/baseball-edits.csv", "a")

output_db = "./baseball/baseball-edits.db"

@db = nil
if File.exists?(output_db) :
   @db = SQLite3::Database.new(output_db ) if File.exists?(output_db)
else
   die "output db file '" + output_db + "' doesn't exist.\n"
end

#Find out what the last processed sequence number was from local file
on_seq_file = File.new("on_seq.txt", "r")
on_seq = on_seq_file.gets.to_i()
on_seq_file.close

puts "on_seq from file: " + on_seq.to_s()

#Loop forever (or until the process dies for some reason)
while (true) :
   
   #Get details of what the latest available changefile is from state.txt
   state_file_url = "http://planet.openstreetmap.org/minute-replicate/state.txt?contact=" + contact
   state_text = open(state_file_url) {|io| io.read} #read to string

   available_seq = 0
   available_timestamp = nil
   state_text.each_line do |line|
      available_timestamp = line[10..99] if line[0..9]=="timestamp="   
      available_seq = line[15..99].to_i() if line[0..14]=="sequenceNumber="   
   end
   puts "available_seq: " + available_seq.to_s()
   puts "available_timestamp: " + available_timestamp
   
   #Loop until we're up to date.
   #During normal operation the loop would run only once to process the latest
   #minutes changefile
   while on_seq < available_seq
      on_seq += 1
      puts "on_seq = " + on_seq.to_s()
      change_file_url = "http://planet.openstreetmap.org/minute-replicate/" + seq_to_filename(on_seq) + ".osc.gz?contact=" + contact
      
      process_change_file(change_file_url)
      
      #Update the number in the on_seq file 
      on_seq_file = File.new("on_seq.txt", "w")
      on_seq_file.write(on_seq.to_s())
      on_seq_file.close
      
   end
   
   $stdout.flush
   
   sleep 60
   
   #(After waiting a minute we'd expect a new changefile to be available) ...loop

end

#never reaches here, but if it did...
@csv_file.close()

