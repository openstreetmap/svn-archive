#-----------------------------------------------------------------
# Parses an OpenStreetMap Wiki looking for the tag descriptions.
# Parse the Tag:Key=Value // Map Features // Key:key and the 
# Tagwatch/Description pages to find them.
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
use utf8;
use MediaWiki::API;

my $c = MediaWiki::API->new( { api_url => 'http://wiki.openstreetmap.org/w/api.php' } );

# global hash array that holds all Tag data
my %RelationDescriptions;
my %TagDescriptions;
my %KeyDescriptions;
my %KeyByGroup;

# find the right Key/Value if we only have a group name (from the templates)
my %LookupKeyInGroup; 
my %LookupValueInGroup; 
my @TemplateGroups;
my $verbose = 0;

sub buildWikiTagCache
{
	my (%Config) = @_;

	$c->{ua}->agent($Config{'user_agent'} || "TagWatch/1.0");
	$verbose = $Config{'verbose'};
	my $CacheTopDir = $Config{'cache_folder'};
	mkdir $CacheTopDir if ! -d $CacheTopDir;

	my $CacheDir = "$CacheTopDir/wiki_desc";
	mkdir $CacheDir if ! -d $CacheDir;

	# get Map Feature Template groups
	print "\tparse Map_Features templates list ...\n";
	my @Pages = getTemplateGroups();

	# parse english map feature Templates to get all known keys and tags together with their description.
	print "\tparse English Map_Features templates ...\n";
	parseEnMapFeatures();

	# parse translated Map_Features pages to get the translated descriptions.
	for my $PageName (@Pages)
	{
		$PageName =~ m{^([A-Za-z-]+)};
		my $Language = lc($1);
		print "\tparse $PageName page (language $Language) ...\n";
		parseTranslatedMapFeatures($Language, $PageName);
	}

	# parse the Key pages for descriptions and general Key informations
	print "\tparse Key description and translated tag description from <lang>:Key:<name> pages ...\n";
	parseKeyCategories();

	# check if we can find some missing descriptions in the Tags category.
	print "\tparse <lang>:Tag:<key>=<value> pages ...\n";
	parseTagCategory();

	# some group cleanup map template groups <--> general keyGroups
	groupCleanup();

	# get relation information from the wiki
	print "\tparse relation pages ...\n";
	parseRelationCategory();

	# write down all data to the cache directory
	print "\twrite down all cached data ...\n";
	writeData($CacheDir);
}

#--------------------------------------------------------------------------
# parse all Key groups that can be found on the Map Feature page
# this is necessary because we group many keys together for example
# under restrictions or properties.
#--------------------------------------------------------------------------
sub getTemplateGroups
{
	my @pages;
	foreach my $article (@{$c->list({
	action => 'query', list => 'categorymembers', cmlimit => 'max',
	cmtitle => 'Category:Map Features template'})})
	{
		my $PageName = $article->{title};
		if($PageName =~ m{Template:Map Features:(.*)})
		{
			push(@TemplateGroups,$1);
		}
		elsif($PageName =~ m{^[A-Za-z-]+:Map Features$})
		{
			push(@pages, $PageName);
		}
	}
	return @pages;
}

#--------------------------------------------------------------------------
# parse every Template:Map Features:<group> page and get all
# tags with their description
#--------------------------------------------------------------------------
sub parseEnMapFeatures
{
	my $TempKey;
	my $TempValue;
	my $TempIdentifier;

	foreach my $Group (@TemplateGroups)
	{
		$TempKey = "";
		$TempValue = "";
		$TempIdentifier = "";

		# parse every line from every available template
		my $page = $c->get_page({title => "Template:Map Features:$Group"})->{'*'};
		utf8::decode($page);
		foreach my $Line(split(/\n/, $page))
		{
			# get key name
			if($Line =~ m{\|\s*\[\[\{\{\{\s*(.*)\s*:key\s*\|\s*(.*)\s*\}\}\}\s*\|\s*(.*)\s*\]\]\s*})
			{	
				$TempKey = $3;
				$TempKey =~ s/\'//g;
			}
			# get key name
			# TODO combine this regex with the one above
			if($Line =~ m{\|\s*\{\{\{\s*(.*)\s*:key\s*\|\s*(.*)\s*\}\}\}\s*})
			{
				$TempKey = $2;
				$TempKey =~ s/\'//g;
			}

			# get value name and used identifier from the template
			if($Line =~ m{\|\s*\[\[\{\{\{\s*(.*):value\s*\|\s*(.*)\s*\}\}\}\s*\|\s*(.*)\s*\]\]\s*$})
			{
				$TempIdentifier = $1;
				$TempValue = $3;
				if(!($TempValue =~ /^{{{/) && $TempKey ne 'Key')
				{
					$TempValue =~ s/ /_/g;
					$TempValue =~ s/\'//g;

					$LookupKeyInGroup{$Group}->{$TempIdentifier} = $TempKey;
					push (@{$LookupKeyInGroup{$Group}->{'keylist'}},$TempKey);
					$LookupValueInGroup{$Group}->{$TempIdentifier} = $TempValue;
				}
			}
			# get value name and used identifier from the template
			# TODO combine this with above regex
			if($Line =~ m{\|\s*\{\{\{(.*):value\s*\|\s*(.*)\s*\}\}\}\s*$})
			{
				$TempIdentifier = $1;
				$TempValue = $2;
				$TempValue =~ s/ /_/g;
				$TempValue =~ s/'//g;
				if($TempKey ne 'Key')
				{
					$LookupKeyInGroup{$Group}->{$TempIdentifier} = $TempKey;
					push (@{$LookupKeyInGroup{$Group}->{'keylist'}},$TempKey);
					$LookupValueInGroup{$Group}->{$TempIdentifier} = $TempValue;
				}
			}
			# get Tag description
			if($Line =~ m{\|\s*\{\{\{(.*):desc\s*\|\s*(.*)\s*[\}]{3}})
			{
				$TagDescriptions{$TempKey}->{$TempValue}->{'desc'}->{'en'} = $2;
			}
			if(($Line =~ m{\{\{IconNode\}\}}) || ($Line =~ m{\[\[Image:Mf_node.png\]\]}))
			{
				$TagDescriptions{$TempKey}->{$TempValue}->{'onNode'} = "yes";
			}
			if(($Line =~ m{\{\{IconWay\}\}}) || ($Line =~ m{\[\[Image:Mf_way.png\]\]}))
			{
				$TagDescriptions{$TempKey}->{$TempValue}->{'onWay'} = "yes";
			}
			if(($Line =~ m{\{\{IconArea\}\}}) || ($Line =~ m{\[\[Image:Mf_area.png\]\]}))
			{
				$TagDescriptions{$TempKey}->{$TempValue}->{'onArea'} = "yes";
			}
		}
	}
}

#--------------------------------------------------------------------------
# parse every Lang:Map_Features pages and get all
# tags with their description in other languages
#--------------------------------------------------------------------------
sub parseTranslatedMapFeatures
{
	my ($Language, $PageName) = @_;
	my $Group = "";
	
	# parse every line from each translated Map Feature page
	my $page = $c->get_page({title => $PageName})->{'*'};
	utf8::decode($page);
	foreach my $Line(split(/\n/, $page))
	{
		if($Line =~ m{\s*\{\{Map_Features:(\w*\s\w*|\w*)})
		{
			$Group = $1;
			$Group =~ s/ /_/g;
		} #\s*(\||\}\}|\s*)
		if($Line =~ m{\|?(.*):desc=\s*(.*)})
		{
			my $Identifier = $1;
			my $Description = $2;

			$Identifier =~ s/'//g;
			if($Identifier eq 'Proposed_features')
			{
				$Identifier = "User_Defined";
			}

			# Get right Key/Value for this identifier under this Group
			my $Key = $LookupKeyInGroup{$Group}->{$Identifier};
			my $Value = $LookupValueInGroup{$Group}->{$Identifier};

			# TODO find better regex to avoid replacing this afterwards
			$Description =~ s/\|$//g;
			$Description =~ s/\}\}$//g;
			$TagDescriptions{$Key}->{$Value}->{'desc'}->{$Language} = $Description;
		}
	}
}

#--------------------------------------------------------------------------
# parse the Key category to find all <lang>:Key subcategories
# parse the subcategories to get all available Key pages and
# the language.
#--------------------------------------------------------------------------
sub parseKeyCategories
{
	# First check the Key category for available languages
	foreach my $article (@{$c->list({
	action => 'query', list => 'categorymembers', cmlimit => 'max',
	cmtitle => 'Category:Keys'})})
	{
		if($article->{title} =~ m{^Category:.*:Keys$})
		{
			# get the available keypages and extract all informations from it.
			foreach my $keyarticle (@{$c->list({
			action => 'query', list => 'categorymembers', cmlimit => 'max',
			cmtitle => $article->{title}})})
			{
				my $KeyPageName = $keyarticle->{title};
				if($KeyPageName =~ m{([A-Za-z-]*):?Key:(.*)}
				&& !($KeyPageName =~ m{Category}))
				{
					# get language of the Key page
					my $Language;
					if($1 eq "")
					{
						$Language = "en";
					}
					else
					{
						$Language = lc($1);
					}
	
					# replace spaces with underlines
					my $KeyName = $2;
					$KeyName =~ s/ /_/g;
	
					parseKeyPages($Language, $KeyName, $KeyPageName);
				}
			}
		}
	}
}

#--------------------------------------------------------------------------
# parse every Lang:Key:<name> page and get all informations
# result will be the Key group & description
# Furthermore if the Key page includes the Map_features template
# this subroutine extract the Tag informations from it.
#--------------------------------------------------------------------------
sub parseKeyPages
{
	my ($Language, $KeyName, $KeyPageName) = @_;

	print "\t\tParse language $Language key $KeyName pagename $KeyPageName\n" if $verbose;

	my $Section = "-"; # current section of the page (header template or map feature template)
	my $Group   = "";  # used to create groups for keys according to their usage in the map Feature templates, if no Group info was available

	# parse the Key page line by line
	my $page = $c->get_page({title => $KeyPageName})->{'*'};
	utf8::decode($page);
	foreach my $Line(split(/\n/, $page))
	{
		# check for Key template section
		if($Line =~ m{\s*\{\{(Template:.*:)?KeyDescription.*})
		{
			$Section = "KeyDescription";
		}

		# parse Key template for interresting informations
		if($Section eq "KeyDescription")
		{
			if($Line =~ m{\|?description=\s*(.*)\s*\|?\s*$})
			{
				$KeyDescriptions{$KeyName}->{'desc'}->{$Language} = $1;
			}
			if($Line =~ m{\|?group=\s*(.*)\s*\|?\s*$})
			{
				# just write down first result of a group. This comes from the english template in most cases.
				if(!exists $KeyDescriptions{$KeyName}->{'group'})
				{
					$KeyDescriptions{$KeyName}->{'group'} = $1;
					push(@{$KeyByGroup{$1}},$KeyName);
				}
			}
			if($Line =~ m{\|?onNode=\s*(.*)\s*\|?\s*$})
			{
				if(!exists $KeyDescriptions{$KeyName}->{'onNode'})
				{
					$KeyDescriptions{$KeyName}->{'onNode'} = $1;
				}
			}
			if($Line =~ m{\|?onWay=\s*(.*)\s*\|?\s*$})
			{
				if(!exists $KeyDescriptions{$KeyName}->{'onWay'})
				{
					$KeyDescriptions{$KeyName}->{'onWay'} = $1;
				}
			}
			if($Line =~ m{\|?onArea=\s*(.*)\s*\|?\s*$})
			{
				if(!exists $KeyDescriptions{$KeyName}->{'onArea'})
				{
					$KeyDescriptions{$KeyName}->{'onArea'} = $1;
				}
			}
			if($Line =~ m{\|?image=\s*(.*)\s*\|?\s*$})
			{
				if(!exists $KeyDescriptions{$KeyName}->{'image'})
				{
					$KeyDescriptions{$KeyName}->{'image'} = $1;
				}
			}
			# end this section
			if($Line =~ m{(.*)\}\}(.*)})
			{
				$Section = "-";
				# stop parsing here if its an english template, because it has no further informations.
				if($Language eq "en")
				{
					last;
				}
			}
		}

		# check for Map Features template section
		if($Line =~ m{\s*\{\{Map_Features:(\w*\s\w*|\w*)})
		{
			$Section = "Map_Features";
			$Group = $1;
			$Group =~ s/ /_/g;
		}
		# parse map features template for interresting informations
		if($Section eq "Map_Features")
		{ #    \s*(\||\}\})?\s*$
			if($Line =~ m{\|?(.*):desc=\s*(.*)})
			{
				my $Identifier = $1;
				my $Description = $2;
	
				if($Identifier eq 'Proposed_features')
				{
					$Identifier = "User_Defined";
				}

				# Get right Key/Value for this identifier under this Group
				my $Key = $LookupKeyInGroup{$Group}->{$Identifier};
				my $Value = $LookupValueInGroup{$Group}->{$Identifier};
	
				# TODO find better regex to avoid replacing this afterwards
				$Description =~ s/\|$//g;
				$Description =~ s/\}\}$//g;
				if(!(exists $TagDescriptions{$Key}->{$Value}->{'desc'}->{$Language}))
				{
					$TagDescriptions{$Key}->{$Value}->{'desc'}->{$Language} = $Description;
				}
			}
		}
	}
}

#--------------------------------------------------------------------------
# parse the Tag category to find all <lang>:Tag subcategories
# parse the subcategories to get all available Tag pages and
# the language.
#--------------------------------------------------------------------------
sub parseTagCategory
{
	# First check the Key category for available languages
	foreach my $article (@{$c->list({
	action => 'query', list => 'categorymembers', cmlimit => 'max',
	cmtitle => 'Category:Tags'})})
	{
		if($article->{title} =~ m{^Category:.*:Tags$})
		{
			# get the available tag pages and extract all informations from it.
			foreach my $tagarticle (@{$c->list({
			action => 'query', list => 'categorymembers', cmlimit => 'max',
			cmtitle => $article->{title}})})
			{
				my $TagPageName = $tagarticle->{title};
				if($TagPageName =~ m{([A-Za-z-]*):?Tag:(.*)=(.*)})
				{
					# get language of the Key page
					my $Language;
					if($1 eq "")
					{
						$Language = "en";
					}
					else
					{
						$Language = lc($1);
					}
	
					# replace spaces with underlines
					my $KeyName = $2;
					my $ValueName = $3;
					$KeyName =~ s/ /_/g;
					$ValueName =~ s/ /_/g;
	
					parseTagPages($Language, $KeyName, $ValueName, $TagPageName);
				}
			}
		}
	}
}

#--------------------------------------------------------------------------
# parse every Lang:Tag:<keyname>=<valuename> page and get all informations
#--------------------------------------------------------------------------
sub parseTagPages
{
	my ($Language,$KeyName, $ValueName, $TagPageName) = @_;

	print "\t\tParse language $Language tag $KeyName=$ValueName pagename $TagPageName\n" if $verbose;

	my $Section = "-"; # used to change parsing mode for the combination or implies list entry
	my $followredirect = 1;

	while($followredirect eq 1)
	{
		$followredirect = 0;

		# parse the tag page line by line
		my $page = $c->get_page({title => $TagPageName})->{'*'};
		utf8::decode($page);
		foreach my $Line(split(/\n/, $page))
		{
			# parse combination list entry
			if($Section eq "combination")
			{
				if($Line =~ m{\|*\*\s*(.*?)\s*\|*\s*$})
				{
					# add this just for one language and prefer the english page
					if($Language eq "en")
					{
						push(@{$TagDescriptions{$KeyName}->{$ValueName}->{'combination'}},$1);
					}
				}
				else
				{
					$Section = "-";
				}
			}
	
			# parse general entries
			if($Section eq "-")
			{
				if($Line =~ m{\|*\s*(\w+)\s*=\s*(.*?)\s*\|*\s*$})
				{
					if($1 eq "combination")
					{
						$Section = "combination";
						next;
					}
					
					# override all existing entries. This prefers always the entry on the tag page over the Map_feature entry
					if($1 eq 'desc' || $1 eq 'description')
					{
						$TagDescriptions{$KeyName}->{$ValueName}->{'desc'}->{$Language} = $2;
					}
					else
					{
						if(!exists $TagDescriptions{$KeyName}->{$ValueName}->{$1})
						{
							$TagDescriptions{$KeyName}->{$ValueName}->{$1} = $2;
						}
					}
				}
				# end parsing at the end of the header wiki template
				if($Line =~ m{\}\}\s*$})
				{
					last;
				}
				#found redirect and thus follow to new page and read this instead
				if($Line =~ m{#REDIRECT \[\[(.*)\]\]})
				{
					$TagPageName = $1;
					$followredirect = 1;
					last;
				}
			}
		}
	}
}

#--------------------------------------------------------------------------
# merge General Groups with Template Groups to sort all keys
#--------------------------------------------------------------------------
sub groupCleanup
{
	# check every template group
	foreach my $TemplateGroup (@TemplateGroups)
	{
		# check all keys that are listed in this group
		foreach my $KeyName(@{$LookupKeyInGroup{$TemplateGroup}->{'keylist'}})
		{
			# check if this key is already listed in one of the General Groups
			if(!exists $KeyDescriptions{$KeyName}->{'group'})
			{
				# does not exist so we add the Template Group to it
				$KeyDescriptions{$KeyName}->{'group'} = ucfirst($TemplateGroup);
				# and update the Grouplist
				push(@{$KeyByGroup{ucfirst($TemplateGroup)}},$KeyName);
			}
		}
	}
}

#--------------------------------------------------------------------------
# parse the Relation category to find all <lang>:Relation subcategories
# parse the subcategories to get all available Relation pages and
# the language.
#--------------------------------------------------------------------------
sub parseRelationCategory
{
	# First check the Relation category for available languages
	foreach my $article (@{$c->list({
	action => 'query', list => 'categorymembers', cmlimit => 'max',
	cmtitle => 'Category:Relations'})})
	{
		if($article->{title} =~ m{^Category:.*:Relations$})
		{
			# get the available keypages and extract all informations from it.
			foreach my $relationarticle (@{$c->list({
			action => 'query', list => 'categorymembers', cmlimit => 'max',
			cmtitle => $article->{title}})})
			{
				my $RelationPageName = $relationarticle->{title};
				if($RelationPageName =~ m{([A-Za-z-]*):?Relation:(.*)})
				{
					# get language of the Key page
					my $Language;
					if($1 eq "")
					{
						$Language = "en";
					}
					else
					{
						$Language = lc($1);
					}
	
					# replace spaces with underlines
					my $RelationName = $2;
					$RelationName =~ s/ /_/g;
	
					parseRelationPages($Language, $RelationName, $RelationPageName);
				}
			}
		}
	}
}

#--------------------------------------------------------------------------
# parse every Lang:Realtion:<type> page and get all informations
#--------------------------------------------------------------------------
sub parseRelationPages
{
	my ($Language, $RelationName, $RelationPageName) = @_;

	print "\t\tParse language $Language relation $RelationName pagename $RelationPageName\n" if $verbose;

	my $Section = "-"; # used to change parsing mode for the member list entry

	# parse the relation page line by line
	my $page = $c->get_page({title => $RelationPageName})->{'*'};
	utf8::decode($page);
	foreach my $Line(split(/\n/, $page))
	{
		# parse combination list entry
		if($Section eq "members")
		{
			if($Line =~ m{\|*\*\s*(.*?)\s*\|*\s*$})
			{
				# add this just for one language and prefer the english page
				if($Language eq "en")
				{
					push(@{$RelationDescriptions{$RelationName}->{'members'}},$1);
				}
			}
			else
			{
				$Section = "-";
			}
		}

		# parse general entries
		if($Section eq "-")
		{
			if($Line =~ m{\|*\s*(\w+)\s*=\s*(.*?)\s*\|*\s*$})
			{
				if($1 eq "members")
				{
					$Section = "members";
					next;
				}
				
				# override all existing entries. This prefers always the entry on the tag page over the Map_feature entry
				if($1 eq 'description')
				{
					$RelationDescriptions{$RelationName}->{'desc'}->{$Language} = $2;
				}
				else
				{
					if(!exists $RelationDescriptions{$RelationName}->{$1})
					{
						$RelationDescriptions{$RelationName}->{$1} = $2;
					}
				}
			}
			# end parsing at the end of the header wiki template
			if($Line =~ m{.*?\}\}\s*$})
			{
				last;
			}
		}
	}
}

#--------------------------------------------------------------------------
# write down all cached data (Groups, Keys, Tags)
#--------------------------------------------------------------------------
sub writeData
{
	my($CacheDir) = @_;

	open(GROUPLIST, ">:utf8", "$CacheDir/group_list.txt");
	open(KEYLIST, ">:utf8", "$CacheDir/key_list.txt");
	open(GROUPEDKEYS, ">:utf8", "$CacheDir/grouped_keys.txt");

	# go through all available groups
	foreach my $Group(keys(%KeyByGroup))
	{
		printf GROUPLIST "* $Group\n";

		open(GROUP, ">:utf8", "$CacheDir/Group:$Group.txt");
		
		# go through all keys available in this group
		foreach my $KeyName(@{$KeyByGroup{$Group}})
		{
			printf GROUPLIST "** $KeyName\n";
			printf KEYLIST "$KeyName\n";

			open(KEYDESC, ">:utf8", "$CacheDir/Key:$KeyName.txt");

			# print general informations
			printf KEYDESC "group=%s\n", $KeyDescriptions{$KeyName}->{'group'};

			if(exists $KeyDescriptions{$KeyName}->{'image'})
			{
				printf KEYDESC "image=%s\n", $KeyDescriptions{$KeyName}->{'image'};
			}
			if(exists $KeyDescriptions{$KeyName}->{'onNode'})
			{
				printf KEYDESC "onNode=%s\n", $KeyDescriptions{$KeyName}->{'onNode'};
			}
			else
			{
				printf KEYDESC "onNode=no\n";
			}
			if(exists $KeyDescriptions{$KeyName}->{'onWay'})
			{
				printf KEYDESC "onWay=%s\n", $KeyDescriptions{$KeyName}->{'onWay'};
			}
			else
			{
				printf KEYDESC "onWay=no\n";
			}
			if(exists $KeyDescriptions{$KeyName}->{'onArea'})
			{
				printf KEYDESC "onArea=%s\n", $KeyDescriptions{$KeyName}->{'onArea'};
			}
			else
			{
				printf KEYDESC "onArea=no\n";
			}

			# print key description in every available language
			foreach my $Language (keys(%{$KeyDescriptions{$KeyName}->{'desc'}}))
			{
				printf KEYDESC "$Language:desc=%s\n", $KeyDescriptions{$KeyName}->{'desc'}->{$Language};
			}

			# get all values for this key and print them
			foreach my $ValueName (keys(%{$TagDescriptions{$KeyName}}))
			{
				printf KEYDESC "$KeyName = $ValueName\n";
				printf GROUP   "$KeyName = $ValueName\n";

				if($ValueName eq '*')
				{
					printf GROUPEDKEYS   "$KeyName=$ValueName\n";
				}

				# write down this tag values
				open(TAG, ">:utf8", "$CacheDir/Tag:$KeyName=$ValueName.txt");
	
				if(exists $TagDescriptions{$KeyName}->{$ValueName}->{'image'})
				{
					printf TAG "image=%s\n", $TagDescriptions{$KeyName}->{$ValueName}->{'image'};
				}
				if(exists $TagDescriptions{$KeyName}->{$ValueName}->{'onNode'})
				{
					printf TAG "onNode=%s\n", $TagDescriptions{$KeyName}->{$ValueName}->{'onNode'};
				}
				else
				{
					printf TAG "onNode=no\n";
				}
				if(exists $TagDescriptions{$KeyName}->{$ValueName}->{'onWay'})
				{
					printf TAG "onWay=%s\n", $TagDescriptions{$KeyName}->{$ValueName}->{'onWay'};
				}
				else
				{
					printf TAG "onWay=no\n";
				}
				if(exists $TagDescriptions{$KeyName}->{$ValueName}->{'onArea'})
				{
					printf TAG "onArea=%s\n", $TagDescriptions{$KeyName}->{$ValueName}->{'onArea'};
				}
				else
				{
					printf TAG "onArea=no\n";
				}

				# print key description in every available language
				foreach my $Language (keys(%{$TagDescriptions{$KeyName}->{$ValueName}->{'desc'}}))
				{
					printf TAG "$Language:desc=%s\n", $TagDescriptions{$KeyName}->{$ValueName}->{'desc'}->{$Language};
				}
				close TAG;

			}
			close KEYDESC;
		}
		close GROUP;
	}
	close GROUPLIST;
	close KEYLIST;
	close GROUPEDKEYS;

	open(RELATIONLIST, ">:utf8", "$CacheDir/relation_list.txt");

	# go through all relations available in the wiki
	foreach my $RelationName(keys(%RelationDescriptions))
	{
		printf RELATIONLIST "$RelationName\n";

		open(RELATION, ">:utf8", "$CacheDir/Relation:$RelationName.txt");
		
		printf RELATION "group=$RelationDescriptions{$RelationName}->{'group'}\n";
		printf RELATION "type=$RelationDescriptions{$RelationName}->{'type'}\n";

		foreach my $RelationMember(@{$RelationDescriptions{$RelationName}->{'members'}})
		{
			printf RELATION "member=$RelationMember\n";
		}
		foreach my $Language (keys(%{$RelationDescriptions{$RelationName}->{'desc'}}))
		{
			printf RELATION "$Language:desc=%s\n", $RelationDescriptions{$RelationName}->{'desc'}->{$Language};
		}
		close RELATION;
	}
	close RELATIONLIST;
}

1;
