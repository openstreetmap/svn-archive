#!/usr/bin/ruby
	
	# ----------------------------------------------------------------
	# getgps.cgi
	# GPX responder for Potlatch Openstreetmap editor
	# Ruby/Ming version
	# === now superseded by pure Ruby version ===
	#	
	# editions Systeme D / Richard Fairhurst 2006
	# public domain
	# ----------------------------------------------------------------
	
	require 'rubygems'
	require 'web'
	require 'mysql'
	
	require 'ming/ming'
	include Ming

	def lat2coord(a) ; -(lat2y(a)-$basey)*$masterscale+250; end
	def long2coord(a); (a-$baselong)*$masterscale+350;		end
	def lat2y(a)	 ; 180/Math::PI * Math.log(Math.tan(Math::PI/4+a*(Math::PI/180)/2)); end

	# ====================================================================
	# Main code

	# -----	Initialise
	
	Ming.set_scale(20.0)
	Ming.use_SWF_version(6)
	dbh=Mysql.real_connect("localhost","db_user","db_pass","osm")

	m=SWFMovie.new

	$baselong	=Web['baselong'].to_f
	$basey		=Web['basey'].to_f
	$masterscale=Web['masterscale'].to_f

	if (Web::key?('user'))
		user=Web['user'].to_i
	else
		user=-1
	end

	# -----	Start Flash line

	s=SWFShape.new
	s.set_line(1,0,255,255,255)
	
	# -----	Send SQL and draw line

	lasttime=0
	lastfile=-1
	xmin=Web['xmin'].to_f/0.0000001; xmax=Web['xmax'].to_f/0.0000001
	ymin=Web['ymin'].to_f/0.0000001; ymax=Web['ymax'].to_f/0.0000001
	if Web::key?('token')
		token=dbh.escape_string(Web['token'])
		sql="SELECT gps_points.latitude,gps_points.longitude,gpx_files.id AS fileid,UNIX_TIMESTAMP(gps_points.timestamp) AS ts "+
			 " FROM gpx_files,gps_points,users "+
			 "WHERE gpx_files.id=gpx_id "+
			 "  AND gpx_files.user_id=users.id "+
			 "  AND token='#{token}' "+
			 "  AND (gps_points.longitude BETWEEN #{xmin} AND #{xmax}) "+
			 "  AND (gps_points.latitude BETWEEN #{ymin} AND #{ymax}) "+
			 "ORDER BY fileid,ts"
	else
		sql="SELECT latitude,longitude,gpx_id,UNIX_TIMESTAMP(timestamp) AS ts "+
			 " FROM gps_points "+
			 "WHERE (longitude BETWEEN #{xmin} AND #{xmax}) "+
			 "  AND (latitude  BETWEEN #{ymin} AND #{ymax}) "+
			 "ORDER BY gpx_id,ts"
	end
	
	dbr=dbh.query(sql)
	while row=dbr.fetch_row do
		xs=long2coord(row[1].to_f*0.0000001)
		ys=lat2coord(row[0].to_f*0.0000001)
		if (row[3].to_i-lasttime>180 or row[2].to_i!=lastfile)
			s.move_pen_to(xs,ys)
		else
			s.draw_line_to(xs,ys)
		end
		lasttime=row[3].to_i
		lastfile=row[2].to_i
	end
	dbr.free

	m.add(s)


	#Ê=================================================================
	# Output file

	m.next_frame
	filename='/tmp/swf_'+rand(65535).to_s+'.swf'
	m.save(filename)

	Web::open do |connection|
		Web::content_type="application/x-shockwave-flash"
		Web::send_file(filename)
	end

	File.delete(filename)
	dbh.close
