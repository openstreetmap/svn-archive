#!/usr/bin/perl
#--------------------------------------------------------------------
# Search your disk for GPX files, and try to identify some details
# saying what they are, when they were created, etc.
#
# Usage: listTraces.pl YourOsmUsername DirectoryToSearch
#--------------------------------------------------------------------
use File::Find;
use Digest::MD5 qw(md5_hex);

# Get command-line parameters
my $Username = shift();
my $Dir = shift();
my $OutputFile = "tracelist.xml";
die("Usage: $0 [your name] [directory to search]\n") if(! -d $Dir);

# Add all files found to an XML file
open(XML, '>', $OutputFile) || die("I'd like to write to $OutputFile...?\n");
print XML "<gpx_list owner='$Username'>\n";
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
  
  # GPX extension only
  return if(lc(substr($Name,-4)) ne '.gpx');
  
  # Identify the file
  my $Hash = fileHash($Name);
  
  # Get date
  @Stats = stat($Name);
  my $Time = $Stats[9];
  
  # Think of a description
  my $Description = describeGPX($Name);
  $Description =~ s{&}{&amp;};
  $Description =~ s{<}{&lt;};
  $Description =~ s{>}{&gt;};
  $Description =~ s{"}{&quot;};
  
  # Add to the output
  print XML "<record ";
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
  
  # Remove GPX and just return as string
  my $Description = substr($Filename,length($Dir),-4);
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
  
  open(my $fp, '<', $filename) or die("Can't open $filename\n");
  my $Data = <$fp>;
  close $fp;
  return(md5_hex($Data));
}