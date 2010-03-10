#!/usr/bin/perl

use strict;
use warnings;

use constant VERBOSE => 0;

use File::Monitor;
use trapi;

$| = 1;

opendir(DIR, CHANGEDIR);
my @filelist=readdir(DIR);
closedir(DIR);
@filelist=sort(@filelist);

for my $existingfile ( @filelist ) {
	if ($existingfile =~ /\d+\-(\d+)-(\d+)\.osc\.gz$/) {
	  print CHANGEDIR.$existingfile."\n";
	}
}

my $monitor = File::Monitor->new();

$monitor->watch( {
	name        => CHANGEDIR,
	callback    => \&somethingchanged,
	files => 1
	}
);

$monitor->scan();

while ()
{
	$monitor->scan();    
	sleep 10;
}

sub somethingchanged
{
	my ($name, $event, $change) = @_;

	my @adds = $change->files_created;
	@adds=sort(@adds);

	for my $files ( @adds ){
	  print $files."\n" if ($files =~ /\d+\-(\d+)-(\d+)\.osc\.gz$/);
	}
	return 1;
}
