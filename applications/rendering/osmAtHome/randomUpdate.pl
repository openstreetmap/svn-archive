#!/usr/bin/perl
#             Call the random place update for OSM@Home
#             -----------------------------------------
#
# Calls the random place update for OSM@Home, to download new data to the
#  server for places that need updating
# Handles a full download re-attempt for places that have failed to download

use strict;
use LWP::Simple;

# Random update URL
my $url = "http://almien.co.uk/OSM/Places/?action=random_update";
my $force_url = "http://almien.co.uk/OSM/Places/?action=download&id=";

print "Calling $url:\n";
my $contents = get($url);

# Was it nothing to update, update done, or a failing to download?

# Nothing:
#
if($contents =~ /Nothing to update/) {
	print "Nothing to update\n";
}

# Failing to download:
#   Random selection was <a href="./?id=1166">1166</a>, but looks like that's marked as a &quot;failing download&quot;
elsif($contents =~ /Random selection was <a href=".\/\?id=(\d+).*?failing download/) {
	my $place = $1;
	print "Place with id $place was previously failing, forcing it:\n";

	my $retry = get($force_url.$place);
	if($retry =~ /Downloading .*?Done,/) {
		print "Updated\n";
	} else {
		print $retry;
	}
}

# Downloaded:
#   Downloading <a href="./?id=1006">place #1006</a> at 
elsif($contents =~ /Downloading <a href=".\/\?id=(\d+)">place.*?Done,/) {
	my $place = $1;
	print "Updated place with id $place\n";
}

else {
	warn "Unknown contents:\n";
	print $contents."\n";
}
