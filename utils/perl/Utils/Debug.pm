##################################################################
package Utils::Debug;
##################################################################

use Exporter;
@ISA = qw( Exporter );
use vars qw(@ISA @EXPORT @EXPORT_OK %EXPORT_TAGS $VERSION);
@EXPORT = qw( $DEBUG $VERBOSE );

our $DEBUG   = 0;
our $VERBOSE = 0;

use strict;
use warnings;

1;
