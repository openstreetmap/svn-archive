#!/usr/bin/ruby
# This is a small hand-made http server. (Why not use webbrick? Because it always
# caches the whole response before sending it to the client.)

require 'socket'
require 'uri'

# use all api files here. For Debug mode, they will later be reloaded, so that
# changes are in effect even if the server is not restarted.
require 'api/map'
require 'api/test'

include OSM

$stdout.sync = true if $-v

puts "Start server on localhost:3000"
server = TCPServer.new('127.0.0.1', 3000)
loop do
  session = server.accept
  puts "connection accepted" if $-v
  Thread.start(session) do |session|
    uri = URI.parse session.gets.scan(/GET \/(.*) HTTP/)[0][0]
    cmd = uri.path.scan(/[^\/]*/)[0]
    cmd ||= "test"
    puts Thread.current.object_id.to_s + ": call command '#{cmd}'" if $-v
    queries_arr = uri.query.split('&').collect do |x| x.split "=" end
    queries = {}
    queries_arr.each do |x| queries[x[0]] = (x[1] ||= "") end
    begin
      load "api/#{cmd}.rb" if $DEBUG # Reload the file, so that developers can change it on running server
      catch :little_osm_done do
        eval "OSM::#{cmd}(uri, queries, session)"
      end
    rescue => x
      puts Thread.current.object_id.to_s + ": Error while executing #{cmd}: #{x}" if $-v
      # If the error occoured after the api call sent the ok-header, this would not make much sense, but
      # it is the best hint we can give to the client in an unbuffered server response.. (and hopefully,
      # this will invalidate every xml structure making the response unlikely to be misinterpreted ;)
      session.print "HTTP/1.1 500/Exception executing script\r\n",
        "Server: little-osm\r\n",
        "Content-type: text/plain\r\n\r\n",
        "Error while executing script #{cmd}.rb:\r\n",
        "#{x}\r\n  ",
        x.backtrace.join("\r\n  ")
    ensure
      puts Thread.current.object_id.to_s + ": call finished" if $-v
      session.close
    end
  end
end
