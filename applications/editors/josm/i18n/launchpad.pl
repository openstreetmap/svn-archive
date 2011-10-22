#!/usr/bin/perl -w

use strict;

my %lang = map {$_ => 1} (
"ar", "bg", "cs", "da", "de", "el", "en_AU", "en_GB",
"es", "et", "eu", "fi", "fr", "gl", "he", "id", "is",
"it", "ja", "nb", "nl", "pl", "pt_BR", "ru", "sk",
"sv", "tr", "uk", "zh_CN", "zh_TW"
);

my $count = 0;#11;
my $cleanall = 0;#1;
my $upload = 0;#1;

if($#ARGV != 0)
{
    warn "URL not given (try Launchpad download URL or \"bzr\")." 
}
elsif($ARGV[0] eq "bzr" || $ARGV[0] eq "bzronly")
{
    mkdir "build";
    die "Could not change into new data dir." if !chdir "build";
    system "bzr export -v josm_trans lp:~openstreetmap/josm/josm_trans";
    chdir "..";
    copypo("build/josm_trans/josm");
    system "rm -rv build/josm_trans";
    exit(0) if $ARGV[0] eq "bzronly";
}
else
{
    mkdir "build";
    mkdir "build/josm_trans";
    die "Could not change into new data dir." if !chdir "build/josm_trans";
    system "wget $ARGV[0]";
    system "tar -xf laun*";
    chdir "../..";
    copypo("build/josm_trans");
    system "rm -rv build/josm_trans";
}

system "ant";
if($upload)
{
  my $outdate = `date -u +"%Y-%m-%dT%H_%M_%S"`;
  chomp $outdate;
  mkdir "build/josm";
  system "cp po/*.po po/josm.pot build/josm";
  chdir "build";
  if(!$count)
  {
    system "tar -cjf ../launchpad_upload_josm_$outdate.tar.bz2 josm";
  }
  else
  {
    my @files = sort glob("josm/*.po");
    my $num = 1;
    while($#files >= 0)
    {
      my @f = splice(@files, 0, $count);
      system "tar -cjf ../launchpad_upload_josm_${outdate}_$num.tar.bz2 josm/josm.pot ".join(" ",@f);
      ++$num;
    }
  }
  system "rm -rv josm";
  chdir "..";
}

sub copypo
{
    my ($path) = @_;
    foreach my $name (split("\n", `find $path -name "*.po"`))
    {
      $name =~ /([a-zA-Z_]+)\.po/;
      if($lang{$1})
      {
        system "cp -v $name po/$1.po";
      }
      elsif($cleanall)
      {
        local $/; undef $/;
        open FILE,"<",$name or die;
        my $x = <FILE>;
        close FILE;
        $x =~ s/\n\n.*$/\n/s;
        open FILE,">","po/$1.po" or die;
        print FILE $x;
        close FILE;
      }
    }
}
