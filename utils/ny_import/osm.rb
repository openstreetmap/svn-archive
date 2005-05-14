module OSM

  require 'xmlrpc/client'

  class OpenStreetMap
    
    JOURNAL_PATH = "osm-journal.txt"
    OSM_XMLRPC_URL = "http://www.openstreetmap.org/api/xml.jsp"
    ZIP_CODE_KEY_NAME = "zipCode"
    NAME_KEY_NAME = "name"
    
    def call(method, *args)
      $stderr.puts "openstreetmap.#{method}(#{args.join(", ")})"
      return @osm.call("openstreetmap.#{method}", *args)
    end
    
    def initialize(username, password)
      @osm = XMLRPC::Client.new2(OSM_XMLRPC_URL)
      @token = call("login", username, password)
      raise "Invalid username and/or password" if @token =~ /^ERROR/
      open_journal
      @zip_key_id = get_key_id(ZIP_CODE_KEY_NAME)
      @name_key_id = get_key_id(NAME_KEY_NAME)
      $stderr.puts "Retrieved keys: name = #{@name_key_id}, ZIP = #{@zip_key_id}"
    end
    
    def get_key_id(key_name)
      keys = call("getAllKeys", @token, true)
      raise "Could not retrieve all keys" if keys.nil?
      keys.each_index do |i|
        return keys[i - 1].to_i if keys[i] == key_name
      end
      key_id = call("newKey", @token, key_name)
      raise "Could not create ZIP code key \"#{key_name}\"" if key_id == -1
      journal("(\"deleteKey\", @token, #{key_id})")
      return key_id
    end
    
    def close
      begin
#        call("closeDatabase")
      rescue XMLRPC::FaultException => ex
        $stderr.puts "Exception \"#{ex}\" when closing XMLRPC session"
      end
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
            $stderr.puts "Could not rollback on line \"#{code_snippet}\"" unless success
          end
        end
      end
      File.delete(JOURNAL_PATH)
      open_journal
    end
    
    def newNode(lat, long)
      node = call("newNode", @token, lat, long)
      raise "Could not create node at (#{lat} #{long})" if node == -1
      journal("(\"deleteNode\", @token, #{node})")
      return node
    end
    
    def newLine(from_lat, from_long, to_lat, to_long)
      from = newNode(from_lat, from_long)
      to = newNode(to_lat, to_long)
      line = call("newLine", @token, from, to)
      raise "Could not create line from (#{lat1} #{long1}) to (#{lat2} #{long2})" if line == -1
      journal("(\"openstreetmap.deleteLine\", @token, #{line})")
      return line
    end
    
    def assoc_zip(line_id, zip)
      call("updateStreetSegmentKeyValue", @token, line_id, @zip_key_id, zip.to_s)
    end
    
    def newStreet(name, coords, from_zip = nil, to_zip = nil)
      raise "Attempt to create street with less than three coordinates" unless coords.length >= 3
      street_id = nil
      (0..(coords.length - 2)).each do |i|
        from_lat, from_long = coords[i]
        to_lat, to_long = coords[i + 1]
        line_id = newLine(from_lat, from_long, to_lat, to_long)
        if i.zero?
          street_id = call("newStreet", @token, line_id)
          raise "Could not create new street" if street_id == -1
          journal("(\"deleteStreet\", @token, #{street_id})")
          success = call("updateStreetKeyValue", @token, street_id, @name_key_id, name)
          raise "Could not name street #{street_id} \"#{name}\"" unless success
        else
          success = call("addSegmentToStreet", @token, street_id, line_id)
          journal("(\"dropSegmentFromStreet\", @token, #{street_id}, #{line_id})")
          raise "Could not add segment #{line_id} to street #{street_id}" unless success
        end
        assoc_zip(line_id, from_zip) if i.zero? && (! from_zip.nil?)
        assoc_zip(line_id, to_zip) if (i == (coords.length - 2)) && (! to_zip.nil?)
      end
    end
    
  end

end

