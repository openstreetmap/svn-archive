use strict;
use LWP::Simple;
use GD;

# Pick a random zoom-8 tile
my $Z = 8;
my $uploadDir = "../../temp";

my $XC = shift();
my $YC = shift();

for(my $Xi = -3; $Xi <= 3; $Xi++){
  for(my $Yi = -3; $Yi <= 3; $Yi++){
  
    my $X = $XC + $Xi;
    my $Y = $YC + $Yi;
    
    printf "doing %d,%d...", $X,$Y;
    flush STDOUT;
    
    lowZoom($X,$Y,8);

    # Move all low-zoom tiles to upload directory
    moveTiles(tempdir(), $uploadDir);
    
    print "done\n";
  }
}


sub lowZoom(){
  my ($X,$Y,$Z) = @_;
  
  # Get tiles
  if($Z == 12){
    downloadtile($X,$Y,$Z);
  }
  else{
    printf(" - generating %d,%d,%d\n", $X,$Y,$Z);
    
    lowZoom($X*2,$Y*2,$Z+1);
    lowZoom($X*2+1,$Y*2,$Z+1);
    lowZoom($X*2,$Y*2+1,$Z+1);
    lowZoom($X*2+1,$Y*2+1,$Z+1);
  
    # Create supertile
    supertile($X,$Y,$Z);
  }
}

sub downloadtile(){
  my ($X,$Y,$Z) = @_;
  my $f1 = remotefile($X,$Y,$Z);
  my $f2 = localfile($X,$Y,$Z);
  print " - downloading $X,$Y,$Z ";
  
  mirror($f1,$f2);
  
  my $Size = -s $f2;
  printf "   %d bytes\n", $Size;
  
  unlink $f2 if($Size < 500);
}
sub supertile(){
  my ($X,$Y,$Z) = @_;
  
  # Create the supertile
  my $Image = GD::Image->new(256,256,1);
  return if(!$Image);
    
  # default background
  my $BG = $Image->colorAllocate(200,200,255);
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
  
  if(length($Data) < 500){
    print " - too short, blank\n";
    return;
  }
    
  open(my $fp, ">", $Filename) || die($!);
  binmode $fp;
  print $fp $Data;
  close($fp);
  print " - OK, $X,$Y,$Z\n";
}
sub readLocalImage(){
  my ($X,$Y,$Z) = @_;
  my $Filename = localfile($X*2+1,$Y*2+1,$Z+1);
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