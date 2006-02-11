require 'singleton'
require 'logger'
load 'servinfo.rb'

class Osmlog
  include Singleton
  
  begin
    @@l = Logger.new('/tmp/' + $SERVER_NAME, shift_age = 'daily')
  rescue
    @@l = Logger.new('/tmp/' + $SERVER_NAME + '-non_apache', shift_age = 'daily')
  end

  def log(s)
    begin
      @@l.info(s)
    rescue Error =>ex
      
    end
  end

end
