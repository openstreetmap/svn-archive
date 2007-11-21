use strict; 

#--------------------------------------------------------------------------
# Reads a tiles@home config file, returns a hash array
#--------------------------------------------------------------------------
sub ReadConfig
{
    my %Config;
    while (my $Filename = shift())
    {

        open(my $fp,"<$Filename") || die("Can't open \"$Filename\" ($!)\n");
        while(my $Line = <$fp>)
        {
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
                        }x)
            {
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
sub ApplyConfigLogic
{
    my $Config = shift();

    $Config->{"OsmUsername"} =~ s/@/%40/;  # Encode the @-symbol in OSM passwords
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

        if (!defined($Config->{"Layer.$layer.Transparent"}))
        {
            $Config->{"Layer.$layer.Transparent"} = 0;
        }

        if (!defined($Config->{"Layer.$layer.RenderFullTileset"}))
        {
            $Config->{"Layer.$layer.RenderFullTileset"} = 0;
        }
    }

    ## switch on verbose mode if Debug is set
    if ($Config->{"Debug"})
    {
        $Config->{"Verbose"} = 1;
    }

    ## check for Pngcrush config option and set to default if not found
    $Config->{"Pngcrush"} = "pngcrush" unless defined($Config->{"Pngcrush"});

    ## do the same for Zip
    $Config->{"Zip"} = "zip" unless defined($Config->{"Zip"});

    if (($Config->{"WorkingDirectory"} !~ /\/$/) and ("MSWin32" ne $^O))
    {
        $Config->{"WorkingDirectory"} = $Config->{"WorkingDirectory"} . "/";
    }
    elsif (($Config->{"WorkingDirectory"} !~ /\\$/) and ("MSWin32" eq $^O))
    {
        $Config->{"WorkingDirectory"} = $Config->{"WorkingDirectory"} . "\\";
    }
    
    ## Set defaults for Batik options
    $Config->{"Batik"} = "0" unless defined($Config->{"Batik"});
    $Config->{"BatikJVMSize"} = "1300M" unless defined($Config->{"BatikJVMSize"});
    $Config->{"BatikPath"} = "batik-rasterizer.jar" unless defined($Config->{"BatikPath"});
    
    ## Set default download timeout to 3 minutes
    $Config->{"DownloadTimeout"} = "1800" unless defined($Config->{"DownloadTimeout"});
}

#--------------------------------------------------------------------------
# Checks a tiles@home configuration
#--------------------------------------------------------------------------
sub CheckConfig
{
    my $Config = shift();
    my %EnvironmentInfo;

    printf "- Using working directory %s\n", $Config->{"WorkingDirectory"};

    if ($Config->{Batik})
    {
        print "- Using Batik";
        if ($Config->{Batik} == 1)
        {
            print " in jar mode";
        }
        if ($Config->{Batik} == 2)
        {
            print " in wrapper mode";
        }
        print "\n";
    }
    else
    {
        # Inkscape version
        my $InkscapeV = `$Config->{Inkscape} --version`;
        $EnvironmentInfo{Inkscape}=$InkscapeV;

        if($InkscapeV !~ /Inkscape (\d+)\.(\d+\.?\d*)/)
        {
            die("Can't find inkscape (using \"$Config->{Inkscape}\")\n");
        }
    
        if($2 < 42.0){
            die("This version of inkscape ($1.$2) is known not to work with tiles\@home\n");
        }
        if($2 < 45.1){
            print "* Please upgrade to inkscape 0.45.1 due to security problems with your inkscape version:\n"
        }
        print "- Inkscape version $1.$2\n";
    }
    # XmlStarlet version
    my $XmlV = `$Config->{XmlStarlet} --version`;
    $EnvironmentInfo{Xml}=$XmlV;

    if($XmlV !~ /(\d+\.\d+\.\d+)/){
        die("Can't find xmlstarlet (using \"$Config->{XmlStarlet}\")\n");
    }
    print "- xmlstarlet version $1\n";

    # Zip version
    my $ZipV = `$Config->{Zip} -v`;
    $EnvironmentInfo{Zip}=$ZipV;

    if ($ZipV eq "") 
    {
        die("Can't find zip (using \"$Config->{Zip}\")\n");
    }
    print "- zip is present\n";

    # PNGCrush version
    my $PngcrushV = `$Config->{Pngcrush} -version`;
    $EnvironmentInfo{Pngcrush}=$PngcrushV;

    if ($PngcrushV !~ /[Pp]ngcrush\s+(\d+\.\d+\.?\d*)/) 
    {
        # die here if pngcrush shall be mandatory
        print "Can't find pngcrush (using \"$Config->{Pngcrush}\")\n";
    }
    print "- pngcrush version $1\n";

    if ($Config->{"LocalSlippymap"})
    {
        print "- Writing LOCAL slippy map directory hierarchy, no uploading\n";
    }
    else
    {
        # Upload URL, username
        printf "- Uploading with username \"$Config->{UploadUsername}\"\n", ;
        if($Config->{"UploadPassword"} =~ /\W/){
            die("Check your upload password\n");
        }

        if ($Config->{"UploadURL2"})
        {
            if(($Config->{"UploadURL"} ne $Config->{"UploadURL2"}) and ($Config->{"UploadURL"}))
            {
                print "! Please use only UploadURL in the config, this is the default setting";
            }
        }

        if( ($Config->{"UploadChunkSize"}*1024*1024) > ($Config->{"ZipHardLimit"}*1000*1000)){
            die("! Upload chunks may be too large for server\n");
        }

        if($Config->{"UploadChunkSize"} < 0.2){
            $Config->{"UploadChunkSize"} = 2;
            print "! Using default upload chunk size of 2.0 MB\n";
        }

        # $Config->{"UploadURL2"};

        if($Config->{"DeleteZipFilesAfterUpload"}){
            print "- Deleting ZIP files after upload\n";
        }
    }

    if($Config->{"RequestUrl"}){
        print "- Using $Config->{RequestUrl} for Requests\n";
    }

    # OSM username
    if (defined($Config->{"OsmUsername"}))
    {
        print "- Using OSM username \"$Config->{OsmUsername}\"\n";
        print "You have set your OSM username in tilesAtHome.conf or authentication.conf. This is no longer necessary, because from the 0.4 API on the API allows map requests without login. It is recommended you remove the option \"OsmUsername\" from the config files\n";
    }
    #if($Config->{OsmUsername} !~ /%40/){
    #    die("OsmUsername should be an email address, with the \@ replaced by %40\n");
    #}

    if (defined($Config->{"OsmPassword"}))
    {
        print "You have set your OSM password in tilesAtHome.conf or authentication.conf. It is recommended you remove the option \"OsmPassword\" from the config files\n";

    }

    # Misc stuff
    foreach(qw(N S E W)){
        if($Config->{"Border$_"} > 0.5){
            printf "Border$_ looks abnormally large\n";
        }
    }

    if (defined($Config->{"RenderFullTileset"}))
    {
        die "You have the RenderFullTileset option mentioned in one of your config files. This option is no longer supported and has been replaced by a layer-specific option of the same name set in layers.conf. Please remove RenderFullTileset to avoid confusion - ";
    }

    ## not used any longer, superseded by per-layer value
    #if($Config->{"MaxZoom"} < 12 || $Config->{"MaxZoom"} > 20){
    #    print "Check MaxZoom\n";
    #}

    # layers
    foreach my $layer(split(/,/, $Config->{"Layers"}))
    {
        print "- Configured Layer: $layer\n";

        if ($Config->{"Layer.$layer.MaxZoom"} < 12 || $Config->{"Layer.$layer.MaxZoom"} > 20) 
        {
            print "Check Layer.$layer.MaxZoom\n";
        }

        for(my $zoom=12; $zoom<=$Config->{"Layer.$layer.MaxZoom"}; $zoom++)
        {
            if (!defined($Config->{"Layer.$layer.Rules.$zoom"}))
            {
                die "config option Layer.$layer.Rules.$zoom is not set";
            }
            if (!-f $Config->{"Layer.$layer.Rules.$zoom"})
            {
                die "rules file ".$Config->{"Layer.$layer.Rules.$zoom"}.
                    " referenced by config option Layer.$layer.Rules.$zoom ".
                    "is not present";
            }
        }

        if (!defined($Config->{"Layer.$layer.Prefix"}))
        {
            die "config option Layer.$layer.Prefix is not set";
        }

        # any combination of comma-separated preprocessor names is allowed
        die "config option Layer.$layer.Preprocessor has invalid value" 
            if (grep { $_ !~ /frollo|maplint|close-areas|mercator|attribution/} split(/,/, $Config->{"Layer.$layer.Preprocessor"}));

        foreach my $reqfile(split(/,/, $Config->{"Layer.$layer.RequiredFiles"}))
        {
            die "file $reqfile required for layer $layer as per config option ".
                "Layer.$layer.RequiredFiles not found" unless (-f $reqfile);
        }

    }

    return %EnvironmentInfo;

}

1;
