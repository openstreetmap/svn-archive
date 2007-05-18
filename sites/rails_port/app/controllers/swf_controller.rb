class SwfController < ApplicationController

# to log:
# RAILS_DEFAULT_LOGGER.error("Args: #{args[0]}, #{args[1]}, #{args[2]}, #{args[3]}")
# $log.puts Time.new.to_s+','+Time.new.usec.to_s+": started GPS script"
# http://localhost:3000/api/0.4/swf/trackpoints?xmin=-2.32402605810577&xmax=-2.18386309423859&ymin=52.1546608755772&ymax=52.2272777906895&baselong=-2.25325793066437&basey=61.3948537948532&masterscale=5825.4222222222

	# ====================================================================
	# Public methods
	
	# ---- trackpoints	compile SWF of trackpoints

	def trackpoints	
	
		# -	Initialise
	
		baselong	=params['baselong'].to_f
		basey		=params['basey'].to_f
		masterscale	=params['masterscale'].to_f
	
		xmin=params['xmin'].to_f/0.0000001
		xmax=params['xmax'].to_f/0.0000001
		ymin=params['ymin'].to_f/0.0000001
		ymax=params['ymax'].to_f/0.0000001
	
		# -	Begin movie
	
		bounds_left  =0
		bounds_right =320*20
		bounds_bottom=0
		bounds_top   =240*20

		m =''
		m+=swfRecord(9,255.chr + 155.chr + 155.chr)			#�Background
		absx=0
		absy=0
		xl=yb= 9999999
		xr=yt=-9999999
	
		# -	Send SQL and draw line
	
		b=''
		lasttime=0
		lastfile='-1'
	
		if params['token']
			token=sqlescape(params['token'])
			sql="SELECT gps_points.latitude*0.0000001 AS lat,gps_points.longitude*0.0000001 AS lon,gpx_files.id AS fileid,UNIX_TIMESTAMP(gps_points.timestamp) AS ts "+
				 " FROM gpx_files,gps_points,users "+
				 "WHERE gpx_files.id=gpx_id "+
				 "  AND gpx_files.user_id=users.id "+
				 "  AND token='#{token}' "+
				 "  AND (gps_points.longitude BETWEEN #{xmin} AND #{xmax}) "+
				 "  AND (gps_points.latitude BETWEEN #{ymin} AND #{ymax}) "+
				 "ORDER BY fileid DESC,ts "+
				 "LIMIT 10000"
		else
			sql="SELECT latitude*0.0000001 AS lat,longitude*0.0000001 AS lon,gpx_id AS fileid,UNIX_TIMESTAMP(timestamp) AS ts "+
				 " FROM gps_points "+
				 "WHERE (longitude BETWEEN #{xmin} AND #{xmax}) "+
				 "  AND (latitude  BETWEEN #{ymin} AND #{ymax}) "+
				 "ORDER BY fileid DESC,ts "+
				 "LIMIT 10000"
		end
		gpslist=ActiveRecord::Base.connection.select_all sql
	
		# - Draw lines
	
		r=startShape()
		gpslist.each do |row|
			xs=(long2coord(row['lon'].to_f,baselong,masterscale)*20).floor
			ys=(lat2coord(row['lat'].to_f ,basey   ,masterscale)*20).floor
			xl=[xs,xl].min; xr=[xs,xr].max
			yb=[ys,yb].min; yt=[ys,yt].max
			if (row['ts'].to_i-lasttime<180 and row['fileid']==lastfile)
				b+=drawTo(absx,absy,xs,ys)
			else
				b+=startAndMove(xs,ys)
			end
			absx=xs.floor; absy=ys.floor
			lasttime=row['ts'].to_i
			lastfile=row['fileid']
			while b.length>80 do
				r+=[b.slice!(0...80)].pack("B*")
			end
		end
	
		# - Write shape
	
		b+=endShape()
		r+=[b].pack("B*")
		m+=swfRecord(2,packUI16(1) + packRect(xl,xr,yb,yt) + r)
		m+=swfRecord(4,packUI16(1) + packUI16(1))
		
		# -	Create Flash header and write to browser
	
		m+=swfRecord(1,'')									# Show frame
		m+=swfRecord(0,'')									# End
		
		m=packRect(bounds_left,bounds_right,bounds_bottom,bounds_top) + 0.chr + 12.chr + packUI16(1) + m
		m='FWS' + 6.chr + packUI32(m.length+8) + m
	
		response.headers["Content-Type"]="application/x-shockwave-flash"
		render :text=>m
	end

	private

	# =======================================================================
	# SWF functions
	
	# -----------------------------------------------------------------------
	# Line-drawing

	def startShape
		s =0.chr										# No fill styles
		s+=1.chr										# One line style
		s+=packUI16(5) + 0.chr + 255.chr + 255.chr		# Width 5, RGB #00FFFF
		s+=17.chr										# 1 fill, 1 line index bit
		s
	end
	
	def endShape
		'000000'
	end
	
	def startAndMove(x,y)
		d='001001'										# Line style change, moveTo
		l =[lengthSB(x),lengthSB(y)].max
		d+=sprintf("%05b%0#{l}b%0#{l}b",l,x,y)
		d+='1'											# Select line style 1
	end
	
	def drawTo(absx,absy,x,y)
		d='11'											# TypeFlag, EdgeFlag
		dx=x-absx
		dy=y-absy
		
		l =[lengthSB(dx),lengthSB(dy)].max
		d+=sprintf("%04b",l-2)
		d+='1'											# GeneralLine
		d+=sprintf("%0#{l}b%0#{l}b",dx,dy)
	end

	# -----------------------------------------------------------------------
	# Specific data types

	def swfRecord(id,r)
		if r.length>62
			return packUI16((id<<6)+0x3F) + packUI32(r.length) + r
		else
			return packUI16((id<<6)+r.length) + r
		end
	end

	def packRect(a,b,c,d)
		l=[lengthSB(a),
		   lengthSB(b),
		   lengthSB(c),
		   lengthSB(d)].max
		n=sprintf("%05b%0#{l}b%0#{l}b%0#{l}b%0#{l}b",l,a,b,c,d)
		[n].pack("B*")
	end

	# -----------------------------------------------------------------------
	# Generic pack functions
	
	def packUI16(n)
		[n.floor].pack("v")
	end
	
	def packUI32(n)
		[n.floor].pack("V")
	end
	
	# Find number of bits required to store arbitrary-length binary
	
	def lengthSB(n)
		Math.frexp(n+ (n==0?1:0) )[1]+1
	end
	
	# ====================================================================
	# Co-ordinate conversion
	# (this is duplicated from amf_controller, should probably share)
	
	def lat2coord(a,basey,masterscale)
		-(lat2y(a)-basey)*masterscale+250
	end
	
	def long2coord(a,baselong,masterscale)
		(a-baselong)*masterscale+350
	end
	
	def lat2y(a)
		180/Math::PI * Math.log(Math.tan(Math::PI/4+a*(Math::PI/180)/2))
	end

	def sqlescape(a)
		a.gsub("'","''").gsub(92.chr,92.chr+92.chr)
	end

end
