#!/usr/bin/ruby

require 'rubygems'
require 'httpclient'
require 'amf_lib'
require 'stringio'

url='http://www.openstreetmap.org/api/0.5/amf/read'
c=HTTPClient.new
bbox=[-1.4916,51.88447,-1.46949,51.86895]

# =========================================================================
# Demonstration of how to talk to amf_controller from outside Flash
#
# This example gets a list of deleted ways within the bbox, fetches each 
# deleted way, then outputs them in Potlatch's array format
# =========================================================================

# Get list of ways

amf =0.chr+0.chr						# FP8
amf+=0.chr+0.chr						# no headers
amf+=0.chr+1.chr						# one body

amf+=encodestring("whichways_deleted")	# message
amf+=encodestring("1")					# unique ID for this message
amf+=encodelong(0)						# size of body in bytes, Potlatch ignores this
amf+=encodevalue(bbox)					# argument
resp=c.request('post',url,nil,amf)

# Parse response

r=StringIO.new(resp.content)
r.read(6)								# we can ignore the first 6 chars
until r.eof?
	getstring(r)						# this should be 1/onResult
	getstring(r)						# this should be 'null'
	r.read(4)							# this should be -1
	ways=getvalue(r)					# this should be the value
end

# Get each way

for id in ways.flatten
	puts id.to_i

	# Send the request
	amf =0.chr+0.chr+0.chr+0.chr+0.chr+1.chr
	amf+=encodestring("getway_old")
	amf+=encodestring("1")+encodelong(0)
	args=[id,-1]
	amf+=encodevalue(args)
	resp=c.request('post',url,nil,amf)

	# Parse the content
	r=StringIO.new(resp.content)
	r.read(6)
	until r.eof?
		getstring(r)						# this should be 1/onResult
		getstring(r)						# this should be 'null'
		r.read(4)							# this should be -1
		deleted=getvalue(r)					# this should be the value
		puts deleted.inspect

		# ******
		# we now have the deleted way (id) returned in Potlatch format:
		#   [0, id, array of points, hash of tags, version]
		# where each point is
		#   [lat,lon,id,0,hash of tags]
		# ******

		puts
	end
end
