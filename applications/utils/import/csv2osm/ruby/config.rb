@input_file = ARGV[0] #ARGV[0] means take input file name as a command line argument

@output_file = nil #nil means write to standard out

#Define an output mapping. Which <tag> k's & v's to generate from which columns?
@output_mapping = { } #{ } only makes sense if we're using :add_to_mapping 

#Example output mapping
#@output_mapping = {
#   "source" => "TfL bus_stops csv2osm.rb",  #source tag. A static value
#   "$NODElat" => "$COL3",       #reference to an input column (numbered from 1)           
#   "$NODElon" => "$COL4"        #Note that lat and lot are 'special' names.
#}

# :add_to_mapping - add mappings for all columns defined in the header row
# :skip - skip over the header row
# :none - there is no header row
@header_row = :add_to_mapping

#add newlines only for each <node> ?
@tags_on_one_line = false; 

#value to put in tags in case of missing values (gives warnings)
@nil_value = "NIL" 

#Output information messages. Overridden when @output_file is nil
@quiet = ARGV.include?('quiet') #looks for 'quiet' command line argument
