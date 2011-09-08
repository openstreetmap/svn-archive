#!/usr/bin/perl

# This converts *.poly into *.osm or *.gpx or *.wkt and back.
# Can work as a CGI script, format=osm/gpx/wkt (default osm), source: content or file
# Replaces (and is based on) poly2osm.pl, poly2wkt.pl, poly2bb.pl, gpx2poly.pl, osm2poly.pl
#
# written by Ilya Zverev <zverik@textual.ru>, public domain.
# installed at http://textual.ru/poly/

use strict;
use CGI;

my $iscgi = defined($ENV{'REQUEST_METHOD'});
my $cgi = CGI->new if $iscgi;

my $to = $iscgi ? $cgi->param('format') || 'osm' : 'osm';

my $contents;
my $tmpseparator = $/;
undef $/;
if( $iscgi ) {
	my $handle  = $cgi->upload('file');
	$contents = defined($handle) ? <$handle> : $cgi->param('content');
} else {
	$contents = <>;
}
$/ = $tmpseparator;
error("Empty input file") if $contents =~ /^\s*$/;
$contents =~ s/\r//g;

my $result;
my $ext;
if( $contents =~ /END\s+END\s*$/ ) {
	# poly
	if( $to eq 'osm' ) { $result = poly2osm($contents); $ext = 'osm'; }
	elsif( $to eq 'gpx' || $to eq 'ol' ) { $result = poly2gpx($contents); $ext = 'gpx'; }
	elsif( $to eq 'wkt' ) { $result = poly2wkt($contents); $ext = 'wkt'; }
	else { error("Unknown format $to"); }
}
elsif( $contents =~ /^MULTIPOLYGON/ ) {
	$result = wkt2poly($contents);
	$ext = 'poly';
}
elsif( $contents =~ /\<gpx.+\<trkpt/s ) {
	$result = gpx2poly($contents);
	$ext = 'poly';
}
elsif( $contents =~ /\<osm.+\<node/s ) {
	$result = osm2poly($contents);
	$ext = 'poly';
}
else { error("Unknown format of input file"); }

if( $to eq 'ol' ) {
	error("Source file must be poly or gpx") if $result !~ /\<gpx/ && $contents !~ /\<gpx/;
	$result = openlayers($result =~ /\<gpx/ ? $result : $contents);
	print "Content-type: text/html\n\n" if $iscgi;
} else {
	print "Content-Disposition: attachment; filename=result.$ext\n\n" if $iscgi;
}
print $result;

sub error {
	my($error) = @_;
	die($error) if !$iscgi;
	print "Content-type: text/html\n\nError: <b>$error</b><br>Sorry.";
	exit;
}

# Worker subs. Input: the whole file as string. Returns resulting file as string.

sub poly2gpx {
	my @contents = split /^/, $_[0];
	my $pos = 0;
	# first line
	# (employ workaround for polygon files without initial text line)
	my $poly_file = $contents[$pos++]; chomp($poly_file);
	my $workaround = 0;
	if ($poly_file =~ /^\d+$/)
	{
		$workaround=$poly_file;
		$poly_file="none";
	}

	my $tracks;

	while(1)
	{
		my $poly_id = $workaround || $contents[$pos++];
		chomp($poly_id);
		last if ($poly_id =~ /^END/); # end of file
		
		$tracks .= "<trk>\n<name>$poly_file $poly_id</name>\n<trkseg>\n";	

		while(my $line = $contents[$pos++])
		{
			last if ($line =~ /^END/); # end of poly
			my ($dummy, $x, $y) = split(/\s+/, $line);
			$tracks .= sprintf("  <trkpt lat=\"%f\" lon=\"%f\">\n  </trkpt>\n", $y, $x);
		}
		$tracks .= "</trkseg>\n</trk>\n";	
		$workaround=0;
	}

	return "<?xml version='1.0' encoding='UTF-8'?>\n<gpx version=\"1.1\" creator=\"polyconvert.pl\" xmlns=\"http://www.topografix.com/GPX/1/1\" ".
    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n".
	"$tracks</gpx>";
}

sub poly2wkt {
	my @contents = split /^/, $_[0];
	my $pos = 0;
	# first line
	# (employ workaround for polygon files without initial text line)
	my $poly_file = $contents[$pos++]; chomp($poly_file);
	my $workaround = 0;
	if ($poly_file =~ /^\d+$/)
	{
		$workaround=$poly_file;
		$poly_file="none";
	}

	my $polygons;

	while(1)
	{
		my $poly_id = $workaround || $contents[$pos++];
		chomp($poly_id);
		last if ($poly_id =~ /^END/); # end of file
		my $coords;

		while(my $line = $contents[$pos++])
		{
			last if ($line =~ /^END/); # end of poly
			my ($dummy, $x, $y) = split(/\s+/, $line);
			push(@$coords, sprintf("%f %f", $x, $y));
		}
		push(@$polygons, "((".join(",", @$coords)."))");
		$workaround=0;
	}

	return "MULTIPOLYGON(".join(",",@$polygons).")\n";
}

sub poly2osm {
	my @contents = split /^/, $_[0];
	my $pos = 0;
	my %nodehash;

	# first line
	# (employ workaround for polygon files without initial text line)
	my $poly_file = $contents[$pos++]; chomp($poly_file);
	my $workaround = 0;
	if ($poly_file =~ /^\d+$/)
	{
		$workaround=$poly_file;
		$poly_file="none";
	}

	my $nodecnt = -1;
	my $waycnt = -1;

	my $nodes;
	my $ways;
	my $note = "    <tag k='note' v='created by poly2osm.pl from a polygon file. not for uploading!' />\n";
	my $line;

	while(1) {
		my $poly_id;
		if ($workaround==0) {
		   $poly_id=$contents[$pos++];
		} else {
		   $poly_id=$workaround;
		}
		chomp($poly_id);
		my $startnode = $nodecnt;
		last if ($poly_id =~ /^END/); # end of file

		$ways .= sprintf("  <way id='%d'>\n    <tag k='polygon_id' v='%d' />\n    <tag k='polygon_file' v='%s' />\n",
			$waycnt--, $poly_id, $poly_file);
		$ways .= $note;

		while($line = $contents[$pos++])
		{
			last if ($line =~ /^END/); # end of poly
			my ($dummy, $x, $y) = split(/\s+/, $line);
			my $existingnode = $nodehash{"$x|$y"};
			if (defined($existingnode))
			{
				$ways .= sprintf("    <nd ref='%d' />\n", $existingnode);
			}
			else
			{
				$nodehash{"$x|$y"} = $nodecnt;
				$ways .= sprintf("    <nd ref='%d' />\n", $nodecnt);
				$nodes .= sprintf("  <node id='%d' lat='%f' lon='%f' />\n", $nodecnt--, $y, $x);
			}
		}
		$ways .= "  </way>\n";
		undef $workaround;
	};
	return "<osm generator='osm2poly.pl' version='0.5'>\n$nodes$ways</osm>\n";
}

sub gpx2poly {
	my @contents = split /^/, $_[0];
	my $pos = 0;
	my $poly_id = -1;
	my $poly_file;
	my $polybuf;
	my $outbuf;
	my $id=0;

	while($pos <= $#contents) 
	{
		$_ = $contents[$pos++];
		if (/^\s*<trkpt.*\slon=["']([0-9.eE-]+)["'] lat=["']([0-9.eE-]+)["']/)
		{
			$polybuf .= sprintf "\t%f\t%f\n", $1,$2;
		} 
		elsif (/^\s*<trk>/) 
		{
			$polybuf = "";
			$poly_id++;
		}
		elsif (/^\s*<\/trk>/) 
		{
			$outbuf .= "$poly_id\n$polybuf"."END\n";
		}
	}

	$poly_file = "polygon" unless defined($poly_file);
	return "$poly_file\n$outbuf"."END\n";
}

sub wkt2poly {
	error("wkt2poly not implemented yet");
}

sub osm2poly {
	my @contents = split /^/, $_[0];
	my $pos = 0;
	my $poly_id;
	my $poly_file;
	my $polybuf;
	my $outbuf;
	my $nodes;
	my $id=0;

	while($pos <= $#contents) 
	{
		$_ = $contents[$pos++];
		if (/^\s*<node.*\sid=["']([0-9-]+)['"].*lat=["']([0-9.eE-]+)["'] lon=["']([0-9.eE-]+)["']/)
		{
			$nodes->{$1}=[$2,$3];
		}
		elsif (/^\s*<way /)
		{
			undef $poly_id;
			undef $poly_file;
			$polybuf = "";
		}
		elsif (defined($polybuf) && /k=["'](.*)["']\s*v=["'](.*?)["']/)
		{
			if ($1 eq "polygon_file") 
			{
				$poly_file=$2;
			}
			elsif ($1 eq "polygon_id")
			{
				$poly_id=$2;
			}
			elsif ($1 ne "note")
			{
				error("cannot process tag '$1'");
			}
		}
		elsif (/^\s*<nd ref=['"]([0-9-]+)["']/)
		{
			my $id=$1;
			error("dangling reference to node $id") unless defined($nodes->{$id});
			$polybuf .= sprintf("   %f   %f\n", $nodes->{$id}->[1], $nodes->{$id}->[0]);
		}
		elsif (/^\s*<\/way/) 
		{
			if (!defined($polybuf))
			{
				error("incomplete way definition");
			}
			$poly_id = ++$id unless defined($poly_id);
			$outbuf .= "$poly_id\n$polybuf"."END\n";
			undef $polybuf;
		}
	}
	$poly_file = "polygon" unless defined($poly_file);
	return "$poly_file\n$outbuf"."END\n";
}

sub openlayers {
	error("no OpenLayers output yet");
}