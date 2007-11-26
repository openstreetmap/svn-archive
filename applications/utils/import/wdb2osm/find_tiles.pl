#!/usr/bin/perl

use Math::Trig;

use strict;
use warnings;


#
#  From lastmodtile.pl
#
sub z12tile
{
	my ($lat, $lon) = @_;

	my $px = ($lon + 180) / 360;
	$lat = ($lat / 180 * pi);
	my $projectf = log (Math::Trig::tan($lat) + (1 / cos($lat)));
	my $py = (pi - $projectf) / 2 / pi;
	
	my $x = int($px * 4096);
	my $y = int($py * 4096);

	return ($x, $y);

	# return sprintf("%d %d", $px * 4096, $py * 4096);
}


my @all_files;

opendir (DIR, "WDB/keepme");
my @files = grep { /\.osm$/ } readdir(DIR);
closedir (DIR);

for (@files)
{
	push @all_files, "WDB/" . "keepme" . "/" . $_;
}

for my $file (@all_files)
{
	print "$file \n";
	
	my (%tiles_z12);
	my (%tiles_z8);

	open (FILE, "< $file") || die ("Can't open $file: $!\n");
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

			# my $coord_y = ProjectF($lat);
			# my $coord_x = -1;

			my @gaga = z12tile($lat, $lon);

			my $coord_x_z12 = $gaga[0];
			my $coord_y_z12 = $gaga[1];
			my $key_z12 = "$coord_x_z12 $coord_y_z12";
			%tiles_z12->{$key_z12} = $key_z12; 

			my $coord_x_z8  = int($coord_x_z12 / 16);
			my $coord_y_z8  = int($coord_y_z12 / 16);
			my $key_z8 = "$coord_x_z8 $coord_y_z8"; 
			%tiles_z8->{$key_z8} = $key_z8; 
		}
	}

	mkdir ("WDB/keepme.z12", 0755);
	my $z12_file = $file;
	$z12_file =~ s/keepme/keepme\.z12/;
	$z12_file .= ".z12.tilenums";
	open (TILES, "> $z12_file") || die ("Can't open $z12_file: $!\n");
	foreach (sort keys %tiles_z12)
	{
		print TILES "$_\n";
	}
	close (TILES);

	mkdir ("WDB/keepme.z8", 0755);
	my $z8_file = $file;
	$z8_file =~ s/keepme/keepme\.z8/;
	$z8_file .= ".z8.tilenums";
	open (TILES, "> $z8_file") || die ("Can't open $z8_file: $!\n");
	foreach (sort keys %tiles_z8)
	{
		print TILES "$_\n";
	}
	close (TILES);
}
