#!/usr/bin/perl

use strict;
use warnings;

my @all_files;

for my $dir ("asia-riv", "europe-riv", "asia-bdy", "europe-bdy")
{
	opendir (DIR, "WDB/$dir");
	my @files = grep { /\.osm/ } readdir(DIR);
	closedir (DIR);

	for (@files)
	{
		push @all_files, "WDB/" . $dir . "/" . $_;
	}
}

for my $file (@all_files)
{
	my $keep = 0;

	open (FILE, "< $file") || die ("Can't open $file to read: $!\n");
	while (<FILE>)
	{
		if (/node/)
		{
			chomp;
			my @foo = split(/[\"]/, $_);
			
			# print "'" . join("', '", @foo) . "'\n";
			# <node id=', '-251', ' lat=', '45.133333', ' lon=', '27.946944', '/>
			#    0          1         2       3             4        5         
			
			my $lat = $foo[3];
			my $lon = $foo[5];

			if ($lat >= 35.5 && $lat <= 42.5)
			{
				if ($lon >= 26.0 && $lon < 45.0)
				{
					$keep = 1;					
				}
			}
		}
	}
	close (FILE);


	if ($keep)
	{
		my $new_file = $file;
		$new_file =~ s/\//-/g;
		$new_file =~ s/WDB-/WDB\/keepme\//;
		
		print "Renaming $file to $new_file\n";

		mkdir ("WDB/keepme", 0755);
		rename ($file, $new_file);
	}
}
