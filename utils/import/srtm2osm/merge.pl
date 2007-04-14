#!/usr/bin/perl
#-----------------------------------------------------------------------------
# Set data source file name in map-features file
#-----------------------------------------------------------------------------

appendOSMfile($ARGV[0], $ARGV[1]);

sub appendOSMfile(){
  my ($Datafile,$Datafile1) = @_;
  
  # Strip the trailing </osm> from the datafile
  open(my $fpIn1, "<", "$Datafile");
  my $Data = join("",<$fpIn1>);
  close $fpIn1;
  die("no such $Datafile") if(! -f $Datafile);
    
  $Data =~ s/<\/osm>//s;

  #print
  print $Data;

  # Read the merge file remove the xml prolog and opening <osm> tag and append to the datafile
  open(my $fpIn2, "<", "$Datafile1");
  my $discard = <$fpIn2>;
  $discard = <$fpIn2>;
  my $Data = join("",<$fpIn2>);
  close $fpIn2;
  die("no such $Datafile1") if(! -f $Datafile1);
    
  #$Data =~ s/.*server\">//s;

  # Append to the data file
  print $Data;
}

