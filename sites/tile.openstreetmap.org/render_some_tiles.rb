#!/usr/bin/ruby

# exit if the lockfile is there

lockfile = '/tmp/tilegen_running'

if File.file?(lockfile)
  $stderr << 'exiting, lock file present'
  exit!
else
  #otherwise make one
  File.open(lockfile, 'w') do |file|
    file << 'eh-oh'
  end
end


puts `/home/steve/get_tiles_to_render.rb | /home/steve/render_from_list.py`


`rm #{lockfile}`
