if __FILE__ == $0
  dir = File.dirname(__FILE__)
  Dir["#{dir}/**/*_spec.rb"].each do |file|
#    puts "require '#{file}'"
    require file
  end
end
