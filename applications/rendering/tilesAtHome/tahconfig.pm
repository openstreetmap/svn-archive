use strict; 

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
                        ^
                        \s*
                        ([A-Za-z0-9._-]+) # Keyword: just one single word no spaces
                        \s*            # Optional whitespace
                        =              # Equals
                        \s*            # Optional whitespace
                        (.*)           # Value
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
    if (!defined($Config->{"Layers"}))
    {
        $Config->{"Layers"} = "default";
    }

    # check layer configuration and if not present, use sensible defaults
    foreach my $layer(split(/,/, $Config->{"Layers"}))
    {
        $Config->{"Layer.$layer.MaxZoom"} = 17 unless defined($Config->{"Layer.$layer.MaxZoom"});

        for(my $zoom=12; $zoom<=$Config->{"Layer.$layer.MaxZoom"}; $zoom++)
        {
            if (!defined($Config->{"Layer.$layer.Rules.$zoom"}))
            {
                if ($layer eq "default")
                {
                    $Config->{"Layer.$layer.Rules.$zoom"} = "osm-map-features-z$zoom.xml";
                }
            }
        }

        if (!defined($Config->{"Layer.$layer.Prefix"}))
        {
            if($layer eq "default")
            {
                $Config->{"Layer.$layer.Prefix"} = "tile";
            }
        }

        if (!defined($Config->{"Layer.$layer.Preprocessor"}))
        {
            if($layer eq "default")
            {
                $Config->{"Layer.$layer.Preprocessor"} = "frollo";
            }
        }
    }
}

#--------------------------------------------------------------------------
# Checks a tiles@home configuration
#--------------------------------------------------------------------------
sub CheckConfig{
    my %Config = @_;
    my %EnvironmentInfo;

    printf "- Using working directory %s\n", $Config{"WorkingDirectory"};

    # Inkscape version
    my $InkscapeV = `$Config{Inkscape} --version`;
    $EnvironmentInfo{Inkscape}=$InkscapeV;

    if($InkscapeV !~ /Inkscape (\d+)\.(\d+\.?\d*)/)
    {
        die("Can't find inkscape (using \"$Config{Inkscape}\")\n");
    }

    if($2 < 42.0){
        die("This version of inkscape ($1) is known not to work with tiles\@home\n");
    }
    if($2 < 45.1){
        print "* Please upgrade to inkscape 0.45.1 due to security problems with your inkscape version:\n"
    }
    print "- Inkscape version $1.$2\n";

    # XmlStarlet version
    my $XmlV = `$Config{XmlStarlet} --version`;
    $EnvironmentInfo{Xml}=$XmlV;

    if($XmlV !~ /(\d+\.\d+\.\d+)/){
        die("Can't find xmlstarlet (using \"$Config{XmlStarlet}\")\n");
    }
    print "- xmlstarlet version $1\n";

    # Zip version
    $Config{Zip} = "zip" unless defined($Config{Zip});
    my $ZipV = `$Config{Zip} -v`;
    $EnvironmentInfo{Zip}=$ZipV;

    if ($ZipV eq "") 
    {
        die("Can't find zip (using \"$Config{Zip}\")\n");
    }
    print "- zip is present\n";

    # PNGCrush version
    $Config{Pngcrush} = "pngcrush" unless defined($Config{Pngcrush});
    my $PngcrushV = `$Config{Pngcrush} -version`;
    $EnvironmentInfo{Pngcrush}=$PngcrushV;

    if ($PngcrushV !~ /[Pp]ngcrush\s+(\d+\.\d+\.?\d*)/) 
    {
        # die here if pngcrush shall be mandatory
        print "Can't find pngcrush (using \"$Config{Pngcrush}\")\n";
    }
    print "- pngcrush version $1\n";

    if ($Config{"LocalSlippymap"})
    {
        print "- Writing LOCAL slippy map directory hierarchy, no uploading\n";
    }
    else
    {
        # Upload URL, username
        printf "- Uploading with username \"$Config{UploadUsername}\"\n", ;
        if($Config{"UploadPassword"} =~ /\W/){
            die("Check your upload password\n");
        }

        if($Config{"UploadURL"} ne $Config{"UploadURL2"})
        {
            printf "! Please set UploadURL to %s, this will become the default ".
                "UploadURL soon\n", $Config{"UploadURL2"};
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
        if($Config{"Border$_"} > 0.2){
            printf "Border$_ looks abnormally large\n";
        }
    }

    # not used any longer, superseded by per-layer value
    if($Config{"MaxZoom"} < 12 || $Config{"MaxZoom"} > 20){
        print "Check MaxZoom\n";
    }

    # layers
    foreach my $layer(split(/,/, $Config{"Layers"}))
    {
        print "- Configured Layer: $layer\n";

        if ($Config{"Layer.$layer.MaxZoom"} < 12 || $Config{"Layer.$layer.MaxZoom"} > 20) 
        {
            print "Check Layer.$layer.MaxZoom\n";
        }

        for(my $zoom=12; $zoom<=$Config{"Layer.$layer.MaxZoom"}; $zoom++)
        {
            if (!defined($Config{"Layer.$layer.Rules.$zoom"}))
            {
                die "config option Layer.$layer.Rules.$zoom is not set";
            }
            if (!-f $Config{"Layer.$layer.Rules.$zoom"})
            {
                die "rules file ".$Config{"Layer.$layer.Rules.$zoom"}.
                    " referenced by config option Layer.$layer.Rules.$zoom ".
                    "is not present";
            }
        }

        if (!defined($Config{"Layer.$layer.Prefix"}))
        {
            die "config option Layer.$layer.Prefix is not set";
        }

        # any combination of comma-separated preprocessor names is allowed
        die "config option Layer.$layer.Preprocessor has invalid value" 
            if (grep { $_ !~ /frollo|maplint|close-areas/} split(/,/, $Config{"Layer.$layer.Preprocessor"}));

        foreach my $reqfile(split(/,/, $Config{"Layer.$layer.RequiredFiles"}))
        {
            die "file $reqfile required for layer $layer as per config option ".
                "Layer.$layer.RequiredFiles not found" unless (-f $reqfile);
        }

    }

    return %EnvironmentInfo;

}

1;
