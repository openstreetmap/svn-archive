#!/usr/bin/perl -w

	# ----------------------------------------------------------------
	# potlatch.cgi
	# Flash editor for Openstreetmap

	# editions Systeme D / Richard Fairhurst 2006-7
	# public domain

	# last update 18.12.2007 (split into separate files)

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

	# -----	Read ActionScript files

	$actionscript="#include 'potlatch.as'\n";
	while ($actionscript=~/#include '(.+?)'/g) {
		$fn=$1; print "Reading $fn\n";
		local $/;
		open TEXT,$fn or die "Can't open file $fn: $!\n";
		$text=<TEXT>;
		close TEXT;
		$actionscript=~s/#include '$fn'/$text/;
	}

	$m->add(new SWF::Action($actionscript));

	# -----	Output file

	$m->nextFrame();

	if (exists($ENV{'DOCUMENT_ROOT'})) {
		# We're running under a web server, so output to browser
		print "Content-type: application/x-shockwave-flash\n\n";
		$m->output(9);
	} else {
		# Running from command line, so output to file
		if ($fn=shift @ARGV) { print "Saving to $fn\n"; $m->save($fn); }
						else { print "Saving to this directory\n"; $m->save("potlatch.swf"); }
	}

