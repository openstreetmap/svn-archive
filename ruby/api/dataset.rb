#! /usr/bin/ruby -w

# Parses a complete OSM 0.2 format XML document containing both nodes and
# segments.
# If the node IDs are negative, and corresponding negative node IDs are
# present in the segments, it will be assumed that the user is uploading new
# nodes and segments. New nodes will be created and segments will be formed 
# between the new nodes. Each new node will be reallocated a real, positive ID.
# For example, if the file
#
# <osm version='0.2'>
# <node lat='51' lon='-1' uid='-1'/>
# <node lat='51.01' lon='-1.01' uid='-2'/>
# <node lat='51.04' lon='-1.02' uid='-3'/>
# <node lat='51.09' lon='-1.03' uid='-4'/>
# <node lat='52' lon='0' uid='200000' />
# <segment from='-1' to='-2'/>
# <segment from='-2' to='-3'/>
# <segment from='-3' to='-4'/>
# </osm>
#
# is uploaded, four new nodes (the ones with supplied uid of -1 to -4) will
# be created, one existing node (200000) will be updated, and three segments
# will be formed between the four new nodes.
# The real, positive node IDs allocated to the new nodes (with supplied
# negative IDs) will be returned as standard output.

require 'cgi'
load 'osm/dao.rb'
require 'rexml/document'

include Apache
include REXML

r = Apache.request

dao = OSM::Dao.instance

if r.request_method == "PUT"
	r.setup_cgi_env
	userid = dao.useruidfromcreds(r.user,r.get_basic_auth_pw)
	if userid > 0

		id_conv_table = {}

		# This will work if the XML is on separate lines. 
		# reading in from 'gets' won't.
		doc = Document.new $stdin.read

		# Go through each node tag...
		doc.elements.each('osm/node') do |node|
			# Get the attributes
			lat = node.attributes['lat'].to_f
			lon = node.attributes['lon'].to_f
			nodeid = node.attributes['uid'].to_i
			tags = node.attributes['tags']

			tags = '' unless tags
			nodeid = 0 unless nodeid

			# If the supplied ID of the node is 0 or less, it's a new node so 
			# act accordingly.
			if nodeid <= 0 
				new_node_uid = dao.create_node(lat,lon,userid,tags)
				if not new_node_uid
					exit HTTP_INTERNAL_SERVER_ERROR
				end

				# Map the supplied nodeid to the allocated real nodeid 
				# We'll use this later when doing segments
				if nodeid < 0 
					id_conv_table[nodeid] = new_node_uid
				end
				
				# Write out the allocated real node ID
				puts new_node_uid
			else
				# If the nodeid is greater than 0, we want to do an update
				if not dao.update_node?(nodeid,userid,lat,lon,tags)
					exit HTTP_INTERNAL_SERVER_ERROR
				end
			end
		end

		# Go through each segment tag...
		doc.elements.each('osm/segment') do |segment|
			from = segment.attributes['from'].to_i
			to = segment.attributes['to'].to_i
			segid = segment.attributes['uid'].to_i
			tags = segment.attributes['tags']
			tags = '' unless tags
			segid = 0 unless segid

			# If the 'from' node is less than 0, it will relate to the negative
			# IDs supplied in the node list. So convert it to the corresponding
			# real node id.
			if from < 0
				from = id_conv_table[from]
			end

			# same with 'to'
			if to < 0 
				to = id_conv_table[to]
			end

			# If negative numbers which don't map to supplied nodes were given,
			# throw an Internal Server Error and exit.
			if from == nil or to == nil
				exit HTTP_INTERNAL_SERVER_ERROR
			end


			# Again either create or update segment depending on the segment ID
			if segid <= 0
				if not dao.create_segment(from,to,userid,tags)
					exit HTTP_INTERNAL_SERVER_ERROR
				end
			else
				if not dao.update_segment?(segid,userid,from,to,tags)
					exit HTTP_INTERNAL_SERVER_ERROR
				end
			end
		end
	else
		exit AUTH_REQUIRED
	end
end
