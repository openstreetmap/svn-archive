#!/usr/bin/perl
use File::Copy;

#----------------------------------------------------------------
# Packages KML files and their associated icons into zipped KMZ files
#
# Oliver White, 2007, GNU GPL v2 or later
#----------------------------------------------------------------

$KmlDir = "output";
opendir(DIR, $KmlDir) || die("Can't read directory $KmlDir ($!)  have you created KML files? (use run.pl)\n");

while($File = readdir(DIR)){
  
  $KML = "$KmlDir/$File";
  if($File =~ /(\d+)\.kml/){
    $i = $1;
    
    # Icons etc.
    $FilesDir = "osmgoogleearth";
    
    # Place for ZIP to work
    $TempDir = "temp";
    
    # Output
    $Output = "output_packaged/$i.kmz";
    
    # Move all relevant files to a temporary directory, and ZIP them
    mkdir $TempDir;
    copy($KML, "$TempDir/doc.kml");
    
    if(0){ # Sorry, zip is too crap...
      mkdir "$TempDir/icons";
      `cp $FilesDir/icons/*.png $TempDir/icons`;
    }
    
    `cp $FilesDir/*.png $TempDir/`;
    
    `zip -r -j $Output $TempDir/*`;
    
    print "Done $Output\n";
    
  }
}