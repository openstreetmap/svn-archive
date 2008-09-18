#!/usr/bin/perl

	# ----------------------------------------------------------------
	# potlatch.pl
	# Flash editor for Openstreetmap

	# editions Systeme D / Richard Fairhurst 2006-8
	# public domain

	# See http://wiki.openstreetmap.org/index.php/Potlatch/Changelog
	# for revision history

	# To compile:
	# perl potlatch.pl [options] [destination path]

	# Options:
	# --dev		- use OSM dev server instead of localhost
	# --trace	- enable trace windows
	# Destination path is ./potlatch.swf if not specified

	# You may do what you like with this file, but please think very
	# carefully before adding dependencies or complicating the user
	# interface. Thank you!
	# ----------------------------------------------------------------

	use SWF qw(:ALL);
	use SWF::Constants qw(:Button);

	# -----	Initialise

	SWF::setScale(20.0);
	SWF::useSWFVersion(8);

	$m = new SWF::Movie();
	$m->setDimension(700, 600);
	$m->setRate(12);
	$m->setBackground(0xFF,0xFF,0xFF);

	require "potlatch_assets.pl";

	# -----	Get server addresses

	$ofn=''; $debug=0; $dev=0;
	foreach $a (@ARGV) {
		if    ($a eq '--trace') { $debug=1; }
		elsif ($a eq '--dev'  ) { $dev  =1; }
		elsif ($a eq '--local') { $dev  =2; }
		else					{ $ofn=$a;  }
	}
	
	if ($dev==1) { $actionscript=<<EOF;
	var apiurl='http://main.dev.openstreetmap.org/api/0.5/amf';
	var gpsurl='http://main.dev.openstreetmap.org/api/0.5/swf/trackpoints';
	var gpxurl='http://main.dev.openstreetmap.org/trace/';
	var tileprefix='';
	var yahoourl='http://main.dev.openstreetmap.org/potlatch/ymap2.swf';
	var gpxsuffix='/data.xml';
EOF
	} elsif ($dev==2) { $actionscript=<<EOF;
	var apiurl='../api/0.5/amf';
	var gpsurl='../api/0.5/swf/trackpoints';
	var gpxurl="http://"+this._url.split('/')[2]+"/trace/";
	var tileprefix='http://127.0.0.1/~richard/cgi-bin/proxy.cgi?url=';
	var yahoourl='/potlatch/ymap2.swf';
	var gpxsuffix='/data.xml';
EOF
	} else { $actionscript=<<EOF;
	var apiurl='../api/0.5/amf';
	var gpsurl='../api/0.5/swf/trackpoints';
	var gpxurl="http://"+this._url.split('/')[2]+"/trace/";
	var tileprefix='';
	var yahoourl='/potlatch/ymap2.swf';
	var gpxsuffix='/data.xml';
EOF
	}

	# -----	Read ActionScript files

	$actionscript.="#include 'potlatch.as'\n";
	while ($actionscript=~/#include '(.+?)'/g) {
		$fn=$1;
		unless (exists($ENV{'DOCUMENT_ROOT'})) {
			print "Reading $fn               \r";
		}
		local $/;
		open TEXT,$fn or die "Can't open�$fn: $!\n";
		$text=<TEXT>;
		close TEXT;
		$actionscript=~s/#include '$fn'/$text/;
	}

	if ($debug) { $actionscript=~s!false;//#debug!true;!g; }
	$m->add(new SWF::Action($actionscript));
	
	# -----	Output file

	$m->nextFrame();

	if (exists($ENV{'DOCUMENT_ROOT'})) {
		# We're running under a web server, so output to browser
		print "Content-type: application/x-shockwave-flash\n\n";
		$m->output(9);
	} else {
		# Running from command line, so output to file
		print localtime()."\n";
		if ($ofn) { print "Saving to $ofn\n"; $m->save($ofn); }
			 else { print "Saving to this directory\n"; $m->save("potlatch.swf"); }
	}

