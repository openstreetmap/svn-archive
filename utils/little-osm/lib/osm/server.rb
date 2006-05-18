#!/usr/bin/ruby

require 'webrick'

class OsmServer < WEBrick::HTTPServer
  def initialize args = {}
    args[:DocumentRoot] = File.expand_path('api', File.dirname(__FILE__)) unless args.has_key? :DocumentRoot
    p args[:DocumentRoot]
    args[:Port] = 3000 unless args.has_key? :Port
    args[:CGIPathEnv] = ENV['PATH'] unless args[:CGIPathEnv]
    args[:CGIPathEnv] = args[:CGIPathEnv] + File::PATH_SEPARATOR + File.dirname(__FILE__)
    super args

    # mount every file under api/
    Dir.glob(args[:DocumentRoot]+"/**/*") do |file|
      name = file[%r{.*(/api/.*)$}, 1]
      self.mount name, WEBrick::HTTPServlet::CGIHandler, file
    end
    trap "INT" do self.shutdown end
  end
end

OsmServer.new.start if __FILE__ == $0
