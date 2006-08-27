##################################################################
package File;
##################################################################

use Exporter; require DynaLoader; require AutoLoader;
@ISA = qw(Exporter DynaLoader);
@EXPORT = qw(
	     data_open
	     );

use IO::File;

# -----------------------------------------------------------------------------
# Open Data File in predefined Directories
sub data_open($){
    my $filename = shift;
    my $fh;

    # If it's already an open File
    if ( ref($filename) =~ m/IO::File/ ) {
	return $filename;
    }

    if ( $filename eq "-" ) {
	$fh = IO::File->new('<&STDIN');
	$fh or  die("cannot open $filename: $!");
	return $fh;
    } else {
	my $size = (-s $filename)||0;
	if ( $size < 270 ) {
	    warn "cannot Open $filename ($size) Bytes is too small)\n"
		if $verbose || $debug;
	    return undef;
	}
    }

    printf STDERR "Opening $filename\n" if $debug;
    if ( $filename =~ m/\.gz$/ ) {
	$fh = IO::File->new("gzip -dc $filename|")
	    or die("cannot open $filename: $!");
    } elsif ( $filename =~ m/\.bz2$/ ) {
	$fh = IO::File->new("bzip2 -dc $filename|")
	    or die("cannot open $filename: $!");
    } else {
	$fh = IO::File->new("$filename",'r')
	    or die("cannot open $filename: $!");
    }
    return $fh;
}
