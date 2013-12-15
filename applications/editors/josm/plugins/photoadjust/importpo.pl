#! /usr/bin/perl -w
###
### importpo.pl - Import the translation from the tarball downloaded
### from Launchpad.

use strict;
use File::Copy;
use Cwd;
use File::Spec::Functions;

### Name of the tarball downloaded from Launchpad.
my $tarball = "launchpad-export.tar.gz";
### Temporary directory.
my $workdir = "importpo";
### Remove the temp. directory after the work is done (0/1)?
my $rmworkdir = 1;
### Destination directory relative to directory where this script was
### started in.
my $podir = "po";

die "Tarball $tarball not found.\n" if (! -r $tarball);
if (! -d $workdir) {
  mkdir $workdir or die "Failed to create directory $workdir: $!";
}
copy($tarball, $workdir);
my $startdir = getcwd();
chdir $workdir;
system "tar -xf $tarball";
foreach my $lpponame (split("\n", `find po -name "*.po"`)) {
  if ($lpponame =~ /([a-zA-Z_]+\.po)/) {
    my $poname = $1;
    copy($lpponame, catfile($startdir, $podir, $poname));
  }
}

if ($rmworkdir) {
  chdir $startdir;
  system "rm -rf $workdir";
}
