def ok
  print "HTTP/1.1 200/OK\r\nServer: little-osm\r\nContent-type: text/plain\r\n\r\n"
end

def header
  print "<?xml version='1.0' encoding='UTF-8'?>\n<osm version='0.3' generator='little-osm'>"
end

def bad_request reason = "Bad Request"
  print "HTTP/1.1 400/#{reason}\r\n\r\n"
  throw :little_osm_done
end

# return the queries as hash
def get_queries
  arr = Thread.current['uri'].query.split('&').collect do |x| x.split "=" end
  q = {}
  arr.each do |x| q[x[0]] = (x[1] ||= "") end
  q
end
