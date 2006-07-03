#!/usr/bin/ruby
r = Apache.request
r.content_type = 'image/png'
require 'cgi'
cgi = CGI.new

dirname = '/tmp/' + rand().to_s

bbox = cgi['bbox'] #'-0.175782,51.50847560607327,-0.087891,51.53581871485236'

bbox = cgi['BBOX'] if bbox = ''

ex = "bash /var/www/tile/ruby/svg.sh #{dirname} #{bbox}".untaint

puts `#{ex}`

