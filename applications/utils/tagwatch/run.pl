#!/usr/bin/perl -I .
#-----------------------------------------------------------------
# Creates pages describing the tagging schemes in use within 
# OpenStreetmap
#-----------------------------------------------------------------
# Usage: perl run.pl
# Will create an ./html/ directory and fill it with HTML files
# Uses input from http://wiki.openstreetmap.org/index.php/Tagwatch/*
# and other OSMwiki pages
#-----------------------------------------------------------------
# This file is part of Tagwatch
# Tagwatch is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# 
# Tagwatch is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with Tagwatch.  If not, see <http://www.gnu.org/licenses/>.
#---------------------------------------------------------------

use strict;
use libs::getWikiTags;
use libs::getPhotos;
use libs::getWikiSettings;
use libs::processOSMFiles;
use libs::constructHTMLStats;
use libs::makeRenderSamples;
use LWP::Simple;


my $configfile = "tagwatch.conf";

foreach my $arg (@ARGV)
{
  $configfile = $1 if($arg =~ /^config_file=(.+)$/);
}

print "read config ...\n";
my %Config = ReadConfig($configfile);

foreach my $arg (@ARGV)
{
  $Config{$1} = $2 if($arg =~ /^(.+)=(.+)$/);
}

my @urls;
foreach my $key (sort keys %Config)
{
	push(@urls, $Config{$key}) if $key =~ /^osmDownloadUrl/ && $Config{$key};
}
print "\n++++++++++++++++++++++++++++++++++++++++++++\n";
if(!@urls)
{
	print "+ use previous downloaded osm files ...\n";
}
else
{
	print "+ download osm files ...\n";
	getOSMFiles($Config{'osmfile_folder'}, @urls);
}

print "\n++++++++++++++++++++++++++++++++++++++++++++\n";
if($Config{'use_WikiTag_Cache'} eq "yes")
{
	print "+ use cached wiki descriptions ...\n";
}
else
{
	print "+ get Tag description from the wiki ...\n";
	buildWikiTagCache(%Config);
}

print "\n++++++++++++++++++++++++++++++++++++++++++++\n";
if($Config{'use_Photo_Cache'} eq "yes")
{
	print "+ use cached photos ...\n";
}
else
{
	print "+ get photos from the wiki ...\n";
	getPhotos(%Config);
}

print "\n++++++++++++++++++++++++++++++++++++++++++++\n";
if($Config{'use_WikiSettings_Cache'} eq "yes")
{
	print "+ use cached wiki settings ...\n";
}
else
{
	print "+ get Tagwatch settings from the wiki ...\n";
	buildWikiSettingsCache(%Config);
}

print "\n++++++++++++++++++++++++++++++++++++++++++++\n";
if($Config{'use_OSMFile_Cache'} eq "yes")
{
	print "+ use cached OSM files ...\n";
}
else
{
	print "+ process OSM files ...\n";
	processOSMFiles(%Config);
}

print "\n++++++++++++++++++++++++++++++++++++++++++++\n";
print "create html files ...\n";
constructHTML(%Config);

print "\n++++++++++++++++++++++++++++++++++++++++++++\n";
if($Config{'use_TagSample_Cache'} eq "yes")
{
	print "+ use cached tag sample files ...\n";
}
else
{
	print "+ create tag sample files ...\n";
	renderExamples(%Config);
}

print "\n++++++++++++++++++++++++++++++++++++++++++++\n";
if($Config{'delete_OsmFiles'} eq "yes")
{
	print "+ delete osm source files ...\n";
	my $deleteIt = sprintf("rm -f -R %s", $Config{'osmfile_folder'});
	system($deleteIt);
}

#--------------------------------------------------------------------------
# Reads the tagwatch config file, returns a hash array
#--------------------------------------------------------------------------
sub ReadConfig {
	my ($Filename) = @_;
	my %Config;

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
            }
        }
        close $fp;


    if($Config{basedir})
    {
        foreach my $folder (keys %Config)
        {
            next if !($folder =~ /_folder$/);
            $Config{$folder} = "$Config{basedir}/$Config{$folder}";
        }
        system "mkdir -p $Config{basedir}" if ! -d $Config{basedir};
    }
    return %Config;
}

#--------------------------------------------------------------------------
# Downloads the osm files from the url specified in the conf file
# unzip it afterwards.
#--------------------------------------------------------------------------
sub getOSMFiles
{
	my ($Folder, @Urls) = @_;

	my %usefiles;
	foreach my $key (sort keys %Config)
	{
		$usefiles{$Config{$key}} = 1 if $key =~ /^osmDownloadFile/ && $Config{$key};
	}

	mkdir $Folder if ! -d $Folder;

	my %files;
	if(open(FILELIST,"<","$Folder/filelist.txt"))
	{
		while(my $line = <FILELIST>)
		{
			chomp($line);
			my ($name, $date) = split (/\|/,$line);
			$files{$name} = $date;
		}
		close FILELIST;
	}

	open(FILELIST,">","$Folder/filelist.txt");
	my $new = 0;
	foreach my $Url (@Urls)
	{
		foreach my $Line (split("\n",get($Url)))
		{
			if($Line =~ m{(.*)<a href="(.*).osm.bz2">(.*)</a></td><td align="right">(.*?) *</td>(.*)<td align="right">(.*)}
			|| $Line =~ m{(.*)<a href="(.*).osm.bz2">(.*)</a>  +(.*? \d\d:\d\d)})
			{
				my $Date = $4;
				my $FileName = $3;
				my $usename = $FileName;
				next if %usefiles && !$usefiles{$usename};

				$usename =~ s/.bz2// if($Config{'extract_OsmFiles'} eq "yes");

				if(!$files{$usename} || $files{$usename} ne $Date)#
				{
					system("wget --no-verbose --directory-prefix=$Folder \"$Url$FileName\"");
					++$new;
					if($Config{'extract_OsmFiles'} eq "yes")
					{
						system("bunzip2 $Folder/$FileName");
					}
				}
				delete $files{$usename};
				print FILELIST "$usename|$Date\n";
			}
		}
	}
	die "Nothing changed\n" if !$new && $Config{'die_when_unchanged'} eq "yes";
;
	if($Config{'delete_OldOsmFiles'} eq "yes")
	{
		foreach my $file (keys %files)
		{
			unlink("$Folder/$file");
		}
	}
	close FILELIST;
}