#! /usr/bin/perl -w

#####################################################################
### http://www.perl.com/doc/manual/html/utils/pod2man.html
### http://search.cpan.org/dist/perl/pod/perlpod.pod

=head1 NAME

poimport.pl - Import the translation from the tarball downloaded from
Launchpad.

=head1 SYNOPSIS

B<poimport.pl> [B<--help>] [B<--man>] [B<--podir> I<po>]
    [B<--workdir> I<poimport>] [B<--(no)rmworkdir>] I<tarball>

=head1 DESCRIPTION

Import the plugin translations from Launchpad.  The argument
I<tarball> can be a tarball file that was downloaded from Launchpad, a
tarball download URL, a JOSM translation branch revision number
(e.g. I<789>), or the keyword I<latest> for the latest revision.
Default is to download the latest translation branch revision.

=head1 OPTIONS

=over 4

=item B<--help>

Prints a brief help message and exits.

=item B<--man>

Prints the manual page and exits.

=item B<--podir>

Destination directory relative to directory where this script was
started in.  Default is F<po>.

=item B<--workdir>

Temporary directory.  A unique directory name that is not used for
anything else.  Default is F<poimport>.

=item B<--rmworkdir>

Remove the temporary directory after the work is done.  Disable
removal with B<--normworkdir>.  Default is to remove the temporary
directory.

=back

=cut
#####################################################################

use strict;
use File::Copy;
use Cwd;
use File::Spec::Functions;
use File::Basename;
use Getopt::Long;
use Pod::Usage;

### Name of the tarball downloaded from Launchpad.  Or download URL.
### Or JOSM translation branch revision number.  Or keyword "latest".
my $tarball;
#$tarball = "launchpad-export.tar.gz";
#$tarball = "http://launchpadlibrarian.net/159932691/launchpad-export.tar.gz";
#$tarball = "http://bazaar.launchpad.net/~openstreetmap/josm/josm_trans/tarball/747";
#$tarball = "747";
#$tarball = "http://bazaar.launchpad.net/~openstreetmap/josm/josm_trans/tarball";
$tarball = "latest";
my $workdir = "poimport";       ### Temp. directory.
my $rmworkdir = 1;              ### Remove the temp. directory (0/1)?
my $podir = "po";               ### Destination directory.
my $showhelp = 0;               ### Show help screen.
my $showman = 0;                ### Show manual page of this script.

GetOptions('help|?|h'   => \$showhelp,
           'man'        => \$showman,
           'podir=s'    => \$podir,
           'workdir=s'  => \$workdir,
           'rmworkdir!' => \$rmworkdir,
          ) or pod2usage(2);

pod2usage(1) if $showhelp;
pod2usage(-exitstatus => 0, -verbose => 2) if $showman;

### Check for arguments.  The only supported argument is the tarball.
if ($#ARGV == 0) {
  $tarball = $ARGV[0];
}
elsif ($#ARGV > 0) {
  die "This script accepts only one argument.\n";
}

my $josmtburl = "http://bazaar.launchpad.net/~openstreetmap/josm/josm_trans/"
  . "tarball";

### Check for JOSM translation branch revision number.
if ($tarball =~ m/^\d+$/) {
  $tarball = $josmtburl . "/" . $tarball;
}
### Or for keyword "latest", i.e. the latest JOSM translation revision.
elsif ($tarball eq "latest") {
  $tarball = $josmtburl;
}

### Check if tarball is a URL and download it.  The downloaded file
### will not be removed and is available for a second import.
my $downurl;
if ($tarball =~ m,^http://.+/([^/]+)$,) {
  ### URL: Download file.
  $downurl = $tarball;
  my $downfile = $1;
  if ($downfile =~ m/^\d+$/) {
    ### Download of revision number.
    if ($tarball =~ m:/([^/]+)/tarball/(\d+)$:) {
      $downfile = $1 . "_" . $2 . ".tar.gz";
    }
    else {
      $downfile .= ".tar.gz";
    }
  }
  elsif ($downfile eq "tarball") {
    ### Download of latest revision.
    if ($tarball =~ m:/([^/]+)/tarball$:) {
      $downfile = $1 . "_latest.tar.gz";
    }
    else {
      $downfile .= ".tar.gz";
    }
  }
  print "Will download file $downfile from $downurl.\n";
  system("wget -O $downfile $downurl") == 0 or die "wget failed: $?";
  $tarball = $downfile;
}

die "Tarball $tarball not found.\n" if (! -r $tarball);
if (! -d $workdir) {
  mkdir $workdir or die "Failed to create work directory $workdir: $!";
}
copy($tarball, $workdir);
my $startdir = getcwd();
chdir $workdir;
my $tarballfile = basename($tarball);
system "tar -xf $tarballfile";
print "Copy language files:";
foreach my $lpponame (split("\n", `find . -name "*.po"`)) {
  if ($lpponame =~ /([a-zA-Z_@]+)\.po/) {
    my $lang = $1;
    my $poname = $1 . ".po";
    print " $lang";
    copy($lpponame, catfile($startdir, $podir, $poname));
  }
}
print "\n";

if ($rmworkdir) {
  chdir $startdir;
  system "rm -rf $workdir";
}
