#!/usr/bin/perl

=pod

=head1 build-cluster

Build_Cluster is a system to build various Debian Based Packages
we expect a chroot environment to already be setup in order to 
then be able to do the debuild commands inside these.


Show a summary of all results. This also looks for cached results.

=cut

package BuildTask;

use strict;
use warnings;

use Data::Dumper;
use File::Basename;
use File::Copy;
use File::Find;
use File::Path;
use File::Slurp qw( slurp write_file read_file append_file) ;
use Getopt::Long;
use Getopt::Std;
use IO::Select;
use IO::File;
use IPC::Open3;
use Symbol;


my $dir_chroot = "/home/chroot";
my $dir_log = "/home/chroot/log";
my $dir_svn = "$dir_chroot/svn";
my $package_result_dir = "$dir_chroot/results";
my $package_result_upload_dir = "/home/httpd/gpsdrive.de"; #This is the upload Directory
my $user = "tweety";
my $DEBUG   = 0;
my $VERBOSE = 1;
my $MANUAL=0;
my $HELP=0;
my $FORCE=0;

my $do_svn_up=1;
my $do_svn_co=1;
my $do_svn_changelog = 1;
my $do_svn_cp= 1;
my $do_show_results=0;
my $do_write_html_results_only=0;

my $RESULTS={};

my $do_fast= 1; # Skip Stuff like debuild clean, ...

delete $ENV{http_proxy};
delete $ENV{HTTP_PROXY};
$ENV{LANG}="C";
$ENV{DEB_BUILD_OPTIONS}="parallel=4";


# define Colors
my $ESC="\033";
my %COLOR;
$COLOR{RED}="${ESC}[91m";
$COLOR{GREEN}="${ESC}[92m";
$COLOR{YELLOW}="${ESC}[93m";
$COLOR{BLUE}="${ESC}[94m";
$COLOR{MAGENTA}="${ESC}[95m";
$COLOR{CYAN}="${ESC}[96m";
$COLOR{WHITE}="${ESC}[97m";
$COLOR{BG_RED}="${ESC}[41m";
$COLOR{BG_GREEN}="${ESC}[42m";
$COLOR{BG_YELLOW}="${ESC}[43m";
$COLOR{BG_BLUE}="${ESC}[44m";
$COLOR{BG_MAGENTA}="${ESC}[45m";
$COLOR{BG_CYAN}="${ESC}[46m";
$COLOR{BG_WHITE}="${ESC}[47m";
$COLOR{BRIGHT}="${ESC}[01m";
$COLOR{UNDERLINE}="${ESC}[04m";
$COLOR{BLINK}="${ESC}[05m";
$COLOR{REVERSE}="${ESC}[07m";
$COLOR{NORMAL}="${ESC}[0m";


# Platform is a combination of "Distribution - Revision - 32/64Bit"
#    debian-etch-32
#    debian-etch-64
my @available_platforms= qw(
    debian-squeeze-64   debian-squeeze-32
    debian-lenny-64     debian-lenny-32
    ubuntu-hardy-64     ubuntu-hardy-32
    ubuntu-intrepid-64  ubuntu-intrepid-32
);
my @default_platforms= qw(
    debian-squeeze-64
    debian-squeeze-32
    ubuntu-hardy-64
);
@default_platforms= @available_platforms;

my @platforms;

my %proj2path=(
    'gpsdrive-maemo' 	=> 'gpsdrive/contrib/maemo',
    'gpsdrive-data-maps'=> 'gpsdrive/data/maps',
    'gpsdrive'	 	=> 'gpsdrive',
    'gpsdrive-2.10pre7'	=> 'gpsdrive-2.10pre7',
    'gpsdrive-data-maps-2.10pre7'=> 'gpsdrive-2.10pre7/data/maps',
    'opencarbox' 	=> 'opencarbox',
    'mod_tile'		=> 'openstreetmap-applications/utils/mod_tile',
    'osm2pgsql' 	=> 'openstreetmap-applications/utils/export/osm2pgsql',
    'merkaartor' 	=> 'openstreetmap-applications/editors/merkaartor',
    'josm' 		=> 'openstreetmap-applications/editors/josm',
    'osm-utils'		=> 'openstreetmap-applications/utils',
    'osm-mapnik-world-boundaries' 	=> 'openstreetmap-applications/rendering/mapnik/openstreetmap-mapnik-world-boundaries',
    'osm-mapnik-data' 	=> 'openstreetmap-applications/rendering/mapnik/openstreetmap-mapnik-data',
    'map-icons' 	=> 'openstreetmap-applications/share/map-icons',
    'osmosis' 		=> 'openstreetmap-applications/utils/osmosis/trunk',
    'gosmore'	 	=> 'openstreetmap-applications/rendering/gosmore',

    'merkaartor-0.12' 	=> 'openstreetmap-applications/editors/merkaartor-branches/merkaartor-0.12-fixes',
    'merkaartor-0.11' 	=> 'openstreetmap-applications/editors/merkaartor-branches/merkaartor-0.11-fixes',
    'merkaartor-0.13' 	=> 'openstreetmap-applications/editors/merkaartor-branches/merkaartor-0.13-fixes',
#    'osm-editor' 	=> 'openstreetmap-applications/editors/osm-editor',
    'osm-editor-qt3' 	=> 'openstreetmap-applications/editors/osm-editor/qt3',
    'osm-editor-qt4' 	=> 'openstreetmap-applications/editors/osm-editor/qt4',
    );

my %proj2debname=(
    'gpsdrive-maemo' 	=> 'gpsdrive',
    'gpsdrive-data-maps'=> 'gpsdrive-data-maps',
    'gpsdrive'	 	=> 'gpsdrive',
    'gpsdrive-2.10pre5' => 'gpsdrive',
    'gpsdrive-2.10pre7' => 'gpsdrive',
    'gpsdrive-data-maps-2.10pre7'=> 'gpsdrive-data-maps',
    'opencarbox' 	=> 'opencarbox',
    'osm2pgsql' 	=> 'osm2pgsql',
    'mod_tile'		=> 'libapache2-mod-tile',
    'merkaartor' 	=> 'merkaartor',
    'josm' 		=> 'openstreetmap-josm',
    'osm-utils'		=> 'openstreetmap-utils',
    'osm-mapnik-world-boundaries' 	=> 'openstreetmap-mapnik-world-boundaries',
    'osm-mapnik-data' 	=> 'openstreetmap-mapnik-data',
    'map-icons' 	=> 'openstreetmap-map-icons',
    'osmosis' 		=> 'osmosis',
    'gosmore'	 	=> 'openstreetmap-gosmore',

    'merkaartor-0.12' 	=> 'merkaartor-0.12-fixes',
    'merkaartor-0.11' 	=> 'merkaartor-0.11-fixes',
    'merkaartor-0.13' 	=> 'merkaartor-0.13-fixes',
#    'osm-editor' 	=> 'openstreetmap-editor',
    'osm-editor-qt3' 	=> 'openstreetmap-editor',
    'osm-editor-qt4' 	=> 'openstreetmap-editor',
    );
my %package_names=(
    'gpsdrive'	 	=> [qw(gpsdrive gpsdrive-friendsd gpsdrive-utils)],
    'gpsdrive-maemo' 	=> [qw(gpsdrive)],
    'gpsdrive-data-maps'=> [qw(gpsdrive-data-maps)],
    'gpsdrive-2.10pre5' => [qw(gpsdrive gpsdrive-friendsd gpsdrive-utils)],
    'gpsdrive-2.10pre7' => [qw(gpsdrive gpsdrive-friendsd gpsdrive-utils)],
    'gpsdrive-data-maps-2.10pre7'=> [qw(gpsdrive-data-maps)],
    'opencarbox' 	=> [qw(opencarbox)],
    'mod_tile'		=> [qw(libapache2-mod-tile renderd)],
    'osm2pgsql' 	=> [qw(osm2pgsql)],
    'merkaartor' 	=> [qw(merkaartor)],
    'josm' 		=> [qw(openstreetmap-josm)],
    'osm-utils'		=> [qw(openstreetmap-utils openstreetmap-utils-export openstreetmap-utils-filter
                               openstreetmap-utils-lib openstreetmap-utils-import)],
    'osm-mapnik-world-boundaries' 	=> [qw(openstreetmap-mapnik-world-boundaries)],
    'osm-mapnik-data' 	=> [qw(openstreetmap-mapnik-data)],
    'map-icons' 	=> [qw(openstreetmap-map-icons-classic.big openstreetmap-map-icons-classic.small
                               openstreetmap-map-icons-info openstreetmap-map-icons-japan-png
                               openstreetmap-map-icons-japan openstreetmap-map-icons-nickw
                               openstreetmap-map-icons-square.big openstreetmap-map-icons-square.small-minimal
                               openstreetmap-map-icons-square.small openstreetmap-map-icons-svg-png
                               openstreetmap-map-icons-svg-twotone-png openstreetmap-map-icons-svg-twotone
                               openstreetmap-map-icons-svg openstreetmap-map-icons)],
    'osmosis' 		=> [qw(openstreetmap-osmosis)],
    'gosmore'	 	=> [qw(openstreetmap-gosmore)],
    'merkaartor-0.12' 	=> [qw(merkaartor)],
    'merkaartor-0.11' 	=> [qw(merkaartor)],
    'merkaartor-0.13' 	=> [qw(merkaartor)],
#    'osm-editor' 	=> [qw(osm-editor)],
#    'osm-editor-qt3' 	=> [qw(osm-editor)],
#    'osm-editor-qt4' 	=> [qw(osm-editor)],
    );

my %svn_repository_url=(
    'openstreetmap-applications' => 'http://svn.openstreetmap.org/applications',
    'gpsdrive'                   => 'https://gpsdrive.svn.sourceforge.net/svnroot/gpsdrive/trunk',
    'opencarbox'                 => 'https://opencarbox.svn.sourceforge.net/svnroot/opencarbox/OpenCarbox/trunk',

    'gpsdrive-2.10pre5'          => 'https://gpsdrive.svn.sourceforge.net/svnroot/gpsdrive/branches/gpsdrive-2.10pre5',
    'gpsdrive-2.10pre6'          => 'https://gpsdrive.svn.sourceforge.net/svnroot/gpsdrive/branches/gpsdrive-2.10pre6',
    'gpsdrive-2.10pre7'          => 'https://gpsdrive.svn.sourceforge.net/svnroot/gpsdrive/branches/gpsdrive-2.10pre7',
    'gpsdrive-data-maps-2.10pre7'          => 'https://gpsdrive.svn.sourceforge.net/svnroot/gpsdrive/branches/gpsdrive-2.10pre7',
    );

my %svn_update_done;

my @available_proj=  sort keys %package_names;
my @all_proj = grep { $_ !~ m/osmosis|gpsdrive-maemo|merkaartor|merkaartor-0...|gpsdrive-2.10pre/ } @available_proj;# |osm-editor-qt4

my @projs;
#@projs= keys %proj2path;
my @default_projs=@all_proj;
#@default_projs=qw( gpsdrive-data-maps gpsdrive map-icons osm-utils);
#@default_projs=qw( gpsdrive gpsdrive-data-maps map-icons osm-utils merkaartor opencarbox osm2pgsql   );# josm gosmore osmosis

sub usage($);


# --------------------------------------------
# Get Options

my $getopt_result = GetOptions (
    "debug+"        => \$DEBUG,
    "verbose+"      => \$VERBOSE,
    "d+"            => \$DEBUG,
    "v+"            => \$VERBOSE,
    'help!'         => \$HELP,
    'manual!'       => \$MANUAL,

    "fast!"         => \$do_fast,
    "force!"        => \$FORCE,
    "color!"        => sub { my ($a,$b)=(@_);
			     if ( ! $b  ) {
				 for my $k ( keys %COLOR ) {
				     $COLOR{$k}='';
				 }
			     }
    },
    "svn!"          => sub { my ($a,$b)=(@_);
			     $do_svn_up        = $b;
			     $do_svn_co        = $b;
			     $do_svn_changelog = $b;
			     $do_svn_cp        = $b;
    },
    "svn-up!"        => \$do_svn_up,
    "svn-co!"        => \$do_svn_co,
    "svn-changelog!" => \$do_svn_changelog,
    "svn-cp!"        => \$do_svn_cp,
    "dir-chroot=s"   => \$dir_chroot,     
    "dir-svn"        => \$dir_svn,
    "dir-package-results" => \$package_result_dir,
    "user"           => \$user,
    "platforms=s"    => sub { my ($a,$b)=(@_);
			      if ( '*' eq $b ) {
				  @platforms= @available_platforms;
			      } elsif ( $b =~ m/\*/ ) {
				  $b =~ s,\*,\.\*,g;
				  @platforms= grep { $_ =~ m{$b} } @available_platforms;
			       } else {
				   @platforms = split(',',$b);
			       }
},
    "projects=s"     => sub { my ($a,$b)=(@_);
			      if ( '*' eq $b ) {
				  @projs= @all_proj;
			      } elsif ( $b =~ m/\*/ ) {
				  $b =~ s,\*,\.\*,g;
				  @projs= grep { $_ =~ m{$b} } @all_proj;
			      } else {
				  @projs = split(',',$b);
			      }
},
    'show-results'      =>  \$do_show_results,
    'write-html-results-only!' => \$do_write_html_results_only,
    );

if ( ! $getopt_result ) {
    die "Unknown Option\n";
    usage(0);
}

usage( $MANUAL )
    if $MANUAL
    || $HELP;

# ------------------------------------------------------------------
# Create a new BuildTask object
sub new {
    my $pkg = shift;
    my $self;
    $self= {@_};
    bless $self, $pkg;
    $self->{section} = 'all' unless $self->{section};
#    print Dumper(\$self);
    return $self;
}

# ------------------------------------------------------------------
# Debugging output
sub debug($$$){
    my $self = shift;
    my $level = shift;
    my $msg = shift;

    my $platform = $self->{platform}||'';
    my $proj     = $self->{proj}||'';

    return
	unless $DEBUG >= $level;

    my $msg1= '';
    if (  $DEBUG > 5 ) {
	$msg1 = "($platform:$proj)";
    }
    my ( @msg) = split(/\n/,$msg);

    for my $m ( @msg ) {
	print STDERR "DEBUG$msg1: $m\n";
    }
}

# ------------------------------------------------------------------
# Set/Get Section for Logging
sub section($;$){
    my $self = shift;
    my $new_section= shift;

    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";

    if ( defined ($new_section) ) {
	$self->{section} = $new_section;
    }
    my $section  = $self->{section}||'all';
    return $section;
}

# ------------------------------------------------------------------
# Clear/Remove stored Log msgs
sub Clear_Log($$$){
    my $self = shift;

    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";

    my $platform = $self->platform();
    my $proj     = $self->proj();
    my $section  = $self->section();

    if ( ! -d $dir_log ) {
	die "Cannot Log, Directory '$dir_log' does not exist\n";
    }
    my $dst_dir="$dir_log/$proj/$platform";
    for my $file ( glob("$dst_dir/*.log*" ) ) {
	unlink $file;
    }
}

# ------------------------------------------------------------------
# Log a msg
sub Log($$$){
    my $self = shift;
    my $level    = shift;
    my $msg      = shift;

    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";

    my $platform = $self->platform();
    my $proj     = $self->proj();
    my $section  = $self->section();

    if ( ! -d $dir_log ) {
	die "Cannot Log, Directory '$dir_log' does not exist\n";
    }
    my $dst_dir="$dir_log/$proj/$platform";
    if ( ! -d $dst_dir ) {
	mkpath($dst_dir)
	    or warn "WARNING: Konnte Pfad $dst_dir nicht erzeugen: $!\n";
    }

    # Normal Log Output
    my $log_file ="$dst_dir/$section.log"; 
    append_file(  $log_file, "$msg\n" );

    # Html Log
    $log_file ="$dst_dir/$section.log.shtml"; 
    if ( ! -s  $log_file ) {
	append_file(  $log_file, "<html>\n<pre>\n" );
    }
    my $html_msg=$msg;
    $html_msg =~ s/\</&lt;/g;
    $html_msg =~ s/\>/&gt;/g;
    $html_msg =~ s/^(.*(error|warn|No rule to make target|cannot read directory).*)$/<font color="RED">$1<\/font>/gmi;
    $html_msg =~ s/^(.*(No package|was not found|No such file or directory|missing ).*)$/<font color="RED">$1<\/font>/gmi;
    $html_msg =~ s{\033\[0m}{<\/font>}g;
    $html_msg =~ s{\033\[91m}{<font color="RED">}g;
    $html_msg =~ s{\033\[92m}{<font color="GREEN">}g;
    $html_msg =~ s{\033\[94m}{<font color="BLUE">}g;
    append_file(  $log_file, "$html_msg\n" );
}

# ------------------------------------------------------------------
# return the filename for the last_result log File
sub last_result_file($){
    my $self       = shift;

    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";

    my $platform = $self->platform();
    my $proj     = $self->proj();

    if ( ! -d $dir_log ) {
	die "Cannot write Result, Directory '$dir_log' does not exist\n";
    }
    my $dst_dir="$dir_log/../last_result";
    if ( ! -d $dst_dir ) {
	mkpath($dst_dir)
	    or warn "WARNING: Cannot create Path '$dst_dir': $!\n";
    }

    my $last_log="$dst_dir/result-$platform-$proj.log";
    return $last_log;
		 }

# ------------------------------------------------------------------
# write or read the last result of a package 
# add caching of last result. This enables not building (successfull) two times the same package.
sub last_result($;$){
    my $self       = shift;
    my $new_result = shift; # success|fail|dependencies

    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";

    my $platform = $self->platform();
    my $proj     = $self->proj();

    my $last_log=$self->last_result_file();

    if ( defined($new_result) ) {
	my $svn_revision = $self->svn_revision_platform();
	append_file( $last_log , "$new_result: $svn_revision\n" );
	$self->{last_result}=$new_result;
    } else {
	my $last_result;
	if ( -r "$last_log" ) {
	    my @lines = read_file( $last_log ) ;
	    $last_result = pop(@lines);
	    chomp $last_result;
	} else {
	    $last_result='';
	}
	$self->{last_result}=$last_result;
	return $last_result;
    }  
}


# ------------------------------------------------------------------
# read the last good result of a package 
sub last_good_result($){
    my $self       = shift;

    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";

    my $platform = $self->platform();
    my $proj     = $self->proj();

    my $last_log=$self->last_result_file();

    my $last_result;
    if ( -r "$last_log" ) {
	my @lines = grep { $_ =~ m/success:/ } read_file( $last_log ) ;
	$last_result = pop(@lines) ||'';
	chomp $last_result;
    } else {
	$last_result='';
    }
    $last_result =~ s/.*\:\s*//g;
    $self->{last_good_result}=$last_result;
    return $last_result;
		 };


# ------------------------------------------------------------------
# check if errors already occured
sub errors($$){
    my $self = shift;
    my $msg = shift;

    die "Wrong Reference".ref($self)  unless ref($self) eq "BuildTask";

    return $self->{errors};
}


# ------------------------------------------------------------------
# Error output
sub error($$){
    my $self = shift;
    my $msg = shift;

    die "Wrong Reference".ref($self)  unless ref($self) eq "BuildTask";

    my $platform = $self->{platform};
    my $proj     = $self->{proj};

    $self->{errors} .= "\n" if $self->{errors};
    $self->{errors} .= $msg;

    $self->Log(1,"ERROR: $msg");

    my $msg1 = "($platform:$proj)";
    my ( @msg ) = split(/\n/,$msg);

    for my $m ( @msg ) {
	print STDERR "$COLOR{RED}!!!!! ERROR$msg1: $m$COLOR{NORMAL}\n";
    }
}


# ------------------------------------------------------------------
# Warning output
sub warning($$){
    my $self = shift;
    my $msg = shift;

    die "Wrong Reference".ref($self)  unless ref($self) eq "BuildTask";

    my $platform = $self->{platform};
    my $proj     = $self->{proj};

    $self->{warnings}.= $msg;
    $self->Log(1,"WARNING: $msg");

    my $msg1 = "($platform:$proj)";
    my ( @msg ) = split(/\n/,$msg);

    for my $m ( @msg ) {
	print STDERR "$COLOR{RED}!!!!! WARNING:$msg1: $m$COLOR{NORMAL}\n";
    }
}


# ------------------------------------------------------------------
# split a single platform sting into seperate variables
sub split_platform($){
    my $platform = shift; #     ubuntu-intrepid-64
    my ($distri,$version,$bits) = split('-',$platform);
    return($distri,$version,$bits);
    	       };

# ------------------------------------------------------------------
# return platform
sub platform($){
    my $self = shift;
    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";

    my $platform = $self->{platform};
    $platform || die "NO Platform specified";
    if ( grep { $_ eq $platform } @available_platforms ){
	return $platform;
    } elsif ( "independent" eq $platform ) {
	return $platform;
    } else {
	$self->error("Unknown Platform '$self->{platform}' used");
    }
}

# ------------------------------------------------------------------
# return project
sub proj($){
    my $self = shift;
    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";

    return $self->{proj} || die "Unknown Proj";
}

# ------------------------------------------------------------------
# subpath of the project directory
# Example: openstreetmap-applications/utils/osmosis
sub proj_sub_dir($) {
    my $self = shift;
    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";

    my $platform = $self->platform();
    my $proj     = $self->proj();

    my $proj_sub_dir=$proj2path{$proj};
    if ( ! $proj_sub_dir ) {
	die "Unknown Directory for Project '$proj'"
    };
    return  $proj_sub_dir;
	 }

# ------------------------------------------------------------------
# return the base directory for a specific build
# Example: /home/chroot/debian-squeeze-64/home/tweety/openstreetmap-applications/utils/osmosis
sub build_dir($){
    my $self = shift;
    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";

    my $platform = $self->platform();
    my $proj_sub_dir = $self->proj_sub_dir();
    
    my $build_dir = "$dir_chroot/$platform/home/$user/$proj_sub_dir/";
    return $build_dir;
}


# ------------------------------------------------------------------
# Directory where the svn Sourcetree is located for the project
# Example: /home/chroot/svn/openstreetmap-applications/utils/osmosis
sub svn_dir_full($){
    my $self = shift;
    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";

    my $proj_sub_dir = $self->proj_sub_dir();
    return ("$dir_svn/$proj_sub_dir");
}

# ------------------------------------------------------------------
# convert Project name to a svn base Directory
sub svn_dir_base($){
    my $self = shift;
    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";

    my $proj     = $self->proj();
    my $proj_sub_dir = $self->proj_sub_dir();

    my $repository_dir=$proj_sub_dir;
    $repository_dir=~ s,/.*,,; # First Directory-part only
    $self->debug(7,"svn_dir_base() --> Repository: $repository_dir");
    return $repository_dir;
}

# ------------------------------------------------------------------
# Execute a command with dchroot in a chroot environment
sub dchroot($$$){
    my $self = shift;
    my $dir       = shift; # Directory inside chroot
    my $command   = shift; # command to execute
    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";

    my $platform = $self->platform();
    my $proj     = $self->proj();

    # ERR: I: [debian-squeeze-64 chroot] Running command: "debuild clean"

    return $self->command("dchroot --chroot $platform --directory '/home/$user/$dir' '$command'");
};


# ------------------------------------------------------------------
# Execute a command
sub command($$){
    my $self = shift;
    my $cmd  = shift;
    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";

    my ($data,$data_out,$data_err)=('','','');

    $self->debug(5, "Command: $cmd");

    my ($infh,$outfh,$errfh);
    $errfh = gensym();
    my $pid;
    eval {
	$pid = open3($infh, $outfh, $errfh, $cmd);
    };
    if ( $@ ) {
	$self->error("Error running Command $cmd: $@");
	return;
    }

    my $sel = new IO::Select;
    $sel->add($outfh,$errfh);

    while(my @ready = $sel->can_read(1000)) {
	foreach my $fh (@ready) {
	    my $line;
	    my $len = sysread $fh, $line, 4096;
	    my $len1=length($line);
	    my $chomp_line=$line;
	    chomp($chomp_line);
	    if(not defined $len){
		$self->error("Error from child: $!");
		return;
	    } elsif ($len == 0){
		$sel->remove($fh);
		next;
	    } else { # we read data alright
		$self->debug(7,"command: $chomp_line");
		if ($fh == $outfh ) {
		    $data_out .= $line;
		    $data .= $line;
		} elsif ( $fh == $errfh ) {
		    $data_err .= $line;
		    $data .= $line;
		} else {
		    die "Shouldn't be here\n";
		}
		}
	    }
    }

    waitpid( $pid, 0);
    my $rc = $? >> 8;
    $self->debug(7,"Command: ");
    $self->debug(7,"Command: $cmd");
    $self->debug(7,"Command: rc:$rc");
    $self->debug(7,"Command: ^^^^^^^^^^^^^^^^");

    $self->Log(5,"Command: =====================");
    $self->Log(5,"Command: $cmd");
    $self->Log(7,"Command: $data");
    $self->Log(4,"Command: rc:$rc");
    $self->Log(7,"Command: ^^^^^^^^^^^^^^^^");

#    $self->debug(7,"Data: $data");
#    $self->debug(7,"Data_out: $data_out");
#    $self->debug(7,"Data_err: $data_err");

    return $rc,$data_out,$data_err,$data;
}

# ------------------------------------------------------------------
# Get svn revision number and write to svnrevision File
sub write_svn_revision($){
    my $self = shift;
    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";
    $self->section("write_svn_revision");

    my $proj     = $self->proj();
    
    $self->debug(5,"write_svn_revision: Proj: $proj");
 
    my $repository_dir=$self->svn_dir_full($proj);
    
    if ( ! -d "$repository_dir/debian" ) {
	$self->error("No Debian directory found at '$repository_dir/debian'\n");
	return -1;
    }
    my $svn_revision=`cd $repository_dir; svn info . | grep "Last Changed Rev" | sed 's/Last Changed Rev: //'`;
    chomp $svn_revision;
    $self->debug(4,"write_svn_revision: SVN Revision($proj): '$svn_revision'");
    write_file( "$repository_dir/debian/svnrevision", $svn_revision );


    # For josm and all it's plugins write a REVISION File
    if ( $proj =~ /josm/ ) {
	for my $dir ( glob( "$repository_dir/*/build.xml"), glob("$repository_dir/*/*/build.xml" ) ) {
	    $dir = dirname($dir);
	    my $build_xml = slurp( "$dir/build.xml" );
	    if ( $build_xml =~ m/exec .*output="REVISION".*executable="svn"/ ) {
		$self->debug(5,"svn REVISION at $dir");
		my $svn_revision=`cd $dir; export LANG=C; svn info --xml >REVISION`;
	    } else {
		$self->debug(5,"no svn REVISION at $dir requested");
	    }		
	}
    }
		   };

# ------------------------------------------------------------------
# Get the svn-revision from the local stored svnrevision File
# This reflects the revision checked out for ALL others
sub svn_revision($) {
    my $self = shift;
    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";

    my $proj     = $self->proj();
    
    my $proj_sub_dir = $self->proj_sub_dir();
    return '' unless -r "$dir_svn/$proj_sub_dir/debian/svnrevision";
    my $svn_revision = slurp( "$dir_svn/$proj_sub_dir/debian/svnrevision" );
    chomp $svn_revision;

    return $svn_revision;
	     };

# ------------------------------------------------------------------
# Get the svn-revision from the local stored svnrevision File
# in the specified platform directory
sub svn_revision_platform($) {
    my $self = shift;
    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";

    my $proj       = $self->proj();
    my $platform   = $self->platform();
    my $build_dir  = $self->build_dir();
    my $rev_file="$build_dir/debian/svnrevision";
    if ( ! -r  $rev_file ) {
	warn "Cannot read Revision File for '$platform' '$proj': '$rev_file'\n";
	return '';
    }

    my $svn_revision = slurp( $rev_file );
    $svn_revision =~ s/\s\n//g;
    
    return $svn_revision;
		      };


# ------------------------------------------------------------------
# Update the svn source tree
sub svn_update($){
    my $self = shift;
    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";
    return -1 if $self->errors();

    $self->section("svn_update");

    my $proj     = $self->proj();

    $self->debug(4,"");
    $self->debug(4,"-----------");
    $self->debug(3,"svn Update: Proj: $proj");

    my $proj_sub_dir=$self->svn_dir_base($proj);

    if ( $svn_update_done{$proj_sub_dir} ) {
	$self->debug(4,"Repository $proj_sub_dir for $proj already updated");
	return;
    };

    if ( ! -d "$dir_svn/$proj_sub_dir" ) {
	$self->debug(3,"Repository $proj_sub_dir for $proj not existing");
	return 0;
    }

    $self->debug(3,"svn up $dir_svn/$proj_sub_dir");
    my ($rc,$out,$err,$out_all) = $self->command("svn up --accept theirs-full $dir_svn/$proj_sub_dir");
    if ( $rc ) {
	$self->warning("Error '$rc' in 'svn up $dir_svn/$proj_sub_dir'");
	$self->warning("Error '$err'");
    }

    my @out = 
	grep { $_ !~ m/^(\s*$|Fetching external|External at revision|At revision|Checked out external at revision)/ } split(/\n/,$out);
    $self->debug(4,"OUT-U: ".join("\n",@out));
    $self->debug(3,"Counting ".scalar(@out)." Changes while doing svn up");
    if ( $err =~ m/run 'svn cleanup' to remove locks/ ) {
	$self->debug(3,"We need a svn cleanup");
	my ($rc,$out,$err,$out_all) = $self->command("svn cleanup $dir_svn/$proj_sub_dir");
	if ( $rc) {
	    $self->warning("Error '$rc' in 'svn cleanup $dir_svn/$proj_sub_dir'");
	    $self->warning("Error '$err'");
	}
    } 
#     elsif ( $out !~ m/At revision/ ) {
#	$self->error("No final Revision in Output Found\n");
#	return 0;
#    }	

    if ( $err ) {
	my $err_out=$err;
	$self->error("ERR: $err_out\n");
	return 0;
    }
    $svn_update_done{$proj_sub_dir}++;
	   };

# ------------------------------------------------------------------
# Checkout the svn source tree
sub svn_checkout($){
    my $self = shift;
    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";
    return -1 if $self->errors();

    $self->section("svn_checkout");

    my $proj     = $self->proj();

    $self->debug(4,"");
    $self->debug(4,"------------");
    $self->debug(3,"svn Checkout: Proj: $proj");

    my $proj_sub_dir=$self->svn_dir_base($proj);

    if ( $svn_update_done{$proj_sub_dir} ) {
	$self->debug(4,"Repository $proj_sub_dir for $proj already updated");
	return;
    };

    my $url=$svn_repository_url{$proj_sub_dir};

    $self->debug(3,"svn co $url $dir_svn/$proj_sub_dir");
    my ($rc,$out,$err,$out_all) = $self->command("svn co $url $dir_svn/$proj_sub_dir");
    my @out = 
	grep { $_ !~ m/^(\s*$|Fetching external|External at revision|At revision|Checked out external at revision)/ } split(/\n/,$out);
    $self->debug(4,"OUT-U: ".join("\n",@out));
    if ( $err ) {
	$self->warning("WARNING: $err");
    }
    $svn_update_done{$proj_sub_dir}++;
	     };

# ------------------------------------------------------------------
# Update the svn source tree
sub svn_changelog($){
    my $self = shift;
    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";
    return -1 if $self->errors();

    $self->section("svn_changelog");

    my $proj     = $self->proj();

    if ( $DEBUG>1 ) {
	print STDERR "\n";
	print STDERR "------------\n";
	print STDERR "svn Changelog update:\n";
	print STDERR "Proj: $proj\n";
    }
    my $proj_sub_dir = $self->proj_sub_dir();
    my $debname = $proj2debname{$proj};

    my $command="$dir_svn/openstreetmap-applications/utils/packaging/svn_log2debian_changelog.pl";
    $command .= " --project_name='$debname' ";
    
    if ( $proj =~ m/gpsdrive-[^\.\d]*([\.\d]+pre\d+)(-)?/ ) {
	$command .= " --prefix=2.10svn --release=$1";
    } elsif ( $proj =~ m/gpsdrive/ ) {
	$command .= " --prefix=2.10svn ";
    };
    if ( $DEBUG ) {
	$command .= " --debug ";
    };
    
    my ($rc,$out,$err,$out_all) = $self->command("cd $dir_svn/$proj_sub_dir; $command");
    if ( $rc) {
	$self->warning("Error '$rc' in '$command'");
	$self->warning("Error '$err'");
    }
	      };


# ------------------------------------------------------------------
# Update a single chroot the svn source tree
sub svn_copy($$){
    my $self = shift;
    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";
    return -1 if $self->errors();

    $self->section("svn_copy");

    my $platform = $self->platform();
    my $proj     = $self->proj();

    $self->debug( 4, "" );
    $self->debug( 4, "------------" );
    $self->debug( 3, "svn Copy($platform,$proj)" );
    my $proj_sub_dir = $self->proj_sub_dir();

    if ( $do_fast ) {
	if ( $self->svn_revision_platform() eq $self->svn_revision_platform() ){
#	    $self->debug(3,"svn copy already done");    
#	    return 0;
	}
    }

    $self->debug(4, "Proj sub dir: '$proj_sub_dir'");

    my $proj_svn_dir = "$dir_svn/$proj_sub_dir/";
    my $build_dir    = $self->build_dir();

    if ( ! -d "$proj_svn_dir" ) {
	$self->error("SVN Copy Direcoty $proj_svn_dir not found");
    }

    find(
	sub{
	    return if $File::Find::name =~ m,\.svn,;
	    return if $File::Find::name =~ m,\/.#,; # Backup Files from Emacs
	    return if -d $File::Find::name;
	    my $src=$File::Find::name;
	    my $dst=$File::Find::name;
	    $dst=~ s{^$proj_svn_dir}{$build_dir};
	    $self->debug(7,"--------------- missing $dst");
	    $self->debug(7,"SRC: $src");
	    $self->debug(7,"DST: $dst");
	    my $dst_dir=dirname($dst);
	    unless ( -d $dst_dir ) {
		mkpath($dst_dir) 
		|| $self->error("Cannot create '$dst_dir': $!");
	    };
	    copy($src,$dst)
		|| $self->error("!!!!!!!!!! ERROR: Cannot Copy $src->$dst: $!");
	    #print "File::Find::dir       $File::Find::dir\n";
	    #print "File                  $_              \n";
#	    print "File::Find::name      $File::Find::name \n";
    },  $proj_svn_dir);

    # ###############
    # XXXXXXXXXX TODO
    # Remove obsolete Files in the dst_dir 
    # ###############
};



# ------------------------------------------------------------------
# Update a single chroot with svn source tree and apply the patch for this platform
sub apply_patch($){
    my $self = shift;
    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";
    return -1 if $self->errors();

    $self->section("apply_patch");

    my $platform  = $self->platform();
    my $proj      = $self->proj();
    my $build_dir = $self->build_dir();
    my $proj_sub_dir = $self->proj_sub_dir();

    my $patch_file="$dir_svn/$proj_sub_dir/debian/$platform.patch";
    if ( -s  $patch_file) {
	# copy files from SVN-DIR
	for my $file ( glob("$dir_svn/$proj_sub_dir/debian/*") ){
	    next if -d $file;
	    copy($file,"$build_dir/debian/")
		|| $self->error("Cannot copy $file ---> $build_dir/debian/: $!");
	}

	# apply patch
	$self->debug(5,"apply_patch($patch_file)");
	my ($rc,$out,$err,$out_all) = $self->command("cd $build_dir/debian/; patch <$patch_file");
	if ( $out_all =~ /Hunk .* FAILED at / ) {
	    $self->error("Error in 'patch <$patch_file'\n".
			 "Error '$out_all'");
	    my (@files) = ($out_all =~ m/saving rejects to file (.+\.rej)/g );
	    for my $file ( @files ){
		my $txt = slurp("$build_dir/debian/$file");
		$self->error("\n-------------------------------------------------------------------------\n".
			     "Patch Error: '$file'\n".
			     "-------------------------------------------------------------------------\n".
			     $txt);
		    }
	    
	    return -1;
	}
	if ( $rc ) {
	    $self->error("Error '$rc' in 'patch <$patch_file'");
	    $self->error("Error '$out_all'");
	    return -1;
	};
    }

};


# ------------------------------------------------------------------
# Update the svn source tree to be able to work without a svn-binary
sub apply_pre_patch($){
    my $self = shift;
    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";
    return -1 if $self->errors();

    $self->section("apply_pre_patch");

    my $platform = $self->platform();
    my $proj     = $self->proj();
    my $svn_dir_full = $self->svn_dir_full();

    # For josm and all it's plugins replace the svn-REVISION-Command with true-Command File
    if ( $proj =~ /josm/ ) {
	for my $dir ( glob( "$svn_dir_full/*/build.xml"), glob("$svn_dir_full/*/*/build.xml" ) ) {
	    $dir = dirname($dir);
	    my $build_xml = slurp( "$dir/build.xml" );
	    if ( $build_xml =~ m/exec .*output="REVISION".*executable="svn"/ ) {
		$self->debug(4,"replace svn command wit TRUE at $dir");
	    } else {
		$self->debug(4,"no svn REVISION at $dir requested");
	    }		
	    $build_xml =~ s/output="REVISION"/output="REVISION.null"/g;
	    $build_xml =~ s/executable="svn"/executable="true"/g;		
	    $build_xml =~ s,<delete file="REVISION"/>,,g;
	    write_file("$dir/build.xml",$build_xml);
	}
    }
    
    # <exec append="false" output="REVISION" executable="svn" failifexecutionfails="false">

};


# ------------------------------------------------------------------
# Do one build for platform and Project
sub debuild($) {
    my $self = shift;
    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";
    if ( $self->errors() ) {
	$self->last_result("fail");
	return -1
    }

    $self->section("debuild");

    my $platform = $self->platform();
    my $proj     = $self->proj();

    $self->debug(4,"");
    $self->debug(4,"------------");
    $self->debug(3,"Debuild($platform,$proj)");
    $self->debug(4,"Platform: $platform");
    $self->debug(4,"Proj: $proj");
    my $proj_sub_dir = $self->proj_sub_dir();

    if ( ! $do_fast ) {
	my ($rc,$out,$err,$out_all) = $self->dchroot($proj_sub_dir ,"debuild clean");
	if ( $err ) {
	    print "ERR: $err\n";
	}
    }

    my ($rc,$out,$err,$out_all) = $self->dchroot($proj_sub_dir ,"debuild binary");
    if ( $err ) {
	print "ERR: $err\n";
    }
    if ( $err =~ m/error: / ) {
	$self->error("Error in debuild Output:\n".$err);
    }

    # --- Check on missing Build dependencies
    my @dependencies= grep { $_ =~ m/^dpkg-checkbuilddeps:/ } split(/\n/,$err);
    @dependencies = grep { s/.*Unmet build dependencies: //g; }  @dependencies;
    my $dep_file="$dir_chroot/$platform/home/$user/install-debian-dependencies-$proj.sh";
    if (  @dependencies ) {
	$self->error("!!!!!!!!!!!!!!!!!!!!!! Cannot Build Debian Package because of Missing Dependencies: \n".
		     "\t".join("\n\t", @dependencies)."\n".
		     "Written install suggestion to : '$dep_file'\n"	
	    );
	write_file($dep_file,"chroot $dir_chroot/$platform aptitude --assume-yes install ".
		   join("\n", @dependencies)."\n");
	$self->last_result("fail-dependency");
	return -1;
    } else {
	unlink($dep_file);
    }
    if ( $rc) {
	$self->error("Error '$rc' in 'debuild binary'");
	$self->warning("Error '$out_all'");
    }



    # ------ Collect Resulting *.deb names
    my $result_dir=dirname("$dir_chroot/$platform/home/$user/$proj_sub_dir/");
    my $svn_revision = $self->svn_revision_platform();
    my $revision_string= $svn_revision;
    if ( $proj =~ m/gpsdrive-[^\.\d]*([\.\d]+pre\d+)(-)?/ ) {
	$revision_string= "$1";
    }
    my @results= grep { $_ =~ m/\.deb$/ } glob("$result_dir/*$revision_string*.deb");
    if ( $proj =~ m/gpsdrive-(.*pre.*)/ ) {
    } else {
	@results= grep { $_ !~ m/2.10pre/ } @results;
    }

    my $result_count=scalar(@results);
    $self->{'results'}->{'deb-count'}=$result_count;
    $self->{'results'}->{'packages'}= \@results;
    my $result_expected = scalar(@{$package_names{$proj}});
    if ( ! $result_count ) {
	my $all_deb_in_results = join(",",glob("$result_dir/*$revision_string*.deb"));
	$self->error( "!!!!!!!! WARN: NO resulting Packages for Proj '$proj' on Platform $platform.\n".
		      "Expecting $result_expected packages for svn-revision $revision_string\n".
		      "see results in '$result_dir'\n".
		      "Other Debian Files:  $all_deb_in_results\n");
	$self->last_result("fail");
    } elsif ( $result_expected !=  $result_count ) {
	$self->error( "!!!!!!!! WARN: Number of resulting Packages for Proj '$proj' on Platform $platform is Wrong.\n".
		      "Expecting $result_expected packages for svn-revision $revision_string, got: $result_count Packages\n".
		      "see results in '$result_dir'");
	$self->last_result("fail");
    } else {
	# Check for missing result-packages
	my @names=@{$package_names{$proj}};
	my $missing=0;
	for my $name ( @{$package_names{$proj}} ) {
	    my $check_name="${name}_(|2\.10svn)${revision_string}_(i386|amd64|all)\.deb";
	    if ( ! grep { $_ =~ m/$check_name$/ } @results ) {
		$self->error( "!!!!!!!! ERROR: Missing Result Package $name\n");
		$missing++;
	    };
	}

	my $wrong_name=0;
	for my $name ( @results ) {
	    my $short_name=basename($name);
	    if ( ! grep { $short_name =~ m/${_}_(|2\.10svn)${revision_string}_(i386|amd64|all)\.deb$/ }  @{$package_names{$proj}} ) {
		$self->error( "!!!!!!!! ERROR: Unknown Result Package '$name'\n");
		$wrong_name++;
	    };
	}
	if ( $missing ) {
	    $self->last_result("missing");
	} elsif ( $wrong_name ) {
	    $self->last_result("wrong_name");
	} else {
	    $self->last_result("success");
	} 
    }
    $self->debug(3,"Resulting Packages($result_count):");
    $self->debug(4,"\n\t".join("\n\t",@results));




    # Move Result to one Result Place
    my ($distri,$version,$bits)=split_platform($platform);
    my $dst_dir="$package_result_dir/$distri/pool/$version";
    if ( ! -d $dst_dir ) {
	mkpath($dst_dir)
	    or $self->error( "!!!!!!!! WARNING: Konnte Pfad $dst_dir nicht erzeugen: $!");
    }
    for my $result ( @results) {
	my $fn=basename($result);
	rename($result,"$dst_dir/$fn")
	    || $self->error( "!!!!!!!! WARNING Cannot move result '$result' to '$dst_dir/$fn': $!");
    }
}


# ------------------------------------------------------------------
sub show_results(){
    printf "%-15s %6s ","Project","svn";
    for my $platform ( @platforms ) {
	my $print_platform=$platform;
	$print_platform=~ s/(debian|ubuntu)-//;
	$print_platform=~ s/-.*//g;
	printf "%-7s ",$print_platform ;
    }
    print  "\n";
    printf "%-15s %6s ","","";
    for my $platform ( @platforms ) {
	my $print_platform=$platform;
	$print_platform=~ s/(debian|ubuntu)-//;
	$print_platform=~ s/.*-//g;
	printf "%-7s ",$print_platform ;
    }
    print  "\n";

    for my $proj ( @projs ) {
	my $proj_rev=svn_revision( bless({proj=>$proj,platform=>'independent'},'BuildTask') );
	my $p_proj=$proj;
	$p_proj=~ s/osm-mapnik-world-boundaries/osm-world-bound/;
	$p_proj=~ s/gpsdrive-data-maps/gpsdrive-maps/;
	printf "%-15s %6d ",$p_proj,$proj_rev;
	for my $platform ( @platforms ) {
#	    print "$platform ";
	    my $task = $RESULTS->{$platform}->{$proj};
	    if ( ! defined ( $task ) )  {
		$task = BuildTask->new( proj => $proj, platform => $platform );
	    };
	    my $svn_revision_platform = $task->svn_revision_platform()||'';
	    my $svn_revision = $task->svn_revision()||'';
	    my $last_result=$task->last_result();
	    
	    $task->{svn_base_revision}= $svn_revision;
	    if ( $svn_revision eq $svn_revision_platform) {
		$task->{svn_up_to_date}=1;
		$task->{color_rev}=$COLOR{GREEN};
	    } else {
		$task->{svn_up_to_date}=0;
		$task->{color_rev}=$COLOR{BLUE};
	    }    
	    if ( ! $svn_revision_platform ) {
		$task->{color_res}="+$COLOR{GREEN}";
	    } elsif ( $last_result eq "success: $svn_revision_platform" ) {
		$task->{color_res}="+$COLOR{GREEN}";
	    } else {
		$task->{color_res}="-$COLOR{RED}";
		$task->{color_rev}=$COLOR{RED};
	    };

	    my $color_rev = $task->{color_rev};
	    my $color_res = $task->{color_res};
	    my ( $res,$rev)  = split(/:\s*/,$task->last_result());
	    $rev ||='';
	    my $rev_last_good  = $task->last_good_result();
	    my $print_platform=$platform;
	    $print_platform=~ s/(debian-|ubuntu-)//;

	    print "$color_res"; #. $print_platform."$COLOR{NORMAL} " ;
	    printf "$color_rev%-6s$COLOR{NORMAL} ", $rev;
	    if (  $rev_last_good && $rev ne $rev_last_good ) {
		print "$COLOR{GREEN}($rev_last_good)$COLOR{NORMAL}";
	    }
	}
	print "\n";
    }
}
# ------------------------------------------------------------------
sub write_html_results(){
    my $html_report="$dir_log/results.shtml";
    my $fh = IO::File->new(">$html_report");
#    print $fh "<html>\n";
    print $fh "<!--#include virtual=\"/header.shtml\" -->\n";
    print $fh "<div id=\"content\">\n";
    print $fh "<div>\n";
    print $fh "<H1>Results from the Build-Cluster</H1>\n";
    print $fh localtime(time())."\n";
    print $fh "<br/>\n";
    print $fh "<br/>\n";
    print $fh "<table border=1>\n";
    
    print  $fh "<tr><th>Project</th><th>svn</th>";
    for my $platform ( @platforms ) {
	my $print_platform=$platform;
	$print_platform=~ s/(debian|ubuntu)-/$1\<br\/\>/;
	$print_platform=~ s/-/ /g;
	
	print $fh "<th>$print_platform</th>" ;
	
    }
    print  $fh "</tr>\n";

    for my $proj ( @projs ) {
	print  $fh "<tr>\n";
	my $rel_proj_log_dir="$proj";
	printf $fh "	<td><a href=\"$rel_proj_log_dir\">%s</a></td>\n",$proj;

	my $proj_rev=svn_revision( bless({proj=>$proj,platform=>'independent'},'BuildTask') );
	print $fh "     <td>$proj_rev</td>\n";

	for my $platform ( @platforms ) {
#	    print $fh "$platform ";
	    my $task = $RESULTS->{$platform}->{$proj};
	    my $rel_log_dir="$proj/$platform";
	    if ( ! defined ( $task ) )  {
		$task = BuildTask->new( proj => $proj, platform => $platform );
	    };
	    my $svn_revision_platform = $task->svn_revision_platform()||'';
	    my $svn_revision = $task->svn_revision()||'';
	    my $last_result=$task->last_result();
	    my $color_rev;
	    my $color_res;
	    if ( $svn_revision eq $svn_revision_platform) {
		$color_rev="GREEN";
	    } else {
		$color_rev="BLUE";
	    }    
	    if ( ! $svn_revision_platform ) {
		$color_res="GREEN";
	    } elsif ( $last_result eq "success: $svn_revision_platform" ) {
		$color_res="GREEN";
	    } else {
		$color_res="RED";
		$color_rev="RED";
	    };

	    my ( $res,$rev)  = split(/:\s*/,$task->last_result());
	    $rev ||='';
	    my $rev_last_good  = $task->last_good_result();
	    my $print_platform=$platform;
	    $print_platform=~ s/(debian-|ubuntu-)//;

	    print $fh "     <td> <A href=\"$rel_log_dir\">";
	    printf $fh "	<FONT  color=\"$color_rev\">%-6s </font>\t", $rev;
	    if (  $rev_last_good
		  && ( $rev ne $rev_last_good ) 
		) {
		print $fh "\n		<FONT color=\"GREEN\">($rev_last_good)</font>\n";
	    }
	    print $fh " </a> </td>\n\n";
	}
	print $fh "\n";
	print  $fh "</tr>\n";
    }

    print $fh "</table>\n";
    print $fh "<br/>\n";
    print $fh "Colors:\n";
    print $fh "<ul>\n";
    print $fh "<li><FONT color=\"green\">green</font>: Build successfull</li>\n";
    print $fh "<li><FONT color=\"blue\">blue</font>: Build is old but successfull</li>\n";
    print $fh "<li><FONT color=\"red\">red</font>: Build failed</li>\n";
    print $fh "</ul>\n";
    print $fh "</html>\n";
    $fh->close();

    # Create Index for each proj/platform
    for my $proj ( @projs ) {
	for my $platform ( @platforms ) {
	    my $html_index_dir="$dir_log/$proj/$platform";
	    if ( ! -d $html_index_dir ) {
		mkpath($html_index_dir)
		    or warn "WARNING: Konnte Pfad $html_index_dir nicht erzeugen: $!\n";
	    }
	    my $html_index="$html_index_dir/index.shtml";
	    my $fh = IO::File->new(">$html_index");
	    print $fh "<!--#include virtual=\"/header.shtml\" -->\n";
	    print $fh "<div>\n";
	    print $fh "<H1><a href=\"../../results.shtml\">Results from the Build-Cluster</a></H1>\n";
	    print $fh "<H2>Project: $proj</H2>\n";
	    print $fh "<H2>Platform: $platform</H2>\n";

	    print $fh "<table><tr>\n";
	    print $fh "<td valign=\"top\">\n";

	    print $fh "Log Files:";
	    print $fh "<ul>";
	    debug({proj=>$proj,platform=>$platform},5,"Files:");
	    for my $file ( glob("$html_index_dir/*.shtml") ) {
		my $file_name = basename($file);
		debug({proj=>$proj,platform=>$platform},5,"              $file");
		next if $file_name eq "index.shtml";

		my @lines = read_file( $file ) ;
		my $count_warn=(grep {$_ =~ m/warn/i } @lines);
		my $count_error=(grep {$_ =~ m/error/i } @lines);
		my ( $disp_name ) = ( $file_name =~ m/(.*)\.shtml/ );
		print $fh "<li><A href=\"$file_name\">$disp_name</a>";
		print $fh "<br/>&nbsp;&nbsp;error: $count_error" if $count_error;
		print $fh "<br/>&nbsp;&nbsp;warn: $count_warn" if $count_warn;
		print $fh "</li>\n";
	    }
	    print $fh "</ul>";
	    print $fh "</td>\n";


	    # -----------------------------------------------------------------------
	    # list of Debian Packages
	    my $task=BuildTask->new( proj     => $proj ,
				     platform => $platform );    
	    my $rev_last_good  = $task->last_good_result();
	    my $res_last  = $task->last_result();
	    #print "rev_last_good($proj ,$platform) '$rev_last_good'\n";

	    print $fh "<td valign=\"top\">\n";
	    print $fh "Status:<br/><br/>\n";
	    print $fh "Last Good Revision:<br/>&nbsp;&nbsp; $rev_last_good<br/><br/>\n";
	    print $fh "Last Result:<br/>&nbsp;&nbsp; $res_last<br/>\n";
	    print $fh "</td>\n";


	    #$package_result_dir
	    my ($distri,$version,$bits)=split_platform($platform);

	    my $platform_glob='{i386,amd64,all}';
	    $platform_glob='{i386,all}' if $bits eq "32";
	    $platform_glob='{amd64,all}' if $bits eq "64";

	    print $fh "<td valign=\"top\">\n";
	    print $fh "<A href=\"/$distri/pool/$version/\">Packages ($distri $version)</a>:\n";
	    if ( ${rev_last_good} ) {

		print $fh "\t<ul>\n";
		for my $name ( @{$package_names{$proj}} ) {
		    print $fh "\t<li>$name:\n";	

		    $task->debug(7,"glob($package_result_upload_dir/$distri/pool/$version/${name}_${rev_last_good}_${platform_glob}.deb");
		    my @files = glob("$package_result_upload_dir/$distri/pool/$version/${name}_*${rev_last_good}_${platform_glob}.deb");
		    @files = grep { -s $_ } @files;
		    if ( @files ) {
			print $fh "\t<ul>\n";
			for my $file ( @files ) {
			    my $file_name = basename($file);
			    my $link = $file;
			    $link =~ s/^$package_result_upload_dir//;
			    #print "\t$link\n";	
			    print $fh "\t<li><A href=\"$link\"> $file_name</a></li>\n";	
			}
			print $fh "\t</ul>\n";
		    } else {
			print $fh "<FONT color=\"RED\">No Files found</font>\n";
		    }
		    print $fh "</li>\n";
		}
		print $fh "\t<ul>\n";
	    }
	    print $fh "</td>\n";
	    print $fh "</tr></table>\n";

	    print $fh localtime(time())."\n";

	    print $fh "<br/><br/><a href=\"../../results.shtml\">Back to Overview</a>\n";
	    print $fh "</div>\n";    
	    print $fh "<!--#include virtual=\"/footer.shtml\" -->\n";
	    $fh->close();
	}
    }
}

# ------------------------------------------------------------------
sub usage($){
    my $opt_manual = shift;

    print STDERR <<EOUSAGE;
usage: $0 [Options]

    build_cluster is a tool to compile and build packages for some software-projects.

Available Projects:
    @available_proj

These Projects will be compiled for a set of platforms

Available Platforms:
    @available_platforms

The build-cluster-tool expects a set of chroot environments to work in the
directory
$dir_chroot

Logfiles are written to:
       $dir_log

The svn Checkout is done to:
       $dir_svn

Results are collected in the DIrectory
       $package_result_dir

EOUSAGE

die "\n";

}

# ------------------------------------------------------------------
# Main
# ------------------------------------------------------------------
@platforms= @default_platforms
    unless @platforms;

@projs= @default_projs 
    unless @projs;


if ( $do_write_html_results_only ) {
    write_html_results();
    exit 0;
}

if ( $DEBUG >= 1 ) {
    print "----------------------------------------\n";
    print "Platforms: " . join(" ",@platforms)."\n";
    print "Projects:  " . join(" ",@projs)."\n";
    print "\t--".($do_svn_up    ?'':'no-')."svn-up\n";
    print "\t--".($do_svn_co    ?'':'no-')."svn-co\n";
    print "\t--".($do_svn_changelog?'':'no-')."svn-changelog\n";
    print "\t--".($do_svn_cp    ?'':'no-')."svn-cp\n";
    print "\t--".($do_fast      ?'':'no-')."fast\n";
    print "\t--".($FORCE        ?'':'no-')."force\n";
    print "\t--".($DEBUG        ?'':'no-')."debug = $DEBUG\n";
    print "----------------------------------------\n";
}

show_results() 
    if ( $DEBUG > 1 || $VERBOSE > 1 ) && $do_show_results;

# svn Update
for my $proj ( @projs ) {
    my $task=BuildTask->new( proj     => $proj ,
			     platform => 'independent' );

    $task->svn_update( )	if $do_svn_up;
    $task->svn_checkout(  )	if $do_svn_co;
    $task->write_svn_revision()	if $do_svn_up || $do_svn_co;
    $task->apply_pre_patch()	if $do_svn_up || $do_svn_co || $do_svn_cp;;

    # Update Changelogs
    $task->svn_changelog()	if $do_svn_changelog;
}


if ( $DEBUG >= 3 ) {
    print "----------------------------------------\n";
    print "Platforms: " . join(" ",@platforms)."\n";
    print "Projects:  " . join(" ",@projs)."\n";
    print "----------------------------------------\n";
}

for my $platform ( @platforms ) {
    #print STDERR "\n";
    print STDERR "$COLOR{BLUE}------------------------------------------------------------ Platform: $platform$COLOR{NORMAL}\n"
	if $DEBUG > 3;

    for my $proj ( @projs ) {

	my $task = BuildTask->new( 
	    proj     => $proj, 
	    platform => $platform ,
	    );
	if ( $DEBUG>2 || $VERBOSE>2 ) {
	    print STDERR 	"$COLOR{MAGENTA}------------------------------------------------  Platform: $platform$COLOR{NORMAL}	Project: $proj$COLOR{NORMAL}\n";
	}

	$task->Clear_Log();

	if ( $do_fast ) {
	    my $svn_revision = $task->svn_revision_platform();
	    my $last_result=$task->last_result();
	    if ( $svn_revision && $last_result eq "success: $svn_revision" ) {
		if ( $VERBOSE >1 || $DEBUG >2 ) {
		    print STDERR "$COLOR{GREEN}---- Project: $proj ($platform) '$last_result' up-to-date --> skipping$COLOR{NORMAL}\n";
		}
		next;
	    } else {
		$task->debug(3, "$COLOR{BLUE}------ Project: $proj build not up to date: '$last_result'$COLOR{NORMAL}");
	    };
	}

	
	$task->svn_copy()	if $do_svn_cp;
	$task->apply_patch();
	$task->debuild();
	$RESULTS->{$platform}->{$proj}=$task;
	$task->section("summary");
	$task->Log( 1,"\n\nRESULTS:\n".
		    Dumper(\$task) );

	my $last_result=$task->last_result();
	if ( $VERBOSE || $DEBUG ) {
	    print STDERR 	"$COLOR{MAGENTA}---  Platform: $platform$COLOR{NORMAL}	Project: $proj	$last_result $COLOR{NORMAL}\n";
	}
	
    }
};

if ( $DEBUG >= 3) {
    print "RESULTS:\n";
    print Dumper(\$RESULTS);
}

show_results() 
    if $do_show_results;

write_html_results()
    if $do_show_results;


__END__

=pod

=head1 Options

=over

=item  C<--debug>

Add some Debugging Output

=item --verbose

Be more verbose

=item --fast

Skip some not really needed tasks. (debuild clean, ...)
And check for already successfully build packages.

=item --no-color

switch of coloring output. This is needed for example if run in a cronjob.

=item --svn-up / --no-svn-up

Switch on/off doing subversion update to the projects.

=item --svn / --no-svn

Switch on/off doing all subversion actions.

=item --svn-co / --no-svn-co

Switch on/off doing subversion co on the projects.

=item --svn-changelog / --no-svn-changelog

Switch on/off the creation of an automated changelog from subversion.

=item --svn-cp / --no-svn-cp

Switch on/off doing copying from the subversion folder to the chroot subdirectories.

=item --dir-chroot

Specify the directory where all chroots are located.

=item --dir-svn

Specify the directory where the svn checkou copy is located.

=item --dir-package-results

Specify the directory where the resulting packages are located.

=item --user

Specify the username to use for the package build directories.

=item --platforms=

Specify the platforms to build for. a * can be used to specify multiple 
platforms with a wildcard.

=item --projects=

Specify the projects to build for. a * can be used to specify multiple 
projects with a wildcard.

=item --show-results

Show a summary of all results. This also looks for cached results.

=item --write-html-results-only

Write Html Pages for the Results and exit.

=back




=head1 TODO:

  - Check for another build-cluster.pl already running

  - Add timeout to command execution. This might prevent hanging 
    javacompiler from blocking the rest of the build-cluster.pl

  - writing Logfiles

  - Help/manpage

  - Error Code checking

  - while doing "svn up" "svn co": Check if another 
    svn command is running on the same directory

