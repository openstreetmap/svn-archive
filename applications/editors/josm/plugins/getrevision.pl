#! /usr/bin/perl

my $checkrev = qr/commit[ \n]+revision=\"(\d+)\">/;
my $text = `svn info --xml .`;
my $revision = $1 if $text =~ $checkrev;
foreach my $file (@ARGV)
{
  my $ftext = `svn info --xml $file`;
  my $frevision = $1 if $ftext =~ $checkrev;
  if($frevision > $revision)
  {
    $text = $ftext;
    $revision = $frevision;
  }
}

print $text;
