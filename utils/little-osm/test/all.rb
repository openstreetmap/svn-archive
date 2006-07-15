require 'test/unit'

$: << File.dirname(__FILE__)+'/..'

Dir.glob(File.dirname(__FILE__)+"/**/t_*.rb").each do |x|
  puts "executing #{x}"
  load x
end
