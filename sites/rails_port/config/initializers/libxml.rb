require 'rubygems'
gem 'libxml-ruby', '>= 0.8.1'
require 'libxml'

LibXML::XML::Parser.register_error_handler do |message|
  raise message
end
