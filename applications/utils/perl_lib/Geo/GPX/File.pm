##################################################################
package Geo::GPX::File;
##################################################################

use Exporter;
@ISA = qw( Exporter );
use vars qw(@ISA @EXPORT @EXPORT_OK %EXPORT_TAGS $VERSION);
@EXPORT = qw( read_gpx_file write_gpx_file  debug_write_track);


use strict;
use warnings;
use Carp;

use Date::Parse;
use Data::Dumper;
use Date::Parse;
use Date::Manip;
use POSIX qw(strftime);

use Geo::Geometry;
use Utils::File;
use Utils::Math;
use Utils::Debug;

# -----------------------------------------------------------------------------
# Read GPS Data from GPX - File
sub read_gpx_file($;$) { 
    my $filename      = shift;
    my $real_filename = shift || $filename;

    my $start_time=time();
    my $fh;

    my $new_tracks={
	filename => $real_filename,
	tracks => [],
	wpt => []
	};

    $fh = data_open($filename);
    if ( ! ref($filename) =~ m/IO::File/ ) {
	print STDERR "Parsing file: $filename\n" if $DEBUG;
    }
    return $new_tracks unless $fh;

    my $p = XML::Parser->new( Style => 'Objects' ,
			      );
    
    my $content = [{Kids => []}];
    eval {
	$content = $p->parse($fh);
    };
    if ( $@ ) {
	warn "$@Error while parsing\n $filename\n";
	#print "Parsing Content:".Dumper(\$content) if $DEBUG>99;
	#return $content->[0]->{Kids};
    }
    if ( $content && (scalar(@{$content})>1) ) {
	die "More than one top level Section was read in $filename\n";
    }
    if (not $p) {
	print STDERR "WARNING: Could not parse osm data\n";
	return $new_tracks;
    }
    if ( $VERBOSE >1 ) {
	printf STDERR "Read and parsed $filename";
	print_time($start_time);
    }

    #print Dumper(keys %{$content});
    #print Dumper(\$content);
    $content = $content->[0];
    $content = $content->{Kids};


#    print "Parsing Content:".Dumper(\$content) if $DEBUG>99;

    # Extract Waypoints
    for my $elem ( @{$content} ) {
	next unless ref($elem) eq "Geo::GPX::File::wpt";
	my $wpt_elem = $elem->{Kids};
	my $new_wpt={};
	$new_wpt->{lat} = $elem->{lat};
	$new_wpt->{lon} = $elem->{lon};
	for my $elem ( @{$wpt_elem} ) {
	    my $found=0;
	    for my $type ( qw ( name ele
				cmt desc
				sym pdop vdop
				course  fix hdop sat speed time )) {
		if ( ref($elem) eq "Geo::GPX::File::$type" ){
		    $new_wpt->{$type} = $elem->{Kids}->[0]->{Text};
		    $found++;
		}
	    }
	    if ( $found ){
	    } elsif (ref($elem) eq 'Geo::GPX::File::Characters') {
	    } else {
		printf STDERR "unknown tag in Waypoint:".Dumper(\$elem);
	    }
	}
	#printf STDERR Dumper(\$new_wpt);
	push(@{$new_tracks->{wpt}},$new_wpt);
    }
    
    # Extract Tracks
    for my $elem ( @{$content} ) {
	next unless ref($elem) eq "Geo::GPX::File::trk";
	#	    GPX::trkseg
	$elem = $elem->{Kids};
	#printf STDERR "Tracks: ".ref($elem)." ".Dumper(\$elem);
	my $new_track=[];
	for my $trk_elem ( @{$elem} ) {
	    next unless ref($trk_elem) eq "Geo::GPX::File::trkseg";
	    $trk_elem = $trk_elem->{Kids};
	    #printf STDERR "Track: ".ref($elem)." ".Dumper(\$trk_elem);
	    for my $trk_pt ( @{$trk_elem} ) {
		next unless ref($trk_pt) eq "Geo::GPX::File::trkpt";
		#printf STDERR "Track Point:".Dumper(\$trk_pt);
		for my $trk_pt_kid ( @{$trk_pt->{Kids}} ) {
		    next if ref($trk_pt_kid) eq "Geo::GPX::File::Characters";
		    #printf STDERR "Track Point Kid:".Dumper(\$trk_pt_kid);
		    my $ref = ref($trk_pt_kid);
		    my ( $type ) = ($ref =~ m/Geo::GPX::File::(.*)/ );
		    $trk_pt->{$type} = $trk_pt_kid->{Kids}->[0]->{Text};
		}
		my $trk_time = $trk_pt->{time};
		if ( defined $trk_time ) {
		    #printf STDERR "trk_time $trk_time\n";
		    my $time = str2time( $trk_time);
		    my $ltime = localtime($time);
		    my ($year,$month) = split(/-/,$trk_time);
		    if ( $year < 1970 ) {
			warn "Ignoring Dataset because of Strange Date $trk_time ($ltime) in GPX File\n";
			next;
		    };
		    if ( $DEBUG >= 11 ) {
			printf STDERR "time: $ltime  ".$trk_pt->{time}."\n\n";
		    }
		    $trk_pt->{time_string} = $trk_pt->{time};
		    $trk_pt->{time} = $time;
		}

		delete $trk_pt->{Kids};
		#printf STDERR "Final Track Point:".Dumper(\$trk_pt);
		push(@{$new_track},$trk_pt);
	    }
	}
	push(@{$new_tracks->{tracks}},$new_track);
    }

    #printf STDERR Dumper(\$new_tracks);
    return $new_tracks;
}

#------------------------------------------------------------------
sub write_gpx_file($$) { # Write an gpx File
    my $tracks = shift;
    my $filename = shift;

    my $start_time=time();

    # TODO: This has to get a good interface
    my $write_gpx_wpt=$main::write_gpx_wpt;
    my $fake_gpx_date=$main::fake_gpx_date;
    
    die "fake_gpx_date not defined \n" 
	unless defined $fake_gpx_date;

    printf STDERR ("Writing GPS File $filename\n") if $VERBOSE >1 || $DEBUG >1;

    my $fh;
    if ( $filename eq '-' ) {
	$fh = IO::File->new(">&STDOUT");
    } else {
	$fh = IO::File->new(">$filename");
    }
    if ( !$fh ) {
	warn("Cannot Open $filename for writing:$!");
	return;
    }
    print $fh "<?xml version=\"1.0\"?>\n";
    print $fh "<gpx \n";
    print $fh "    version=\"1.0\"\n";
    print $fh "    creator=\"osmfilter Converter\"\n";
    print $fh "    xmlns=\"http://www.ostertag.name\"\n";
    print $fh "    >\n";
    # <bounds minlat="47.855922617" minlon ="8.440864999" maxlat="48.424462667" maxlon="12.829756737" />
    # <time>2006-07-11T08:01:39Z</time>

    my $point_count=0;

    # write tracks
    my $fake_time=0;
    my $track_id=0;
    for my $track ( @{$tracks->{tracks}} ) {
	$track_id++;
	print $fh "\n";
	print $fh "<trk>\n";
	print $fh "   <name>$filename $track_id</name>\n";
	print $fh "   <number>$track_id</number>\n";
	print $fh "    <trkseg>\n";

	for my $elem ( @{$track} ) {
	    $point_count++;
	    my $lat  = $elem->{lat};
	    my $lon  = $elem->{lon};
	    if ( abs($lat) >90 || abs($lon) >180 ) {
		warn "write_gpx_track: Element ($lat/$lon) out of bound\n";
		next;
	    };
	    print $fh "     <trkpt lat=\"$lat\" lon=\"$lon\">\n";
	    if( defined $elem->{ele} ) {
		print $fh "       <ele>$elem->{ele}</ele>\n";
	    };
	    # --- time
	    if ( defined ( $elem->{time} ) ) {
		#print Dumper(\$elem);

		##################
		my ($time_sec,$time_usec)=( $elem->{time} =~ m/(\d+)(\.\d*)?/);
		if ( defined($time_usec) ) {
		    $time_usec =~ s/^\.//;
		}
		if ( $time_sec && $time_sec < 3600*30 ) {
		    #print "---------------- time_sec: $time_sec\n";
		}
		if ( $fake_gpx_date ) {
		    $fake_time += rand(10);
		    $time_sec = $fake_time;
		}
		my $time = strftime("%FT%H:%M:%SZ", localtime($time_sec));
		#UnixDate("epoch ".$time_sec,"%m/%d/%Y %H:%M:%S");
		$time =~ s/Z/.${time_usec}Z/ if $time_usec && ! $fake_gpx_date;
		if ( $DEBUG >20) {
		    printf STDERR "elem-time: $elem->{time} UnixDate: $time\n";
		}
		print $fh "       <time>".$time."</time>\n";
	    }
	    # --- other attributes
	    for my $type ( qw ( name ele
				cmt course  
				fix pdop hdop vdop sat
				speed  )) {
		next if $fake_gpx_date && ($type eq "time");
		my $value = $elem->{$type};
		if( defined $value ) {
		    print $fh "       <$type>$value</$type>\n";
		}
	    };
	    print $fh "     </trkpt>\n";
	}
	print $fh "    </trkseg>\n";
	print $fh "</trk>\n\n";
	
    }

    # write Waypoints
    if ( $write_gpx_wpt ) {
	print $fh "\n";
	for my $wpt ( @{$tracks->{wpt}} ) {
	    my $lat  = $wpt->{lat};
	    my $lon  = $wpt->{lon};
	    print $fh " <wpt lat=\"$lat\" lon=\"$lon\">\n";
	    #print $fh "     <name>$wpt->{name}</name>\n";
	    for my $type ( qw ( name ele
				cmt desc
				sym
				course  fix hdop sat speed time )) {
		my $value = $wpt->{$type};
		next if $fake_gpx_date && ($type eq "time");
		if( defined $value ) {
		    print $fh "     <$type>$value</$type>\n";
		}
	    };
	    print $fh " </wpt>\n";
	}
    }

    print $fh "</gpx>\n";
    $fh->close();

    my $comment = "wrote to GPX File";
    printf STDERR "%-35s: %5d Points in %d Tracks $comment",$filename,$point_count,$track_id;
    print_time($start_time);
}

# ------------------------------------------------------------------
# Write intermediate raw gpx Files for Debugging
sub debug_write_track($$){
    my $tracks       = shift; # Track to write
    my $name_suffix = shift;
    my $filename=$tracks->{filename};

    if ( $main::out_raw_gpx && $DEBUG >3 ){
	my $new_gpx_file = $filename;
	return unless $new_gpx_file =~s/(\.gpx)?$/$name_suffix.gpx/;
	write_gpx_file($tracks,$new_gpx_file);
    };
}


1;

=head1 NAME

Geo::GPX::File

=head1 COPYRIGHT

Copyright 2006, Jörg Ostertag

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

=head1 AUTHOR

Jörg Ostertag (planet-count-for-openstreetmap@ostertag.name)

=head1 SEE ALSO

http://www.openstreetmap.org/

=cut
