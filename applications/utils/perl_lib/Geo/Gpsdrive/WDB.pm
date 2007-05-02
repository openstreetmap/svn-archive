# Einlesen der WDB Daten und schreiben in die geodb Datenbank von 
# gpsdrive
#

package Geo::Gpsdrive::WDB;

use strict;
use warnings;

use IO::File;
use File::Basename;
use File::Path;
use Data::Dumper;

use Geo::Gpsdrive::DBFuncs;
use Geo::Gpsdrive::Utils;

$|=1;

my $LINES_COUNT_FILE =0;
my $LINES_COUNT_UNPARSED =0;

##################################################################
# Alle Punkte rausschreiben
# Args:
#    $fo : Filedescriptor to write t
#    @{$points_in_segment} : List of points
sub write_points($$){
    my $fo = shift;
    my $points_in_segment = shift;
    if ( @{$points_in_segment} ) {
	for my $point ( @{$points_in_segment} , $points_in_segment->[0]  ) {
	    #$fo->point($point);
	    #$fo{$rank}->point($point) if $rank;
	    print $fo "$point->{lat} $point->{lon}\n";
	}
	print $fo "1001.0 1001.0\n";
    }
}

##########################################################################

sub import_wdb($){
    my  $full_filename = shift;

    print "Reading $full_filename                   \n";
    my $base_filename = basename($full_filename);
    my ( $area ) = ($base_filename =~ m/^([^-]+)/ );
    my $fh = IO::File->new("<$full_filename");
    my $segment = 0;
    my $rank    = 0;
    my $points  = 0;
    my ($lat1,$lon1) = (0,0);
    my ($lat2,$lon2) = (0,0);

    my ( $sub_source ) = ( $base_filename =~ m/(.*).txt/ );
    my ( $country,$type_string) = ( $base_filename =~ m/(.*)-(.*).txt/);

    my $source = "WDB $sub_source";
    Geo::Gpsdrive::DBFuncs::delete_all_from_source($source);
    my $source_id = Geo::Gpsdrive::DBFuncs::source_name2id($source);

    unless ( $source_id ) {
	my $source_hash = {
	    'source.url'     => "http://www.evl.uic.edu/pape/data/WDB/WDB-text.tar.gz",
	    'source.name'    => $source ,
	    'source.comment' => '' ,
	    'source.licence' => ""
	    };
	Geo::Gpsdrive::DBFuncs::insert_hash("source", $source_hash);
	$source_id = Geo::Gpsdrive::DBFuncs::source_name2id($source);
    }
    


    my $area_limit=0;
    if ( $main::lat_min ||
	 $main::lat_max ||
	 $main::lon_min ||
	 $main::lon_max ) {
	$area_limit=1;
    }




    my $streets_type_id=0;
    my @segments;
    my $line_number = 0;
    my $sum_points  = 0;
    while ( my $line = $fh->getline() ) {
	chomp $line;
	#print "line: $line\n";
	$line_number++;
	if ( $line =~ m/^\s*$/)  {
	} elsif ( $line =~ m/^segment\s+(\d+)\s+rank\s+(\d+)\s+points\s+(\d+)/ ) {
	    if ( @segments ) {
		street_segments_add(
				{ streets_type_id => $streets_type_id, 
				  source_id       => $source_id,
				  segments        => \@segments
				  }
				    ); 
	    };
	    # Segment: segment 27  rank 1  points 1131
	    ($segment,$rank,$points) = ( $1,$2,$3) ;
	    @segments=();
	    $sum_points += $points;
	    print "Segment: $segment, rank: $rank  points: $points        \r";
	    print "\n" if $verbose>1;
	    ( $lat1,$lon1 ) = ( $lat2 , $lon2 ) = (0,0);

	    # ---------------------- Type    
	    my $type_name = "WDB $area $type_string rank $rank";
	    $streets_type_id = streets_type_name2id($type_name);
	    die "Missing Street Type $type_name\n" unless $streets_type_id;
	} elsif ( $line =~ m/^\s*([\d\.\-]+)\s+([\d\.\-]+)\s*$/ ) {
	    ( $lat1,$lon1 ) = ( $lat2 , $lon2 );
	    ( $lat2,$lon2 ) = ($1,$2);
	    # 31.646111 25.148056
	    if ( $area_limit  && 
		 ( $lat2 < $main::lat_min || $lat2 > $main::lat_max ||
		   $lon2 < $main::lon_min || $lon2 > $main::lon_max 
		   )
		 ){
		#print "Skipping $lat2,$lon2\n";
		next;
	    } 


	    push(@segments,{
		lat=> $lat2, lon=>$lon2,
		name => "$rank : $segment : $points"
		});
	} else {
	    warn "WDB import: Unrecognized Line  $line_number:'$line'\n";
	}
    }
    street_segments_add( {
	streets_type_id => $streets_type_id, 
	source_id       => $source_id,
	segments        => \@segments
	} );

    print "Read $line_number lines and inserted $sum_points Points\n" 
	if $verbose;
}




# *****************************************************************************
sub import_Data($){
    my $what = shift || "europe,africa,asia,namer,samer";

    my $mirror_dir="$main::MIRROR_DIR/wdb";
    my $unpack_dir="$main::UNPACK_DIR/wdb";

    print "\nDownload and import CIA World DataBank II for $what\n";

    -d $mirror_dir or mkpath $mirror_dir
	or die "Cannot create Directory $mirror_dir:$!\n";
    
    -d $unpack_dir or mkpath $unpack_dir
	or die "Cannot create Directory $unpack_dir:$!\n";
    
    my $url = "http://www.evl.uic.edu/pape/data/WDB/WDB-text.tar.gz";
    my $tar_file = "$mirror_dir/WDB-text.tar.gz";
    print "Mirror $url\n";
    my $mirror = mirror_file($url,$tar_file);

    my $dst_file="$unpack_dir/WDB/europe-bdy.txt";
    if ( (!-s $dst_file) ||
	 file_newer($tar_file,$unpack_dir ) ) {
	print "Unpacking $tar_file\n";
	`(cd $unpack_dir/; tar -xvzf $tar_file)`;
    } else {
	print "unpack: $dst_file up to date\n" unless $verbose;
    }
    

    # extract to desired data from the files
    if ( $what =~  m/^\d/ ) {
	$what ="europe,africa,asia,namer,samer";
    }

    disable_keys('streets');
    
    for my $country ( split(",",$what) ) {
	if ( $country !~ m/europe|africa|asia|namer|samer/ ) {
	    die ("$country for WDB not supported\n");
	}
	debug("$unpack_dir/WDB/*.txt");
	foreach  my $full_filename ( glob("$unpack_dir/WDB/$country*.txt") ) {
	    # print "Mirror: $mirror\n";
	    import_wdb($full_filename);
	};
    };
    enable_keys('streets');
      
    print "Download an import WDB Data FINISHED\n";
}

1;
