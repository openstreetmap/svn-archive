
# Class for better handling access to the servers REST api.

require "open-uri"

require "data/xml"

module OSM

  class RESTAccess

    def initialize apiurl = 'http://www.openstreetmap.org', username = 'immanuel.scholz@gmx.de', password = 'immanuel'
      @apiurl, @username, @password = apiurl, username, password
      @apiurl += "/" unless @apiurl[-1] == "/"
    end


    # Pass uids of objects to retrieve them from the server.
    # if you pass a single uid, it will be retrieved and returned.
    # If you pass an array, you get an array of all objects in same order.
    # In case of problems, an OsmError will be raised.
    def get uid
      n = uid_to_class(uid).canonical_name
      if uid.respond_to? :each
        path = "#{n}s?#{n}s="+uid.join(",")
      else
        path = n+"/"+uid.to_s
      end
      resp = open(@apiurl+path, :http_basic_authentication=>[@username, @password]).read
      raise "Got an empty document" if resp.nil? or resp == ""
      result = []
      REXML::Document.new(resp).root.each_element do |e| result << OsmPrimitive.from_rexml(e) end
      if uid.respond_to? each
        result[0]
      else
        result
      end
    end
  end
end
