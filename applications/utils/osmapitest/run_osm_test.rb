#! /bin/ruby
#
require 'osmapitest'

#Get a new test object
#bbox = [minlon, maxlon, minlat, laxlat]
test = OSMAPITEST::CreateTest.new([bbox], 'www.openstreetmap.org')

i = 0 
n = 1

#Test until interrupted
while i < n

#Select a random number of nodes for a way
nodes = rand 1000
#Select a random incrementation for x and y
xinc = rand 100
yinc = rand 100

puts "At #{Time.now}, attempting with nodes = #{nodes}, xinc = #{xinc}, yinc = #{yinc}"

   res = test.makenode(nodes,xinc,yinc)

   segs = test.makeseg(res)

   ways = test.makeway(segs)

end
