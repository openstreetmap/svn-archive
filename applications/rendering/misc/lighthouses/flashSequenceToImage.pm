use GD;

sub flashSequenceToImage
{
  use GD;
  my ($Text, $Filename) = @_;
  
  $Text = $Text x 3;
  my $dx = 4;
  
  my $width = 0;
  foreach my $Block(split(/\|/,$Text))
  {
    my ($Colour,$len) =split(/,/, $Block);
    $width += $len * $dx;
  }
  return if($width == 0);
  
  my $height = 20;
  
  my $Image = new GD::Image( $width, $height );
  
  my %Colours;
  my %Palette = (
    'white'=>'225,225,225',
    'red'=>'255,127,127',
    'yellow'=>'255,255,127',
    'green'=>'127,255,127',
    'black'=>'0,0,0');
    
  while(my($Col,$RGB) = each(%Palette))
  {
    my($r,$g,$b)=split(/,/, $RGB);
    $Colours{$Col} = $Image->colorAllocate($r,$g,$b);
  }
  
  
  my $x = 0;
  foreach my $Block(split(/\|/,$Text))
  {
    my ($Colour,$len) =split(/,/, $Block);
    $len *= $dx;
    $Image->filledRectangle($x,0,$x+$len,$height,$Colours{$Colour});
    $x+=$len;
  }
  
  open(my $fp, ">$Filename") || return; 
  binmode $fp;
  print $fp $Image->png();
  close $fp;
}

1