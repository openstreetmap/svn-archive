#! /usr/bin/perl -w

# Written by Dirk Stöcker <openstreetmap@dstoecker.de>
# Public domain, no rights reserved.

use strict;

my $item;
my $comment = 0;

# This is a simple conversion and in no way a complete XML parser
# but it works with a default Perl installation

while(my $line = <>)
{
  chomp($line);
  if($line =~ /<item\s+name="(.*?)\/ ".*<\/item>/)
  {
    print "tr(\"$1/ \") /* empty item \"$1\" */\n";
  }
  elsif($line =~ /<item\s+name=" ".*<\/item>/)
  {
    print "/* empty item */\n";
  }
  elsif($line =~ /<item\s+name=" ".*\/>/)
  {
    print "/* empty item */\n";
  }
  elsif($line =~ /<item\s+name=(".*?")/)
  {
    $item = $1;
    print "tr($item) /* item $item */\n";
  }
  elsif($line =~ /<label\s+text=" "/)
  {
    print "/* item $item empty label */\n";
  }
  elsif($line =~ /<label\s+text=(".*?")/)
  {
    print "tr($1) /* item $item label $1 */\n";
  }
  elsif($line =~ /<text.*text=(".*?")/)
  {
    my $n = $1;
    print "tr($n) /* item $item text $n */\n";
  }
  elsif($line =~ /<check.*text=(".*?")/)
  {
    my $n = $1;
    print "tr($n) /* item $item check $n */\n";
  }
  # first handle display values
  elsif($line =~ /<combo.*text=(".*?").*display_values="(.*?)"/)
  {
    my ($n,$vals) = ($1,$2);
    print "tr($n) /* item $item combo $n */";
    foreach my $val (split ",",$vals)
    {
      next if $val =~ /^[0-9-]+$/; # search for non-numbers
      print " tr(\"$val\")";
    }
    print "\n";
  }
  elsif($line =~ /<combo.*text=(".*?").*values="(.*?)"/)
  {
    my ($n,$vals) = ($1,$2);
    print "tr($n) /* item $item combo $n */";
    foreach my $val (split ",",$vals)
    {
      next if $val =~ /^[0-9-]+$/; # search for non-numbers
      print " tr(\"$val\")";
    }
    print "\n";
  }
  elsif($line =~ /^\s*$/
     || $line =~ /<\/item>/
     || $line =~ /<key/
     || $line =~ /annotations/
     || $line =~ /<!--/
     || $line =~ /-->/
     || $comment)
  {
    print "\n";
  }
  else
  {
    print "/* unparsed line $line */\n";
    print STDERR "Unparsed line $line\n";
  }

  # note, these two must be in this order ore oneliners aren't handled
  $comment = 1 if($line =~ /<!--/);
  $comment = 0 if($line =~ /-->/);
}
