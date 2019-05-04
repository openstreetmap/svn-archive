#! /usr/bin/perl -w

# Written by Dirk Stöcker <openstreetmap@dstoecker.de>
# Public domain, no rights reserved.

use strict;
use LWP::Simple;
use open qw/:std :encoding(utf8)/;

my $item;
my $comment = 0;

# This is a simple conversion and in no way a complete XML parser
# but it works with a default Perl installation

# Print a header to write valid Java code.  No line break,
# so that the input and output line numbers will match.
print "class trans_maps { void tr(String s){} void f() {";

my @lines;
if($ARGV[0] && $ARGV[0] =~ /^https?:\/\//)
{
  my $content = get($ARGV[0]);
  die "Couldn't get $ARGV[0]" unless defined $content;
  @lines = split("\r?\n", $content);
}
else
{
  @lines = <>;
}

for my $line (@lines)
{
  $line =~ s/\r//g;
  chomp($line);
  print "tr(\"---DUMMY-MARKER---\"); ";
  if($line =~ /<name(?: +lang=['"]en['"])?>(.*)<\/name>/)
  {
    my $val = $1;
    $val =~ s/&amp;/&/g;
    $val =~ s/"/\\"/g;
    print "tr(\"$val\"); /* $line */\n";
  }
  elsif($line =~ /<description +lang=['"]en['"]><!\[CDATA\[(.*)\]\]><\/description>/)
  {
    my $val = $1;
    $val =~ s/"/\\"/g;
    print "tr(\"$val\"); /* $line */\n";
  }
  elsif($line =~ /<description +lang=['"]en['"]>(.*)<\/description>/)
  {
    my $val = $1;
    $val =~ s/&amp;/&/g;
    $val =~ s/"/\\"/g;
    print "tr(\"$val\"); /* $line */\n";
  }
  elsif($line =~ /^[ \t]*$/)
  {
    print "\n";
  }
  elsif($line =~ /<entry>/) # required or the gettext info texts get too large
  {
    print "public newEntry() {};\n";
  }
  else
  {
    print "/* $line */\n";
  }
}

print "}}\n";
