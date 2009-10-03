#! /usr/bin/perl -w

# Written by Dirk Stöcker <openstreetmap@dstoecker.de>
# Public domain, no rights reserved.

use strict;

my $item = "";
my $group;
my $comment = 0;

# This is a simple conversion and in no way a complete XML parser
# but it works with a default Perl installation

# Print a header to write valid Java code.  No line break,
# so that the input and output line numbers will match.
print "class trans_preset { void tr(String s){} void f() {";

while(my $line = <>)
{
  chomp($line);
  if($line =~ /<item\s+name=(".*?")/)
  {
    my $val = $1;
    $item = $group ? "$group$val" : $val;
    $item =~ s/""/\//;
    if($line =~ /name_context=(".*?")/)
    {
      print "trc($1, $val); /* item $item */\n";
    }
    else
    {
      print "tr($val); /* item $item */\n";
    }
  }
  elsif($line =~ /<group.*\s+name=(".*?")/)
  {
    my $gr = $1;
    $group = $group ? "$group$gr" : $gr;
    $group =~ s/\"\"/\//;
    if($line =~ /name_context=(".*?")/)
    {
      print "trc($1,$gr); /* group $group */\n";
    }
    else
    {
      print "tr($gr); /* group $group */\n";
    }
  }
  elsif($line =~ /<label.*\s+text=" "/)
  {
    print "/* item $item empty label */\n";
  }
  elsif($line =~ /<label.*\s+text=(".*?")/)
  {
    my $text = $1;
    if($line =~ /text_context=(".*?")/)
    {
      print "trc($1,$text); /* item $item label $text */\n";
    }
    else
    {
      print "tr($text); /* item $item label $text */\n";
    }
  }
  elsif($line =~ /<text.*\s+text=(".*?")/)
  {
    my $n = $1;
    if($line =~ /text_context=(".*?")/)
    {
      print "trc($1,$n); /* item $item text $n */\n";
    }
    else
    {
      print "tr($n); /* item $item text $n */\n";
    }
  }
  elsif($line =~ /<check.*\s+text=(".*?")/)
  {
    my $n = $1;
    if($line =~ /text_context=(".*?")/)
    {
      print "trc($1,$n); /* item $item check $n */\n";
    }
    else
    {
      print "tr($n); /* item $item check $n */\n";
    }
  }
  elsif($line =~ /<role.*\s+text=(".*?")/)
  {
    my $n = $1;
    if($line =~ /text_context=(".*?")/)
    {
      print "trc($1,$n); /* item $item role $n */\n";
    }
    else
    {
      print "tr($n); /* item $item role $n */\n";
    }
  }
  # first handle display values
  elsif($line =~ /<combo.*\s+text=(".*?").*\s+display_values="(.*?)"/)
  {
    my ($n,$vals) = ($1,$2);
    my $vctx = ($line =~ /values_context=(".*?")/) ? $1 : undef;
    if($line =~ /text_context=(".*?")/)
    {
      print "trc($1,$n); /* item $item combo $n */";
    }
    else
    {
      print "tr($n); /* item $item combo $n */";
    }
    foreach my $val (split ",",$vals)
    {
      next if $val =~ /^[0-9-]+$/; # search for non-numbers
      print $vctx ? " trc($vctx, \"$val\");" : " tr(\"$val\");";
    }
    print "\n";
  }
  elsif($line =~ /<combo.*\s+text=(".*?").*\s+values="(.*?)"/)
  {
    my ($n,$vals) = ($1,$2);
    my $vctx = ($line =~ /values_context=(".*?")/) ? $1 : undef;
    if($line =~ /text_context=(".*?")/)
    {
      print "trc($1,$n); /* item $item combo $n */";
    }
    else
    {
      print "tr($n); /* item $item combo $n */";
    }
    foreach my $val (split ",",$vals)
    {
      next if $val =~ /^[0-9-]+$/; # search for non-numbers
      print $vctx ? " trc($vctx, \"$val\");" : " tr(\"$val\");";
    }
    print "\n";
  }
  elsif($line =~ /<\/group>/)
  {
    $group = 0 if !($group =~ s/(.*\/).*?$//);
    print "\n";
  }
  elsif($line =~ /<\/item>/)
  {
    $item = "";
    print "\n";
  }
  elsif(!$line)
  {
    print "\n";
  }
  elsif($line =~ /^\s*$/
     || $line =~ /<separator *\/>/
     || $line =~ /<space *\/>/
     || $line =~ /<\/?optional>/
     || $line =~ /<key/
     || $line =~ /annotations/
     || $line =~ /roles/
     || $line =~ /href=/
     || $line =~ /<!--/
     || $line =~ /-->/
     || $comment)
  {
    print "// $line\n";
  }
  else
  {
    print "/* unparsed line $line */\n";
#    print STDERR "Unparsed line $line\n";
  }

  # note, these two must be in this order ore oneliners aren't handled
  $comment = 1 if($line =~ /<!--/);
  $comment = 0 if($line =~ /-->/);
}

print "}}\n";
