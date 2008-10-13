#!/usr/bin/ruby

require 'open-uri'
require 'date'
require 'expire'

##
## EDIT THESE PARAMETERS TO REFLECT YOUR LOCAL INSTALLATION
##
REMOTE_REPOS="http://planet.openstreetmap.org/hourly"
TIMESTAMP=REMOTE_REPOS + "/timestamp.txt"
LOCAL_REPOS="/home/matt/planets/hourly"
OSM2PGSQL_DIR="/home/matt/src/osm2pgsql"

def get_lock(file_name, &block)
  # open the locking file, in our case this is a time stamp
  f = File.new(file_name, "r+")

  # assume we didn't get the lock
  got_lock = false

  begin
    # try to acquire an exclusive lock, but return instantly if
    # we can't.
    got_lock = f.flock(File::LOCK_EX | File::LOCK_NB)

    if (got_lock)
      # while we're in the passed block we can use the file
      # knowing that nothing else is using it.
      block.call(f)
    end

  ensure 
    # make sure we don't leave the file locked, even if an exception
    # is thrown.
    f.flock(File::LOCK_UN)
  end

  return got_lock
end

# the name of an hourly change file which *ends* at +time+.
def change_file(time)
  prev_time = time - Rational(1,24)
  prev_time.strftime("%Y%m%d%H") + "-" + time.strftime("%Y%m%d%H") + ".osc.gz"
end

# change to the osm2pgsql directory so it can find default.style and
# update the postgres database.
def update_pgsql(osc)
  Dir.chdir(OSM2PGSQL_DIR) do
    `./osm2pgsql --merc --slim --append --database gis #{osc}`
  end
end

# main method:
Dir.chdir(LOCAL_REPOS) do
  # lock the timestamp file while the script is running. this prevents a 
  # long-running update from being screwed up by another instance of this
  # script being run. for example, i run this every 10 minutes from cron, 
  # but some hourly changes can take longer than 10 minutes to update & 
  # expire...
  get_lock("timestamp.txt") do |f|
    puts "Locked"

    last_update = DateTime.parse(f.read)
    last_available = DateTime.parse(open(TIMESTAMP).read)
    # loop over all the available updates, if there are any
    if (last_update < last_available)
      while (last_update < last_available)
        # increment the time by an hour (1/24) of a day, in DateTime terms
        last_update += Rational(1,24)
        file = change_file(last_update)

        # download the hourly change - use curl or whatever here if you don't
        # have wget installed.
        `wget -O "#{file}" "#{REMOTE_REPOS + '/' + file}"`

        update_pgsql(LOCAL_REPOS + "/" + file)
        Expire::expire(file)
      end

      # update the timestamp
      g = File.new(f.path, "w")
      g.write(last_update.strftime("%Y-%m-%dT%H:%M:%SZ\n"))
    else
      puts "No updates available."
    end
    puts "Unlocked"
  end or puts "Didn't get the lock"
end
