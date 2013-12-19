#! /usr/bin/perl -w
###
### poimport.pl - Import the translation from the tarball downloaded
###     from Launchpad.

use strict;
use File::Copy;
use Cwd;
use File::Spec::Functions;
use File::Basename;

### Name of the tarball downloaded from Launchpad.  Or download URL.
### Or JOSM translation branch revision number.
my $tarball = "launchpad-export.tar.gz";
#$tarball = "http://launchpadlibrarian.net/159932691/launchpad-export.tar.gz";
#$tarball = "http://bazaar.launchpad.net/~openstreetmap/josm/josm_trans/tarball/747";
#$tarball = "747";
### Temporary directory.  A unique directory name that is not used for
### anything else.
my $workdir = "importpo";
### Remove the temp. directory after the work is done (0/1)?
my $rmworkdir = 1;
### Destination directory relative to directory where this script was
### started in.
my $podir = "po";

### Check for arguments.  The only supported argument is the tarball.
if ($#ARGV == 0) {
  $tarball = $ARGV[0];
}
elsif ($#ARGV > 0) {
  die "This script accepts only one argument.\n";
}

### Check for JOSM translation branch revision number.
if ($tarball =~ m/^\d+$/) {
  $tarball = "http://bazaar.launchpad.net/~openstreetmap/josm/josm_trans/"
    . "tarball/" . $tarball;
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
