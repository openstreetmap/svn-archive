#!/usr/bin/ruby
require 'digest/md5'

SALT = 'PUT YOUR SALT HERE'

while line = gets
  ar = line.split(' ')
  ar[0] = Digest::MD5.hexdigest(SALT + ar[0])
  ar[2] = Digest::MD5.hexdigest(SALT + ar[2])
  puts ar.join(' ')
end


