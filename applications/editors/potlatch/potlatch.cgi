#!/usr/bin/perl

	# ----------------------------------------------------------------
	# potlatch.cgi
	# Flash editor for Openstreetmap

	# editions Systeme D / Richard Fairhurst 2006-7
	# public domain

	# see http://wiki.openstreetmap.org/index.php/Potlatch/Changelog
	# for revision history

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
	$m->setRate(50);
	$m->setBackground(0xFF,0xFF,0xFF);

	require "potlatch_assets.pl";

	$debug=0;

	# -----	Read ActionScript files

	$actionscript="#include 'potlatch.as'\n";
	while ($actionscript=~/#include '(.+?)'/g) {
		$fn=$1;
		unless (exists($ENV{'DOCUMENT_ROOT'})) {
			print "Reading $fn              \r";
		}
		local $/;
		open TEXT,$fn or die "Can't open file $fn: $!\n";
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
		if ($fn=shift @ARGV) { print "Saving to $fn\n"; $m->save($fn); }
						else { print "Saving to this directory\n"; $m->save("potlatch.swf"); }
	}

