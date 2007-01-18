#!/usr/bin/perl
use File::Copy;

#----------------------------------------------------------------
# Packages KML files and their associated icons into zipped KMZ files
#
# Oliver White, 2007, GNU GPL v2 or later
#----------------------------------------------------------------

for($i = 1; 1; $i++){

  # Icons etc.
  $FilesDir = "osmgoogleearth";
  
  # Input
  $KML = "output/$i.kml";
  
  # Place for ZIP to work
  $TempDir = "temp";
  
  # Output
  $Output = "output_packaged/$i.kmz";


  # No more files
  exit if(! -f $KML);
  
  # Move all relevant files to a temporary directory, and ZIP them
  mkdir $TempDir;
  copy($KML, "$TempDir/doc.kml");
  mkdir "$TempDir/icons";
  `cp $FilesDir/icons/*.png $TempDir/icons`;
  `cp $FilesDir/*.png $TempDir/`;
  `zip -R $Output $TempDir/*`;
  
  print "Done $Output\n";
  
}