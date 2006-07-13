print "HTTP/1.1 200/OK\r\nServer: little-osm\r\nContent-type: text/plain\r\n\r\n"
puts "little-osm is up and running. API version 0.3"
puts
puts "URL: #{Thread.current['uri'].to_s}"
puts "From: #{Thread.current['session'].peeraddr[2]}:#{Thread.current['session'].peeraddr[1]}"
