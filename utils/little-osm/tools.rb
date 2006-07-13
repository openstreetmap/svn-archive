def ok
  print "HTTP/1.1 200/OK\r\nServer: little-osm\r\nContent-type: text/xml\r\n\r\n"
end
  
def header
  print "<?xml version='1.0' encoding='UTF-8'?>\n<osm version='0.3' generator='little-osm'>"
end    
