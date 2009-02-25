#-----------------------------------------------------------------
# parse the settings for Tagwatch from the Wiki
# ignored values / list with watched keys translation of the Interface etc
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
#use strict;
use MediaWiki;

my $c = MediaWiki->new;
   $c->setup({'wiki' => {
	                 'host' => 'wiki.openstreetmap.org',
	                 'path' => ''}});

my @Languages;

sub buildWikiSettingsCache
{
	my (%Config) = @_;

	$c->{ua}->agent($Config{'user_agent'} || "TagWatch/1.0");
	@Languages = split(/,/, $Config{'languages'});

	my $CacheDir = "$Config{'cache_folder'}/wiki_settings";
	mkdir $CacheDir if ! -d $CacheDir;

	# parse page with all tags that will be ignored.
	print "\tgrab list of ignored tags ...\n";
	parseIgnoredTags($CacheDir);

	# parse page with all tags that will be ignored/grouped with an *.
	print "\tgrab list of ignored/grouped values ...\n";
	parseIgnoredValues($CacheDir);

	# parse page with all tags that are on the watchlist.
	print "\tgrab list of keys on the watchlist ...\n";
	parseWatchlist($CacheDir);

	# parse page with all tags that are on the watchlist.
	print "\tgrab interface translations ...\n";
	parseInterface($CacheDir);
}

#--------------------------------------------------------------------------
# parse the Ignore list and write down the contend in the cache folder
#--------------------------------------------------------------------------
sub parseIgnoredTags
{
	my ($CacheDir) = @_;

	open(IGNORETAGLIST,  ">$CacheDir/ignored_tags.txt");

	foreach my $Line(split(/\n/, $c->text("Tagwatch/Ignore")))
	{
		if($Line =~ m{\* (.+):(.*)})
		{
			printf IGNORETAGLIST "$1\n";
		}
	}
	close IGNORETAGLIST;
}

#--------------------------------------------------------------------------
# parse the Volatile list and write down the contend in the cache folder
#--------------------------------------------------------------------------
sub parseIgnoredValues
{
	my ($CacheDir) = @_;

	open(LIST,  ">$CacheDir/ignored_values.txt");

	foreach my $Line(split(/\n/, $c->text("Tagwatch/Volatile")))
	{
		if($Line =~ m{\* (.*)})
		{
			printf LIST "$1\n";
		}
	}
	close LIST;
}


#--------------------------------------------------------------------------
# parse the Watchlist for some watched keys
#--------------------------------------------------------------------------
sub parseWatchlist
{
	my ($CacheDir) = @_;

	open(WATCHLIST,  ">$CacheDir/watchlist.txt");

	foreach my $Line(split(/\n/, $c->text("Tagwatch/Watchlist")))
	{
		if($Line =~ m{\* (.*)})
		{
			printf WATCHLIST "$1\n";
		}
	}
	close WATCHLIST;
}

#--------------------------------------------------------------------------
# Reads translated interface strings from the Tagwatch/Description/<lang>
# pages. Use english language as default otherwise.
# TODO add more translations to the wiki site
#--------------------------------------------------------------------------
sub parseInterface
{
	my ($CacheDir) = @_;

	open(INTERFACE,  ">$CacheDir/interface.txt");

	foreach my $Language (@Languages)
	{
		foreach my $Line(split(/\n/, $c->text("Tagwatch/Interface/".ucfirst($Language))))
		{
			if($Line =~ m{^\* (.*?) *= *(.*?)$})
			{
				print INTERFACE ucfirst($Language).":$1 = $2\n";
			}
		}
	}
	close INTERFACE;
}

#--------------------------------------------------------------------------
# Create a list of tags to ignore
#--------------------------------------------------------------------------
sub getIgnoredTags
{
	my ($CacheDir) = @_;
	my %Ignore;

	open(IGNORELIST,  "<$CacheDir/ignored_tags.txt");

	while(my $Line = <IGNORELIST>)
	{
		$Line =~ s/\n//g;
		$Ignore{$Line} = 1;
	}
	close IGNORELIST;

	return %Ignore;
}

#--------------------------------------------------------------------------
# Create a list of tags with unique values
#--------------------------------------------------------------------------
sub getIgnoredValues
{
	my ($CacheDir) = @_;
	my @Ignore;

	open(IGNORELIST,  "<$CacheDir/ignored_values.txt");

	while(my $Line = <IGNORELIST>)
	{
		$Line =~ s/\n//g;
		push(@Ignore, $Line);
	}
	close IGNORELIST;

	return(@Ignore);
}

#--------------------------------------------------------------------------
# Create a list of tags with unique values
#--------------------------------------------------------------------------
sub getWatchedKeys
{
	my ($CacheDir) = @_;
	my %WatchedKeys;

	open(WATCHLIST,  "<$CacheDir/watchlist.txt");

	while(my $Line = <WATCHLIST>)
	{
		$Line =~ s/\n//g;
		$WatchedKeys{$Line}  = 1;
	}
	close WATCHLIST;

	return %WatchedKeys;
}

#--------------------------------------------------------------------------
# Loads the Interface translations from the cache file
#--------------------------------------------------------------------------
sub getInterfaceTranslations
{
	my (%Config) = @_;

	my @Languages = split(/,/, $Config{'languages'});
	my %Interface;

	open(INTERFACE,  "<$Config{'cache_folder'}/wiki_settings/interface.txt");

	# read in all cached interface translations
	while(my $Line = <INTERFACE>)
	{
		$Line =~ s/\n//g;
		if($Line =~ m{(\w+)\:(\w+)\s=\s(.*)}x)
		{
			$Interface{$2}->{$1} = $3;
		}
	}
	close INTERFACE;

	# iterate through all translations and if one is missing, add the english translation.
	foreach my $TemplateName(keys %Interface)
	{
		foreach my $Language(@Languages)
		{
			$Language = ucfirst($Language);
			if(!exists $Interface{$TemplateName}->{$Language})
			{
				 $Interface{$TemplateName}->{$Language} =  $Interface{$TemplateName}->{'En'};
			}
		}

	}

	return %Interface;
}

1;