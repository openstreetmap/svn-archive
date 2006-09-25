#!/usr/bin/perl -w
use strict;
use Tk;
use IO::Socket::INET;
use Tk::Pane;
use Tk::Label;
use Tk::Menu;
use XML::Simple;
use Data::Dumper;
use LWP::Simple qw(getstore);
#DownloadFiles();exit;
#-----------------------------------------------------------------------------
# Options
#-----------------------------------------------------------------------------
my $NAME = "RenderControl";
my $Files = "Files"; # Main directory

#-----------------------------------------------------------------------------
# Look for some useful objects
#-----------------------------------------------------------------------------
my @Browsers = ("\"C:\\Program Files\\Mozilla Firefox\\firefox\"", "/usr/bin/firefox");
my $Browser;
foreach(@Browsers){$Browser = $_ if(-f $_);}

# Globals
my $Status = "Statusbar";
my ($Lat1,$Long1,$Lat2,$Long2,$Title,$CoordsValid);
my %Options;

FirstTime() if(! -d $Files);

LoadOptions("$Files/options.txt");

#-----------------------------------------------------------------------------
# Create GUI
#-----------------------------------------------------------------------------
print STDERR "\n\nLoading GUI...\n";
my $Window = new MainWindow(-title => $NAME);
my %Controls;

$Window->optionAdd("*tearOff", "false");
$Window->optionAdd('*BorderWidth' => 1);
         
#-----------------------------------------------------------------------------
# Create menus
#-----------------------------------------------------------------------------
# File menu
my $Menu = $Window->Menu;
my $FileMenu = $Menu->cascade(-label => "~File");
  my $Bookmarks = $FileMenu->cascade(-label=> "Bookmarks");
  BookmarkMenu($Bookmarks, "$Files/Bookmarks");
  $FileMenu->command(-label=> "E~xit", -command => sub{exit});

# Update from web
my $UpdateMenu = $Menu->cascade(-label=>"~Update");
  $UpdateMenu->command(-label=>"~Download latest files", 
    -command => \&DownloadFiles);
    
# Help menu
my $HelpMenu = $Menu->cascade(-label => "~Help");
  $HelpMenu->command(-label=>"~Web page", 
    -command => sub{BrowseTo("http://www.slashdot.org/");});

$Window->configure(-menu => $Menu);

#-----------------------------------------------------------------------------
# Create rest of window
#-----------------------------------------------------------------------------
my $Footer = $Window->Frame()->pack(-side=>"bottom", -fill=>"x");
$Footer->Button(-text => 'Render', -command => \&Render)->pack(-side=>'left');
$Footer->Button(-text => 'Quit', -command => sub{exit})->pack(-side=>'right');
$Footer->Message(-textvariable => \$Status)->pack(-side=>'left', -expand=>1, -fill=>'x');

my $LeftFrame = $Window->Frame()->pack(-expand=>1,-fill=>'both',-side=>"left");
my $ControlFrame = $LeftFrame->Frame(-borderwidth=>2,-relief=>'ridge')->pack(-side=>'top', -expand=>1,-fill=>'both');
my $ListFrame = $LeftFrame->Scrolled('Pane', -scrollbars => 'e',-borderwidth=>2,-relief=>'ridge')->pack(-side=>'bottom', -expand=>1,-fill=>'both');

$ControlFrame->Entry(-textvariable=>\$Lat1)->pack(-side=>'top');
$ControlFrame->Entry(-textvariable=>\$Lat2)->pack(-side=>'top');
$ControlFrame->Entry(-textvariable=>\$Long1)->pack(-side=>'top');
$ControlFrame->Entry(-textvariable=>\$Long2)->pack(-side=>'top');
$ControlFrame->Button(-text=>"Download", -command=>\&DownloadData)->pack(-side=>'top');

my $RightFrame = $Window->Frame(-borderwidth=>2,-relief=>'ridge')->pack(-expand=>1,-fill=>'both',-side=>"right");
my $List2 = $RightFrame->Scrolled('Pane', -scrollbars => 'e',-relief=>'ridge')->pack(-expand=>1,-fill=>'both');

#AddControlsFromFile($List1, "Files/freemap.xml");
MainLoop;

#-----------------------------------------------------------------------------
# Download all rendering files from disk
#-----------------------------------------------------------------------------
sub DownloadFiles(){
  my @Files = ("http://svn.openstreetmap.org/utils/osmarender/osmarender.xsl", 
  "http://svn.openstreetmap.org/utils/osmarender/osm-map-features.xml",
  "http://wiki.openstreetmap.org/index.php/OJW/Bookmarks",
  "http://svn.openstreetmap.org/freemap/freemap.xml");

  foreach my $File(@Files){
    if($File =~ /\/([^\\\/]*?)$/){
      my $Title = $1;
      $Status = "Downloading $Title";
      my $Store = "$Files/$Title";
      print "Downloading $File to $Store\n";
      getstore($File, $Store);
    }
  }
  $Status = "Download complete";
}

sub DownloadData(){
  if(!$CoordsValid){
    $Status = "Enter some coordinates, or load a bookmark";
    return;
    }
    
  $Options{"username"} =~ s/\@/%40/;
  
  my $URL = sprintf( "http://%s:%s\@www.openstreetmap.org/api/0.3/map?bbox=$Long1,$Lat1,$Long2,$Lat2",
  $Options{"username"},
  $Options{"password"});
  
  $Status = "Downloading\n";
  getstore($URL, "$Files/data.osm");
  $Status = "Download complete\n";
}

sub Render(){

}

sub BookmarkMenu(){
  my ($Menu, $File) = @_;
  open(IN, $File) || return;
  my $Data = join('', <IN>);
  close IN;
  if($Data =~ /START_DATA(.*)END_DATA/ms){
    $Data = $1;
    foreach my $Line(split(/\n/, $Data)){
      my ($Name, $Lat1, $Long1, $Lat2, $Long2, $Spare) = split(/,/,$Line);
      $Menu->command(-label=> $Name, -command => sub{SetData($Name,$Lat1, $Long1, $Lat2, $Long2);}) if($Name && $Spare eq "false");
    }
  }
  $Menu->separator();
  $Menu->command(-label=> "Edit this list (web link)", 
    -command => sub{BrowseTo("http://wiki.openstreetmap.org/index.php/OJW/Bookmarks")});
}

sub BrowseTo(){
  my $URL = shift();
  `$Browser $URL` if($Browser);
}
sub FirstTime(){
  print "Welcome to $NAME\n\nThis looks like the first time you've run this program.\nIs it okay to create a directory in $Files, and download some data from the web? (y/n)\n";
  exit if(<> !~ /Y/i);
  mkdir($Files);
    
  
  # Get username and password
  print "\n\nYou now have the opportunity to enter an OSM username and password.\n\nThese will appear on your screen, so check nobody is watching!\nThese will be stored in plaintext on disk\n\nIf you don't want to enter a password yet, just press enter twice,\nthen edit $Files/options.txt later\n";
  print "Username:"; $Options{"username"} = <>;
  print "Password:"; $Options{"password"} = <>;
  chomp($Options{"username"}, $Options{"password"});
  print "\n";
  
  LookFor("Browser", 
    "C:\\Program Files\\Mozilla Firefox\\firefox.exe", 
    "/usr/bin/firefox");
  
  LookFor("XmlStarlet", 
    "C:\\xml\\xml.exe", 
    "/usr/bin/xmlstarlet");
  
  LookFor("Inkscape", 
    "C:\\Program Files\\inkscape\\inkscape.exe", 
    "/usr/bin/inkscape");
    
  SaveOptions("$Files/options.txt");
  
  # Download files for the first time
  DownloadFiles();  
  
}

sub SaveOptions(){
  my $Filename = shift();
  if(open(SAVEOPTIONS, ">", $Filename)){
    foreach my $Option(keys %Options){
      printf SAVEOPTIONS "%s=%s\n", $Option, $Options{$Option};
    }
    close SAVEOPTIONS;
  }
  else{
    print STDERR "Problem: Couldn't save options to $Filename\n";
  }
}

sub LookFor(){
  my ($Name, @Options) = @_;
  foreach my $Option(@Options){
    if(-f $Option){
      $Options{$Name} = $Option;
      print "Using $Name: $Option\n";
      return;
    }
  }
  print "Where can I find $Name on this computer? (press Enter if you don't know)\n";
  $Options{$Name} = <>;
  chomp($Options{$Name});
}

sub LoadOptions(){
  my $Filename = shift();
  open(OPT, "<", $Filename) || return;
  while(my $Line = <OPT>){
    chomp $Line;
    if($Line =~ /^\s*(\w+)\s*=\s*(.*)/){
      $Options{$1} = $2;
    }
  }
  close OPT;
}
#-----------------------------------------------------------------------------
# not finished
#-----------------------------------------------------------------------------
sub AddControlsFromFile(){
	my($Frame, $File) = @_;
	my $xml = new XML::Simple;
	my $data = $xml->XMLin($File);
	
	my @Rules = @{$data->{"rule"}};
	foreach my $Rule(@Rules){
		
		my $ItemFrame = $Frame->Frame(-borderwidth=>2,-relief=>'ridge')->pack(-expand=>1,-fill=>'x',-padx=>8,-pady=>3);
		$ItemFrame->Checkbutton(-text => "Enable")->pack(-side=>'top')->deselect();
		#$Frame->Entry()->pack(-side=>'top');
		$ItemFrame->Label(-text => $Rule->{"style"}->{"colour"})->pack(-side=>'top');
		$ItemFrame->Label(-text => $Rule->{"style"}->{"casing"})->pack(-side=>'top');
		$ItemFrame->Label(-text => sprintf("%s = %s", $Rule->{"condition"}->{"k"}, $Rule->{"condition"}->{"v"}))->pack(-side=>'top');
	}
	close DBG;
}


sub SetData(){
  ($Title,$Lat1,$Long1,$Lat2,$Long2) = @_;
  $CoordsValid = 1;
  print "Ok, loading $Title,$Lat1,$Long1,$Lat2,$Long2\n";
}


=head1 NAME

rendercontrol.pl - Perl/TK GUI interface to OpenStreetMap renderers

=head1 SYNOPSIS

Just run the program.  It will create a Files/ directory, download some
osmarender stuff from the web, and a list of interesting places, and popup
a window.

=head1 AUTHOR

Oliver White (oliver.white@blibbleblobble.co.uk)

=head1 COPYRIGHT

Copyright 2006, Oliver White

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

=cut

