use strict;
use LWP::Simple;
use GD;

# Pick a random zoom-8 tile
my $uploadDir = "../../temp";

my $X = shift();
my $Y = shift();
my $Z = shift();
my $MaxZ = shift() || 12;
my $Options = shift();
die() if($MaxZ > 12);

my $Status = new status; 
$Status->area($X,$Y,$Z,$MaxZ);

lowZoom($X,$Y,$Z, $MaxZ, $Status);

# Move all low-zoom tiles to upload directory
moveTiles(tempdir(), $uploadDir) if($Options ne "keep");

$Status->final();


sub lowZoom(){
  my ($X,$Y,$Z,$MaxZ, $Status) = @_;
  
  # Get tiles
  if($Z >= $MaxZ){
    downloadtile($X,$Y,$Z);
  }
  else{
    lowZoom($X*2,$Y*2,$Z+1,$MaxZ, $Status);
    lowZoom($X*2+1,$Y*2,$Z+1,$MaxZ, $Status);
    lowZoom($X*2,$Y*2+1,$Z+1,$MaxZ, $Status);
    lowZoom($X*2+1,$Y*2+1,$Z+1,$MaxZ, $Status);
  
    # Create supertile
    supertile($X,$Y,$Z);
  }
}

sub downloadtile(){
  my ($X,$Y,$Z) = @_;
  my $f1 = remotefile($X,$Y,$Z);
  my $f2 = localfile($X,$Y,$Z);
  
  mirror($f1,$f2);
  
  my $Size = -s $f2;
  $Status->downloadCount($X,$Y,$Z,$Size);
  
  unlink $f2 if($Size < 200);
}
sub supertile(){
  my ($X,$Y,$Z) = @_;
  
  # Create the supertile
  my $Image = GD::Image->new(256,256,1);
  return if(!$Image);
    
  # default background
  my $BG = $Image->colorAllocate(255,255,255);
  $Image->filledRectangle(0,0,256,256,$BG);
  
  # Load the subimages
  my $AA = readLocalImage($X*2,$Y*2,$Z+1);
  my $BA = readLocalImage($X*2+1,$Y*2,$Z+1);
  my $AB = readLocalImage($X*2,$Y*2+1,$Z+1);
  my $BB = readLocalImage($X*2+1,$Y*2+1,$Z+1);
  
  # Copy the parts
  $Image->copyResampled($AA, 0,0,     0,0, 128,128, 256,256) if($AA);
  $Image->copyResampled($BA, 128,0,   0,0, 128,128, 256,256) if($BA);
  $Image->copyResampled($AB, 0,128,   0,0, 128,128, 256,256) if($AB);
  $Image->copyResampled($BB, 128,128, 0,0, 128,128, 256,256) if($BB);
  
  # Save the supertile
  my $Filename = localfile($X,$Y,$Z);
  my $Data = $Image->png();
  undef $Image;
  
  if(length($Data) < 200){
    return;
  }
    
  open(my $fp, ">", $Filename) || die($!);
  binmode $fp;
  print $fp $Data;
  close($fp);
}

sub readLocalImage(){
  my ($X,$Y,$Z) = @_;
  my $Filename = localfile($X,$Y,$Z);
  return(0) if(!-f $Filename);
  return(GD::Image->newFromPng($Filename));
}
sub moveTiles(){
  my ($from, $to) = @_;
  opendir(my $dp, $from) || die($!);
  while(my $file = readdir($dp)){
    if($file =~ /^tile_(\d+)_(\d+)_(\d+)\.png$/){
      my ($Z,$X,$Y) = ($1,$2,$3);
      my $f1 = "$from/$file";
      my $f2 = "$to/$file";
      if($Z < 12){
        print " - moving $X,$Y,$Z\n";
        rename($f1, $f2);
      }
      else{
        print " - deleting $X,$Y,$Z\n";
        unlink $f1;
      }
    }
  }  
  close $dp;
}

sub localfile(){
  my ($X,$Y,$Z) = @_;
  return sprintf("%s/tile_%d_%d_%d.png", tempdir(), $Z,$X,$Y);
}
sub remotefile(){
  my ($X,$Y,$Z) = @_;
  return sprintf("http://dev.openstreetmap.org/~ojw/Tiles/tile.php/%d/%d/%d.png", $Z,$X,$Y);
}
sub tempdir(){
  return("temp");
}

package status;
sub new {
  my $self  = {};
  $self->{DONE} = 0;
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
}
sub display(){
  my $self = shift();
  printf( "Job %d,%d,%d: %03.1f%% done (%d,%d,%d = %1.1f KB)\n", 
    $self->{X},
    $self->{Y},
    $self->{Z},
    $self->{PERCENT}, 
    $self->{LAST_X},
    $self->{LAST_Y},
    $self->{LAST_Z},
    $self->{LAST_SIZE}/1024
    );
  
}
sub final(){
  my $self = shift();
  printf("Done, %d downloads, %1.1fKB total\n",
    $self->{DONE},
    $self->{SIZE} / 1024);
}