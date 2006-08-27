##################################################################
package Utils::Timing;
##################################################################

use Exporter; require DynaLoader; require AutoLoader;
@ISA = qw(Exporter DynaLoader);
@EXPORT = qw(print_time);


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
