#!/usr/bin/ruby
require 'socket'
require 'uri'

$stdout.sync = true rescue 

puts "Start server on localhost:3000"
server = TCPServer.new('127.0.0.1', 3000)
loop do
  session = server.accept
  puts "connection accepted"
  Thread.start(session) do |session|
    request = session.gets.chomp
    puts "request: "+request
    cmd = "test" unless request =~ /GET .* HTTP.*/
    cmd = request.gsub(/GET \//, '').gsub(/ HTTP.*/, '').strip if cmd == nil
    Thread.current["uri"] = URI.parse request.scan(/GET \/(.*) HTTP/)[0][0]
    Thread.current["session"] = session
    m = cmd.match(/\?|\//)
    cmd = m.pre_match if m
    puts "call command '#{cmd}'"
    begin
      old_out, old_in = $stdout, $stdin
      $stdout, $stdin = session, session
      begin
        load "api/#{cmd}.rb"
      rescue => x
        session.print "HTTP/1.1 500/Exception executing script\r\n",
          "Server: little-osm\r\n",
          "Content-type: text/plain\r\n\r\n",
          "Error while executing script #{cmd}.rb:\r\n",
          "#{x}\r\n  ",
          x.backtrace.join("\r\n  ")
      ensure
        $stdout, $stdin = old_out, old_in
      end
    ensure
      session.close
    end
  end
end
