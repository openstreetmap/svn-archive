#!/usr/bin/perl

	# ----------------------------------------------------------------
	# getgps.cgi
	# GPX responder for Potlatch Openstreetmap editor
	#	
	# editions Systeme D / Richard Fairhurst 2006
	# public domain
	# ----------------------------------------------------------------

	use SWF qw(:ALL);
	use DBI;
	use Math::Trig;
	
	# -----	Initialise
	
	SWF::setScale(20.0);
	SWF::useSWFVersion(6);
	$m=new SWF::Movie();
	print "Content-type: application/x-shockwave-flash\n\n";

	$dbh=DBI->connect('DBI:mysql:osm','db_user','db_pass', { RaiseError =>1 } );
	Parse_Form();

	$pi = 3.1415926539;
	$baselong	=$formdata{'baselong'};
	$basey		=$formdata{'basey'};
	$masterscale=$formdata{'masterscale'};
	if (exists $formdata{'user'}) { $user=$formdata{'user'}; $user=~s/\D//g; }
							 else { $user=-1; }

	# -----	Start Flash line

	$s=new SWF::Shape();
	$s->setLine(1,0,255,255,255);
	
	# -----	Send SQL and draw line

	$lasttime=0; $lastfile=-1;
	if (exists $formdata{'token'}) {
		$sql="SELECT gps_points.latitude,gps_points.longitude,gpx_files.id AS fileid,UNIX_TIMESTAMP(gps_points.timestamp) AS ts ".
			 "  FROM gpx_files,gps_points,users ".
			 " WHERE gpx_files.id=gpx_id ".
			 "   AND gpx_files.user_id=users.id ".
			 "   AND token=? ".
			 "   AND (gps_points.longitude BETWEEN ? AND ?) ".
			 "   AND (gps_points.latitude BETWEEN ? AND ?) ".
			 " ORDER BY fileid,ts";
		$query=$dbh->prepare($sql);
		$query->execute($formdata{'token'},$formdata{'xmin'}/0.0000001,$formdata{'xmax'}/0.0000001,$formdata{'ymin'}/0.0000001,$formdata{'ymax'}/0.0000001);
	} else {
		$sql="SELECT latitude,longitude,gpx_id,UNIX_TIMESTAMP(timestamp) AS ts ".
			 "  FROM gps_points ".
			 " WHERE (longitude BETWEEN ? AND ?) ".
			 "   AND (latitude  BETWEEN ? AND ?) ".
			 " ORDER BY gpx_id,ts";
		$query=$dbh->prepare($sql);
		$query->execute($formdata{'xmin'}/0.0000001,$formdata{'xmax'}/0.0000001,$formdata{'ymin'}/0.0000001,$formdata{'ymax'}/0.0000001);
	}

	while (($lat,$long,$file,$time)=$query->fetchrow_array()) {
		$xs=long2coord($long*0.0000001);
		$ys=lat2coord($lat*0.0000001);
		if ($time-$lasttime>180 or $file!=$lastfile) { $s->movePenTo($xs,$ys); }
												else { $s->drawLineTo($xs,$ys); }
		$lasttime=$time;
		$lastfile=$file;
	}
	$query->finish();

	# -----	Output movie

	$m->add($s);
	$m->nextFrame();
	$m->output();

	$dbh->disconnect();



	
	#Ê=================================================================

	# =====	Subroutines	

	# -----	Lat/long <-> coord conversion
	
	sub lat2coord 	{ return -(lat2y($_[0])-$basey)*$masterscale+250; }
	sub long2coord	{ return      ($_[0]-$baselong)*$masterscale+350; }
	sub lat2y	    { return 180/$pi * log(Math::Trig::tan($pi/4+$_[0]*($pi/180)/2)); }

	# -----	Parse_Form subroutine - POST|GET
	
	sub Parse_Form {
	
		if ($ENV{'REQUEST_METHOD'} eq 'GET') {
			@pairs=split(/&/, $ENV{'QUERY_STRING'});
	
		} elsif ($ENV{'REQUEST_METHOD'} eq 'POST') {
			read (STDIN, $buffer, $ENV{'CONTENT_LENGTH'});
			@pairs = split(/&/, $buffer);
			
			if ($ENV{'QUERY_STRING'}) {
				@getpairs=split(/&/, $ENV{'QUERY_STRING'});
				push (@pairs,@getpairs);
			}
		}
	
		foreach $pair (@pairs) {
			($key, $value) = split (/=/, $pair);
			$key =~ tr/+/ /;
			$key =~ s/%([a-fA-F0-9][a-fA-f0-9])/pack("C", hex($1))/eg;
			$value =~ tr/+/ /;
			$value =~ s/%([a-fA-F0-9][a-fA-f0-9])/pack("C", hex($1))/eg;
			$formdata{$key}=$value;
		}
	}
	
