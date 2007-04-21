##################################################################
package Utils::LWP::Utils;
##################################################################

use Exporter;
@ISA = qw( Exporter );
use vars qw(@ISA @EXPORT @EXPORT_OK %EXPORT_TAGS $VERSION);
@EXPORT = qw( mirror_file 
	      $PROXY
	      $NO_MIRROR);

use strict;
use warnings;

use LWP::UserAgent;

use Utils::Debug;

our $PROXY='';

our $NO_MIRROR=0;

our $lwp_last_was_bytes=0;
our $lwp_bytes=0;
our $lwp_timer=0;
{
    no warnings;
    eval {
	sub LWP::Debug::debug {    
	    my $out_string = shift;
	    if ( $out_string =~ m/read (\d+) bytes/) {
		my $anz = $1;
		$lwp_bytes += $anz;
		if ( time() > $lwp_timer+1){
		    printf STDERR"LWP: got %d Bytes (%.4f MB)\r",$lwp_bytes,$lwp_bytes/1024/1024
			if $DEBUG>3 || $VERBOSE>4;
		    $lwp_last_was_bytes=1;
		    $lwp_timer = time();
		}
	    } else {
		if ( $lwp_last_was_bytes) {
		    $out_string ="\n".$out_string;
		    $lwp_bytes=0;
		}
		printf STDERR "LWP: $out_string\n"
		    if $DEBUG>3 || $VERBOSE>6;
		$lwp_last_was_bytes=0;
	    }
	};
    }
}

sub mirror_file($$){
    my $url            = shift;
    my $local_filename = shift;

    my $mirror=1;


    return 1 if $NO_MIRROR;

    # LPW::UserAgent initialisieren
    my $ua = LWP::UserAgent->new;

    # Set Proxy from Environment
    if (!$PROXY) {
        $PROXY ||= $ENV{'PROXY'};
        $PROXY ||= $ENV{'http_proxy'};
        print "Set Proxy to $PROXY\n" 
	    if $PROXY && ( $DEBUG >2|| $VERBOSE>4);
    }
    if ( $PROXY ){
        $PROXY = "http://$PROXY" unless $PROXY =~ m,^.?.tp://,;
        $PROXY = "$PROXY/"       unless $PROXY =~ m,/$,;
        $ua->proxy(['http','ftp'],$PROXY);
    }
    
    #$ua->level("+trace") if $DEBUG;

    print STDERR "mirror_file($url --> $local_filename)\n" if $DEBUG>2 || $VERBOSE>2;
    my $response = $ua->mirror($url,$local_filename);
#   printf STDERR "success = %d <%s>",$response->is_success,$response->status_line if $DEBUG;
    
    if ( ! $response->is_success ) {
        if ( $response->status_line =~ /^304/ ) {
            print "mirror_file($url): NOT MODIFIED\n" if $DEBUG ;
            $mirror=2;
        } else {
            print "mirror_file($url): COULD NOT GET\n";
	    print sprintf("ERROR: %s\n",$response->message)
                if $DEBUG || $VERBOSE;
            $mirror=0;
        }
    } else {
        print STDERR "mirror_file($url): OK\n" if $DEBUG>1 || $VERBOSE>4;
    }    
    return $mirror;
}

1;

__END__

=head1 NAME

Utils.pm

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
