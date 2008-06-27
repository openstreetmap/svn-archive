#! /usr/bin/perl -w

use strict;

my $item;
my $line = 1;
my $comment = 0;

# this is a simple conversion and in no way a complete XML parser
# but it works with a default Perl installation

while(my $line = <>)
{
  chomp($line);
  if($line =~ /<item\s+name="(.*?)\/ ".*<\/item>/)
  {
    print "tr(\"$1/ \") /* empty item \"$1\" */\n";
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
    print "tr($1) /* item $item text $1 */\n";
  }
  elsif($line =~ /<check.*text=(".*?")/)
  {
    print "tr($1) /* item $item check $1 */\n";
  }
  elsif($line =~ /<combo.*text=(".*?").*values="(.*?)"/)
  {
    print "tr($1) /* item $item combo $1 */";
    foreach my $val (split ",",$2)
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
