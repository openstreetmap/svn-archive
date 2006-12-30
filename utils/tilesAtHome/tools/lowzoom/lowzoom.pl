use LWP::Simple;
use GD;

# Pick a random zoom-8 tile
$Z = 8;
for($X = 0; $X < 256; $X++){
  for($Y = 0; $Y < 256; $Y++){
    #lowZoom(127,85,8);
    lowZoom($X,$Y,8);

    # Move all low-zoom tiles to upload directory
    moveTiles(tempdir(), "../../temp");
    
    `rm temp/*.png`;
  }
}

sub lowZoom(){
  my ($X,$Y,$Z) = @_;
  
  # Get tiles
  if($Z == 12){
    
    mirror(remotefile($X,$Y,$Z), localfile($X,$Y,$Z));
  }
  else{
    lowZoom($X*2,$Y*2,$Z+1);
    lowZoom($X*2+1,$Y*2,$Z+1);
    lowZoom($X*2,$Y*2+1,$Z+1);
    lowZoom($X*2+1,$Y*2+1,$Z+1);
  
    # Create supertile
    supertile($X,$Y,$Z);
    
    #print "done";    exit;    
  }
}

sub supertile(){
  my ($X,$Y,$Z) = @_;
  
  # Create the supertile
  $Image = GD::Image->new(256,256,1);
  
  # default background
  $BG = $Image->colorAllocate(200,200,255);
  $Image->filledRectangle(0,0,256,256,$BG);
  
  # Load the subimages
  $AA = GD::Image->newFromPng(localfile($X*2,$Y*2,$Z+1), 1);
  $BA = GD::Image->newFromPng(localfile($X*2+1,$Y*2,$Z+1), 1);
  $AB = GD::Image->newFromPng(localfile($X*2,$Y*2+1,$Z+1), 1);
  $BB = GD::Image->newFromPng(localfile($X*2+1,$Y*2+1,$Z+1), 1);
  
  # Copy the parts
  $Image->copyResampled($AA, 0,0,     0,0, 128,128, 256,256) if($AA);
  $Image->copyResampled($BA, 128,0,   0,0, 128,128, 256,256) if($BA);
  $Image->copyResampled($AB, 0,128,   0,0, 128,128, 256,256) if($AB);
  $Image->copyResampled($BB, 128,128, 0,0, 128,128, 256,256) if($BB);
  
  # Save the supertile
  open(my $fp, ">", localfile($X,$Y,$Z)) || die($!);
  binmode $fp;
  print $fp $Image->png();
  close($fp);
}

sub moveTiles(){
  my ($from, $to) = @_;
  print "from $from to $to\n";
  opendir($dp, $from) || die($!);
  while($file = readdir($dp)){
    if($file =~ /^tile_(\d+)_(\d+)_(\d+)\.png$/){
      $Z = $1;
      if($Z < 12){
        $f1 = "$from/$file";
        $f2 = "$to/$file";
        
        rename($f1, $f2);
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