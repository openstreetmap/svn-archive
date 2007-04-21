require 'test/unit'

$: << File.dirname(__FILE__)+'/../lib'

Dir.glob(File.dirname(__FILE__)+"/**/tc_*.rb").each do |x|
  puts "executing #{x}"
  load x
end
