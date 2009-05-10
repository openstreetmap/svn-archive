#!/usr/bin/perl
# Build_Cluster is a system to build various Debian Based Packages
# we expect a chroot environment to already be setup in order to 
# then be able to do the debuild commands inside these.

# TODO:
#  - writing Logfiles
#  - Debug/Log -levels
#  - Error Code checking
#  - getopt integration
#  - Help/manpage
#  - Check for another build_cluster.pl already running

package BuildTask;

use strict;
use warnings;

use Data::Dumper;
use File::Basename;
use File::Copy;
use File::Find;
use File::Path;
use File::Slurp qw( slurp write_file) ;
use Getopt::Long;
use Getopt::Std;
use IO::Select;
use IPC::Open3;
use Symbol;


my $dir_chroot = "/home/chroot";
my $dir_log = "/home/chroot/log";
my $dir_svn = "$dir_chroot/svn";
my $package_results = "$dir_chroot/results";
my $user = "tweety";
my $DEBUG   = 3;
my $VERBOSE = 1;
my $MANUAL=0;
my $HELP=0;
my $FORCE=0;

my $do_svn_up=1;
my $do_svn_co=1;
my $do_svn_changelog = 1;
my $do_svn_cp= 1;

my $do_fast= 1; # Skip Stuff like debuild clean, ...

delete $ENV{http_proxy};
delete $ENV{HTTP_PROXY};
$ENV{LANG}="C";
$ENV{DEB_BUILD_OPTIONS}="parallel=4";


# define Colors
my $ESC=`echo -e "\033"`;
my $RED="${ESC}[91m";
my $GREEN="${ESC}[92m";
my $YELLOW="${ESC}[93m";
my $BLUE="${ESC}[94m";
my $MAGENTA="${ESC}[95m";
my $CYAN="${ESC}[96m";
my $WHITE="${ESC}[97m";
my $BG_RED="${ESC}[41m";
my $BG_GREEN="${ESC}[42m";
my $BG_YELLOW="${ESC}[43m";
my $BG_BLUE="${ESC}[44m";
my $BG_MAGENTA="${ESC}[45m";
my $BG_CYAN="${ESC}[46m";
my $BG_WHITE="${ESC}[47m";
my $BRIGHT="${ESC}[01m";
my $UNDERLINE="${ESC}[04m";
my $BLINK="${ESC}[05m";
my $REVERSE="${ESC}[07m";
my $NORMAL="${ESC}[0m";


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
    'opencarbox' 	=> 'opencarbox',
    'osm2pgsql' 	=> 'openstreetmap-applications/utils/export/osm2pgsql',
    'merkaartor' 	=> 'openstreetmap-applications/editors/merkaartor',
    'josm' 		=> 'openstreetmap-applications/editors/josm',
    'osm-utils'	=> 'openstreetmap-applications/utils',
    'osm-mapnik-world-boundaries' 	=> 'openstreetmap-applications/rendering/mapnik/openstreetmap-mapnik-world-boundaries',
    'osm-mapnik-data' 	=> 'openstreetmap-applications/rendering/mapnik/openstreetmap-mapnik-data',
    'map-icons' 	=> 'openstreetmap-applications/share/map-icons',
    'osmosis' 		=> 'openstreetmap-applications/utils/osmosis/trunk',
    'gosmore'	 	=> 'openstreetmap-applications/rendering/gosmore',

    'merkaartor-0.12' 	=> 'openstreetmap-applications/editors/merkaartor-branches/merkaartor-0.12-fixes',
    'merkaartor-0.11' 	=> 'openstreetmap-applications/editors/merkaartor-branches/merkaartor-0.11-fixes',
    'merkaartor-0.13' 	=> 'openstreetmap-applications/editors/merkaartor-branches/merkaartor-0.13-fixes',
    'osm-editor-qt3' 	=> 'openstreetmap-applications/editors/openstreetmap-editor/qt3',
    'osm-editor' 	=> 'openstreetmap-applications/editors/openstreetmap-editor',
    'osm-editor-qt4' 	=> 'openstreetmap-applications/editors/openstreetmap-editor/qt4',
    );

my %proj2debname=(
    'gpsdrive-maemo' 	=> 'gpsdrive',
    'gpsdrive-data-maps'=> 'gpsdrive-data-maps',
    'gpsdrive'	 	=> 'gpsdrive',
    'opencarbox' 	=> 'opencarbox',
    'osm2pgsql' 	=> 'osm2pgsql',
    'merkaartor' 	=> 'merkaartor',
    'josm' 		=> 'openstreetmap-josm',
    'osm-utils'		=> 'openstreetmap-utils',
    'osm-world-boundaries' 	=> 'openstreetmap-mapnik-world-boundaries',
    'osm-mapnik-data' 	=> 'openstreetmap-mapnik-data',
    'map-icons' 	=> 'openstreetmap-map-icons',
    'osmosis' 		=> 'osmosis',
    'gosmore'	 	=> 'openstreetmap-gosmore',

    'merkaartor-0.12' 	=> 'merkaartor-0.12-fixes',
    'merkaartor-0.11' 	=> 'merkaartor-0.11-fixes',
    'merkaartor-0.13' 	=> 'merkaartor-0.13-fixes',
    'osm-editor-qt3' 	=> 'openstreetmap-editor',
    'osm-editor' 	=> 'openstreetmap-editor',
    'osm-editor-qt4' 	=> 'openstreetmap-editor',
    );
my %num_packages=(
    'gpsdrive-maemo' 	=> 1,
    'gpsdrive-data-maps'=> 1,
    'gpsdrive'	 	=> 3,
    'opencarbox' 	=> 1,
    'osm2pgsql' 	=> 1,
    'merkaartor' 	=> 1,
    'josm' 		=> 1,
    'osm-utils'		=> 5,
    'osm-world-boundaries' 	=> 1,
    'osm-mapnik-data' 	=> 1,
    'map-icons' 	=> 14,
    'osmosis' 		=> 1,
    'gosmore'	 	=> 1,
    'merkaartor-0.12' 	=> 1,
    'merkaartor-0.11' 	=> 1,
    'merkaartor-0.13' 	=> 1,
    'osm-editor-qt3' 	=> 1,
    'osm-editor' 	=> 1,
    'osm-editor-qt4' 	=> 1,
    );

my %svn_repository_url=(
    'openstreetmap-applications' => 'http://svn.openstreetmap.org/applications',
    'gpsdrive'                   => 'https://gpsdrive.svn.sourceforge.net/svnroot/gpsdrive/trunk',
    'opencarbox'                 => 'https://opencarbox.svn.sourceforge.net/svnroot/opencarbox/OpenCarbox/trunk',
    );

my %svn_update_done;

my @available_proj=keys %num_packages;

my @projs;
#@projs= keys %proj2path;
my @default_projs=qw( gpsdrive-data-maps gpsdrive map-icons osm-utils);
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
    "package-results" => \$package_results,
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
				  @projs= keys %proj2path;
			      } elsif ( $b =~ m/\*/ ) {
				  $b =~ s,\*,\.\*,g;
				  @projs= grep { $_ =~ m{$b} } keys %proj2path;
			      } else {
				  @projs = split(',',$b);
			      }
},
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
#    print Dumper(\$self);
}

# ------------------------------------------------------------------
# Debugging output
sub debug($$$){
    my $self = shift;
    my $level = shift;
    my $msg = shift;

    die "Wrong Reference".ref($self)  unless ref($self) eq "BuildTask";

    my $platform = $self->{platform} || die "Unknown Platform";
    my $proj     = $self->proj();

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
# Log a msg
sub Log($$$){
    my $self = shift;
    my $level    = shift;
    my $msg      = shift;

    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";

    my $platform = $self->{platform} || die "Unknown Platform";
    my $proj     = $self->proj();
    my $section  = $self->{section}  || 'all';

    if ( ! -d $dir_log ) {
	die "Cannot Log, Directory '$dir_log' does not exist\n";
    }
    my $dst_dir="$dir_log/$platform-$proj";
    if ( ! -d $dst_dir ) {
	mkpath($dst_dir)
	    or warn "WARNING: Konnte Pfad $dst_dir nicht erzeugen: $!\n";
    }

    write_file( "$dst_dir/$section.log", $msg );

}

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

    my $platform = $self->{platform} || die "Unknown Platform";
    my $proj     = $self->proj();

    $self->{errors}.= $msg;

    my $msg1 = "($platform:$proj)";
    my ( @msg ) = split(/\n/,$msg);

    for my $m ( @msg ) {
	print STDERR "${RED}!!!!! ERROR$msg1: $m${NORMAL}\n";
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

    return $self->{platform} || die "Unknown Platform";
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
    eval{
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
    $self->debug(7,"Command: ");
    $self->debug(7,"Command: $cmd");
    $self->Log(7,"Command: ",$cmd);
    $self->Log(7,"Command: ",$data);
    $self->Log(7,"Command: ^^^^^^^^^^^^^^^^");
#    $self->debug(7,"Data: $data");
#    $self->debug(7,"Data_out: $data_out");
#    $self->debug(7,"Data_err: $data_err");
    return $data_out,$data_err;
}

# ------------------------------------------------------------------
# Get svn revision number and write to svnrevision File
sub write_svn_revision($){
    my $self = shift;
    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";

    my $proj     = $self->proj();
    
    $self->debug(5,"write_svn_revision: Proj: $proj");
 
    my $repository_dir=$self->svn_dir_full($proj);
    
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
		$self->debug(4,"svn REVISION at $dir");
		my $svn_revision=`cd $dir; export LANG=C; svn info --xml >REVISION`;
	    } else {
		$self->debug(4,"no svn REVISION at $dir requested");
	    }		
	}
    }
		   };

# ------------------------------------------------------------------
# Get the svn-revision from the local stored svnrevision File
sub svn_revision($) {
    my $self = shift;
    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";

    my $proj     = $self->proj();
    
    my $proj_sub_dir = $self->proj_sub_dir();
    my $svn_revision = slurp( "$dir_svn/$proj_sub_dir/debian/svnrevision" );
    chomp $svn_revision;

    return $svn_revision;
	     };

# ------------------------------------------------------------------
# Get the svn-revision from the local stored svnrevision File
# in the platform directory
sub svn_revision_platform($) {
    my $self = shift;
    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";

    my $proj     = $self->proj();
    my $build_dir    = $self->build_dir();
    
    my $proj_sub_dir = $self->proj_sub_dir();
    my $svn_revision = slurp( "$build_dir/debian/svnrevision" );
    chomp $svn_revision;

    return $svn_revision;
	     };


# ------------------------------------------------------------------
# Update the svn source tree
sub svn_update($){
    my $self = shift;
    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";
    return -1 if $self->errors();

    my $proj     = $self->proj();

    $self->debug(4,"");
    $self->debug(4,"-----------");
    $self->debug(3,"svn Update: Proj: $proj");

    my $proj_sub_dir=$self->svn_dir_base($proj);

    if ( $svn_update_done{$proj_sub_dir} ) {
	$self->debug(3,"Repository $proj_sub_dir for $proj already updated");
	return;
    };

    $self->debug(3,"svn up $dir_svn/$proj_sub_dir");
    my ($out,$err) = $self->command("svn up $dir_svn/$proj_sub_dir");
    my @out = 
	grep { $_ !~ m/^(\s*$|Fetching external|External at revision|At revision|Checked out external at revision)/ } split(/\n/,$out);
    $self->debug(4,"OUT-U: ".join("\n",@out));
    $self->debug(3,"Counting ".scalar(@out)." Changes while doing svn up");
    if ( $err =~ m/run 'svn cleanup' to remove locks/ ) {
	$self->debug(3,"We need a svn cleanup");
	my ($out,$err) = $self->command("svn cleanup $dir_svn/$proj_sub_dir");
    }
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

    my $proj     = $self->proj();
#    my $proj = shift || die "Unknown Project";

    $self->debug(4,"");
    $self->debug(4,"------------");
    $self->debug(3,"svn Checkout: Proj: $proj");

    my $proj_sub_dir=$self->svn_dir_base($proj);

    if ( $svn_update_done{$proj_sub_dir} ) {
	$self->debug(3,"Repository $proj_sub_dir for $proj already updated");
	return;
    };

    my $url=$svn_repository_url{$proj_sub_dir};

    $self->debug(3,"svn co $url $dir_svn/$proj_sub_dir");
    my ($out,$err) = $self->command("svn co $url $dir_svn/$proj_sub_dir");
    my @out = 
	grep { $_ !~ m/^(\s*$|Fetching external|External at revision|At revision|Checked out external at revision)/ } split(/\n/,$out);
    $self->debug(4,"OUT-U: ".join("\n",@out));
    if ( $err ) {
	print "ERR: $err\n";
    }
    $svn_update_done{$proj_sub_dir}++;
	   };

# ------------------------------------------------------------------
# Update the svn source tree
sub svn_changelog($){
    my $self = shift;
    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";
    return -1 if $self->errors();

    my $proj     = $self->proj();

    print "\n";
    print "------------\n";
    print "svn Changelog update:\n";
    print "Proj: $proj\n";
    my $proj_sub_dir = $self->proj_sub_dir();
    my $debname = $proj2debname{$proj};

    my $command="$dir_svn/openstreetmap-applications/utils/packaging/svn_log2debian_changelog.pl";
    $command .= " --project_name='$debname' ";
	      
    if ( $proj =~ m/gpsdrive/ ) {
	$command .= " --prefix=2.10svn ";
    };
    if ( $DEBUG ) {
	$command .= " --debug ";
    };
    
    $self->command("cd $dir_svn/$proj_sub_dir; $command");
	      };


# ------------------------------------------------------------------
# Update a single chroot the svn source tree
sub svn_copy($$){
    my $self = shift;
    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";
    return -1 if $self->errors();

    my $platform = $self->platform();
    my $proj     = $self->proj();

    $self->debug( 4, "" );
    $self->debug( 4, "------------" );
    $self->debug( 3, "svn Copy($platform,$proj)" );
    $self->debug( 4, "Platform: $platform" );
    $self->debug( 4, "Proj: $proj" );
    my $proj_sub_dir = $self->proj_sub_dir();

    $self->debug(4, "Repository $proj_sub_dir");

    my $proj_svn_dir = "$dir_svn/$proj_sub_dir/";
    my $build_dir    = $self->build_dir();

    find(
	sub{
	    return if $File::Find::name =~ m,\.svn,;
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
		|| warn "Cannot create '$dst_dir': $!\n";
	    };
	    copy($src,$dst)
		|| die "!!!!!!!!!! ERROR: Cannot Copy $src->$dst: $!\n";
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

    my $platform = $self->platform();
    my $proj     = $self->{proj}     || die "Unknown Project";;
    my $build_dir    = $self->build_dir();

    my $proj_sub_dir = $self->proj_sub_dir();

    my $patch_file="$dir_svn/$proj_sub_dir/debian/$platform.patch";
    if ( -s  $patch_file) {
	$self->command("cp $dir_svn/$proj_sub_dir/debian/* $build_dir/debian/");
	print " XXXXXXXXXX NEW apply_patch()\n";
	$self->command("cd $build_dir/$proj_sub_dir/debian/; patch <$patch_file");
    }

};


# ------------------------------------------------------------------
# Update the svn source tree to be able to work without a svn-binary
sub apply_pre_patch($){
    my $self = shift;
    die "Wrong Reference '".ref($self)."'"  unless ref($self) eq "BuildTask";
    return -1 if $self->errors();

    my $platform = $self->platform();
    my $proj     = $self->proj();
    my $svn_dir_full = $self->svn_dir_full();

    # For josm and all it's plugins write a REVISION File
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
    return -1 if $self->errors();

    my $platform = $self->platform();
    my $proj     = $self->proj();

    $self->debug(4,"");
    $self->debug(4,"------------");
    $self->debug(3,"Debuild($platform,$proj)");
    $self->debug(4,"Platform: $platform");
    $self->debug(4,"Proj: $proj");
    my $proj_sub_dir = $self->proj_sub_dir();

    my $svn_revision = $self->svn_revision_platform();

    if ( ! $do_fast ) {
	my ($out,$err) = $self->dchroot($proj_sub_dir ,"debuild clean");
	if ( $err ) {
	    print "ERR: $err\n";
	}
    }

    my ($out,$err) = $self->dchroot($proj_sub_dir ,"debuild binary");
    if ( $err ) {
	print "ERR: $err\n";
    }

    # --- Check on missing Build dependencies
    my @dependencies= grep { $_ =~ m/^dpkg-checkbuilddeps:/ } split(/\n/,$err);
    @dependencies = grep { s/.*Unmet build dependencies: //g; }  @dependencies;
    my $dep_file="$dir_chroot/$platform/home/$user/install-debian-dependencies-$proj.sh";
    if (  @dependencies ) {
	warn("!!!!!!!!!!!!!!!!!!!!!! Cannot Build Debian Package because of Missing Dependencies: ".
	     join("\n", @dependencies));
	warn("Logged to: '$dep_file'\n");
	write_file($dep_file,"chroot $dir_chroot/$platform aptitude install ".
		   join("\n", @dependencies)."\n");
	return -1;
    } else {
	unlink($dep_file);
    }



    # ------ Collect Resulting *.deb names
    my $result_dir=dirname("$dir_chroot/$platform/home/$user/$proj_sub_dir/");
    my @results= grep { $_ =~ m/\.deb$/ } glob("$result_dir/*$svn_revision*.deb");
    my $result_count=scalar(@results);
    my $result_expected =$num_packages{$proj}; 
    if ( $result_expected !=  $result_count ) {
	warn "!!!!!!!! WARN: Number of resulting Packages for Proj '$proj' on Platform $platform is Wrong.\n";
	warn "Expecting $result_expected packages for svn-revision $svn_revision, got: $result_count Packages\n";
	warn "see results in $result_dir\n";
    }
    $self->debug(3,"Resulting Packages($result_count):");
    $self->debug(4,"\n\t".join("\n\t",@results));




    # Move Result to one Result Place
    my ($distri,$version,$bits)=split_platform($platform);
    my $dst_dir="$package_results/$distri/pool/$version";
    if ( ! -d $dst_dir ) {
	mkpath($dst_dir)
	    or warn "WARNING: Konnte Pfad $dst_dir nicht erzeugen: $!\n";
    }
    for my $result ( @results) {
	my $fn=basename($result);
	rename($result,"$dst_dir/$fn")
	    || warn "Cannot move result '$result' to '$dst_dir/$fn': $!\n";;
    }
}

# ------------------------------------------------------------------
# Main
# ------------------------------------------------------------------
@platforms= @default_platforms
    unless @platforms;

@projs= @default_projs 
    unless @projs;


if ( $DEBUG > 3 ) {
    print "----------------------------------------\n";
    print "Platforms: " . join(" ",@platforms)."\n";
    print "Projects:  " . join(" ",@projs)."\n";
    print "svn_up $do_svn_up\n";
    print "svn_co $do_svn_co\n";
    print "svn_changelog: $do_svn_changelog\n";
    print "svn_cp $do_svn_cp\n";
    print "fast: $do_fast\n";
    print "force: $FORCE\n";
    print "debug: $DEBUGa\n";
    print "----------------------------------------\n";
}

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

for my $platform ( @platforms ) {
    print "\n";
    print "---------- Platform: $platform\n";

    for my $proj ( @projs ) {

	my $task = BuildTask->new( 
	    proj     => $proj, 
	    platform => $platform ,
	    );
	$task->debug(3, "------------- Project: $proj");
	
	$task->svn_copy()	if $do_svn_cp;
	$task->apply_patch();
	$task->debuild();
	$RESULTS->{$platform}->{$proj}=$task;
    }
};

if ( $DEBUG > 3) {
    print "RESULTS:\n"
    print Dumper(\$RESULTS);
}


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
       $package_results

EOUSAGE

die "\n";

}
