#!/usr/bin/ruby
require 'socket'
require 'uri'

$stdout.sync = true

puts "Start server on localhost:3000"
server = TCPServer.new('127.0.0.1', 3000)
loop do
  session = server.accept
  puts "connection accepted"
  Thread.start(session) do |session|
    uri = URI.parse session.gets.scan(/GET \/(.*) HTTP/)[0][0]
    cmd = uri.path.split("/")[-1]
    cmd ||= "test"
    Thread.current["uri"] = uri
    Thread.current["session"] = session
    puts Thread.current.object_id.to_s + ": call command '#{cmd}'"
    old_out, old_in = $stdout, $stdin
    $stdout, $stdin = session, session
    begin
      catch :little_osm_done do
        load "api/#{cmd}.rb"
      end
    rescue => x
      session.print "HTTP/1.1 500/Exception executing script\r\n",
        "Server: little-osm\r\n",
        "Content-type: text/plain\r\n\r\n",
        "Error while executing script #{cmd}.rb:\r\n",
        "#{x}\r\n  ",
        x.backtrace.join("\r\n  ")
    ensure
      $stdout, $stdin = old_out, old_in
      puts Thread.current.object_id.to_s + ": call finished"
      session.close
    end
  end
end
