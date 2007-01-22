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

# Option: Where to move tiles, so that they get uploaded by another program
my $uploadDir = "../../temp";

# Command-line arguments
my $X = shift();
my $Y = shift();
my $Z = shift();
my $MaxZ = shift() || 12;
my $Options = shift();

# Check the command-line arguments, and display usage information
my $Usage = "Usage: $0 x y z maxZ [keep]\n  x,y,z are the tile to generate\n  maxZ is the zoom level to download tiles from\n  Options: \n    * 'keep' - don't move tiles to an upload area afterwards\nOther options (URLs, upload staging area) are part of the script - change them in source code\n";
if(($MaxZ > 12)
  || ($MaxZ <= $Z)
  || ($Z <= 0)
  || ($MaxZ > 17)
  || ($X < 0)
  || ($Y < 0)  
  || ($X >= 2 ** $Z)
  || ($Y >= 2 ** $Z)
  ){
  die($Usage);
}

# What we intend to do
my $Status = new status; 
$Status->area($X,$Y,$Z,$MaxZ);

# Create the requested tile
lowZoom($X,$Y,$Z, $MaxZ, $Status);

# Move all low-zoom tiles to upload directory
moveTiles(tempdir(), $uploadDir, $MaxZ) if($Options ne "keep");

# Status message, saying what we did
$Status->final();

# Recursively create (including any downloads necessary) a tile
sub lowZoom(){
  my ($X,$Y,$Z,$MaxZ, $Status) = @_;
  
  # Get tiles
  if($Z >= $MaxZ){
    downloadtile($X,$Y,$Z);
  }
  else{
    # Recursively get/create the 4 subtiles
    lowZoom($X*2,$Y*2,$Z+1,$MaxZ, $Status);
    lowZoom($X*2+1,$Y*2,$Z+1,$MaxZ, $Status);
    lowZoom($X*2,$Y*2+1,$Z+1,$MaxZ, $Status);
    lowZoom($X*2+1,$Y*2+1,$Z+1,$MaxZ, $Status);
  
    # Create the tile from those subtiles
    supertile($X,$Y,$Z);
  }
}
# Download a tile from the tileserver
sub downloadtile(){
  my ($X,$Y,$Z) = @_;
  my $f1 = remotefile($X,$Y,$Z);
  my $f2 = localfile($X,$Y,$Z);
  
  mirror($f1,$f2);
  
  my $Size = -s $f2;
  $Status->downloadCount($X,$Y,$Z,$Size);
  
  # Don't bother storing blank or invalid tiles
  unlink $f2 if($Size < 1000);
}
# Create a supertile, by merging together 4 local image files, and creating a new local file
sub supertile(){
  my ($X,$Y,$Z) = @_;
  
  # Create the supertile
  my $Image = Image::Magick->new;
  $Image->Set(size=>'512x512');
  $Image->ReadImage('xc:white');
    
  # Load the subimages
  my $AA = readLocalImage($X*2,$Y*2,$Z+1);
  my $BA = readLocalImage($X*2+1,$Y*2,$Z+1);
  my $AB = readLocalImage($X*2,$Y*2+1,$Z+1);
  my $BB = readLocalImage($X*2+1,$Y*2+1,$Z+1);
  
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
  my $Filename = localfile($X,$Y,$Z);
  $Image->Set(type=>"Palette");
  $Image->Write($Filename);
 
  # Don't bother saving blank or invalid images
  # if(length($Data) < 1000){
  #   return;
  # }
}
# Open a PNG file, and return it as a Magick image (or 0 if not found)
sub readLocalImage(){
  my ($X,$Y,$Z) = @_;
  my $Filename = localfile($X,$Y,$Z);
  return undef unless(-f $Filename);
  my $Image = new Image::Magick;
  if (my $err = $Image->Read($Filename))
  {
      print STDERR "$err\n";
      return undef;
  }
  return($Image);
}
# Take any tiles that were created (as opposed to downloaded), and move them to
# an area ready for upload.
# + Delete any tiles that were downloaded
sub moveTiles(){
  my ($from, $to, $MaxZ) = @_;
  opendir(my $dp, $from) || die($!);
  while(my $file = readdir($dp)){
    if($file =~ /^tile_(\d+)_(\d+)_(\d+)\.png$/){
      my ($Z,$X,$Y) = ($1,$2,$3);
      my $f1 = "$from/$file";
      my $f2 = "$to/$file";
      if($Z < $MaxZ){
        rename($f1, $f2);
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
sub localfile(){
  my ($X,$Y,$Z) = @_;
  return sprintf("%s/tile_%d_%d_%d.png", tempdir(), $Z,$X,$Y);
}
# Option: URL for downloading tiles
sub remotefile(){
  my ($X,$Y,$Z) = @_;
  return sprintf("http://dev.openstreetmap.org/~ojw/Tiles/tile.php/%d/%d/%d.png", $Z,$X,$Y);
}
# Option: what to use as temporary storage for tiles
sub tempdir(){
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
  
  printf( "Job %d,%d,%d: %03.1f%% done, %1.1f min (%d,%d,%d = %1.1f KB)\n", 
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
