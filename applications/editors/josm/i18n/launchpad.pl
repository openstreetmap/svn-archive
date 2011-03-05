#!/usr/bin/perl -w

use strict;


if($#ARGV != 0)
{
    warn "URL not given." 
}
else
{
    mkdir "new";
    die "Could not change into new data dir." if !chdir "new";
    system "wget $ARGV[0]";
    system "tar -xf laun*";
    chdir "..";
    foreach my $name (split("\n", `find new -name "*.po"`))
    {
      my $a=$name;
      $a =~ s/.*-//;
      system "mv -v $name po/$a" if -f "po/$a";
      # activate in case we need a "clean all upstream texts launchpad upload"
      # system "touch po/$a" if ! -f "po/$a";
    }
}
system "ant";
my $outdate = `date -u +"%Y-%m-%dT%H_%M_%S"`;
chomp $outdate;
mkdir "josm";
system "cp po/*.po po/josm.pot josm";
system "tar -czf launchpad_upload_josm_$outdate.tgz josm";
system "rm -rv josm new";
