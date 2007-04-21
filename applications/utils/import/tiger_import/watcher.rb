#!/usr/bin/env ruby

THIS_DIR = File.expand_path(File.dirname(__FILE__))
TIGER_PID_PATH = "#{THIS_DIR}/import.pid"

def tiger_import_running?
	return false unless File.exist?(TIGER_PID_PATH)
	pid = IO.read(TIGER_PID_PATH).strip.to_i
	`/bin/ps -p #{pid}`
	$?.to_i.zero?
end

`#{THIS_DIR}/import.rb #{THIS_DIR}/spider/tiger2005fe` unless tiger_import_running?

