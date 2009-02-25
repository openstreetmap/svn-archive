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
use MediaWiki;

my $c = MediaWiki->new;
   $c->setup({'wiki' => {
	                 'host' => 'wiki.openstreetmap.org',
	                 'path' => ''}});

# global hash array that holds all Tag data
my %RelationDescriptions;
my %TagDescriptions;
my %KeyDescriptions;
my %KeyByGroup;

# find the right Key/Value if we only have a group name (from the templates)
my %LookupKeyInGroup; 
my %LookupValueInGroup; 
my @TemplateGroups;


sub buildWikiTagCache
{
	my (%Config) = @_;

	$c->{ua}->agent($Config{'user_agent'} || "TagWatch/1.0");
	my $CacheTopDir = $Config{'cache_folder'};
	mkdir $CacheTopDir if ! -d $CacheTopDir;

	my $CacheDir = "$CacheTopDir/wiki_desc";
	mkdir $CacheDir if ! -d $CacheDir;

	# get Map Feature Template groups
	getTemplateGroups();

	# parse english map feature Templates to get all known keys and tags together with their description.
	print "\tparse english Map_Features templates ...\n";
	parseEnMapFeatures();

	# parse translated Map_Features pages to get the translated descriptions.
	print "\tparse <lang>:Map_Features pages ...\n";
	parseTranslatedMapFeatures();

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
	my($Template_articles, $Template_subcats) = $c->readcat("Map_Features_template");

	foreach my $PageName (@{$Template_articles})
	{
		if($PageName =~ m{Template:Map Features:(.*)})
		{
			push(@TemplateGroups,$1);
		}
	}
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

		# parse every line from every availible template
		foreach my $Line(split(/\n/, $c->text("Template:Map Features:$Group")))
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
	my %PageNames = getMFPageNames();
	my $Group = "";

	foreach my $Lang (keys %PageNames)
	{		
		$Group = "";
		
		# parse every line from each translated Map Feature page
		foreach my $Line(split(/\n/, $c->text($PageNames{$Lang})))
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
				$TagDescriptions{$Key}->{$Value}->{'desc'}->{$Lang} = $Description;
			}
		}
	}
}

#--------------------------------------------------------------------------
# parse the Key category to find all <lang>:Key subcategories
# parse the subcategories to get all availible Key pages and
# the language.
#--------------------------------------------------------------------------
sub parseKeyCategories
{
	# First check the Key category for availible languages
	my($KeyArticles, $KeySubcats) = $c->readcat("Keys");

	# run through all subcategories
	foreach my $Pages(@{$KeyArticles})
	{
		if($Pages =~ m{^Category:(.*):Keys$})
		{
			my $Subcategory = "$1:Keys";

			# get the availible keypages
			my($LangKeyArticles, $LangKeySubcats) = $c->readcat($Subcategory);
	
			# now check the Key page and extract all informations from it.
			foreach my $KeyPageName(@{$LangKeyArticles})
			{
				if($KeyPageName =~ m{(\w*):?Key:(.*)})
				{
					# get language of the Key page
					my $Language;
					if($1 eq "")
					{
						$Language = "En";
					}
					else
					{
						$Language = $1;
					}
	
					# replace spaces with underlines
					my $KeyName = $2;
					$KeyName =~ s/ /_/g;
	
					parseKeyPages($Language,$KeyName, $KeyPageName);
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

	my $Section = "-"; # current section of the page (header template or map feature template)
	my $Group   = "";  # used to create groups for keys according to their usage in the map Feature templates, if no Group info was availible

	# parse the Key page line by line
	foreach my $Line(split(/\n/, $c->text($KeyPageName)))
	{
		# check for Key template section
		if($Line =~ m{\s*\{\{KeyDescription(.*)})
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
				if($Language eq "En")
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
				#if($Language eq "Fr") { print "$1 :: $2\n"; }
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
# parse the subcategories to get all availible Tag pages and
# the language.
#--------------------------------------------------------------------------
sub parseTagCategory
{
	# First check the Key category for availible languages
	my($TagArticles, $TagSubcats) = $c->readcat("Tags");

	# run through all subcategories
	foreach my $Pages(@{$TagArticles})
	{
		if($Pages =~ m{^Category:(.*):Tags$})
		{
			my $Subcategory = "$1:Tags";

			# get the availible tag pages
			my($LangTagArticles, $LangTagSubcats) = $c->readcat($Subcategory);
	
			# now check the Tag page and extract all informations from it.
			foreach my $TagPageName(@{$LangTagArticles})
			{
				if($TagPageName =~ m{(\w*):?Tag:(.*)=(.*)})
				{
					# get language of the Key page
					my $Language;
					if($1 eq "")
					{
						$Language = "En";
					}
					else
					{
						$Language = $1;
					}
	
					# replace spaces with underlines
					my $KeyName = $2;
					$KeyName =~ s/ /_/g;
					my $ValueName = $3;
					$ValueName =~ s/ /_/g;
	
					parseTagPages($Language,$KeyName, $ValueName, $TagPageName);
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

	my $Section = "-"; # used to change parsing mode for the combination or implies list entry
	my $followredirect = 1;

	while($followredirect eq 1)
	{
		$followredirect = 0;

		# parse the tag page line by line
		foreach my $Line(split(/\n/, $c->text($TagPageName)))
		{
			# parse combination list entry
			if($Section eq "combination")
			{
				if($Line =~ m{\|*\*\s*(.*?)\s*\|*\s*$})
				{
					# add this just for one language and prefer the english page
					if($Language eq "En")
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
				if($Line =~ m{.*?\}\}\s*$})
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
# parse the subcategories to get all availible Relation pages and
# the language.
#--------------------------------------------------------------------------
sub parseRelationCategory
{
	# First check the Relation category for availible languages
	my($RelationArticles, $RelationSubcats) = $c->readcat("Relations");

	# run through all subcategories
	foreach my $Pages(@{$RelationArticles})
	{
		if($Pages =~ m{^Category:(.*):Relations$})
		{
			my $Subcategory = "$1:Relations";

			# get the availible keypages
			my($LangRelationArticles, $LangRelationSubcats) = $c->readcat($Subcategory);
	
			# now check the Key page and extract all informations from it.
			foreach my $RelationPageName(@{$LangRelationArticles})
			{
				if($RelationPageName =~ m{(\w*):?Relation:(.*)})
				{
					# get language of the Key page
					my $Language;
					if($1 eq "")
					{
						$Language = "En";
					}
					else
					{
						$Language = $1;
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

	my $Section = "-"; # used to change parsing mode for the member list entry

	# parse the relation page line by line
	foreach my $Line(split(/\n/, $c->text($RelationPageName)))
	{
		# parse combination list entry
		if($Section eq "members")
		{
			if($Line =~ m{\|*\*\s*(.*?)\s*\|*\s*$})
			{
				# add this just for one language and prefer the english page
				if($Language eq "En")
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

	open(GROUPLIST,  ">$CacheDir/group_list.txt");
	open(KEYLIST, ">$CacheDir/key_list.txt");
	open(GROUPEDKEYS, ">$CacheDir/grouped_keys.txt");

	# go through all availible groups
	foreach my $Group(keys(%KeyByGroup))
	{
		printf GROUPLIST "* $Group\n";

		open(GROUP, ">$CacheDir/Group:$Group.txt");
		
		# go through all keys availible in this group
		foreach my $KeyName(@{$KeyByGroup{$Group}})
		{
			printf GROUPLIST "** $KeyName\n";
			printf KEYLIST "$KeyName\n";

			open(KEYDESC, ">$CacheDir/Key:$KeyName.txt");

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

			# print key description in every availible language
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
				open(TAG, ">$CacheDir/Tag:$KeyName=$ValueName.txt");
	
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

				# print key description in every availible language
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

	open(RELATIONLIST,  ">$CacheDir/relation_list.txt");

	# go through all relations availible in the wiki
	foreach my $RelationName(keys(%RelationDescriptions))
	{
		printf RELATIONLIST "$RelationName\n";

		open(RELATION,  ">$CacheDir/Relation:$RelationName.txt");
		
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


#--------------------------------------------------------------------------
# Workaround ... 
# used to get all Map_Features pages and their corresponding language
# mainly used, because the dutch ppl don't use Nl:Map_Features as page name
#--------------------------------------------------------------------------
sub getMFPageNames
{
	my %workaround = (
			"Bg" => "Bg:Map Features",
			"Cz" => "Cz:Map Features",
			"Co" => "Co:Map Features",
			"De" => "De:Map Features",
			"Dk" => "Dk:Map Features",
			"Fr" => "Fr:Map Features",
			"It" => "It:Map Features",
			"Es" => "Es:Map Features",
			"Ja" => "Ja:Map Features",
			"Hu" => "Hu:Map Features",
			"Nl" => "Kaart eigenschappen",
			"Pl" => "Pl:Map Features",
			"Ro" => "Ro:Map Features",
			"Ro-md" => "Ro-md:Map Features",
			"Pt" => "Pt:Map Features",
			"SK" => "SK:Map Features",
			"Sl" => "Sl:Map Features",
			"Fi" => "Fi:Map Features",
			"Sv" => "Sv:Map Features",
			"Tr" => "Tr:Map Features",
			"Ru" => "Ru:Map Features"
			);

	return %workaround;
}

1;