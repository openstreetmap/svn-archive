#!/usr/bin/perl -w

use strict;

my $count = 0;#11;
my $cleanall = 0;#1;

if($#ARGV != 0)
{
    warn "URL not given." 
}
else
{
    mkdir "build";
    mkdir "build/new";
    die "Could not change into new data dir." if !chdir "build/new";
    system "wget $ARGV[0]";
    system "tar -xf laun*";
    chdir "../..";
    foreach my $name (split("\n", `find build/new -name "*.po"`))
    {
      my $a=$name;
      $a =~ s/.*-//;
      if(-f "po/$a")
      {
        system "mv -v $name po/$a";
      }
      elsif($cleanall)
      {
        local $/; undef $/;
        open FILE,"<",$name or die;
        my $x = <FILE>;
        close FILE;
        $x =~ s/\n\n.*$/\n/s;
        open FILE,">","po/$a" or die;
        print FILE $x;
        close FILE;
      }
    }
}
system "ant";
my $outdate = `date -u +"%Y-%m-%dT%H_%M_%S"`;
chomp $outdate;
mkdir "build/josm";
system "cp po/*.po po/josm.pot build/josm";
chdir "build";
if(!$count)
{
  system "tar -cjf launchpad_upload_josm_$outdate.tar.bz2 josm";
}
else
{
  my @files = sort glob("josm/*.po");
  my $num = 1;
  while($#files >= 0)
  {
     my @f = splice(@files, 0, $count);
     system "tar -cjf launchpad_upload_josm_${outdate}_$num.tar.bz2 josm/josm.pot ".join(" ",@f);
     ++$num;
  }
}
system "rm -rv josm new";
chdir "..";
