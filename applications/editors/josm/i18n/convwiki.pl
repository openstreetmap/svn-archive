#! /usr/bin/perl -w

# Written by Dirk Stöcker <openstreetmap@dstoecker.de>
# Public domain, no rights reserved.

use strict;
use LWP::Simple;
use open qw/:std :encoding(utf8)/;

my $filename;
my $dir = $ARGV[1] || "build/josmfiles";
print "$ARGV[0]\n";
if($ARGV[0] && $ARGV[0] =~ /^https?:\/\//)
{
  $filename = $ARGV[2] || "build/josmfiles.zip";
  my $content = get($ARGV[0]);
  die "Couldn't get $ARGV[0]" unless defined $content;
  open FILE,">:raw",$filename or die "Could not open $filename";
  print FILE $content;
  close FILE
}
else
{
  $filename = $ARGV[0];
}
system "rm -rf $dir/";
print "Extracting to $dir\n";
mkdir $dir;
system "unzip -q -d $dir $filename";
foreach my $name (glob "$dir/*")
{
  if($name =~ /^(.*?)([^\/]+-preset\.xml)$/)
  {
    system "mv \"$name\" \"$name.orig\"";
    my ($path, $xmlname) = ($1, $2);
    my $res = `xmllint --format --schema ../core/data/tagging-preset.xsd \"$name.orig\" --encode utf-8 --output \"$name\" 2>&1`;
    print $res if $res !~ /\.orig validates/;
    system "perl convpreset.pl \"$name\" >\"${path}trans_$xmlname\"";
    unlink "$name.orig";
  }
  elsif($name =~ /^(.*?)([^\/]+-style\.xml$)/)
  {
    system "perl convstyle.pl \"$name\" >${1}trans_$2";
  }
  elsif($name =~ /^(.*?)([^\/]+\.mapcss)$/)
  {
    system "perl convcss.pl \"$name\" >${1}trans_$2";
  }
  else
  {
    die "Unknown file type \"$name\".";
  }
  unlink $name;
}
