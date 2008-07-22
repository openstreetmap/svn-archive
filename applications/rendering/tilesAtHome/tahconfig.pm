use strict;

#--------------------------------------------------------------------------
# Any application-specific knowledge regarding config file options
# e.g. correct common errors in config files, or enforce naming conventions
#--------------------------------------------------------------------------
sub ApplyConfigLogic
{
    my $Config = $main::Config;

    if (!defined($Config->get("Layers")))
    {
        die("no layers configured");
    }

    # check layer configuration and if not present, use sensible defaults
    foreach my $layer(split(/,/, $Config->get("Layers")))
    {

        if (!defined($Config->get($layer."_Prefix")))
        {
            die($layer.": Prefix not configured");
        }

        if (!defined($Config->get($layer."_Preprocessor")))
        {
            die($layer.": Preprocessor not configured");
        }

        if (!defined($Config->get($layer."_Transparent")))
        {
            die($layer.": Transparent not configured");
        }

        if (!defined($Config->get($layer."_RenderFullTileset")))
        {
            die($layer.": RenderFullTileset not configured");
        }
    }

    ## switch on verbose mode if Debug is set
    if ($Config->get("Debug"))
    {
        $Config->set("Verbose",1);
    }

    if ($Config->get("WorkingDirectory"))
    {
        $Config->set("WorkingDirectory",File::Spec->rel2abs($Config->get("WorkingDirectory")));
    }

    if (($Config->get("WorkingDirectory") !~ /\/$/) and ("MSWin32" ne $^O))
    {
        $Config->set("WorkingDirectory",$Config->get("WorkingDirectory") . "/");
    }
    elsif (($Config->get("WorkingDirectory") !~ /\\$/) and ("MSWin32" eq $^O))
    {
        $Config->set("WorkingDirectory",$Config->get("WorkingDirectory") . "\\");
    }

}

#--------------------------------------------------------------------------
# Checks a tiles@home basic configuration
#--------------------------------------------------------------------------
sub CheckBasicConfig
{
    my $Config = shift();
    my %EnvironmentInfo;
    my $cmd;
    printf "- Using working directory %s\n", $Config->get("WorkingDirectory");

    printf "- Using process log file %s\n", $Config->get("ProcessLogFile") if ($Config->get("ProcessLog"));

    if ($Config->get("Subversion"))
    {
        $cmd=$Config->get("Subversion");
        my $SubversionV = `\"$cmd\" --version`;
        $EnvironmentInfo{"Subversion"}=$SubversionV;
        # die here if svn executable not found, but before enabling try on windows machines wether --version works.
    }
    else
    {
        die ("! no subversion command set");
    }

    # LocalSplippymap
    if ($Config->get("LocalSlippymap"))
    {
        print "- Writing LOCAL slippy map directory hierarchy, no uploading\n";
    }
    else
    {
        # Upload URL, username
        printf "- Uploading with username \"".$Config->get("UploadUsername")."\"\n", ;
        my $pw = $Config->get("UploadPassword");
        if($pw =~ /\W/){
            die("Check your upload password\n");
        }

        if($Config->get("UploadChunkSize") < 0.2){
            $Config->get("UploadChunkSize") = 2;
            print "! Using default upload chunk size of 2.0 MB\n";
        }

        if($Config->get("DeleteZipFilesAfterUpload")){
            print "- Deleting ZIP files after upload\n";
        }
    }

    if ($Config->get("UploadToDirectory"))
    {
        if (! $Config->get("UseHostnameInZipname")) 
        {
            print " * UseHostnameInZipname should be set when using UploadToDirectory\n";
        }
    }

    # layers
    foreach my $layer(split(/,/, $Config->get("Layers")))
    {
        print "- Configured Layer: $layer\n";

        if ($Config->get($layer."_MaxZoom") < 12 || $Config->get($layer."_MaxZoom") > 20) 
        {
            print "Check $layer._MaxZoom\n";
        }

        for(my $zoom=12; $zoom<=$Config->get($layer."_MaxZoom"); $zoom++)
        {
            if (!defined($Config->get($layer."_Rules.$zoom")))
            {
                die "config option $layer._Rules.$zoom is not set";
            }
            if (!-f $Config->get($layer."_Rules.$zoom"))
            {
                die "rules file ".$Config->get($layer."_Rules.$zoom").
                    " referenced by config option $layer._Rules.$zoom ".
                    "is not present";
            }
        }

        if (!defined($Config->get($layer."_Prefix")))
        {
            die "config option $layer._Prefix is not set";
        }

        # any combination of comma-separated preprocessor names is allowed
        die "config option $layer._Preprocessor has invalid value" 
            if (grep { $_ !~ /frollo|maplint|close-areas|mercator|attribution|autocut/} split(/,/, $Config->get($layer."_Preprocessor")));

        foreach my $reqfile(split(/,/, $Config->get($layer."_RequiredFiles")))
        {
            die "file $reqfile required for layer $layer as per config option ".
                $layer."_RequiredFiles not found" unless (-f $reqfile);
        }

    }
    print "* UploadConfiguredLayersOnly not set. \n  Defaulting to uploading all zipfiles found, not just configured layers\n" unless defined($Config->get("UploadConfiguredLayersOnly"));

    # Zip version
    $cmd=$Config->get("Zip");
    my $ZipV = `\"$cmd\" -v`;
    $EnvironmentInfo{Zip}=$ZipV;

    return %EnvironmentInfo;

}

#--------------------------------------------------------------------------
# Checks a tiles@home configuration
#--------------------------------------------------------------------------
sub CheckConfig
{
    my $Config = shift();
    my $cmd;

    my %EnvironmentInfo = CheckBasicConfig($Config);

    if ($Config->get("Batik"))
    {
        print "- Using Batik";
        if ($Config->get("Batik") == 1)
        {
            print " in jar mode";
        }
        if ($Config->get("Batik") == 2)
        {
            print " in wrapper mode";
        }
        if ($Config->get("Batik") == 3)
        {
            print " in agent mode";
        }
        print "\n";
    }
    else
    {
        # Inkscape version
        $cmd=$Config->get("Inkscape");
        my $InkscapeV = `\"$cmd\" --version`;
        $EnvironmentInfo{"Inkscape"}=$InkscapeV;
 
        if($InkscapeV !~ /Inkscape (\d+)\.(\d+\.?\d*)/)
        {
            die("Can't find inkscape (using \"".$Config->get("Inkscape")."\")\n");
        }
    
        if($2 < 42.0){
            die("This version of inkscape ($1.$2) is known not to work with tiles\@home\n");
        }
        if($2 < 45.1){
            print "* Please upgrade to inkscape 0.45.1 due to security problems with your inkscape version:\n"
        }
        print "- Inkscape version $1.$2\n";
    }

    # Rendering through Omsarender/XSLT or or/p
    if ($Config->get("Osmarender") eq "XSLT")
    {
        print "- rendering using Osmarender/XSLT\n";
        die "! Can't find osmarender/osmarender.xsl" unless (-f "osmarender/osmarender.xsl");

        # XmlStarlet version
        $cmd=$Config->get("XmlStarlet");
        my $XmlV = `\"$cmd\" --version`;
        $EnvironmentInfo{Xml}=$XmlV;

        if($XmlV !~ /(\d+\.\d+\.\d+)/) {
            die("Can't find xmlstarlet (using \"".$Config->{"XmlStarlet"}."\")\n");
        }
        print "- xmlstarlet version $1\n";
    }
    elsif ($Config->get("Osmarender") eq "orp")
    {
        print "- rendering using or/p\n";
        die "! Can't find orp/orp.pl" unless (-f "orp/orp.pl");
    }
    else
    {
        die "! invalid configuration setting for 'Osmarender' - allowed values are 'XSLT', 'orp'";
    }

    # Zip version
    if ($EnvironmentInfo{Zip} eq "") 
    {
        die("! Can't find zip (using \"".$Config->get("Zip")."\")\n");
    }
    print "- zip is present\n";

    # check a correct pngoptimizer is set
    if ( ! (($Config->get("PngOptimizer") eq "pngcrush") or ($Config->get("PngOptimizer") eq "optipng")))
    {
        die("! Can't find valid PngOptimizer setting, check config");
    }
    print "- going to use ".$Config->get("PngOptimizer")."\n";
    
    # PNGCrush version
    $cmd=$Config->get("Pngcrush");
    my $PngcrushV = `\"$cmd\" -version`;
    $EnvironmentInfo{Pngcrush}=$PngcrushV;

    if (($PngcrushV !~ /[Pp]ngcrush\s+(\d+\.\d+\.?\d*)/) and ($Config->get("PngOptimizer") eq "pngcrush"))
    {
        print "! Can't find pngcrush (using \"".$Config->get("Pngcrush")."\")\n";
    }
    else
    {
        print "- Pngcrush version $1\n";
    }

    # Optipng version
    $cmd=$Config->get("Optipng");
    my $OptipngV = `\"$cmd\" -v`;
    $EnvironmentInfo{Optipng}=$OptipngV;

    if ( $Config->get("PngOptimizer") eq "optipng" ) 
    {
        if ($OptipngV !~ /[Oo]pti[Pp][Nn][Gg]\s+(\d+\.\d+\.?\d*)/) 
        {
            die("! Can't find OptiPNG (using \"".$Config->get("Optipng")."\")\n");
        }
        else
        {
            print "- OptiPNG version $1\n";
        }
    }

    if ( $Config->get("PngQuantizer") eq "pngnq" )
    {
        $cmd=$Config->get("pngnq");
        my $PngnqV=`\"$cmd\" -V 2>&1`;
        if ($PngnqV !~ /pngnq.+(\d+\.\d+)/)
        {
            print "! Can't find pngnq (using \"".$Config->get("pngnq")."\")\n";
        }
        else
        {
            $EnvironmentInfo{"pngnq"}=$PngnqV;
            print "- pngnq version $1\n";
        }
    }

    if($Config->get("RequestUrl")){
        print "- Using ".$Config->get("RequestUrl")." for Requests\n";
    }

    # Misc stuff
    foreach(qw(N S E W)){
        if($Config->get("Border".$_) > 0.5){
            printf "Border".$_." looks abnormally large\n";
        }
    }

    ## not used any longer, superseded by per-layer value
    #if($Config->get("MaxZoom") < 12 || $Config->get("MaxZoom") > 20){
    #    print "Check MaxZoom\n";
    #}

    return %EnvironmentInfo;

}

1;
