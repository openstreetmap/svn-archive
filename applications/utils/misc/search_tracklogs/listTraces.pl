#!/usr/bin/perl
#--------------------------------------------------------------------
# Search your disk for GPX files, and try to identify some details
# saying what they are, when they were created, etc.
#
# Usage: listTraces.pl YourOsmUsername DirectoryToSearch
#--------------------------------------------------------------------
use strict;
use warnings;

use File::Basename;
use File::Find;
use Digest::MD5 qw(md5_hex);

# Get command-line parameters
my $Username = shift();
my $Public = shift();
my $Dir = shift();
my $OutputFile = "tracelist.xml";
die("Usage: $0 [your name] [make public?] [directory to search]\n") unless $Public =~ /^(0|1)$/;
die("Usage: $0 [your name] [make public?] [directory to search]\n") if(! -d $Dir);

# Escape username
$Username =~ s{&}{&amp;};
$Username =~ s{<}{&lt;};
$Username =~ s{>}{&gt;};
$Username =~ s{"}{&quot;};
  
# Add all files found to an XML file
open(XML, '>', $OutputFile) || die("I'd like to write to $OutputFile...?\n");
print XML "<gpx_list owner='$Username' public='$Public'>\n";
find({wanted=>\&wanted,no_chdir=>1}, $Dir);
print XML "</gpx_list>\n";
close XML;

print STDERR "Done\nNow check $OutputFile using a text editor, and remove any traces which aren't yours\n";

#--------------------------------------------------------------------
# Callback for all files found in directory
#--------------------------------------------------------------------
sub wanted
{
  my $Name = $File::Find::name;
  my $Leafname = basename($Name);
  
  # GPX extension only
  return unless $Name =~ /\.gpx(?:\.gz|\.bz2)?$/;
  
  # Identify the file
  my $Hash = fileHash($Name);
  
  # Get date
  my @Stats = stat($Name);
  my $Time = $Stats[9];
  
  # Think of a description
  my $Description = describeGPX($Name);
  $Description =~ s{&}{&amp;};
  $Description =~ s{<}{&lt;};
  $Description =~ s{>}{&gt;};
  $Description =~ s{"}{&quot;};
  
  # Add to the output
  print XML "<record ";
  print XML "filename='$Leafname' ";
  print XML "hash='$Hash' ";
  print XML "timet='$Time' ";
  print XML "description='$Description' ";
  print XML "/>\n";
}

#--------------------------------------------------------------------
# Given a GPX file, conjure-up a good description of it
#
# TODO: add your improvements here - count tracklog points, 
# look for clues in the filename, look at its location, speed, 
# whatever you can find in the data
#--------------------------------------------------------------------
sub describeGPX
{
  my $Filename = shift();
  
  # Remove extension and just return as string
  my $Description = $Filename;
  $Description =~ s/^\Q$Dir\E[\\\/]//;
  $Description =~ s/\.gpx(?:\.gz|\.bz2)?$//;
  $Description =~ s{\W+}{ - }g;
  
  return($Description);
}

#--------------------------------------------------------------------
# Get the MD5 of a file
#--------------------------------------------------------------------
sub fileHash
{
  local $/;
  my $filename = shift();
  die("No such $filename\n") if(! -f $filename);

  my $Data;

  if ($filename =~ /\.gz$/) 
  {
      $Data = `zcat $filename`;
  }
  elsif ($filename =~ /\.bz2$/) 
  {
      $Data = `bzcat $filename`;
  }
  else
  {
      open(my $fp, '<', $filename) or die("Can't open $filename\n");
      $Data = <$fp>;
      close $fp;
  }

  return(md5_hex($Data));
}
