


#--------------------------------------------------------------------------
# Reads a tiles@home config file, returns a hash array
#--------------------------------------------------------------------------
sub ReadConfig{
  my %Config;
  while (my $Filename = shift()){
  
    open(my $fp,"<$Filename") || die("Can't open \"$Filename\" ($!)\n");
    while(my $Line = <$fp>){
      $Line =~ s/#.*$//; # Comments
      $Line =~ s/\s*$//; # Trailing whitespace
      
      if($Line =~ m{
        (\w+)         # Keyword: just one single word no spaces
        \s*           # Optional whitespace
        =             # Equals
        \s*           # Optional whitespace
        (.*)          # Value
        }x){
        
        # Store config options in a hash array
        $Config{$1} = $2;
        print "Found $1 ($2)\n" if(0); # debug option
        }
    }
    close $fp;

  }
  ApplyConfigLogic(\%Config);
  
  return %Config;
}

#--------------------------------------------------------------------------
# Any application-specific knowledge regarding config file options
# e.g. correct common errors in config files, or enforce naming conventions
#--------------------------------------------------------------------------
sub ApplyConfigLogic{
  my $Config = shift();

  $Config->{OsmUsername} =~ s/@/%40/;  # Encode the @-symbol in OSM passwords
}

#--------------------------------------------------------------------------
# Checks a tiles@home configuration
#--------------------------------------------------------------------------
sub CheckConfig{
  my %Config = @_;
  
  printf "- Using working directory %s\n", $Config{"WorkingDirectory"};
  
  # Inkscape version
  $InkscapeV = `$Config{Inkscape} --version`;
  if($InkscapeV !~ /Inkscape (\d+\.\d+)/){
    die("Can't find inkscape (using \"$Config{Inkscape}\")\n");
  }
  if($1 < 0.42){
    die("This version of inkscape ($1) is known not to work with tiles\@home\n");
  }
  print "- Inkscape version $1\n";

  # XmlStarlet version
  $XmlV = `$Config{XmlStarlet} --version`;
  if($XmlV !~ /(\d+\.\d+\.\d+)/){
    die("Can't find xmlstarlet (using \"$Config{XmlStarlet}\")\n");
  }
  print "- xmlstarlet version $1\n";
  
  # Upload URL, username
  printf "- Uploading with username \"$Config{UploadUsername}\"\n", ;
  if($Config{"UploadPassword"} =~ /\W/){
    die("Check your upload password\n");
  }

  if($Config{"UploadURL"} ne $Config{"UploadURL2"}){
    printf "! Please set UploadURL to %s, this will become the default UploadURL soon\n", $Config{"UploadURL2"};
  } 
  if($Config{"UploadChunkSize"} > 2){
    print "! Upload chunks may be too large for server\n";
  }
  
  if($Config{"UploadChunkSize"} < 0.1){
    $Config{"UploadChunkSize"} = 1;
    print "! Using default upload chunk size of 1.0 MB\n";
  }
  
  # $Config{"UploadURL2"};

  if($Config{"DeleteZipFilesAfterUpload"}){
    print "- Deleting ZIP files after upload\n";
  }

  if($Config{"RequestUrl"}){
    print "- Using $Config{RequestUrl} for Requests\n";
  }

  # OSM username
  if($Config{OsmUsername} !~ /%40/){
    die("OsmUsername should be an email address, with the \@ replaced by %40\n");
  }
  print "- Using OSM username \"$Config{OsmUsername}\"\n";

  # $Config{"OsmPassword"};
  
  # Misc stuff
  foreach(qw(N S E W)){
    if($Config{"Border$_"} > 0.5){
      printf "Border$_ looks abnormally large\n";
    }
  }

  if($Config{"MaxZoom"} < 12 || $Config{"MaxZoom"} > 20){
    print "Check MaxZoom\n";
  }

}

1
