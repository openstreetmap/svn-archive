require 'test/unit'

$: << File.dirname(__FILE__)+'/../lib'

Dir.glob(File.dirname(__FILE__)+"/**/t_*.rb").each do |x| 
  load x
end
