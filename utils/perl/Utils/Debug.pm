##################################################################
package Utils::Debug;
##################################################################

use Exporter;
@ISA = qw( Exporter );
use vars qw(@ISA @EXPORT @EXPORT_OK %EXPORT_TAGS $VERSION);
@EXPORT = qw( $DEBUG $VERBOSE 
	      print_time);

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

1;
