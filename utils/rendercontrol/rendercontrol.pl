#!/usr/bin/perl -w
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
use strict;
use Tk;
use IO::Socket::INET;
use Tk::Pane;
use Tk::Label;
use Tk::Menu;
use Tk::Image;
use Data::Dumper;
use LWP::Simple qw(getstore);
use File::Copy qw(copy);
#DownloadFiles();exit;
#-----------------------------------------------------------------------------
# Options
#-----------------------------------------------------------------------------
my $NAME = "RenderControl";
my $Files = "Files"; # Main directory

#-----------------------------------------------------------------------------
# Pre-GUI stage
#-----------------------------------------------------------------------------

# Globals
my $Status = "Statusbar";
my ($Lat1,$Long1,$Lat2,$Long2,$Title,$CoordsValid,$DisplayText,$ProjectName);
my %Options = ("RenderWidth"=>"1000");
my $WikiPage = "http://wiki.openstreetmap.org/index.php/Rendercontrol";

# If this is the first time the program has run, ask user some questions
FirstTime() if(! -d $Files);

# Load options from a file
LoadOptions("$Files/options.txt");

#-----------------------------------------------------------------------------
# Create GUI
#-----------------------------------------------------------------------------
PrintBig("Loading GUI");
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
  
  $FileMenu->command(-label=> "~Load OSM file", -command => sub{ReplaceFile("$Files/data.osm","osm");});
  $FileMenu->command(-label=> "~Load SVG file", -command => sub{ReplaceFile("$Files/output.svg","svg");});
  $FileMenu->separator();
  $FileMenu->command(-label=> "E~xit", -command => sub{exit});

# View menu
my $ViewMenu = $Menu->cascade(-label => "~View");
  $ViewMenu->command(-label=> "OSM data", -command => sub{SetView("text", "$Files/data.osm");});
  $ViewMenu->command(-label=> "Osmarender", -command => sub{SetView("text", "$Files/osm-map-features.xml");});
  $ViewMenu->command(-label=> "SVG", -command => sub{SetView("text", "$Files/output.svg");});
  $ViewMenu->command(-label=> "Image", -command => sub{SetView("image", "$Files/output.gif");});

  $ViewMenu->separator();
  $ViewMenu->command(-label=> "Open SVG", -command => sub{OpenFile("Editor", "$Files/output.svg");});
  $ViewMenu->command(-label=> "Open image", -command => sub{OpenFile("ImageEditor", "$Files/output.png");});

# Project menu
my $ProjectMenu = $Menu->cascade(-label => "~Project");
  my $LoadProjectMenu = $ProjectMenu->cascade(-label=> "Load ~project");
  ListProjects($LoadProjectMenu);
  
  $ProjectMenu->command(-label=> "~Save project as", -command => \&SaveProjectDialog);
  
  $ProjectMenu->separator();
  $ProjectMenu->command(-label=>"Reload project list",
    -command => sub{RefreshListProjects($LoadProjectMenu);});

# Bookmarks menu
my $BookmarksMenu = $Menu->cascade(-label => "~Bookmarks");
  my $Bookmarks1 = $BookmarksMenu->cascade(-label=> "Global bookmarks");
  BookmarkMenu($Bookmarks1, "$Files/Bookmarks");
  my $Bookmarks2 = $BookmarksMenu->cascade(-label=> "JOSM Bookmarks");
  BookmarkMenu($Bookmarks2, $Options{"JosmBookmarks"});

# Update from web
my $UpdateMenu = $Menu->cascade(-label=>"~Update");
  $UpdateMenu->command(-label=>"~Download latest files", 
    -command => \&DownloadFiles);
  $UpdateMenu->command(-label=>"~Reload options", 
    -command => sub{LoadOptions("$Files/options.txt");});

# Help menu
my $HelpMenu = $Menu->cascade(-label => "~Help");
  $HelpMenu->command(-label=>"~Web page", 
    -command => sub{BrowseTo($WikiPage);});

$Window->configure(-menu => $Menu);
# tst change

#-----------------------------------------------------------------------------
# Create rest of window
#-----------------------------------------------------------------------------
my $LeftFrame = $Window->Frame()->pack(-expand=>0,-fill=>'y',-side=>"left");
my $ControlFrame = $LeftFrame->Frame(-borderwidth=>2,-relief=>'ridge')->pack(-side=>'top');
my $ListFrame = $LeftFrame->Scrolled('Pane', -scrollbars => 'e',-borderwidth=>2,-relief=>'ridge')->pack(-side=>'bottom', -expand=>1,-fill=>'both');

$ControlFrame->Entry(-textvariable=>\$Lat1)->pack(-side=>'top');
$ControlFrame->Entry(-textvariable=>\$Lat2)->pack(-side=>'top');
$ControlFrame->Entry(-textvariable=>\$Long1)->pack(-side=>'top');
$ControlFrame->Entry(-textvariable=>\$Long2)->pack(-side=>'top');
$ControlFrame->Button(-text=>"Download", -command=>\&DownloadData)->pack(-side=>'top');

$ControlFrame->Entry()->pack(-side=>'top');
$ControlFrame->Button(-text => 'Render', -command => \&Render)->pack(-side=>'left');
$ControlFrame->Message(-textvariable => \$Status)->pack(-side=>'left', -expand=>1, -fill=>'x');

my $RightFrame = $Window->Frame(-borderwidth=>2,-relief=>'ridge')->pack(-expand=>1,-fill=>'both',-side=>"right");
my $List2 = $RightFrame->Scrolled('Pane', -scrollbars => 'se',-relief=>'ridge')->pack(-expand=>1,-fill=>'both');

# Create image
my $Image = $Window->Photo(-format=>"GIF");
my $Canvas = $List2->Canvas(-width => $Image->width,
        -height => $Image->height)->pack(-expand=>1, -fill=>'both');
my $TextView = $List2->Text()->pack(-expand=>1, -fill=>'both');

my $IID = $Canvas->createImage(0, 0,
        -anchor => 'nw',
        -image  => $Image);
    
AddDemoControls($ListFrame) if(0);
MainLoop;

#-----------------------------------------------------------------------------
# Refresh the list of projects in the file menu
#-----------------------------------------------------------------------------
sub RefreshListProjects(){
  my ($Menu) = @_;
  $ProjectMenu->cget(-menu)->delete(0, "end");
  ListProjects($Menu);
}
sub ListProjects(){
  my ($Menu) = @_;
  opendir(DIR, $Files) || return;
  while(my $File = readdir(DIR)){
    my $FullFile = "$Files/$File";
    if(-d $FullFile && $File ne "." && $File ne ".."){
      $ProjectMenu->command(
          -label => "Proj " . $File, 
          -command => sub{$ProjectName = $File; WithProject("load");});
    }
  }
  closedir(DIR);
}

#-----------------------------------------------------------------------------
# Dialog box allowing project files to be saved in particular place
#-----------------------------------------------------------------------------
sub SaveProjectDialog(){
  my $SaveProject;
  my $Dialog = $Window->Toplevel(-title=>"Save project");
  $Dialog->Message(-text=>"Enter a project name")->pack();
  $Dialog->Entry(-textvariable=>\$SaveProject)->pack();
  $Dialog->Button(-text=>"Cancel", -command=>sub{$Dialog->withdraw()})->pack(-side=>'left');
  $Dialog->Button(-text=>"OK", -command=>sub{$ProjectName = $SaveProject; WithProject("save"); $Dialog->withdraw()})->pack(-side=>'right');
}

#-----------------------------------------------------------------------------
# Load a file, storing it in the specified location
#-----------------------------------------------------------------------------
sub ReplaceFile(){
  my ($ToFile, $Ext) = @_;
  my @FileTypes = (["$Ext files", "*.$Ext"], ["All Files", "*"] );
  
  my $FromFile = $Window->getOpenFile(-filetypes => \@FileTypes);
}

#-----------------------------------------------------------------------------
# Do something with a "project" (group of data files)
# - load (from project directory into Files/ directory)
# - save (into project directory)
# - delete project directory
#-----------------------------------------------------------------------------
sub WithProject(){
  my $Mode = shift();
  return if(!$ProjectName);
  
  my $ProjectDir = "$Files/$ProjectName";
  mkdir($ProjectDir) if(! -d $ProjectDir && $Mode eq "save");
  
  foreach ("data.osm","output.svg","output.png","output.jpg","output.gif"){
    if($Mode eq "save"){
      copy("$Files/$_","$ProjectDir/$_");
    }
    elsif($Mode eq "load"){
      copy("$ProjectDir/$_", "$Files/$_");
    }
    elsif($Mode eq "delete"){
      unlink "$ProjectDir/$_";
    }
  }
  unlink $ProjectDir if($Mode eq "delete");
}

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

#-----------------------------------------------------------------------------
# Downloads OSM data from web
#-----------------------------------------------------------------------------
sub DownloadData(){
  if(!$CoordsValid){
    $Status = "Enter some coordinates, or load a bookmark";
    return;
    }

  PrintBig("Downloading $Title ($Lat1,$Long1,$Lat2,$Long2)");
    
  $Options{"username"} =~ s/\@/%40/;
  
  my $URL = sprintf( "http://%s:%s\@www.openstreetmap.org/api/0.3/map?bbox=$Long1,$Lat1,$Long2,$Lat2",
  $Options{"username"},
  $Options{"password"});
  
  $Status = "Downloading\n";
  getstore($URL, "$Files/data.osm");
  $Status = "Download complete\n";
}

#-----------------------------------------------------------------------------
# Renders a map
#-----------------------------------------------------------------------------
sub Render(){
  if($Options{"XmlStarlet"}){
    my $Cmd = sprintf("\"%s\" tr %s %s > %s",
      $Options{"XmlStarlet"},
      "$Files/osmarender.xsl",
      "$Files/osm-map-features.xml",
      "$Files/output.svg");
    `$Cmd`;
    $Status = "Render complete";
  }
  else {
    $Status = "No XLST program (e.g. xmlstarlet) available";
    return;
  }
  
  if($Options{"Inkscape"}){
    my $Cmd = sprintf("\"%s\" -D -w %d -b FFFFFF -e %s %s",
      $Options{"Inkscape"},
      $Options{"RenderWidth"},
      "$Files/output.png",
      "$Files/output.svg");
    
    PrintBig($Cmd);  
    `$Cmd`;
  }
  else{
    $Status = "No SVG renderer available";
    return;
  }
  ConvertOutputs();
}
#-----------------------------------------------------------------------------
# Convert between bitmap graphics formats
#-----------------------------------------------------------------------------
sub ConvertOutputs(){
  return if(!$Options{"ImageMagick"});
  
  foreach my $Output("$Files/output.png","$Files/output.gif"){
    my $Cmd = sprintf("\"%s\" %s %s",
      $Options{"ImageMagick"},
      "$Files/output.png",
      $Output);
    
    `$Cmd`;
  }
  
  SetView("image","$Files/output.gif");
}

#-----------------------------------------------------------------------------
# Set what's displayed in the right-hand pane
#-----------------------------------------------------------------------------
sub SetView(){
  my ($Style, $Filename) = @_;
  if($Style eq "text"){
    print "Loading text file $Filename\n";

    if(open(IN, "<",$Filename)){
      $DisplayText = join('', <IN>);
      close IN;
    }
    else{
      $DisplayText = "File not available:\n$Filename\n";
    }

    $TextView->delete('1.0','end');
    $TextView->insert("end", $DisplayText);

    $Canvas->pack('forget');
    $TextView->pack();
  }
  elsif($Style eq "image"){
    print "Loading image $Filename\n";
    $Canvas->pack();
    
    $Image->configure(-file=>$Filename);

    $Canvas->configure(-width => $Image->width);
    $Canvas->configure(-height => $Image->height);
    
    $TextView->pack('forget');
  }
  else{
    print STDERR "Unknown type of object to display";
  }
}

#-----------------------------------------------------------------------------
# Open a file using external editor
#  * param1 is what program to use, and is a reference to something in %Options
#  * param2 is the filename to open
#-----------------------------------------------------------------------------
sub OpenFile("ImageEditor", "$Files/output.png"){
  my ($ProgramType, $Filename) = @_;
  my $Program = $Options{$ProgramType};
  if(!$Program){
    print STDERR "Can't find a $ProgramType to open $Filename\n";
    return;
  }
  my $Cmd = sprintf("\"%s\" \"%s\"", $Program, $Filename);
  PrintBig($Cmd);
  `$Cmd`;
}

#-----------------------------------------------------------------------------
# Create a submenu showing all bookmarks loaded from a file, where clicking on
# any menu loads its coordinates into appropriate places
#-----------------------------------------------------------------------------
sub BookmarkMenu(){
  my ($Menu, $File) = @_;
  return if(!$File);
  
  open(IN, $File) || return;
  my $Data = join('', <IN>);
  close IN;
  $Data = $1 if($Data =~ /START_DATA(.*)END_DATA/ms);

  foreach my $Line(split(/\n/, $Data)){
    my ($Name, $Lat1, $Long1, $Lat2, $Long2, $Spare) = split(/,/,$Line);
    $Menu->command(-label=> $Name, -command => sub{SetData($Name,$Lat1, $Long1, $Lat2, $Long2);}) if($Name && $Spare eq "false");
  }

  $Menu->separator();
  $Menu->command(-label=> "Edit this list (web link)", 
    -command => sub{BrowseTo("http://wiki.openstreetmap.org/index.php/OJW/Bookmarks")});
}

#-----------------------------------------------------------------------------
# Opens a web browser to display the specified URL
#-----------------------------------------------------------------------------
sub BrowseTo(){
  my $URL = shift();
  my $Browser = $Options{"Browser"};
  `\"$Browser\" $URL` if($Browser);
}

#-----------------------------------------------------------------------------
# Sets-up the application, first time it's run
#-----------------------------------------------------------------------------
sub FirstTime(){
  print "Welcome to $NAME\n\nThis looks like the first time you've run this program.\nIs it okay to create a directory in $Files, and download some data from the web? (y/n)\n";
  exit if(<> =~ /N/i);
  mkdir($Files);
    
  
  # Get username and password
  print "\n\nYou now have the opportunity to enter an OSM username and password.\n\nThese will appear on your screen, so check nobody is watching!\nThese will be stored in plaintext on disk\n\nIf you don't want to enter a password yet, just press enter twice,\nthen edit $Files/options.txt later\n";
  print "Username:"; $Options{"username"} = <>;
  print "Password:"; $Options{"password"} = <>;
  chomp($Options{"username"}, $Options{"password"});
  print "\n";
  
  LookFor("Browser", 
    "C:\\Program Files\\Mozilla Firefox\\firefox.exe", 
    "C:\\windows\\iexplore.exe", 
    "C:\\winnt\\iexplore.exe", 
    "C:\\windows\\iexplore.exe", 
	"C:/Program Files/Internet Explorer/iexplore.exe",
    "/usr/bin/firefox");
  
  LookFor("XmlStarlet", 
    "C:\\xml\\xml.exe", 
    "/usr/bin/xmlstarlet");
  
  LookFor("Inkscape", 
    "C:\\Program Files\\inkscape\\inkscape.exe", 
    "/usr/bin/inkscape");
    
  LookFor("ImageMagick", 
    "/usr/bin/convert");
  
  LookFor("JosmBookmarks", 
    "~/.josm/Bookmarks",
	"C:/Documents and Settings/oliverwhite.CUESIM/.josm/bookmarks");

  LookFor("JOSM", 
    "C:/home/osm/josm-latest.jar");
  
  LookFor("Editor", 
    "c:/windows/notepad.exe",
    "c:/winnt/notepad.exe",
	"/usr/bin/kate",
	"/usr/bin/emacs");
	
  LookFor("ImageEditor", 
    "C:/Program Files/GIMP-2.0/bin/gimp-win-remote.exe",
	"/usr/bin/gimp",
	"/usr/bin/gqview");

  SaveOptions("$Files/options.txt");
  
  # Download files for the first time
  PrintBig("Downloading latest osmarender");
  DownloadFiles();  
  
}

#-----------------------------------------------------------------------------
# Save all options to disk
#-----------------------------------------------------------------------------
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

#-----------------------------------------------------------------------------
# Try to find a particular program
# Usage: LookFor("SettingName", "PossibleLocation1", "PossibleLocation2",...)
#-----------------------------------------------------------------------------
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

#-----------------------------------------------------------------------------
# Load all options from disk
# as name=value pairs (one per line)
#-----------------------------------------------------------------------------
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
# Temporary: fill the left column with dummy controls to see what it looks like
#-----------------------------------------------------------------------------
sub AddDemoControls(){
  my($Frame) = @_;
  
  foreach (1..60){
    my $ItemFrame = $Frame->Frame()->pack(-expand=>1, -fill=>'x');
    $ItemFrame->Checkbutton(-text => "TODO $_")->pack(-side=>'top')->deselect();
  }
}

#-----------------------------------------------------------------------------
# Sets the download area (used when clicking on bookmark menu)
#-----------------------------------------------------------------------------
sub SetData(){
  ($Title,$Lat1,$Long1,$Lat2,$Long2) = @_;
  $CoordsValid = 1;
}


#-----------------------------------------------------------------------------
# Prints a message in big letters on console
#-----------------------------------------------------------------------------
sub PrintBig(){
    print STDERR "=" x 80 . "\n" . shift() . "\n" . "=" x 80 . "\n";
}