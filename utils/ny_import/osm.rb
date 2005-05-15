module OSM

  require 'xmlrpc/client'

  class OpenStreetMap
    
    JOURNAL_PATH = "osm-journal.txt"
    OSM_XMLRPC_URL = "http://www.openstreetmap.org/api/xml.jsp"
    ZIP_CODE_KEY_NAME = "zipCode"
    NAME_KEY_NAME = "name"
    
    def coords_to_s(coords)
      return coords.map do |coord|
        "(#{coord.first}, #{coord[1]})"
      end.join(" -> ")
    end
    
    def call(method, *args)
      result = @osm.call("openstreetmap.#{method}", *args)
      if method == "login"
        $stderr.puts "openstreetmap.login(#{args.join(", ")}) -> #{result}"
      else
        # don't display the token, redundant
        $stderr.puts "    #{method}(#{args[1..-1].join(", ")}) -> #{result}"
      end
      return result
    end
    
    def initialize(username, password)
      @osm = XMLRPC::Client.new2(OSM_XMLRPC_URL)
      @token = call("login", username, password)
      raise "Invalid username and/or password" if @token =~ /^ERROR/
      open_journal
      @zip_key_id = get_key_id(ZIP_CODE_KEY_NAME)
      @name_key_id = get_key_id(NAME_KEY_NAME)
    end
    
    def get_key_id(key_name)
      keys = call("getAllKeys", @token, true)
      raise "Could not retrieve all keys" if keys.nil?
      keys.each_index do |i|
        return keys[i - 1].to_i if keys[i] == key_name
      end
      key_id = call("newKey", @token, key_name)
      raise "Could not create ZIP code key [#{key_name}]" if key_id == -1
      journal("(\"deleteKey\", @token, #{key_id})")
      return key_id
    end
    
    def close
      close_journal
    end
    
    def open_journal
      @journal = File.new(JOURNAL_PATH, File::CREAT | File::APPEND | File::WRONLY)
    end
    
    def close_journal
      @journal.close unless @journal.nil? || @journal.closed?
    end
    
    def journal(s)
      @journal.puts s
    end
    
    def rollback
      close_journal
      File.open(JOURNAL_PATH, File::RDONLY) do |f|
        f.readlines.each do |line|
          code_snippet = line.strip
          begin
            success = eval("call#{code_snippet}")
          rescue XMLRPC::FaultException => ex
            $stderr.puts "Could not rollback on line [#{code_snippet}]" unless success
          end
        end
      end
      File.delete(JOURNAL_PATH)
      open_journal
    end
    
    def newNode(lat, long)
      raise "Invalid latitude [#{lat}] in newNode" if lat.nil? || (! lat.respond_to?(:to_f))
      raise "Invalid longitude [#{long}] in newNode" if long.nil? || (! long.respond_to?(:to_f))
      node = call("newNode", @token, lat, long)
      raise "Could not create node at (#{lat} #{long})" if node == -1
      journal("(\"deleteNode\", @token, #{node})")
      return node
    end
    
    def newLine(from_lat, from_long, to_lat, to_long)
      from_id = newNode(from_lat, from_long)
      to_id = newNode(to_lat, to_long)
      line_id = call("newLine", @token, from_id, to_id)
      raise "Could not create line from (#{from_lat} #{from_long}) to (#{to_lat} #{to_long})" if line_id == -1
      journal("(\"openstreetmap.deleteLine\", @token, #{line_id})")
      return line_id, to_id
    end
    
    def newExtendedLine(from_node_id, to_lat, to_long)
      to_id = newNode(to_lat, to_long)
      line_id = call("newLine", @token, from_node_id, to_id)
      raise "Could not create line from ID #{from_node_id} to (#{to_lat} #{to_long})" if line_id == -1
      journal("(\"openstreetmap.deleteLine\", @token, #{line_id})")
      return line_id, to_id
    end
    
    def assoc_zip(line_id, zip)
      call("updateStreetSegmentKeyValue", @token, line_id, @zip_key_id, zip.to_s)
    end
    
    def newStreet(name, coords, from_zip = nil, to_zip = nil)
      $stderr.puts "Creating new street [#{name}], ZIPs: #{from_zip} to #{to_zip}, coords: #{coords_to_s(coords)}" 
      raise "Attempt to create street with less than two coordinates" if coords.nil? || (coords.length < 2)
      line_id, prev_node_id = newLine(coords.first.first, coords.first[1], coords[1].first, coords[1][1])
      street_id = call("newStreet", @token, line_id)
      raise "Could not create new street" if street_id == -1
      journal("(\"deleteStreet\", @token, #{street_id})")
      success = call("updateStreetKeyValue", @token, street_id, @name_key_id, name)
      raise "Could not name street #{street_id} \"#{name}\"" unless success
      assoc_zip(line_id, from_zip) unless from_zip.nil?
      if coords.length > 2
        (2..(coords.length - 1)).each do |i|
          to_lat, to_long = coords[i]
          line_id, prev_node_id = newExtendedLine(prev_node_id, to_lat, to_long)
          success = call("addSegmentToStreet", @token, street_id, line_id)
          raise "Could not add segment #{line_id} to street #{street_id}" unless success
          journal("(\"dropSegmentFromStreet\", @token, #{street_id}, #{line_id})")
          assoc_zip(line_id, to_zip) unless to_zip.nil? || (i < (coords.length - 1))
        end
      end
    end
    
  end

end

