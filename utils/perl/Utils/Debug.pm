##################################################################
package Utils::Debug;
##################################################################

use Exporter;
@ISA = qw( Exporter );
use vars qw(@ISA @EXPORT @EXPORT_OK %EXPORT_TAGS $VERSION);
@EXPORT = qw( $DEBUG $VERBOSE
	      mem_usage
	      time_estimate
	      print_time
	      );

our $DEBUG   = 0;
our $VERBOSE = 0;

use strict;
use warnings;


# print the time elapsed since starting
# starting_time is the first argument
sub print_time($){
    my $start_time = shift;
    my $time_diff = time()-$start_time;
    if ( $time_diff > 1 ) {
	printf STDERR " in %.0f sec", $time_diff;
    }
    printf STDERR "\n";
}

# get memory usage from /proc Filesystem
sub mem_usage(){
    my $proc_file = "/proc/$$/statm";
    my $msg = '';
    if ( -r $proc_file ) {
	my $statm = `cat $proc_file`;
	chomp $statm;
	my @statm = split(/\s+/,$statm);
	my $vsz = ($statm[0]*4)/1024;
	my $rss = ($statm[1]*4)/1024;
	#      printf STDERR " PID: $$ ";
	$msg .= sprintf( "VSZ: %.0f MB ",$vsz);
	$msg .= sprintf( "RSS: %.0f MB",$rss);
    }
    return $msg;
}

sub time_estimate($$$){
    my $start_time = shift;
    my $elem_no    = shift;
    my $elem_max   = shift;

    my $time_diff=time()-$start_time;
    my $time_estimated= $time_diff*$elem_no/$elem_max;
    my $msg = sprintf( " time %.0f min rest: %.0f min",$time_diff/60,$time_estimated/60);
    return $msg;
}

1;
