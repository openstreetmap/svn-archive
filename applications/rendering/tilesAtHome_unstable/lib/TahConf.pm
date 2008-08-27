# A Config class for t@h.
#
# Copyright 2008, by Matthias Julius
# licensed under the GPL v2 or (at your option) any later version.

package TahConf;

use strict;
use AppConfig qw(:argcount);
use Config; #needed to check flock availability

my $instance = undef; # Singleton instance of Config class

#-----------------------------------------------------------------------
# returns the AppConfig object that can be globally used throughout the
# application and initializes it if called for the first time.
# This method is called on the class, and not on an instance.
#-----------------------------------------------------------------------
sub getConfig 
{
    my $class = shift;

    if (defined($instance))
    {
        # already initialized, do nothing
        return $instance;
    }

    # not yet initialized
    my $self = {};       # TahConf instance


    my $Config = AppConfig->new(
                 {CREATE => 1,              # Autocreate unknown config variables
                  GLOBAL => {DEFAULT  => undef, # Create undefined Variables by default
                  ARGCOUNT => ARGCOUNT_ONE} # Simple Values (no arrays, no hashmaps)
                 });
    $Config->define("help|usage!");
    $Config->define("nodownload=s");
    $Config->set("nodownload",0);
    $Config->file("config.defaults", "layers.conf", "tilesAtHome.conf", "authentication.conf");
    $Config->args();                # overwrite config options with command line options
    $Config->file("general.conf");  # overwrite with hardcoded values that must not be changed

    $self->{Config} = $Config;

    bless $self, $class;
    $instance = $self;
    return $self;
}


#-----------------------------------------------------------------------------
# retrieve a specific config setting
#-----------------------------------------------------------------------------
sub get
{
    my $self = shift;
    return $self->{Config}->get(shift);
}


#-----------------------------------------------------------------------------
# set a config setting
#-----------------------------------------------------------------------------
sub set
{
    my $self = shift();
    return $self->{Config}->set(@_);
}



#--------------------------------------------------------------------------
# Checks a tiles@home basic configuration which is needed for uploading
# also sets some runtime environment information
#--------------------------------------------------------------------------
sub CheckBasicConfig
{
    my $self = shift();
    my %EnvironmentInfo;
    my $cmd;

    #------
    ## switch on verbose mode if Debug is set
    if ($self->get("Debug"))
    {
        $self->set("Verbose",10);
    }

    #------
    ## check and set the WorkingDirectory.
    #  NOTE: it will contain no trailing slash after we are finished
    if ($self->get("WorkingDirectory"))
    {
        $self->set("WorkingDirectory", 
                    File::Spec->rel2abs($self->get("WorkingDirectory")));
    }
    else
    {   # no WorkingDir set in config file. Use system default tmp dir
        $self->set("WorkingDirectory", File::Spec->tmpdir());
    }

    #------
    ## Finally create the WorkingDir if necessary.
    printf "- Using working directory %s\n", $self->get("WorkingDirectory");
    File::Path::mkpath($self->get("WorkingDirectory"));

    #------
    printf "- Using process log file %s\n", $self->get("ProcessLogFile") if ($self->get("ProcessLog"));

    #------
    if ($self->get("Subversion"))
    {
        $cmd=$self->get("Subversion");
        my $SubversionV = `\"$cmd\" --version`;
        $EnvironmentInfo{"Subversion"}=$SubversionV;
        # die here if svn executable not found, but before enabling try on windows machines wether --version works.
    }
    else
    {
        die ("! no subversion command set");
    }

    #------
    # LocalSplippymap
    if ($self->get("LocalSlippymap"))
    {
        print "- Writing LOCAL slippy map directory hierarchy, no uploading\n";
    }
    else
    {
        if($self->get("DeleteZipFilesAfterUpload") == 0){
            print "- Keeping ZIP files after upload\n";
        }
    }

    #------
    if ($self->get("UploadToDirectory"))
    {
        if (! -d $self->get("UploadTargetDirectory")) {
            # we chose to upload to directory, but it does not exist!
            print "- Upload Directory does not exist. Trying to create ",
                   $self->get("UploadTargetDirectory"),"\n";
            File::Path::mkpath $self->get("UploadTargetDirectory");
           if (! -d $self->get("UploadTargetDirectory")) {
               die "! Failed to create Upload directory.";
           }
        }

        if (! $self->get("UseHostnameInZipname")) 
        {
            print " * UseHostnameInZipname should be set when using UploadToDirectory\n";
        }
    }

    #------
    # Zip version
    $cmd=$self->get("Zip");
    my $ZipV = `\"$cmd\" -v`;
    $EnvironmentInfo{Zip}=$ZipV;
    if ($EnvironmentInfo{Zip} eq "") 
    {
        die("! Can't find zip (using \"".$self->get("Zip")."\")\n");
    }
    #------
    # check if flock is available on this OS
    # Theoertically the next 2 lines are correct, but Winows seems to cannot lock directories
    # even if flock exists. So bypass flock there completely
    #if (($Config{'d_flock'} && $Config{'d_flock'} eq 'define') or
    #   ($Config{'d_fcntl_can_lock'} && $Config{'d_fcntl_can_lock'} eq 'define'))
    if ($^O ne "MSWin32")
    {
	# it's a sane OS and flock is there
        $self->set('flock_available',1);
    } else {
	print "! 'flock' not available. Do not run concurrent uploads\n";
        $self->set('flock_available',0);
    }

    #------
    return %EnvironmentInfo;

}

#--------------------------------------------------------------------------
# Checks a tiles@home configuration which is needed for rendering
#--------------------------------------------------------------------------
sub CheckConfig
{
    my $self = shift;
    my $cmd;

    my %EnvironmentInfo = $self->CheckBasicConfig();

    if (!defined($self->get("Layers")))
    {
        die("no layers configured");
    }



    if ($self->get("Batik"))
    {
        print "- Using Batik";
        if ($self->get("Batik") == 1)
        {
            print " in jar mode";
        }
        if ($self->get("Batik") == 2)
        {
            print " in wrapper mode";
        }
        if ($self->get("Batik") == 3)
        {
            print " in agent mode";
        }
        print "\n";
    }
    else
    {
        # Inkscape version
        $cmd=$self->get("Inkscape");
        my $InkscapeV = `\"$cmd\" --version`;
        $EnvironmentInfo{"Inkscape"}=$InkscapeV;
 
        if($InkscapeV !~ /Inkscape (\d+)\.(\d+\.?\d*)/)
        {
            die("Can't find inkscape (using \"".$self->get("Inkscape")."\")\n");
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
    if ($self->get("Osmarender") eq "XSLT")
    {
        print "- rendering using Osmarender/XSLT\n";
        die "! Can't find osmarender/osmarender.xsl" unless (-f "osmarender/osmarender.xsl");

        # XmlStarlet version
        $cmd=$self->get("XmlStarlet");
        my $XmlV = `\"$cmd\" --version`;
        $EnvironmentInfo{Xml}=$XmlV;

        if($XmlV !~ /(\d+\.\d+\.\d+)/) {
            die("Can't find xmlstarlet (using \"" . $self->get("XmlStarlet") . "\")\n");
        }
        print "- xmlstarlet version $1\n";
    }
    elsif ($self->get("Osmarender") eq "orp")
    {
        print "- rendering using or/p\n";
        die "! Can't find orp/orp.pl" unless (-f "orp/orp.pl");
    }
    else
    {
        die "! invalid configuration setting for 'Osmarender' - allowed values are 'XSLT', 'orp'";
    }

    # check a correct pngoptimizer is set
    if ( ! (($self->get("PngOptimizer") eq "pngcrush") or ($self->get("PngOptimizer") eq "optipng")))
    {
        die("! Can't find valid PngOptimizer setting, check config");
    }
    print "- going to use ".$self->get("PngOptimizer")."\n";
    
    # PNGCrush version
    $cmd = $self->get("Pngcrush");
    my $PngcrushV = `\"$cmd\" -version`;
    $EnvironmentInfo{Pngcrush}=$PngcrushV;

    if (($PngcrushV !~ /[Pp]ngcrush\s+(\d+\.\d+\.?\d*)/) and ($self->get("PngOptimizer") eq "pngcrush"))
    {
        print "! Can't find pngcrush (using \"".$self->get("Pngcrush")."\")\n";
    }
    else
    {
        print "- Pngcrush version $1\n";
    }

    # Optipng version
    $cmd = $self->get("Optipng");
    my $OptipngV = `\"$cmd\" -v`;
    $EnvironmentInfo{Optipng}=$OptipngV;

    if ( $self->get("PngOptimizer") eq "optipng" ) 
    {
        if ($OptipngV !~ /[Oo]pti[Pp][Nn][Gg]\s+(\d+\.\d+\.?\d*)/) 
        {
            die("! Can't find OptiPNG (using \"".$self->get("Optipng")."\")\n");
        }
        else
        {
            print "- OptiPNG version $1\n";
        }
    }

    if ( $self->get("PngQuantizer") eq "pngnq" )
    {
        $cmd = $self->get("pngnq");
        my $PngnqV=`\"$cmd\" -V 2>&1`;
        if ($PngnqV !~ /pngnq.+(\d+(\.\d+)+)/)
        {
            print "! Can't find pngnq (using \"".$self->get("pngnq")."\")\n";
        }
        else
        {
            my $minVersion = "0.5";
            if ($self->CompareVersions($1, $minVersion) == -1) {
                print "! pngnq version ${1} too low, needs to be at least ${minVersion}\n";
                print "! disabling pngnq\n";
                $self->set("PngQuantizer", "");
            } else {
                $EnvironmentInfo{"pngnq"}=$PngnqV;
                print "- pngnq version $1\n";
            }
        }
    } else {
        print "! no valid PngQuantizer specified\n";
    }

    #-------------------------------------------------------------------
    # check all layers for existing and sane values
    #------------------------------------------------------------------
    foreach my $layer(split(/,/, $self->get("LayersCapability")))
    {
        if ($self->get($layer."_MaxZoom") < $self->get($layer."_MinZoom"))
        {
            die " ! Check MinZoom and MaxZoom for section [".$layer."]\n";
        } 

        for(my $zoom=$self->get($layer."_MinZoom"); $zoom<=$self->get($layer."_MaxZoom"); $zoom++)
        {
            if (!defined($self->get($layer."_Rules.$zoom")))
            {
                die " ! config option Rules.".$zoom." is not set for layer".$layer;
            }
            if (!-f $self->get($layer."_Rules.".$zoom))
            {
                die " ! rules file ".$self->get($layer."_Rules.".$zoom).
                    " referenced by config option Rules.".$zoom." in section [".$layer."]".
                    "is not present";
            }
        }

        if (!defined($self->get($layer."_Prefix")))
        {
            die " ! config option \"Prefix\" is not set for layer ".$layer;
        }

        if (!defined($self->get($layer."_Transparent")))
        {
            die($layer.": Transparent not configured");
        }

        if (!defined($self->get($layer."_RenderFullTileset")))
        {
            die($layer.": RenderFullTileset not configured");
        }

        if (!defined($self->get($layer."_Preprocessor")))
        {
            die(" ! config option \"Preprocessor\" is not set for layer ".$layer);
        }

        # any combination of comma-separated preprocessor names is allowed
        die "config option Preprocessor has invalid value in section [".$layer."]" 
            if (grep { $_ !~ /maplint|close-areas|autocut|noop/} split(/,/, $self->get($layer."_Preprocessor")));

        foreach my $reqfile(split(/,/, $self->get($layer."_RequiredFiles")))
        {
            die " ! file $reqfile required for layer $layer as per config option ".
                "RequiredFiles in section [".$layer."] not found" unless (-f $reqfile);
        }

    }

    if($self->get("RequestUrl")){ 
        ## put back Verbose output to make remote debugging a bit easier
        print "- Using ".$self->get("RequestUrl")." for Requests\n" if($self->get("Verbose") >=10); 
    }

    # Misc stuff
    foreach(qw(NS WE)){
        if($self->get("Border".$_) > 0.5){
            printf "Border".$_." looks abnormally large\n";
        }
    }

    return %EnvironmentInfo;

}

#--------------------------------------------------------------------------
# internal helper function to compare two version numbers
# works only for dotted numerical numbers like 'x.yy.zz'
# returns -1 when first is lower, 0 when equal and 1 when first is higher
#--------------------------------------------------------------------------
sub CompareVersions {
    my $self = shift;
    my ($version1, $version2) = @_;
    my @v1 = split(/\./, $version1);
    my @v2 = split(/\./, $version2);
    for (my $i = 0; ($i <= scalar @v1 - 1) or ($i <= scalar @v2 - 1); $i++) {
        if ($v1[$i] < $v2[$i]) {
            return -1;
        }
        if ($v1[$i] > $v2[$i]) {
            return 1;
        }
    }
    return 0;
}

1;
