require 'singleton'
require 'logger'
load 'servinfo.rb'

class Osmlog
  include Singleton

  @@l = Logger.new('/tmp/' + $SERVER_NAME, shift_age = 'daily')

  def log(s)
    puts s
    @@l.info(s)
  end

end
