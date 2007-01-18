#!/usr/bin/perl
use LWP::Simple;

# downloads one map then exits
$numfail = 0;
for($i = 0; 1; $i++){
  $URL = "http://almien.co.uk/OSM/Places/Data/$i.osm.gz";
  $File = "data/data_$i.osm.gz";
  
  # If we don't already have a file, download it
  if(!-f $File){
    if(is_success(getstore($URL,$File))){
      print "We've got file $i!\n";
      exit;
    }
  }
  
  # Stop if 20 consecutive numbered items don't exist
  die("Finished\n") if($numfail++ >20);
}