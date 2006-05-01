require 'rubygems'

spec = Gem::Specification.new do |s|
    s.name = "little-osm"
    s.version = "1.0.0"
    s.author = "Immanuel Scholz"
    s.email = "immanuel.scholz@gmx.de"
    s.homepage = "http://www.openstreetmap.org"
    s.platform = Gem::Platform::RUBY
    s.summary = "A little server for home machines serving OSM API."
    files = Dir.glob "{lib,test,conf}/**/*"
    files << ".project" << "README" << "little-osm.gemspec"
    s.files = files.delete_if do |f| f =~ /\.db$|\.osm$/ end
    s.require_path = "lib"
    s.test_file = "test/all.rb"
    s.has_rdoc = false
end

if $0 == __FILE__
    Gem::manage_gems
    Gem::Builder.new(spec).build
end

