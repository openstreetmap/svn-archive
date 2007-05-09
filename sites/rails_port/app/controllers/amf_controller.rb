class AmfController < ApplicationController
=begin
  require 'stringio'

# to log:
# RAILS_DEFAULT_LOGGER.error("Args: #{args[0]}, #{args[1]}, #{args[2]}, #{args[3]}")

  # ====================================================================
  # Main AMF handler
  
  # ---- talk	process AMF request

  def talk
	req=StringIO.new(request.raw_post)	# Get POST data as request
	req.read(2)							# Skip version indicator and client ID
	results={}							# Results of each body

	# -------------
	# Parse request

	headers=getint(req)					# Read number of headers
	for i in (1..headers)				# Read each header
		name=getstring(req)				#  |
		req.getc						#  | skip boolean
		value=getvalue(req)				#  |
		header["name"]=value			#  |
	end

	bodies=getint(req)					# Read number of bodies
	for i in (1..bodies)				# Read each body
		message=getstring(req)			#  | get message name
		index=getstring(req)			#  | get index in response sequence
		bytes=getlong(req)				#  | get total size in bytes
		args=getvalue(req)				#  | get response (probably an array)
	
		case message
			when 'getpresets';	results[index]=putdata(index,getpresets)
			when 'whichways';	results[index]=putdata(index,whichways(args))
			when 'getway';		results[index]=putdata(index,getway(args))
			when 'putway';		results[index]=putdata(index,putway(args))
			when 'deleteway';	results[index]=putdata(index,deleteway(args))
		end
	end

	# ------------------
	# Write out response

	response.headers["Content-Type"]="application/x-amf"
	a,b=results.length.divmod(256)
	ans=0.chr+0.chr+0.chr+0.chr+a.chr+b.chr
	results.each do |k,v|
		ans+=v
	end
	render :text=>ans

  end


	# ====================================================================
	# Remote calls

	# -----	getpresets
	#		return presets,presetmenus and presetnames arrays

	def getpresets
		presets={}
		presetmenus={}; presetmenus['point']=[]; presetmenus['way']=[]
		presetnames={}; presetnames['point']={}; presetnames['way']={}
		presettype=''
		presetcategory=''
		
		File.open("config/potlatch/presets.txt") do |file|
			file.each_line {|line|
				t=line.chomp
				if (t=~/(\w+)\/(\w+)/) then
					presettype=$1
					presetcategory=$2
					presetmenus[presettype].push(presetcategory)
					presetnames[presettype][presetcategory]=["(no preset)"]
				elsif (t=~/^(.+):\s?(.+)$/) then
					pre=$1; kv=$2
					presetnames[presettype][presetcategory].push(pre)
					presets[pre]={}
					kv.split(',').each {|a|
						if (a=~/^(.+)=(.*)$/) then presets[pre][$1]=$2 end
					}
				end
			}
		end
		[presets,presetmenus,presetnames]
	end

	# -----	whichways(left,bottom,right,top)
	#		return array of ways in current bounding box
	#		at present, instead of using correct (=more complex) SQL to find
	#		corner-crossing ways, it simply enlarges the bounding box by +/- 0.01
	
	def whichways(args)
		waylist=WaySegment.find_by_sql("SELECT DISTINCT current_way_segments.id AS wayid"+
			 "  FROM current_way_segments,current_segments,current_nodes "+
			 " WHERE segment_id=current_segments.id "+
			 "   AND current_segments.visible=1 "+
			 "   AND node_a=current_nodes.id "+
			 "   AND (latitude  BETWEEN "+(args[1].to_f-0.01).to_s+" AND "+(args[3].to_f+0.01).to_s+") "+
			 "   AND (longitude BETWEEN "+(args[0].to_f-0.01).to_s+" AND "+(args[2].to_f+0.01).to_s+")")
		ways=[]
		waylist.each {|a|
			ways<<a.wayid.to_i
		}
		ways
	end

	# -----	getway (objectname, way, baselong, basey, masterscale)
	#		returns objectname, array of co-ordinates, attributes,
	#				xmin,xmax,ymin,ymax
	
	def getway(args)
		objname,wayid,baselong,basey,masterscale=args
		wayid=wayid.to_i
		points=[]
		lastid=-1
		xmin=999999; xmax=-999999
		ymin=999999; ymax=-999999

		readwayquery(wayid).each {|row|
			xs1=long2coord(row['long1'].to_f,baselong,masterscale); ys1=lat2coord(row['lat1'].to_f,basey,masterscale)
			xs2=long2coord(row['long2'].to_f,baselong,masterscale); ys2=lat2coord(row['lat2'].to_f,basey,masterscale)
			if (row['id1'].to_i!=lastid)
				points<<[xs1,ys1,row['id1'].to_i,0,tag2array(row['tags1']),0]
			end
			lastid=row['id2'].to_i
			points<<[xs2,ys2,row['id2'].to_i,1,tag2array(row['tags2']),row['segment_id'].to_i]
			xmin=[xmin,row['long1'].to_f,row['long2'].to_f].min
			xmax=[xmax,row['long1'].to_f,row['long2'].to_f].max
			ymin=[ymin,row['lat1'].to_f,row['lat2'].to_f].min
			ymax=[ymax,row['lat1'].to_f,row['lat2'].to_f].max
		}

		attributes={}
		attrlist=ActiveRecord::Base.connection.select_all "SELECT k,v FROM current_way_tags WHERE id=#{wayid}"
		attrlist.each {|a| attributes[a['k']]=a['v'] }

		[objname,points,attributes,xmin,xmax,ymin,ymax]
	end
	
	# -----	putway (user token, way, array of co-ordinates, array of attributes,
	#				baselong, basey, masterscale)
	#		returns current way ID, new way ID, hash of renumbered nodes,
	#				xmin,xmax,ymin,ymax

	def putway(args)
		usertoken,originalway,points,attributes,baselong,basey,masterscale=args
		uid=getuserid(usertoken); if !uid then return end
		db_uqs='uniq'+uid.to_s+originalway.to_i.abs.to_s+Time.new.to_i.to_s	# temp uniquesegments table name, typically 51 chars
		db_now='@now'+uid.to_s+originalway.to_i.abs.to_s+Time.new.to_i.to_s	# 'now' variable name, typically 51 chars
		ActiveRecord::Base.connection.execute("SET #{db_now}=NOW()")
		originalway=originalway.to_i
		
		# -- 3.	read original way into memory
	
		xc={}; yc={}; tagc={}; seg={}
		if (originalway>0)
			way=originalway
			readwayquery(way).each { |row|
				id1=row['id1'].to_i; xc[id1]=row['long1'].to_f; yc[id1]=row['lat1'].to_f; tagc[id1]=row['tags1']
				id2=row['id2'].to_i; xc[id2]=row['long2'].to_f; yc[id2]=row['lat2'].to_f; tagc[id2]=row['tags2']
				seg[row['segment_id'].to_i]=id1.to_s+'-'+id2.to_s
			}
		else
			way=ActiveRecord::Base.connection.insert("INSERT INTO current_ways (user_id,timestamp,visible) VALUES (#{uid},#{db_now},1)")
		end

		# -- 4.	get version by inserting new row into ways

		version=ActiveRecord::Base.connection.insert("INSERT INTO ways (id,user_id,timestamp,visible) VALUES (#{way},#{uid},#{db_now},1)")

		# -- 5. compare nodes and update xmin,xmax,ymin,ymax

		xmin=999999; xmax=-999999
		ymin=999999; ymax=-999999
		insertsql=''
		nodelist=''
		renumberednodes={}
	
		points.each_index do |i|
			xs=coord2long(points[i][0],masterscale,baselong)
			ys=coord2lat(points[i][1],masterscale,basey)
			xmin=[xs,xmin].min; xmax=[xs,xmax].max
			ymin=[ys,ymin].min; ymax=[ys,ymax].max
			node=points[i][2].to_i
			tagstr=array2tag(points[i][4])
			tagsql="'"+sqlescape(tagstr)+"'"
	
			# compare node
			if node<0
				# new node - create
				newnode=ActiveRecord::Base.connection.insert("INSERT INTO current_nodes (   latitude,longitude,timestamp,user_id,visible,tags) VALUES (           #{ys},#{xs},#{db_now},#{uid},1,#{tagsql})")
						ActiveRecord::Base.connection.insert("INSERT INTO nodes         (id,latitude,longitude,timestamp,user_id,visible,tags) VALUES (#{newnode},#{ys},#{xs},#{db_now},#{uid},1,#{tagsql})")
				points[i][2]=newnode
				renumberednodes[node.to_s]=newnode.to_s
				
			elsif xc.has_key?(node)
				# old node from original way - update
				if (xs!=xc[node] or (ys/0.0000001).round!=(yc[node]/0.0000001).round or tagstr!=tagc[node])
					ActiveRecord::Base.connection.insert("INSERT INTO nodes (id,latitude,longitude,timestamp,user_id,visible,tags) VALUES (#{node},#{ys},#{xs},#{db_now},#{uid},1,#{tagsql})")
					ActiveRecord::Base.connection.update("UPDATE current_nodes SET latitude=#{ys},longitude=#{xs},timestamp=#{db_now},user_id=#{uid},tags=#{tagsql},visible=1 WHERE id=#{node}")
				else
					if (nodelist!='') then nodelist+=',' end; nodelist+=node.to_s
				end
			else
				# old node, created in another way and now added to this way
				if (nodelist!='') then nodelist+=',' end; nodelist+=node.to_s
			end
	
		end

		if nodelist!='' then
			ActiveRecord::Base.connection.update("UPDATE current_nodes SET timestamp=#{db_now},user_id=#{uid},visible=1 WHERE id IN (#{nodelist})")
		end

		# -- 6.i compare segments
	
		numberedsegments={}
		seglist=''
		for i in (0..(points.length-2))
			if (points[i+1][3].to_i==0) then next end
			segid=points[i+1][5].to_i
			from =points[i  ][2].to_i
			to   =points[i+1][2].to_i
			if seg.has_key?(segid)
				if seg[segid]=="#{from}-#{to}" then 
					if (seglist!='') then seglist+=',' end; seglist+=segid.to_s
					next
				end
			end
			segid=ActiveRecord::Base.connection.insert("INSERT INTO current_segments (   node_a,node_b,timestamp,user_id,visible,tags) VALUES (         #{from},#{to},#{db_now},#{uid},1,'')")
				  ActiveRecord::Base.connection.insert("INSERT INTO segments         (id,node_a,node_b,timestamp,user_id,visible,tags) VALUES (#{segid},#{from},#{to},#{db_now},#{uid},1,'')")
			points[i+1][5]=segid
			numberedsegments[(i+1).to_s]=segid.to_s
		end
		# numberedsegments.each{|a,b| RAILS_DEFAULT_LOGGER.error("Sending back: seg no. #{a} -> id #{b}") }

		if seglist!='' then
			ActiveRecord::Base.connection.update("UPDATE current_segments SET timestamp=#{db_now},user_id=#{uid},visible=1 WHERE id IN (#{seglist})")
		end


		# -- 6.ii insert new way segments

		createuniquesegments(way,db_uqs)

		# a=''
		# ActiveRecord::Base.connection.select_values("SELECT segment_id FROM #{db_uqs}").each {|b| a+=b+',' }
		# RAILS_DEFAULT_LOGGER.error("Unique segments are #{a}")
		# a=ActiveRecord::Base.connection.select_value("SELECT #{db_now}")
		# RAILS_DEFAULT_LOGGER.error("Timestamp of this edit is #{a}")
		# RAILS_DEFAULT_LOGGER.error("Userid of this edit is #{uid}")

		#		delete nodes from uniquesegments (and not in modified way)
	
		sql=<<-EOF
			INSERT INTO nodes (id,latitude,longitude,timestamp,user_id,visible)  
			SELECT DISTINCT cn.id,cn.latitude,cn.longitude,#{db_now},#{uid},0 
			  FROM current_nodes AS cn, 
				   current_segments AS cs,
				   #{db_uqs} AS us 
			 WHERE(cn.id=cs.node_a OR cn.id=cs.node_b) 
			   AND cs.id=us.segment_id AND cs.visible=1 
			   AND (cn.timestamp!=#{db_now} OR cn.user_id!=#{uid})
		EOF
		ActiveRecord::Base.connection.insert(sql)

		sql=<<-EOF
			UPDATE current_nodes AS cn, 
				   current_segments AS cs, 
				   #{db_uqs} AS us 
			   SET cn.timestamp=#{db_now},cn.visible=0,cn.user_id=#{uid} 
			 WHERE (cn.id=cs.node_a OR cn.id=cs.node_b) 
			   AND cs.id=us.segment_id AND cs.visible=1 
			   AND (cn.timestamp!=#{db_now} OR cn.user_id!=#{uid})
		EOF
		ActiveRecord::Base.connection.update(sql)
	
		#		delete segments from uniquesegments (and not in modified way)
	
		sql=<<-EOF
			INSERT INTO segments (id,node_a,node_b,timestamp,user_id,visible) 
			SELECT DISTINCT segment_id,node_a,node_b,#{db_now},#{uid},0
			  FROM current_segments AS cs, #{db_uqs} AS us
			 WHERE cs.id=us.segment_id AND cs.visible=1 
			   AND (cs.timestamp!=#{db_now} OR cs.user_id!=#{uid})
		EOF
		ActiveRecord::Base.connection.insert(sql)
		
		sql=<<-EOF
			   UPDATE current_segments AS cs, #{db_uqs} AS us
				  SET cs.timestamp=#{db_now},cs.visible=0,cs.user_id=#{uid} 
				WHERE cs.id=us.segment_id AND cs.visible=1 
				  AND (cs.timestamp!=#{db_now} OR cs.user_id!=#{uid})
		EOF
		ActiveRecord::Base.connection.update(sql)
		ActiveRecord::Base.connection.execute("DROP TABLE #{db_uqs}")

		#		insert new version of route into way_segments
	
		insertsql =''
		currentsql=''
		sequence  =1
		for i in (0..(points.length-2))
			if (points[i+1][3].to_i==0) then next end
			if insertsql !='' then insertsql +=',' end
			if currentsql!='' then currentsql+=',' end
			insertsql +="(#{way},#{points[i+1][5]},#{version})"
			currentsql+="(#{way},#{points[i+1][5]},#{sequence})"
			sequence  +=1
		end
		
		ActiveRecord::Base.connection.execute("DELETE FROM current_way_segments WHERE id=#{way}");
		ActiveRecord::Base.connection.insert("INSERT INTO         way_segments (id,segment_id,version    ) VALUES #{insertsql}");
		ActiveRecord::Base.connection.insert("INSERT INTO current_way_segments (id,segment_id,sequence_id) VALUES #{currentsql}");
	
		# -- 7. insert new way tags
	
		insertsql =''
		currentsql=''
		attributes.each do |k,v|
			if v=='' then next end
			if v[0,6]=='(type ' then next end
			if insertsql !='' then insertsql +=',' end
			if currentsql!='' then currentsql+=',' end
			insertsql +="(#{way},'"+sqlescape(k)+"','"+sqlescape(v)+"',version)"
			currentsql+="(#{way},'"+sqlescape(k)+"','"+sqlescape(v)+"')"
		end
		
		ActiveRecord::Base.connection.execute("DELETE FROM current_way_tags WHERE id=#{way}")
		if (insertsql !='') then ActiveRecord::Base.connection.insert("INSERT INTO way_tags (id,k,v,version) VALUES #{insertsql}" ) end
		if (currentsql!='') then ActiveRecord::Base.connection.insert("INSERT INTO current_way_tags (id,k,v) VALUES #{currentsql}") end
	
		[originalway,way,renumberednodes,numberedsegments,xmin,xmax,ymin,ymax]
	end
	
	# -----	deleteway (user token, way)
	#		returns way ID only
	
	def deleteway(args)
		usertoken,way=args
		uid=getuserid(usertoken); if !uid then return end
		way=way.to_i

		db_uqs='uniq'+uid.to_s+way.to_i.abs.to_s+Time.new.to_i.to_s	# temp uniquesegments table name, typically 51 chars
		db_now='@now'+uid.to_s+way.to_i.abs.to_s+Time.new.to_i.to_s	# 'now' variable name, typically 51 chars
		ActiveRecord::Base.connection.execute("SET #{db_now}=NOW()")
		createuniquesegments(way,db_uqs)
	
		sql=<<-EOF
			INSERT INTO nodes (id,latitude,longitude,timestamp,user_id,visible) 
			SELECT DISTINCT cn.id,cn.latitude,cn.longitude,#{db_now},#{uid},0 
			  FROM current_nodes AS cn, 
				   current_segments AS cs, 
				   #{db_uqs} AS us
			 WHERE (cn.id=cs.node_a OR cn.id=cs.node_b) 
			   AND cs.id=us.segment_id
		EOF
		ActiveRecord::Base.connection.insert(sql)
	
		sql=<<-EOF
			UPDATE current_nodes AS cn, 
				   current_segments AS cs, 
				   #{db_uqs} AS us
			   SET cn.timestamp=#{db_now},cn.visible=0,cn.user_id=#{uid} 
			 WHERE (cn.id=cs.node_a OR cn.id=cs.node_b) 
			   AND cs.id=us.segment_id
		EOF
		ActiveRecord::Base.connection.update(sql)
	
		# -	delete any otherwise unused segments
				
		sql=<<-EOF
			INSERT INTO segments (id,node_a,node_b,timestamp,user_id,visible) 
			SELECT DISTINCT segment_id,node_a,node_b,#{db_now},#{uid},0 
			  FROM current_segments AS cs, #{db_uqs} AS us
			 WHERE cs.id=us.segment_id
		EOF
		ActiveRecord::Base.connection.insert(sql)
				
		sql=<<-EOF
			UPDATE current_segments AS cs, #{db_uqs} AS us
			   SET cs.timestamp=#{db_now},cs.visible=0,cs.user_id=#{uid} 
			 WHERE cs.id=us.segment_id
		EOF
		ActiveRecord::Base.connection.update(sql)
		ActiveRecord::Base.connection.execute("DROP TABLE #{db_uqs}")
	
		# - delete way
		
		ActiveRecord::Base.connection.insert("INSERT INTO ways (id,user_id,timestamp,visible) VALUES (#{way},#{uid},#{db_now},0)")
		ActiveRecord::Base.connection.update("UPDATE current_ways SET user_id=#{uid},timestamp=#{db_now},visible=0 WHERE id=#{way}")
		ActiveRecord::Base.connection.execute("DELETE FROM current_way_segments WHERE id=#{way}")
		ActiveRecord::Base.connection.execute("DELETE FROM current_way_tags WHERE id=#{way}")
	
		way
	end
	
	# ====================================================================
	# Support functions for remote calls

	def readwayquery(id)
		ActiveRecord::Base.connection.select_all "SELECT n1.latitude AS lat1,n1.longitude AS long1,n1.id AS id1,n1.tags as tags1, "+
			"		  n2.latitude AS lat2,n2.longitude AS long2,n2.id AS id2,n2.tags as tags2,segment_id "+
			"    FROM current_way_segments,current_segments,current_nodes AS n1,current_nodes AS n2 "+
			"   WHERE current_way_segments.id=#{id} "+
			"     AND segment_id=current_segments.id "+
			"     AND n1.id=node_a and n2.id=node_b "+
			"   ORDER BY sequence_id"
	end

	def createuniquesegments(way,uqs_name)
		sql=<<-EOF
			CREATE TEMPORARY TABLE #{uqs_name}
							SELECT a.segment_id,COUNT(a.segment_id) AS ct
							  FROM current_way_segments AS a, current_way_segments AS b
							 WHERE a.segment_id=b.segment_id 
							   AND a.id=#{way} 
						  GROUP BY a.segment_id
							HAVING ct=1
		EOF
		ActiveRecord::Base.connection.execute(sql)
	end
	

	def sqlescape(a)
		a.gsub("'","''").gsub(92.chr,92.chr+92.chr)
	end

	def tag2array(a)
		tags={}
		a.gsub(';;;','#%').split(';').each do |b|
			b.gsub!('#%',';;;')
			b.gsub!('===','#%')
			k,v=b.split('=')
			tags[k.gsub('#%','=')]=v.gsub('#%','=')
		end
		tags
	end

	def array2tag(a)
		str=''
		a.each do |k,v|
			if v=='' then next end
			if v[0,6]=='(type ' then next end
			if str!='' then str+=';' end
			str+=k.gsub(';',';;;').gsub('=','===')+'='+v.gsub(';',';;;').gsub('=','===')
		end
		str
	end
	
	def getuserid(token)
		token=sqlescape(token)
		if (token=~/^(.+)\+(.+)$/) then
			return ActiveRecord::Base.connection.select_value("SELECT id FROM users WHERE active=1 AND timeout>NOW() AND email='#{$1}' AND pass_crypt=MD5('#{$2}')")
		else
			return ActiveRecord::Base.connection.select_value("SELECT id FROM users WHERE active=1 AND timeout>NOW() AND token='#{token}'")
		end
	end
	


	# ====================================================================
	# AMF read subroutines
	
	# -----	getint		return two-byte integer
	# -----	getlong		return four-byte long
	# -----	getstring	return string with two-byte length
	# ----- getdouble	return eight-byte double-precision float
	# ----- getobject	return object/hash
	# ----- getarray	return numeric array
	
	def getint(s)
		s.getc*256+s.getc
	end
	
	def getlong(s)
		((s.getc*256+s.getc)*256+s.getc)*256+s.getc
	end
	
	def getstring(s)
		len=s.getc*256+s.getc
		s.read(len)
	end
	
	def getdouble(s)
		a=s.read(8).unpack('G')			# G big-endian, E little-endian
		a[0]
	end
	
	def getarray(s)
		len=getlong(s)
		arr=[]
		for i in (0..len-1)
			arr[i]=getvalue(s)
		end
		arr
	end
	
	def getobject(s)
		arr={}
		while (key=getstring(s))
			if (key=='') then break end
			arr[key]=getvalue(s)
		end
		s.getc		# skip the 9 'end of object' value
		arr
	end
	
	# -----	getvalue	parse and get value
	
	def getvalue(s)
		case s.getc
			when 0;	return getdouble(s)			# number
			when 1;	return s.getc				# boolean
			when 2;	return getstring(s)			# string
			when 3;	return getobject(s)			# object/hash
			when 5;	return nil					# null
			when 6;	return nil					# undefined
			when 8;	s.read(4)					# mixedArray
					return getobject(s)			#  |
			when 10;return getarray(s)			# array
			else;	return nil					# error
		end
	end

	# ====================================================================
	# AMF write subroutines
	
	# -----	putdata		envelope data into AMF writeable form
	# -----	encodevalue	pack variables as AMF
	
	def putdata(index,n)
		d =encodestring(index+"/onResult")
		d+=encodestring("null")
		d+=[-1].pack("N")
		d+=encodevalue(n)
	end
	
	def encodevalue(n)
		case n.class.to_s
			when 'Array'
				a=10.chr+encodelong(n.length)
				n.each do |b|
					a+=encodevalue(b)
				end
				a
			when 'Hash'
				a=3.chr
				n.each do |k,v|
					a+=encodestring(k)+encodevalue(v)
				end
				a+0.chr+0.chr+9.chr
			when 'String'
				2.chr+encodestring(n)
			when 'Bignum','Fixnum','Float'
				0.chr+encodedouble(n)
			when 'NilClass'
				5.chr
			else
				RAILS_DEFAULT_LOGGER.error("Unexpected Ruby type for AMF conversion: "+n.class.to_s)
		end
	end
	
	# -----	encodestring	encode string with two-byte length
	# -----	encodedouble	encode number as eight-byte double precision float
	# -----	encodelong		encode number as four-byte long
	
	def encodestring(n)
		a,b=n.size.divmod(256)
		a.chr+b.chr+n
	end
	
	def encodedouble(n)
		[n].pack('G')
	end
	
	def encodelong(n)
		[n].pack('N')
	end
	
	# ====================================================================
	# Co-ordinate conversion
	
	def lat2coord(a,basey,masterscale)
		-(lat2y(a)-basey)*masterscale+250
	end
	
	def long2coord(a,baselong,masterscale)
		(a-baselong)*masterscale+350
	end
	
	def lat2y(a)
		180/Math::PI * Math.log(Math.tan(Math::PI/4+a*(Math::PI/180)/2))
	end
	
	def coord2lat(a,masterscale,basey)
		y2lat((a-250)/-masterscale+basey)
	end
	
	def coord2long(a,masterscale,baselong)
		(a-350)/masterscale+baselong
	end
	
	def y2lat(a)
		180/Math::PI * (2*Math.atan(Math.exp(a*Math::PI/180))-Math::PI/2)
	end

=end
end
