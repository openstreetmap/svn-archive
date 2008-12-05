#-----------------------------------------------------------------
# parse the cached key/tag details and grabs the photos from
# the OSMWiki. Resize and cache them afterwards
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
	                 'path' => '',
			 'has_filepath' => 1}});

sub getPhotos
{
	my (%Config) = @_;

	my $CacheDir = "$Config{'cache_folder'}/photos";
	mkdir $CacheDir if ! -d $CacheDir;

	my %ImageList = getImageList($Config{'cache_folder'});

	foreach my $Filename (keys %ImageList)
	{
		if($ImageList{$Filename} =~ /[nN]one[ _]yet\.jpg/)
		{
			print "\t skip $Filename ($ImageList{$Filename}) ...\n";
			next;
		}
		print "\t get $Filename ($ImageList{$Filename}) ...\n";

		my $Data =  $c->download($ImageList{$Filename});

		if($Data ne "")
		{
			my $ok;
			if($ImageList{$Filename} =~ /(.*).jpg$/i)
			{
				eval
				{
					use GD;
					my $Image = GD::Image->newFromJpegData($Data);
					# Calculate image size
					my ($W, $H, $scale, $WO, $HO) = getsize($Image->width, $Image->height);
	
					open(IMOUT, ">:raw","$CacheDir/$Filename.jpg") || die;
					# Make a resized copy, and save that
					if($scale)
					{
						my $NewImage = new GD::Image($W,$H);
						$NewImage->copyResampled($Image,0,0,0,0,$W,$H,$WO,$HO);
						print IMOUT $NewImage->jpeg();
					}
					else
					{
						print IMOUT $Image->jpeg();
					}
					close IMOUT;
					$ok = 1;
				};
				print "Could not handle $Filename with GD: $@" if $@;
			}
			if(!$ok)
			{
				my ($name,$ext) = ($1,$2);
				eval
				{
					require Image::Magick;
					Image::Magick->import();
				};
				if($@)
				{
					print "Fileformat $ext is not supported.";
					next;
				}
				eval
				{
					my $x;
					my $image = Image::Magick->new();
					die if !$image;
					open(IMAGE, '>:raw', "$CacheDir/tmp.$ext") or die;
					print IMAGE $Data;
					close(IMAGE);
					$x = $image->Read(filename=>"$CacheDir/tmp.$ext") and die $x;
					unlink "$CacheDir/tmp.$ext";
					# Calcuate image size
					my ($W, $H, $scale) = getsize($image->Get('width', 'height'));

					if($scale)
					{
						$x = $image->Resize("width" => $W, "height" => $H) and die $x;
					}
					$x = $image->Write("$CacheDir/$Filename.png") and die $x;
				};
				print "Could not handle $Filename with ImageMagick: $@" if $@;
			}
		}
		else
		{
			print "\t\t could not download $ImageList{$Filename} ...\n";
		}
	}

}

sub getsize
{
	my $max = 200;
	my ($WO, $HO) = @_;
	my ($W, $H) = @_;
	if($WO > $HO)
	{
		($W, $H) = ($max, int($HO * ($max / $WO))) if($WO > $max);
	}
	elsif($HO > $max)
	{
		$H = $max;
		$W = int($WO * ($max / $HO));
	}
	return $W, $H, ($WO != $W), $WO, $HO;
}

sub getImageList
{
	my ($CacheFolder) = @_;

	my %ImageList;

	open(WIKIGROUP, "<$CacheFolder/wiki_desc/group_list.txt") || die("Could not find group_list for all keys.");

	# go through the conmplete group list to get the Key/Tag informations
	while(my $GroupLine = <WIKIGROUP>)
	{
		$GroupLine  =~ s/\n//g;

		# found keyname
		if($GroupLine =~ m{^\*\*\s(.*)})
		{
			my $Key = $1;
			
			open(WIKIKEY, "<$CacheFolder/wiki_desc/Key:$Key.txt") || next;

			# parse all Key informations
			while(my $KeyLine = <WIKIKEY>)
			{
				next if $KeyLine =~ /<!--/;
				$KeyLine  =~ s/\n//g;

				# parse key image
				if($KeyLine =~ m{^image=(?:Image:)?(.*\.[a-zA-Z]+)})
				{
					my $name = $1;
					$name =~ s/%(..)/chr(hex($1))/eg;
					$ImageList{$Key} = $name;
				}
				# parse availible tags
				elsif($KeyLine =~ m{^$Key\s=\s(.*)\s*$})
				{
					my $TagValue = $1;
	
					open(WIKITAG, "<$CacheFolder/wiki_desc/Tag:$Key=$TagValue.txt") || next;

					# parse all tag informations
					while(my $TagLine = <WIKITAG>)
					{
						next if $TagLine =~ /<!--/;
						$TagLine  =~ s/\n//g;
						# parse tag description
						if($TagLine =~ m{^image=(?:Image:)?(.*\.[a-zA-Z]+)})
						{
							my $name = $1;
							$name =~ s/%(..)/chr(hex($1))/eg;
							$ImageList{$Key."_".$TagValue} = $name;
						}
					}
					close WIKITAG;
				}
			}
			close WIKIKEY;
		}
	}
	close WIKIGROUP;

	return %ImageList;
} 


1;