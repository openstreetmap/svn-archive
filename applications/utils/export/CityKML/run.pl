#!/usr/bin/perl
use File::Copy;

#----------------------------------------------------------------
# Convert city OSM files (from http://almien.co.uk/OSM/Places) 
# to KML files, using the OsmGoogleEarth converter
#
# Oliver White, 2007, GNU GPL v2 or later
#----------------------------------------------------------------

# Search for files to convert
$DataDir = "data";
opendir(DIR, $DataDir) || die("Can't read data directory ($!) - has any data been downloaded?");

while($File = readdir(DIR)){
  
  $GZip = "$DataDir/$File";
  if($File =~ /data_(\d+)\.osm\.gz/){
    $i = $1;
    $TempDir = "osmgoogleearth";
    $Data = "$TempDir/data.osm";
    $DataG = "$Data.gz";
    $Output = "output/$i.kml";
    
    # If input file exists, but output doesn't
    if((-f $GZip && ! -f $Output) || (-s $Output == 0)){
      # Transform OSM to KML
      copy($GZip, $DataG);
      `gunzip $DataG`;
      `xmlstarlet tr $TempDir/osm2kml.xsl $TempDir/osm2kml.xml > $Output`;
      unlink($Data);
    }
  }
}

