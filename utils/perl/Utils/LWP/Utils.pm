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
			if $DEBUG || $VERBOSE;
		    $lwp_last_was_bytes=1;
		    $lwp_timer = time();
		}
	    } else {
		if ( $lwp_last_was_bytes) {
		    $out_string ="\n".$out_string;
		    $lwp_bytes=0;
		}
		printf STDERR "LWP: $out_string\n"; 
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
        print "Set Proxy to $PROXY\n" if $PROXY;
    }
    if ( $PROXY ){
        $PROXY = "http://$PROXY" unless $PROXY =~ m,^.?.tp://,;
        $PROXY = "$PROXY/"       unless $PROXY =~ m,/$,;
        $ua->proxy(['http','ftp'],$PROXY);
    }
    
    #$ua->level("+trace") if $DEBUG;

    print STDERR "mirror_file($url --> $local_filename)\n" if $DEBUG || $VERBOSE;
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
        print "mirror_file($url): OK\n" if $DEBUG; 
    }    
    return $mirror;
}

1;
