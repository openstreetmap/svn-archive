#!/usr/bin/perl
use strict;
use LWP::Simple;
use Image::Magick;
#------------------------------------------------------------------------------------
# LowZoom.pl
# Generates low-zoom map tiles, by downloading high-zoom map tiles, and merging them
# together. 
#
# Part of the OpenStreetMap tiles@home project
#
# Copyright 2007, Oliver White.
# Copying license: GNU general public license, v2 or later
#-----------------------------------------------------------------------------------

$|=1;

## nicked from tahconfig.pm from main t@h. 
## FIXME: use the actual module instead.
my %Config;
open(my $fp,"<lowzoom.conf") || die("Can't open \"lowzoom.conf\" ($!)\n");
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

# Option: Where to move tiles, so that they get uploaded by another program
my $uploadDir = $Config{UploadDir};

die "can't find upload directory \"$uploadDir\"" unless (-d $uploadDir);

# Command-line arguments
my $X = shift();
my $Y = shift();
my $Z = shift();
my $MaxZ = shift() || 12;
my $Layer = shift() || "tile";
my $Options = shift();

# Check the command-line arguments, and display usage information
my $Usage = "Usage: $0 x y z maxZ [layer] [keep]\n  x,y,z are the tile to generate\n  maxZ is the zoom level to download tiles from\n  Options: \n    * layer - which layer to run lowzoom on (tile (default) or maplint)\n    * 'keep' - don't move tiles to an upload area afterwards\nOther options (URLs, upload staging area) are part of the script - change them in source code\n";
if(($MaxZ > 12)
  || ($MaxZ <= $Z)
  || ($Z < 0) || (!defined($Z))
  || ($MaxZ > 17)
  || ($X < 0) || (!defined($X))
  || ($Y < 0) || (!defined($Y))
  || ($X >= 2 ** $Z)
  || ($Y >= 2 ** $Z)
  ){
  die($Usage);
}

# Timestamp to assign to generated tiles
my $Timestamp = time();

# What we intend to do
my $Status = new status; 
$Status->area($Layer,$X,$Y,$Z,$MaxZ);

# Create the requested tile
lowZoom($X,$Y,$Z, $MaxZ, $Layer, $Status);

# Move all low-zoom tiles to upload directory
moveTiles(tempdir(), $uploadDir, $MaxZ) if($Options ne "keep");

# Status message, saying what we did
$Status->final();

# Recursively create (including any downloads necessary) a tile
sub lowZoom {
  my ($X,$Y,$Z,$MaxZ, $Prefix, $Status) = @_;
  
  # Get tiles
  if($Z >= $MaxZ){
		downloadtile($X,$Y,$Z,$Layer);
  }
  else{
    # Recursively get/create the 4 subtiles
    lowZoom($X*2,$Y*2,$Z+1,$MaxZ, $Status);
    lowZoom($X*2+1,$Y*2,$Z+1,$MaxZ, $Status);
    lowZoom($X*2,$Y*2+1,$Z+1,$MaxZ, $Status);
    lowZoom($X*2+1,$Y*2+1,$Z+1,$MaxZ, $Status);
  
    # Create the tile from those subtiles
    supertile($X,$Y,$Z,$Layer);
  }
}
# Download a tile from the tileserver
sub downloadtile {
  my ($X,$Y,$Z,$Layer) = @_;
  my $f1 = remotefile($X,$Y,$Z,$Layer);
  my $f2 = localfile($X,$Y,$Z,$Layer);
  
  mirror($f1,$f2);
  
  my $Size = -s $f2;
  $Status->downloadCount($Layer,$X,$Y,$Z,$Size);
  
  # Don't bother storing blank or invalid tiles
	
	unlink $f2 if($Size < 103);
}
# Create a supertile, by merging together 4 local image files, and creating a new local file
sub supertile {
  my ($X,$Y,$Z,$Layer) = @_;
  
  # Load the subimages
  my $AA = readLocalImage($X*2,$Y*2,$Z+1,$Layer);
  my $BA = readLocalImage($X*2+1,$Y*2,$Z+1,$Layer);
  my $AB = readLocalImage($X*2,$Y*2+1,$Z+1,$Layer);
  my $BB = readLocalImage($X*2+1,$Y*2+1,$Z+1,$Layer);
  
	my $Filename = localfile($X,$Y,$Z,$Layer);
	# Always delete file first. The use of hardlinks means we might accedently overwrite other files.
	unlink($Filename);
	print "generating $Filename \n";

	# all images the same size? 
	if ($AA == undef) { $AA = Image::Magick->new; }
	if ($AB == undef) { $AB = Image::Magick->new; }
	if ($BA == undef) { $BA = Image::Magick->new; }
	if ($BB == undef) { $BB = Image::Magick->new; }

	if(($AA->Get('filesize') == 103 )  && ($AA->Get('filesize') == $BA->Get('filesize')) && ($BA->Get('filesize') == $AB->Get('filesize')) && ( $AB->Get('filesize') == $BB->Get('filesize')) ) 
	{#if its a "404 sea" or a "sea.png" and all 4 sizes are the same, make one 69 bytes sea of it
			my $SeaFilename = "../../emptysea.png"; 
			link($SeaFilename,$Filename);
			return;
	}
	elsif(($AA->Get('filesize') == 179 ) && ($AA->Get('filesize') == $BA->Get('filesize')) && ($BA->Get('filesize') == $AB->Get('filesize')) && ( $AB->Get('filesize') == $BB->Get('filesize')) ) 
	{#if its a "blank land" or a "land.png" and all 4 sizes are the same, make one 69 bytes land of it
			my $LandFilename = "../../emptyland.png"; 
			link($LandFilename,$Filename);
			return;
	}
	else{
		my $Image = Image::Magick->new;

		# Create the supertile
		$Image->Set(size=>'512x512');
		$Image->ReadImage('xc:white');

		# Copy the subimages into the 4 quadrants
		foreach my $x (0, 1)
		{
				foreach my $y (0, 1)
				{
						next unless (($Z < 9) || (($x == 0) && ($y == 0)));
						$Image->Composite(image => $AA, 
										geometry => sprintf("512x512+%d+%d", $x, $y),
										compose => "darken") if ($AA);

						$Image->Composite(image => $BA, 
										geometry => sprintf("512x512+%d+%d", $x + 256, $y),
										compose => "darken") if ($BA);

						$Image->Composite(image => $AB, 
										geometry => sprintf("512x512+%d+%d", $x, $y + 256),
										compose => "darken") if ($AB);

						$Image->Composite(image => $BB, 
										geometry => sprintf("512x512+%d+%d", $x + 256, $y + 256),
										compose => "darken") if ($BB);
				}
		}

		$Image->Scale(width => "256", height => "256");
		$Image->Set(type=>"Palette");
		$Image->Set(quality => 90);  # compress image
		$Image->Write($Filename);
		utime $Timestamp, $Timestamp, $Filename;
	}

	 
  # Don't bother saving blank or invalid images
  # if(length($Data) < 1000){
  #   return;
  # }
}

# Open a PNG file, and return it as a Magick image (or 0 if not found)
sub readLocalImage
{
    my ($X,$Y,$Z,$Layer) = @_;
    my $Filename = localfile($X,$Y,$Z,$Layer); 
    if (!-f $Filename)
    {
        return undef;
    }
    my $Image = new Image::Magick;
    if (my $err = $Image->Read($Filename))
    {
        print STDERR "$err\n";
        return undef;
    }
		if ($Image->Get('filesize') == 69) 
		{
			# do not return 1x1 pixel images since we might have to put them into a lower zoom
			@$Image=();
			if (my $err = $Image->Read("sea.png"))
			{
					print STDERR "$err\n";
					return undef;
			}
		}
		if ($Image->Get('filesize') == 67) 
		{
			# do not return 1x1 pixel images since we might have to put them into a lower zoom
			@$Image=();
			if (my $err = $Image->Read("land.png"))
			{
					print "$err\n";
					return undef;
			}
		}
    return($Image);
}

# Take any tiles that were created (as opposed to downloaded), and move them to
# an area ready for upload.
# + Delete any tiles that were downloaded
sub moveTiles {
  my ($from, $to, $MaxZ) = @_;
  opendir(my $dp, $from) || die($!);
  while(my $file = readdir($dp)){
    if($file =~ /^${Layer}_(\d+)_(\d+)_(\d+)\.png$/o){
      my ($Z,$X,$Y) = ($1,$2,$3);
      my $f1 = "$from/$file";
      my $f2 = "$to/$file";
      if($Z < $MaxZ){
        # Rename can fail if the target is on a different filesystem
        rename($f1, $f2) or system("mv",$f1,$f2);
      }
      else{
        unlink $f1;
      }
    }
  }  
  close $dp;
}
# Option: filename for our temporary map tiles
# (note: this should match whatever is expected by the upload scripts)
sub localfile {
  my ($X,$Y,$Z,$Layer) = @_;
  return sprintf("%s/%s_%d_%d_%d.png", tempdir(), $Layer,$Z,$X,$Y);
}
# Option: URL for downloading tiles
sub remotefile {
  my ($X,$Y,$Z,$Layer) = @_;
  return sprintf("http://dev.openstreetmap.org/~ojw/Tiles/%s.php/%d/%d/%d.png", $Layer,$Z,$X,$Y);
}
# Option: what to use as temporary storage for tiles
sub tempdir {
  return("temp");
}

package status;
use Time::HiRes qw(time); # Comment-this out if you want, it's not important
sub new {
  my $self  = {};
  $self->{DONE} = 0;
  $self->{TODO} = 1;
  $self->{SIZE} = 0;
  bless($self);
  return $self;
}
sub downloadCount(){
  my $self = shift();
  $self->{LAYER} = shift();
  $self->{LAST_X} = shift();
  $self->{LAST_Y} = shift();
  $self->{LAST_Z} = shift();
  $self->{LAST_SIZE} = shift();
  $self->{DONE}++;
  $self->{SIZE} += $self->{LAST_SIZE};
  $self->{PERCENT} = $self->{TODO} ? (100 * ($self->{DONE} / $self->{TODO})) : 0;
  $self->display();
}
sub area(){
  my $self = shift();
  $self->{LAYER}=shift();
  $self->{X} = shift();
  $self->{Y} = shift();
  $self->{Z} = shift();
  $self->{MAX_Z} = shift();
  $self->{RANGE_Z} = $self->{MAX_Z} - $self->{Z};
  $self->{TODO} = 4 ** $self->{RANGE_Z};
  $self->display();
  $self->{START_T} = time();
}
sub update(){
  my $self = shift();
  $self->{T} = time();
  $self->{DT} = $self->{T} - $self->{START_T};
  $self->{EXPECT_T} = $self->{DONE} ? ($self->{TODO} * $self->{DT} / $self->{DONE}) : 0;
  $self->{EXPECT_FINISH} = $self->{START_T} + $self->{EXPECT_T};
  $self->{REMAIN_T} = $self->{EXPECT_T} - $self->{EXPECT_DT};
}
sub display(){
  my $self = shift();
  $self->update();
  
  printf( "Job %s(%d,%d,%d): %03.1f%% done, %1.1f min (%d,%d,%d = %1.1f KB)\r", 
    $self->{LAYER},
    $self->{X},
    $self->{Y},
    $self->{Z},
    $self->{PERCENT}, 
    $self->{REMAIN_T} / 60,
    $self->{LAST_X},
    $self->{LAST_Y},
    $self->{LAST_Z},
    $self->{LAST_SIZE}/1024
    );
}
sub final(){
  my $self = shift();
  $self->{END_T} = time();
  printf("Done, %d downloads, %1.1fKB total, took %1.0f seconds\n",
    $self->{DONE},
    $self->{SIZE} / 1024,
    $self->{DT});
}


