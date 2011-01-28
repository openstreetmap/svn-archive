require "csv"
require "./config.rb"

if @output_file.nil? 
   outfile=$stdout
   @quiet=true
else   
   puts "opening output file '" + @output_file + "'" unless @quiet
   outfile = File.open(@output_file, "w")
end

if @input_file.nil?
   infile = $stdin
else
   puts "opening input file '" + @input_file + "'" unless @quiet
   infile = File.open(@input_file, "rb")
end

outfile.write("<?xml version='1.0' encoding='UTF-8'?>\n")
outfile.write("<!-- Converted by csv2osm.rb ")
outfile.write("reading file '"+ @input_file + "' ") if not @input_file.nil?
outfile.write("at " + Time.now.to_s + " -->\n")
outfile.write("<osm version='0.6' generator='csv2osm.rb'>\n")


#XML escaping conversions
#http://blade.nagaokaut.ac.jp/cgi-bin/scat.rb/ruby/ruby-talk/263194
@@convert = {
   '&' => '&amp;',
   '<' => '&lt;',
   '>' => '&gt;',
   "'" => '&apos;',
   '"' => '&quot;'
}

on_id = -10000  #negative ids starting here

row_count = 0;

#Loop through CSV rows
CSV::Reader.parse(infile) do |row|
   if @header_row==:skip :
         #Skip the header row
         @header_row=:done 
   
   elsif @header_row==:add_to_mapping :
      #Add to the output_mapping all columns named from the header row
      row.each_with_index do |colheading,colindex|
         @output_mapping[colheading] = "$COL" + (colindex + 1).to_s
      end
      @header_row=:done  #subsequent interations are data rows
      
      puts "output_mapping with column headings added:" unless @quiet
      p @output_mapping unless @quiet
      
   else
      
      lat = nil
      lon = nil
      
      uid = nil
      user = nil
      visible = nil
      version = nil
      
      tag_xml = "" 
      @output_mapping.each do |tag_key, col_value_source|
         colvalue = ""
         if col_value_source[0..3]=="$COL"
            colindex = col_value_source[4..99].to_i - 1
            colvalue = row[colindex]
            if colvalue.nil?
               puts "WARNING row " + row_count.to_s + " had a missing value for " + col_value_source unless @quiet
               colvalue = @nil_value
            end
         else
            colvalue = col_value_source
         end
         
         #xml escaping conversions
         colvalue.gsub!(/[&<>'"]/) do | match |
            @@convert[match]
         end
         
         #Special tag keys (these become node attributes)
         if tag_key=="$NODElat" :
            lat = colvalue
         
         elsif tag_key=="$NODElon" :
            lon = colvalue
         
         elsif tag_key=="$NODEuid" :
            uid = colvalue
         
         elsif tag_key=="$NODEuser" :
            user = colvalue
         
         elsif tag_key=="$NODEvisible" :
            visible = colvalue
         
         elsif tag_key=="$NODEversion" :
            version = colvalue
         
         else 
            #Everything else becomes a tag
            tag_xml += "<tag k='" + tag_key  + "' v='" + colvalue + "' />"
            tag_xml += "\n" if not @tags_on_one_line
         end
                     
      end #next column
      
      puts "WARNING row " + row_count.to_s + " had no lat/lon value(s). Invalid <node> output" if (lat.nil? or lon.nil?) and not @quiet 
      
      on_id -= 1
      
      
      node_xml  = "<node "
      node_xml += "id='" + on_id.to_s + "' "
      node_xml += "uid='" + xmlEscape(uid) + "' " unless uid.nil?
      node_xml += "user='" + user + "' " unless user.nil?
      node_xml += "visible='" + visible + "' " unless visible.nil?
      node_xml += "version='" + version + "' " unless version.nil?
      node_xml += "lat='" + lat + "' " unless lat.nil?
      node_xml += "lon='" + lon + "'" unless lon.nil?
      node_xml += ">"
      
      node_xml += tag_xml
      
      node_xml += "</node>"
      
      outfile.write( node_xml + "\n");
      
      
      row_count +=1
      puts "converted " + row_count.to_s + " rows" if row_count % 1000 == 0 and not @quiet
      
   end #next row
   
end

outfile.write("</osm>");
outfile.close


puts "DONE" unless @quiet
